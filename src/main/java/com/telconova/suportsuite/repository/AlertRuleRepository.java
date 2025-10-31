package com.telconova.suportsuite.repository;

import com.telconova.suportsuite.entity.AlertRule;
import com.telconova.suportsuite.entity.EventTrigger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRuleRepository extends JpaRepository<AlertRule, Long> {

    // CRITERIO 3: Obtener solo reglas activas
    List<AlertRule> findByIsActiveTrue();

    // Buscar reglas por evento disparador y activas
    List<AlertRule> findByTriggerEventAndIsActiveTrue(EventTrigger triggerEvent);

    // Obtener todas las reglas ordenadas
    List<AlertRule> findAllByOrderByCreatedAtDesc();
}
