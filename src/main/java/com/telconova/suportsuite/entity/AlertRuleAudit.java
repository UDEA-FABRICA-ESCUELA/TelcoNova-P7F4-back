package com.telconova.suportsuite.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "alert_rule_audit")

public class AlertRuleAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private AlertRule alertRule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action; // CREATE, UPDATE, DELETE, ACTIVATE, DEACTIVATE

    @Column(name = "performed_by", nullable = false)
    private String performedBy; // CRITERIO 4: Quién modificó

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp; // CRITERIO 4: Cuándo se modificó

    @Column(name = "changes", columnDefinition = "TEXT")
    private String changes; // CRITERIO 4: Qué fue modificado (JSON)

    @Column(name = "ip_address")
    private String ipAddress; // Información adicional de auditoría

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
