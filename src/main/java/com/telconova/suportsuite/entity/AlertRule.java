package com.telconova.suportsuite.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


@Entity
@Data
@Table(name= "alert_rules")

public class AlertRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Evento disparador
    @Enumerated(EnumType.STRING)
    @Column (nullable = false)
    private EventTrigger triggerEvent;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "template_id")
    private MessageTemplate messageTemplate;

    @Column (name = "target_audience", columnDefinition = "TEXT")
    private String targetAudience;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    //Estado de la regla (activa / inactiva)
    @Column (nullable = false)
    private Boolean isActive = true;

    @Column (name = "priority")
    private Integer priority = 5;

    //Trazabilidad

    @Column (name = "created_by")
    private  String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column (name = "updated_by")
    private String updatedBy;

    @Column (name = "updated_at")
    private LocalDateTime updatedAt;

//Se utiliza para inicializar campos que solo deben configurarse una vez en el momento de la creación, como la fecha de creación
    @PrePersist
    protected  void onCreate(){
        createdAt= LocalDateTime.now();
    }
//Se utiliza principalmente para actualizar automáticamente los campos de auditoría, como la fecha de la última modificación (updatedAt) y el usuario que hizo la modificación (updatedBy).
    @PreUpdate
    protected void onUpdate(){
        updatedAt = LocalDateTime.now();
    }


}
