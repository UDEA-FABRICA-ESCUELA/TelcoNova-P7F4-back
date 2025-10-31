package com.telconova.suportsuite.repository;

import com.telconova.suportsuite.entity.AlertRuleAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRuleAuditRepository extends JpaRepository<AlertRuleAudit, Long> {

    // CRITERIO 4: Obtener historial de cambios de una regla
    List<AlertRuleAudit> findByAlertRuleIdOrderByTimestampDesc(Long ruleId);

    // Obtener auditoría por usuario
    List<AlertRuleAudit> findByPerformedByOrderByTimestampDesc(String performedBy);
}
