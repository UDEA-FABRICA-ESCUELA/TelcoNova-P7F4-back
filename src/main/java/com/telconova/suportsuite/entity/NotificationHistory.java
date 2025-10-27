package com.telconova.suportsuite.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "notification_history")

public class NotificationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //Indica a la base de datos que genere automáticamente el ID (auto-incrementable).
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Define la relación Many-to-One. FetchType.LAZY asegura que la 'AlertRule' solo se cargue de la DB cuando se acceda a ella (optimización).
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Enumerated(EnumType.STRING) //Indica que el enum debe persistirse en la base de datos como una cadena de texto (ej. "EMAIL")
    @Column(nullable = false)
    private NotificationStatus status;

    @Column(columnDefinition = "TEXT")
    private String menssage; // Descripción

    @Column (name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @PrePersist // Se ejecuta justo antes de guardar la entidad por primera vez. Establece la fecha y hora de creación automática.
    protected void onCreate(){
        timestamp = LocalDateTime.now();
    }

}
