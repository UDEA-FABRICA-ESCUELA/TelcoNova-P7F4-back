package com.telconova.suportsuite.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.telconova.suportsuite.entity.NotificationChannel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name ="notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Integer priority = 5;

    @Column(name = "reintentos_count")
    private Integer reintentosCount; // Inicializado en el servicio

    @Column (name = "max_reintentos")
    private Integer maxReintentos; // Inicializado en el servicio

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Enumerated (EnumType.STRING)
    private NotificationChannel channel;

    @Enumerated (EnumType.STRING)
    private NotificationStatus status = NotificationStatus.PENDIENTE;

    @Column (name = "error_menssage" )
    private String errorMenssage;

    @ManyToOne
    @JoinColumn(name = "alert_rule_id")
    private AlertRule alertRule;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum NotificationStatus{
        PENDIENTE, PROCESANDO, ENVIADO, FALLIDA, REINTENTANDO
    }
}