package com.telconova.suportsuite;

import com.telconova.suportsuite.entity.EventTrigger;
import com.telconova.suportsuite.service.AlertEventService;
import com.telconova.suportsuite.service.TicketService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private AlertEventService alertEventService;

    @InjectMocks
    private TicketService ticketService;

    @Test
    void createTicket_ShouldTriggerEventSuccessfully() {
        // ------------------ ARRANGE ------------------
        String ticketNumber = "TCK-123";
        String customerName = "Juan Perez";

        // ------------------ ACT ------------------
        ticketService.createTicket(ticketNumber, customerName);

        // ------------------ ASSERT ------------------
        verify(alertEventService, times(1))
                .processEvent(eq(EventTrigger.TICKET_CREATED), any(Map.class));
    }

    @Test
    void assignTicket_ShouldTriggerAssignmentEvent() {
        // ------------------ ARRANGE ------------------
        String ticketNumber = "TCK-456";
        String technicianName = "Maria Lopez";

        // ------------------ ACT ------------------
        ticketService.assignTicket(ticketNumber, technicianName);

        // ------------------ ASSERT ------------------
        verify(alertEventService, times(1))
                .processEvent(eq(EventTrigger.TICKET_ASSIGNED), any(Map.class));
    }
}
