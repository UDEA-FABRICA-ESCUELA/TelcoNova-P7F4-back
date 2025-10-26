package com.telconova.suportsuite.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "tn_audit_log")
@Data
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "user_id")
    private Long userId; // ID del usuario que realizó la acción o intentó acceder

    @Column(name = "event_type", nullable = false)
    private String eventType; // LOGIN_SUCCESS, LOGIN_FAILURE, ACCOUNT_LOCKED

    @Column(name = "source_ip")
    private String sourceIp; // Dirección IP (HU-03.4)

    @Column(length = 512)
    private String details; // Descripción del evento
}
