package com.telconova.suportsuite.service;

import com.telconova.suportsuite.DTO.CreateNotificationRequest;
import com.telconova.suportsuite.DTO.NotificationDTO;
import com.telconova.suportsuite.DTO.NotificationStatusDTO;
import com.telconova.suportsuite.entity.AlertRule;
import com.telconova.suportsuite.entity.Notification;
import com.telconova.suportsuite.entity.NotificationHistory;
import com.telconova.suportsuite.entity.Notification.NotificationStatus;
import com.telconova.suportsuite.repository.AlertRuleRepository;
import com.telconova.suportsuite.repository.NotificationHistoryRepository;
import com.telconova.suportsuite.repository.NotificationRepository;
import com.telconova.suportsuite.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final int MAX_REINTENTOS_DEFAULT = 3;

    private final NotificationRepository notificationRepository;
    private final NotificationHistoryRepository historyRepository;
    private final AlertRuleRepository alertRuleRepository;
    private final List<NotificationSender> notificationSenders;


    @Transactional
    public NotificationDTO createNotification(CreateNotificationRequest request){
        log.info("Creando nueva notificación para {} (Regla ID: {})", request.getRecipient(), request.getAlertRuleId());

        // 1. BUSCAR Y VALIDAR LA REGLA
        AlertRule alertRule = alertRuleRepository.findById(request.getAlertRuleId())
                .orElseThrow(() -> new ResourceNotFoundException("Regla de Alerta no encontrada con ID: " + request.getAlertRuleId()));

        Notification notification = new Notification();
        notification.setRecipient(request.getRecipient());
        notification.setContent(request.getContent());
        notification.setSubject(request.getSubject());
        notification.setChannel(request.getChannel());
        notification.setPriority(request.getPriority());
        notification.setAlertRule(alertRule);

        // Inicializar los campos de reintentos
        if (notification.getReintentosCount() == null) {
            notification.setReintentosCount(0);
        }
        if (notification.getMaxReintentos() == null) {
            notification.setMaxReintentos(MAX_REINTENTOS_DEFAULT);
        }

        Notification saved = notificationRepository.save(notification);

        // El historial se guarda en una nueva transacción
        addHistory(saved, NotificationStatus.PENDIENTE, "Notificación agregada a la cola de envíos", null);

        log.info("Notificación ID {} agregada a la cola ", saved.getId());

        return convertToDto(saved);
    }

    // HU-005.2: Procesar mensajes de la cola en orden
    @Transactional
    public void processNotification (Notification notification) {
        log.info("Procesando notificación ID: {}", notification.getId());

        // Actualizar estado a procesando (HU-005.2)
        notification.setStatus(NotificationStatus.PROCESANDO);
        notificationRepository.save(notification); // Persistir el estado transitorio

        // El historial se guarda en una nueva transacción
        addHistory(notification, NotificationStatus.PROCESANDO, "Iniciando envío de notificación", null);

        try {
            // Buscar el sender apropiado para el canal
            NotificationSender sender = notificationSenders.stream()
                    .filter(s -> s.canSend(notification))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(
                            "No hay sender disponible para el canal: " + notification.getChannel()));

            // Intentar envío
            boolean success = sender.send(notification);

            if (success) {
                // Éxito: marcar como entregada correctamente
                notification.setStatus(NotificationStatus.ENVIADO);
                notification.setSentAt(LocalDateTime.now());

                addHistory(notification, NotificationStatus.ENVIADO,
                        "Notificación enviada correctamente", null);
                log.info("Notificación ID {} enviada exitosamente ", notification.getId());

            } else {
                // Manejo de fallo y preparar reintento
                handleFailure(notification, "Error en el envío de la notificación");
            }
        } catch (Exception e) {
            // Manejo de fallo por excepción
            log.error("Excepción al intentar enviar notificación ID {}: {}", notification.getId(), e.getMessage());
            handleFailure(notification, "Excepción durante el envío: " + e.getMessage());
        } finally {
            // Guardar el estado final de la notificación
            notificationRepository.save(notification);
        }
    }

    /**
     * Obtiene la cola de notificaciones elegibles para ser procesadas (PENDIENTE y REINTENTANDO).
     */
    public List<Notification> getQueueNotifications() {
        log.debug("Obteniendo notificaciones PENDIENTES y REINTENTANDO para procesamiento de cola.");
        return notificationRepository.findByStatusInOrderByPriorityAscCreatedAtAsc(
                List.of(NotificationStatus.PENDIENTE, NotificationStatus.REINTENTANDO)
        );
    }

    // HU-005.4: Obtener estadísticas para monitoreo
    public NotificationStatusDTO getEstadisticas(){
        Long enviado = notificationRepository.countByStatus(NotificationStatus.ENVIADO);
        Long pendiente= notificationRepository.countByStatus(NotificationStatus.PENDIENTE);
        Long fallida = notificationRepository.countByStatus(NotificationStatus.FALLIDA);
        Long procesando = notificationRepository.countByStatus(NotificationStatus.PROCESANDO);
        Long reintentando = notificationRepository.countByStatus(NotificationStatus.REINTENTANDO);

        Long total = enviado + pendiente + fallida + procesando + reintentando;

        // Calcular la cola activa (pendiente + reintentando)
        Long colaActiva = pendiente + reintentando;

        Double tazaExito = total > 0 ? (enviado.doubleValue() / total.doubleValue()) * 100 : 0.0;

        // Retornar las estadísticas con la cola activa
        return new NotificationStatusDTO(enviado , colaActiva, fallida, procesando, tazaExito);
    }

    public List<Notification> getNotificationsForRetry() {
        return notificationRepository.findNotificationsEligibleForRetry();
    }

    public List<NotificationDTO> getErrorLogs() {
        List<Notification> failedAndRetrying =
                notificationRepository.findByStatusInOrderByCreatedAtDesc(
                        List.of(NotificationStatus.FALLIDA, NotificationStatus.REINTENTANDO)
                );

        return failedAndRetrying.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<NotificationDTO> getPendingQueueNotifications(){
        List<Notification> queue = notificationRepository.findByStatusInOrderByPriorityAscCreatedAtAsc(
                List.of(NotificationStatus.PENDIENTE, NotificationStatus.PROCESANDO)
        );
        return queue.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // HU-005.3: Reintentar envío y registrar error
    @Transactional
    public void handleFailure(Notification notification, String errorMessage){
        notification.setReintentosCount(notification.getReintentosCount()+1);
        notification.setErrorMenssage(errorMessage); // Usa el setter correcto para 'errorMenssage'

        if (notification.getReintentosCount() < notification.getMaxReintentos()){
            notification.setStatus(NotificationStatus.REINTENTANDO);

            addHistory(notification, NotificationStatus.REINTENTANDO,
                    String.format("Intento %d/%d fallido. Se reintentará el envío",
                            notification.getReintentosCount(), notification.getMaxReintentos()),
                    errorMessage);
            log.warn("Notificación ID {} fallida. Reintento {}/{}",
                    notification.getId(), notification.getReintentosCount(),
                    notification.getMaxReintentos());
        } else {
            notification.setStatus(NotificationStatus.FALLIDA);

            addHistory(notification, NotificationStatus.FALLIDA,
                    "Envío fallido definitivo después de "+ notification.getReintentosCount()+
                            " intentos", errorMessage);

            log.error("Notificación ID {} falló definitivo después de {} intentos ",
                    notification.getId(), notification.getReintentosCount());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addHistory (Notification notification, NotificationStatus status,
                            String message, String errorDetails){
        NotificationHistory history = new NotificationHistory();
        history.setNotification(notification);
        history.setStatus(status);

        history.setDescription(message);
        history.setErrorDetails(errorDetails);

        historyRepository.save(history);
    }

    private NotificationDTO convertToDto ( Notification notification){
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setRecipient(notification.getRecipient());
        dto.setSubject(notification.getSubject());
        dto.setContent(notification.getContent());
        dto.setChannel(notification.getChannel());
        dto.setStatus(notification.getStatus());
        dto.setCreatedAt(notification.getCreatedAt());

        dto.setErrorMenssage(notification.getErrorMenssage());
        return dto;
    }
}