package com.telconova.suportsuite.controller;

import com.telconova.suportsuite.DTO.AlertRuleAuditDto;
import com.telconova.suportsuite.DTO.AlertRuleDto;
import com.telconova.suportsuite.DTO.CreateAlertRuleRequest;
import com.telconova.suportsuite.service.AlertRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "https://telco-nova-p7-f4-front.vercel.app")
@RestController
@RequestMapping("/api/v1/alert-rules")
@RequiredArgsConstructor
public class AlertRuleController {

    private final AlertRuleService alertRuleService;

    /**
     *  Crear nueva regla de alerta
     * POST /api/v1/alert-rules
     */
    @PostMapping()
    public ResponseEntity<AlertRuleDto> createAlertRule(
            @Valid @RequestBody CreateAlertRuleRequest request,
            @RequestHeader(value = "X-User-Name", defaultValue = "system") String username) {

        AlertRuleDto rule = alertRuleService.createAlertRule(request, username);
        return new ResponseEntity<>(rule, HttpStatus.CREATED);
    }

    /**
     *  Editar regla existente
     * PUT /api/v1/alert-rules/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<AlertRuleDto> updateAlertRule(
            @PathVariable Long id,
            @Valid @RequestBody CreateAlertRuleRequest request,
            @RequestHeader(value = "X-User-Name", defaultValue = "system") String username) {

        AlertRuleDto rule = alertRuleService.updateAlertRule(id, request, username);
        return ResponseEntity.ok(rule);
    }

    /**
     *  Eliminar regla
     * DELETE /api/v1/alert-rules/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlertRule(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Name", defaultValue = "system") String username) {

        alertRuleService.deleteAlertRule(id, username);
        return ResponseEntity.noContent().build();
    }

    /**
     *  Activar regla
     * PATCH /api/v1/alert-rules/{id}/activate
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<AlertRuleDto> activateRule(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Name", defaultValue = "system") String username) {

        AlertRuleDto rule = alertRuleService.activateRule(id, username);
        return ResponseEntity.ok(rule);
    }

    /**
     *  Desactivar regla
     * PATCH /api/v1/alert-rules/{id}/deactivate
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<AlertRuleDto> deactivateRule(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Name", defaultValue = "system") String username) {

        AlertRuleDto rule = alertRuleService.deactivateRule(id, username);
        return ResponseEntity.ok(rule);
    }

    /**
     * Listar todas las reglas
     * GET /api/v1/alert-rules
     */
    @GetMapping
    public ResponseEntity<List<AlertRuleDto>> getAllRules() {
        List<AlertRuleDto> rules = alertRuleService.getAllRules();
        return ResponseEntity.ok(rules);
    }

    /**
     * Obtener regla específica
     * GET /api/v1/alert-rules/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AlertRuleDto> getRule(@PathVariable Long id) {
        // Implementar metodo en el servicio
        return ResponseEntity.ok(null);
    }
}
