package com.telconova.suportsuite.service;

import com.telconova.suportsuite.entity.AuditLog;
import com.telconova.suportsuite.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * Registra un evento de seguridad en la bitácora y activa alertas internas si es necesario.
     * Renombrado a recordEvent para coincidir con la llamada en AuthServiceImpl.
     */
    public void recordEvent(Long userId, String eventType, String details, String sourceIp) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setEventType(eventType);
        log.setDetails(details);
        log.setSourceIp(sourceIp);
        log.setTimestamp(LocalDateTime.now()); // Aunque la entidad tiene un default, es mejor configurarlo aquí

        auditLogRepository.save(log);

        // Lógica de Alerta Interna (cumple con el requisito de generar alertas)
        // Se activa para bloqueos o fallos de login.
        if (eventType.equals("ACCOUNT_LOCKED") || eventType.equals("LOGIN_FAILED") || eventType.equals("LOGIN_BLOCKED_TEMPORAL")) {
            generateInternalAlert(log);
        }
    }

    private void generateInternalAlert(AuditLog log) {
        logger.error("!!! ALERTA INTERNA DE SEGURIDAD !!! Tipo: {} | Usuario ID: {} | IP: {}",
                log.getEventType(), log.getUserId(), log.getSourceIp());
        // En una implementación real, esto enviaría un email o notificación al equipo de soporte.
    }
}
