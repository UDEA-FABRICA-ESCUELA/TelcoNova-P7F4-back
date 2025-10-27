package com.telconova.suportsuite.DTO;

import com.telconova.suportsuite.entity.NotificationChannel;

import com.telconova.suportsuite.entity.Notification.NotificationStatus;
import lombok.Data;
// Importamos la Entidad Notification para el Enum.
import com.telconova.suportsuite.entity.Notification;

import java.time.LocalDateTime;

@Data
public class NotificationDTO {

    private Long id;
    private String recipient;
    private String subject;
    private String content;

    private NotificationChannel channel;
    private NotificationStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private Integer reintentosCount;
    private String errorMenssage;

}