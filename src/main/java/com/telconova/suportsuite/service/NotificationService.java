package com.telconova.suportsuite.service;

import com.telconova.suportsuite.DTO.CreateNotificationRequest;
import com.telconova.suportsuite.DTO.NotificationDTO;
import com.telconova.suportsuite.DTO.NotificationStatusDTO;
import com.telconova.suportsuite.entity.AlertRule; // Importado
import com.telconova.suportsuite.entity.Notification;
import com.telconova.suportsuite.entity.NotificationHistory;
import com.telconova.suportsuite.entity.Notification.NotificationStatus;
import com.telconova.suportsuite.repository.AlertRuleRepository; // Importado
import com.telconova.suportsuite.repository.NotificationHistoryRepository;
import com.telconova.suportsuite.repository.NotificationRepository;
import com.telconova.suportsuite.exception.ResourceNotFoundException; // Asumiendo que esta es tu excepción
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationHistoryRepository historyRepository;
    private final AlertRuleRepository alertRuleRepository; // 🟢 INYECCIÓN AÑADIDA
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
        notification.setAlertRule(alertRule); // 🟢 ASIGNAR LA REGLA (SOLUCIÓN AL ERROR DE CLAVE FORÁNEA)

        Notification saved = notificationRepository.save(notification);

        addHistory(saved, NotificationStatus.PENDIENTE, "Notificación agregada a la cola de envíos", null);

        log.info("Notificación ID {} agregada a la cola ", saved.getId());

        return convertToDto(saved);
    }

    // Procesar mensajes de la cola en orden
    @Transactional
    public void processNotification (Notification notification) {
        log.info("Procesando notificación ID: {}", notification.getId());

        // Actualizar estado a procesando
        notification.setStatus(NotificationStatus.PROCESANDO);
        // Se guarda el estado al final de la transacción.

        addHistory(notification, NotificationStatus.PROCESANDO, "Iniciando envío de notificación", null);

        // Buscar el sender apropiado para el canal
        NotificationSender sender = notificationSenders.stream()
                .filter(s -> s.canSend(notification))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "No hay sender disponible para el canal: " + notification.getChannel()));

        // Intentar envío
        boolean success = sender.send(notification);

        if (success) {
            // marcar como entregada correctamente
            notification.setStatus(NotificationStatus.ENVIADO);
            notification.setSentAt(LocalDateTime.now());
            // No es necesario guardar aquí, se guarda al final.

            addHistory(notification, NotificationStatus.ENVIADO,
                    "Notificación enviada correctamente", null);
            log.info("Notificación ID {} enviada exitosamente ", notification.getId());

        } else {
            // Manejo de fallo y preparar reintento
            handleFailure(notification, "Error en el envío de la notificación");
        }

        // Guardar el estado final
        notificationRepository.save(notification);
    }

    // Obtener cola de pendientes
    public List<Notification>getPendingNotifications(){
        return notificationRepository.findByStatusOrderByPriorityAscCreatedAtAsc(
                NotificationStatus.PENDIENTE);
    }



    // Obtener estadísticas para monitoreo
    public NotificationStatusDTO getEstadisticas(){
        Long enviando = notificationRepository.countByStatus(NotificationStatus.ENVIADO);
        Long pendiente= notificationRepository.countByStatus(NotificationStatus.PENDIENTE);
        Long fallida = notificationRepository.countByStatus(NotificationStatus.FALLIDA);
        Long procesando = notificationRepository.countByStatus(NotificationStatus.PROCESANDO);
        Long total = enviando + pendiente + fallida + procesando;


        Double tazaExito = total > 0 ? (enviando.doubleValue() / total.doubleValue()) * 100 : 0.0;

        return new NotificationStatusDTO(enviando , pendiente, fallida, procesando, tazaExito);
    }

    public List<Notification> getNotificationsForRetry() {
        // Llama al nuevo metodo del repositorio
        return notificationRepository.findNotificationsEligibleForRetry();
    }

    public List<NotificationDTO> getErrorLogs() {
        List<Notification> failedAndRetrying =
                notificationRepository.findByStatusInOrderByCreatedAtDesc(
                        List.of(NotificationStatus.FALLIDA, NotificationStatus.REINTENTANDO)
                );

        // Mapear los resultados al DTO que usará el frontend para la tabla.
        return failedAndRetrying.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<NotificationDTO> getPendingQueueNotifications(){
        // Buscamos PENDIENTE y PROCESANDO, ordenado por prioridad
        List<Notification> queue = notificationRepository.findByStatusInOrderByPriorityAscCreatedAtAsc(
                List.of(NotificationStatus.PENDIENTE, NotificationStatus.PROCESANDO)
        );
        return queue.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Reintentar envío y registrar error
    @Transactional
    public void handleFailure(Notification notification, String errorMessage){
        notification.setReintentosCount(notification.getReintentosCount()+1);
        notification.setErrorMenssage(errorMessage);

        if (notification.getReintentosCount() < notification.getMaxReintentos()){
            // CORRECCIÓN: RENTRY no existe. Usamos REINTENTANDO.
            notification.setStatus(NotificationStatus.REINTENTANDO);
            // Se guarda en el metodo processNotification.

            // CORRECCIÓN: RETRY no existe. Usamos REINTENTANDO.
            addHistory(notification, NotificationStatus.REINTENTANDO,
                    String.format("Intento %d/%d fallido. Se reintentará el envío",
                            notification.getReintentosCount(), notification.getMaxReintentos()),
                    errorMessage);
            log.warn("Notificación ID {} fallida. Reintento {}/{}",
                    notification.getId(), notification.getReintentosCount(),
                    notification.getMaxReintentos());
        } else {
            notification.setStatus(NotificationStatus.FALLIDA);
            // Se guarda en el metodo processNotification.

            addHistory(notification, NotificationStatus.FALLIDA,
                    "Envío fallido definitivo después de "+ notification.getReintentosCount()+
                            " intentos", errorMessage);

            log.error("Notificación ID {} falló definitivo después de {} intentos ",
                    notification.getId(), notification.getReintentosCount());
        }

    }

    private void addHistory (Notification notification, NotificationStatus status,
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