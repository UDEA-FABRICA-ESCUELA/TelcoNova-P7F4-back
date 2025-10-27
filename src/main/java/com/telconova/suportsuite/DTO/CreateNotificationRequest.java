package com.telconova.suportsuite.DTO;

import com.telconova.suportsuite.entity.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateNotificationRequest {

    @NotBlank(message = "El destiono es obligatorio")
    private String recipient;

    @NotBlank(message = "El asunto es obligatorio")
    private String subject;

    @NotBlank(message = " El contenido es obligatorio")
    private String content;

    @NotNull (message = "El canal de notificación es obligatorio")
    private NotificationChannel channel;

    /**
     * Nivel de prioridad para la cola de envíos (HU-004).
     * El valor por defecto '5' se establece como la prioridad más baja (normal)
     * para nuevas notificaciones. (1 = más urgente).
     */
    private Integer priority = 5;

}
