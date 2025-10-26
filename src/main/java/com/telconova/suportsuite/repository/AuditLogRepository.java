package com.telconova.suportsuite.repository;

import com.telconova.suportsuite.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    // Métodos de consulta futuros para el panel de auditoría (si fueran necesarios)
}
