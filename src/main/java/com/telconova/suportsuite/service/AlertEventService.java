package com.telconova.suportsuite.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telconova.suportsuite.DTO.CreateNotificationRequest;
import com.telconova.suportsuite.entity.AlertRule;
import com.telconova.suportsuite.entity.EventTrigger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertEventService {

    private final AlertRuleService alertRuleService;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    /**
     * Este metodo se llama cuando ocurre un evento en el sistema
     * Evalúa las reglas y genera notificaciones automáticas
     */
    public void processEvent(EventTrigger eventType, Map<String, Object> eventData) {
        log.info("Procesando evento: {}", eventType);

        // Obtener reglas activas para este tipo de evento
        List<AlertRule> rules = alertRuleService.getRulesByEvent(eventType);

        if (rules.isEmpty()) {
            log.debug("No hay reglas activas para el evento: {}", eventType);
            return;
        }

        log.info("Encontradas {} reglas activas para el evento {}",
                rules.size(), eventType);

        // Procesar cada regla
        for (AlertRule rule : rules) {
            try {
                processAlertRule(rule, eventData);
            } catch (Exception e) {
                log.error("Error procesando regla ID {}: {}",
                        rule.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * Procesa una regla específica y genera la notificación
     */
    private void processAlertRule(AlertRule rule, Map<String, Object> eventData) {
        log.debug("Evaluando regla: {} (ID: {})", rule.getName(), rule.getId());

        // Filtrar destinatarios según público objetivo
        List<String> recipients = filterTargetAudience(
                rule.getTargetAudience(), eventData);

        if (recipients.isEmpty()) {
            log.debug("No hay destinatarios que cumplan el criterio de la regla ID: {}",
                    rule.getId());
            return;
        }

        // Generar contenido del mensaje desde el template
        String messageContent = generateMessageContent(
                rule.getMessageTemplate().getContent(), eventData);

        // Crear notificación para cada destinatario
        for (String recipient : recipients) {
            CreateNotificationRequest request = new CreateNotificationRequest();
            request.setRecipient(recipient);
            request.setSubject(rule.getMessageTemplate().getName());
            request.setContent(messageContent);
            request.setChannel(rule.getChannel());
            request.setPriority(rule.getPriority());

            // Encolar notificación automáticamente
            notificationService.createNotification(request);

            log.info("Notificación automática creada para {} por regla '{}'",
                    recipient, rule.getName());
        }
    }

    /**
     * Filtra los destinatarios según el público objetivo
     */
    private List<String> filterTargetAudience(String targetAudienceJson,
                                              Map<String, Object> eventData) {
        try {
            // El targetAudience puede contener criterios como:
            // {"role": "admin", "status": "active"}
            // o una lista directa: ["admin@example.com", "support@example.com"]

            Map<String, Object> criteria = objectMapper.readValue(
                    targetAudienceJson, Map.class);

            // AQUÍ IRÍA LA LÓGICA DE FILTRADO SEGÚN TU MODELO DE USUARIOS
            // Por ahora retornamos un ejemplo
            return List.of("user@example.com");

        } catch (Exception e) {
            log.error("Error filtrando público objetivo: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Reemplaza variables dinámicas en el template
     */
    private String generateMessageContent(String template, Map<String, Object> eventData) {
        String content = template;

        // Reemplazar variables {variable} con datos del evento
        for (Map.Entry<String, Object> entry : eventData.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            content = content.replace(placeholder, value);
        }

        return content;
    }
}
