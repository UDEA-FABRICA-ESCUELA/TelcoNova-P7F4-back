package com.telconova.suportsuite.service;

import com.telconova.suportsuite.entity.EventTrigger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Ejemplo de cómo disparar eventos desde otras partes del sistema
 */
@Service
@RequiredArgsConstructor
public class TicketService {

    private final AlertEventService alertEventService;

    public void createTicket(String ticketNumber, String customerName) {
        // ... lógica de creación de ticket ...

        // Disparar evento automático
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("ticket_number", ticketNumber);
        eventData.put("customer_name", customerName);
        eventData.put("created_date", java.time.LocalDateTime.now());

        // Esto evaluará automáticamente las reglas activas para TICKET_CREATED
        alertEventService.processEvent(EventTrigger.TICKET_CREATED, eventData);
    }

    public void assignTicket(String ticketNumber, String technicianName) {
        // ... lógica de asignación ...

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("ticket_number", ticketNumber);
        eventData.put("technician_name", technicianName);

        alertEventService.processEvent(EventTrigger.TICKET_ASSIGNED, eventData);
    }
}
