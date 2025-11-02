package com.telconova.suportsuite.repository;

import com.telconova.suportsuite.entity.AlertRuleAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.telconova.suportsuite.entity.AlertRule;

import java.util.List;

@Repository
public interface AlertRuleAuditRepository extends JpaRepository<AlertRuleAudit, Long> {

    @Transactional // Es requerido para operaciones de eliminación que no sean 'delete(entity)'
    void deleteByAlertRule(AlertRule alertRule);

    // CRITERIO 4: Obtener historial de cambios de una regla
    List<AlertRuleAudit> findByAlertRuleIdOrderByTimestampDesc(Long ruleId);

    // Obtener auditoría por usuario
    List<AlertRuleAudit> findByPerformedByOrderByTimestampDesc(String performedBy);
}
