package com.telconova.suportsuite.controller;

import com.telconova.suportsuite.DTO.CreateTemplateRequest;
import com.telconova.suportsuite.DTO.MessageTemplateDto;
import com.telconova.suportsuite.service.MessageTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/templates")
public class MessageTemplateController {

    @Autowired
    private MessageTemplateService messageTemplateService;

    @PostMapping
    public ResponseEntity<MessageTemplateDto> createTemplate(@Valid @RequestBody CreateTemplateRequest request) {
        // HU-01: Agregar una nueva plantilla
        MessageTemplateDto newTemplate = messageTemplateService.createTemplate(request);
        return new ResponseEntity<>(newTemplate, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<MessageTemplateDto>> getAllTemplates() {
        // HU-02: Listar plantillas existentes
        List<MessageTemplateDto> templates = messageTemplateService.getAllTemplates();
        return ResponseEntity.ok(templates);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageTemplateDto> updateTemplate(@PathVariable Long id, @RequestBody MessageTemplateDto dto) {
        // HU-02: Editar una plantilla
        MessageTemplateDto updatedTemplate = messageTemplateService.updateTemplate(id, dto);
        return ResponseEntity.ok(updatedTemplate);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        // HU-02: Eliminar una plantilla
        messageTemplateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }
}