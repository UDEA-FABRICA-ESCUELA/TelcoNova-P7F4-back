package com.telconova.suportsuite.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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

    @Column(name = "reintentos_count", nullable = false)
    private Integer reintentosCount = 0;

    @Column (name = "max_reintentos", nullable = false)
    private Integer maxReintentos = 3;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "sent_at") // Fecha de envío
    private LocalDateTime sentAt;


    // El Enum NotificationChannel DEBE estar definido como una clase independiente
    @Enumerated (EnumType.STRING)
    private NotificationChannel channel;

    @Enumerated (EnumType.STRING)

    private NotificationStatus status = NotificationStatus.PENDIENTE;

    @Column (name = "error_menssage" )
    private String errorMenssage;

    @ManyToOne
    @JoinColumn(name = "alert_rule_id")
    //private AlertRule alertRule;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum NotificationStatus{
        PENDIENTE, PROCESANDO, ENVIADO, FALLIDA, REINTENTANDO
    }

}