package com.telconova.suportsuite.DTO;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class CreateTemplateRequest {
    @NotBlank(message = "El nombre de la plantilla no puede estar vacío.")
    private String name;
    @NotBlank(message = "El contenido del mensaje no puede estar vacío.")
    private String content;
}
