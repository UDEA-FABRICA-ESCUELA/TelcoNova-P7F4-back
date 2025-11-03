package com.telconova.suportsuite.DTO;

import com.telconova.suportsuite.entity.EventTrigger;
import com.telconova.suportsuite.entity.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class CreateAlertRuleRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    private String description;

    @NotBlank(message = "El evento disparador es obligatorio") // Usar @NotBlank para String
    private String eventTrigger;

    @NotNull(message = "El template ID es obligatorio")
    private Long templateId; //  tipo de mensaje

    @NotBlank(message = "El público objetivo es obligatorio")
    private String targetAudience;

    @NotBlank(message = "El canal de envío es obligatorio") // Cambiar de @NotNull a @NotBlank si era @NotNull
    private String channel;

    private Integer priority = 5;

    private Boolean isActive = true;
}
