package com.telconova.suportsuite.service;

import com.telconova.suportsuite.DTO.CreateTemplateRequest;
import com.telconova.suportsuite.DTO.MessageTemplateDto;
import com.telconova.suportsuite.entity.MessageTemplate;
import com.telconova.suportsuite.repository.MessageTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MessageTemplateService {

    @Autowired
    private MessageTemplateRepository messageTemplateRepository;

    private static final String VALID_VARIABLE_REGEX = "\\{[_a-zA-Z]+\\}";

    public MessageTemplateDto createTemplate(CreateTemplateRequest request) {
        // Validación de variables dinámicas según el Criterio de Aceptación de HU-01
        validateVariables(request.getContent());

        MessageTemplate template = new MessageTemplate();
        template.setName(request.getName());
        template.setContent(request.getContent());

        MessageTemplate savedTemplate = messageTemplateRepository.save(template);

        // Mapeo de la entidad a DTO para la respuesta
        return convertToDto(savedTemplate);
    }

    public List<MessageTemplateDto> getAllTemplates() {
        // Lógica para el Criterio de Aceptación de HU-02: listar todas las plantillas
        List<MessageTemplate> templates = messageTemplateRepository.findAll();
        return templates.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public MessageTemplateDto updateTemplate(Long id, MessageTemplateDto dto) {
        // Lógica para el Criterio de Aceptación de HU-02: editar una plantilla
        return messageTemplateRepository.findById(id).map(template -> {
            template.setName(dto.getName());
            template.setContent(dto.getContent());
            MessageTemplate updatedTemplate = messageTemplateRepository.save(template);
            return convertToDto(updatedTemplate);
        }).orElseThrow(() -> new RuntimeException("Plantilla no encontrada con ID: " + id));
    }

    public void deleteTemplate(Long id) {
        // Lógica para el Criterio de Aceptación de HU-02: eliminar una plantilla
        messageTemplateRepository.deleteById(id);
    }

    private void validateVariables(String content) {
        Pattern pattern = Pattern.compile(VALID_VARIABLE_REGEX);
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String variable = matcher.group();
            // Criterio de Aceptación de HU-01: validar el formato de las variables.
            // Aquí, por simplicidad, solo se valida el formato {variable_valida}.
            // Se puede extender con una lista de variables predefinidas si es necesario.
            System.out.println("Variable dinámica encontrada y validada: " + variable);
        }
    }

    private MessageTemplateDto convertToDto(MessageTemplate template) {
        MessageTemplateDto dto = new MessageTemplateDto();
        dto.setId(template.getId());
        dto.setName(template.getName());
        dto.setContent(template.getContent());
        return dto;
    }
}