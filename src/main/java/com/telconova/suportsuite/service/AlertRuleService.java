package com.telconova.suportsuite.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telconova.suportsuite.DTO.AlertRuleDto;
import com.telconova.suportsuite.DTO.CreateAlertRuleRequest;
import com.telconova.suportsuite.entity.*;
import com.telconova.suportsuite.repository.AlertRuleAuditRepository;
import com.telconova.suportsuite.repository.AlertRuleRepository;
import com.telconova.suportsuite.repository.MessageTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertRuleService {

    private final AlertRuleRepository alertRuleRepository;
    private final AlertRuleAuditRepository auditRepository;
    private final MessageTemplateRepository templateRepository;
    private final ObjectMapper objectMapper;

    /**
     * Crear nueva regla de notificación
     */
    @Transactional
    public AlertRuleDto createAlertRule(CreateAlertRuleRequest request, String username) {
        log.info("Creando nueva regla de alerta: {}", request.getName());

        // Validar que el template existe
        MessageTemplate template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new RuntimeException("Template no encontrado"));

        // Crear la regla
        AlertRule rule = new AlertRule();
        rule.setName(request.getName());
        rule.setDescription(request.getDescription());
        rule.setTriggerEvent(request.getEventTrigger());
        rule.setMessageTemplate(template);
        rule.setTargetAudience(request.getTargetAudience());
        rule.setChannel(request.getChannel());
        rule.setPriority(request.getPriority());
        rule.setIsActive(request.getIsActive());
        rule.setCreatedBy(username);

        AlertRule saved = alertRuleRepository.save(rule);

        //  Registrar auditoría
        registerAudit(saved, AuditAction.CREATE, username, null, "127.0.0.1");

        log.info("Regla de alerta ID {} creada exitosamente", saved.getId());

        return convertToDto(saved);
    }

    /**
     * Editar regla existente
     */
    @Transactional
    public AlertRuleDto updateAlertRule(Long id, CreateAlertRuleRequest request, String username) {
        log.info("Actualizando regla de alerta ID: {}", id);

        AlertRule rule = alertRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Regla no encontrada"));

        // Guardar estado anterior para auditoría
        Map<String, Object> oldValues = captureRuleState(rule);

        // Validar template
        MessageTemplate template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new RuntimeException("Template no encontrado"));

        // Actualizar campos
        rule.setName(request.getName());
        rule.setDescription(request.getDescription());
        rule.setTriggerEvent(request.getEventTrigger());
        rule.setMessageTemplate(template);
        rule.setTargetAudience(request.getTargetAudience());
        rule.setChannel(request.getChannel());
        rule.setPriority(request.getPriority());
        rule.setIsActive(request.getIsActive());
        rule.setUpdatedBy(username);

        AlertRule updated = alertRuleRepository.save(rule);

        // Registrar cambios en auditoría
        Map<String, Object> newValues = captureRuleState(updated);
        String changes = generateChangesJson(oldValues, newValues);
        registerAudit(updated, AuditAction.UPDATE, username, changes, "127.0.0.1");

        log.info("Regla de alerta ID {} actualizada exitosamente", id);

        return convertToDto(updated);
    }

    /**
     *  Eliminar regla
     */
    @Transactional
    public void deleteAlertRule(Long id, String username) {
        log.info("Eliminando regla de alerta ID: {}", id);

        AlertRule rule = alertRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Regla no encontrada"));

        //  Registrar eliminación en auditoría
        registerAudit(rule, AuditAction.DELETE, username, null, "127.0.0.1");

        alertRuleRepository.delete(rule);

        log.info("Regla de alerta ID {} eliminada exitosamente", id);
    }

    /**
     *  Activar regla sin eliminarla
     */
    @Transactional
    public AlertRuleDto activateRule(Long id, String username) {
        log.info("Activando regla de alerta ID: {}", id);

        AlertRule rule = alertRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Regla no encontrada"));

        rule.setIsActive(true);
        rule.setUpdatedBy(username);

        AlertRule updated = alertRuleRepository.save(rule);

        //  Registrar activación
        registerAudit(updated, AuditAction.ACTIVATE, username,
                "{\"isActive\":\"false→true\"}", "127.0.0.1");

        log.info("Regla de alerta ID {} activada", id);

        return convertToDto(updated);
    }

    /**
     *  Desactivar regla sin eliminarla
     */
    @Transactional
    public AlertRuleDto deactivateRule(Long id, String username) {
        log.info("Desactivando regla de alerta ID: {}", id);

        AlertRule rule = alertRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Regla no encontrada"));

        rule.setIsActive(false);
        rule.setUpdatedBy(username);

        AlertRule updated = alertRuleRepository.save(rule);

        //  Registrar desactivación
        registerAudit(updated, AuditAction.DEACTIVATE, username,
                "{\"isActive\":\"true→false\"}", "127.0.0.1");

        log.info("Regla de alerta ID {} desactivada", id);

        return convertToDto(updated);
    }

    /**
     * Listar todas las reglas
     */
    public List<AlertRuleDto> getAllRules() {
        return alertRuleRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtener reglas activas
     */
    public List<AlertRule> getActiveRules() {
        return alertRuleRepository.findByIsActiveTrue();
    }

    /**
     * Obtener reglas por evento
     */
    public List<AlertRule> getRulesByEvent(EventTrigger eventType) {

        return alertRuleRepository.findByTriggerEventAndIsActiveTrue(eventType);
    }

    /**
     * Registrar en auditoría
     */
    private void registerAudit(AlertRule rule, AuditAction action, String username,
                               String changes, String ipAddress) {
        AlertRuleAudit audit = new AlertRuleAudit();
        audit.setAlertRule(rule);
        audit.setAction(action);
        audit.setPerformedBy(username); // Quién
        audit.setChanges(changes); // Qué cambió
        audit.setIpAddress(ipAddress);
        // timestamp se establece automáticamente con @PrePersist (Cuándo)

        auditRepository.save(audit);
    }

    /**
     * Capturar estado actual de la regla para comparación
     */
    private Map<String, Object> captureRuleState(AlertRule rule) {
        Map<String, Object> state = new HashMap<>();
        state.put("name", rule.getName());
        state.put("description", rule.getDescription());
        state.put("eventTrigger", rule.getTriggerEvent());
        state.put("templateId", rule.getMessageTemplate().getId());
        state.put("targetAudience", rule.getTargetAudience());
        state.put("channel", rule.getChannel());
        state.put("priority", rule.getPriority());
        state.put("isActive", rule.getIsActive());
        return state;
    }

    /**
     * Generar JSON de cambios para auditoría
     */
    private String generateChangesJson(Map<String, Object> oldValues,
                                       Map<String, Object> newValues) {
        try {
            Map<String, String> changes = new HashMap<>();

            for (String key : oldValues.keySet()) {
                Object oldValue = oldValues.get(key);
                Object newValue = newValues.get(key);

                if (!oldValue.equals(newValue)) {
                    changes.put(key, oldValue + " → " + newValue);
                }
            }

            return objectMapper.writeValueAsString(changes);
        } catch (Exception e) {
            log.error("Error generando JSON de cambios", e);
            return "{}";
        }
    }

    /**
     * Convertir entidad a DTO
     */
    private AlertRuleDto convertToDto(AlertRule rule) {
        AlertRuleDto dto = new AlertRuleDto();
        dto.setId(rule.getId());
        dto.setName(rule.getName());
        dto.setDescription(rule.getDescription());
        dto.setEventTrigger(rule.getTriggerEvent());
        dto.setTemplateId(rule.getMessageTemplate().getId());
        dto.setTemplateName(rule.getMessageTemplate().getName());
        dto.setTargetAudience(rule.getTargetAudience());
        dto.setChannel(rule.getChannel());
        dto.setIsActive(rule.getIsActive());
        dto.setPriority(rule.getPriority());
        dto.setCreatedBy(rule.getCreatedBy());
        dto.setCreatedAt(rule.getCreatedAt());
        dto.setUpdatedBy(rule.getUpdatedBy());
        dto.setUpdatedAt(rule.getUpdatedAt());
        return dto;
    }
}

