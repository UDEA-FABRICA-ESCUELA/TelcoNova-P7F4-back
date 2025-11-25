package com.telconova.suportsuite.entity;

import jakarta.persistence.*;
import lombok.Data;
// CORRECCIÓN 1: Se eliminaron las importaciones innecesarias que causaban errores
// import org.springframework.boot.autoconfigure.web.WebProperties;
// import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "notification_history")
public class NotificationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)

    private Notification.NotificationStatus status;

    @Column(columnDefinition = "TEXT")

    private String description;

    @Column(columnDefinition = "TEXT")

    private String errorDetails;

    @Column (name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate(){
        timestamp = LocalDateTime.now();
    }
}