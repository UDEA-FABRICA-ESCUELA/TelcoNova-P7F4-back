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

    @NotNull(message = "El evento disparador es obligatorio")
    private EventTrigger eventTrigger;

    @NotNull(message = "El template ID es obligatorio")
    private Long templateId; //  tipo de mensaje

    @NotBlank(message = "El público objetivo es obligatorio")
    private String targetAudience;

    @NotNull(message = "El canal de envío es obligatorio")
    private NotificationChannel channel;

    private Integer priority = 5;

    private Boolean isActive = true;
}
