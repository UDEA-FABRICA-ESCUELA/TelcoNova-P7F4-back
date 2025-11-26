package com.telconova.suportsuite;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telconova.suportsuite.DTO.CreateNotificationRequest;

import com.telconova.suportsuite.entity.AlertRule;
import com.telconova.suportsuite.entity.EventTrigger;
import com.telconova.suportsuite.entity.MessageTemplate;
import com.telconova.suportsuite.entity.NotificationChannel;
import com.telconova.suportsuite.service.AlertEventService;
import com.telconova.suportsuite.service.AlertRuleService;
import com.telconova.suportsuite.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertEventServiceTest {

    @Mock
    private AlertRuleService alertRuleService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AlertEventService alertEventService;

    // =======================================================
    // 1. processEvent() — no hay reglas
    // =======================================================
    @Test
    void processEvent_NoRules_ShouldDoNothing() {
        when(alertRuleService.getRulesByEvent(EventTrigger.TICKET_CREATED))
                .thenReturn(Collections.emptyList());

        alertEventService.processEvent(EventTrigger.TICKET_CREATED, Map.of());

        verify(notificationService, never()).createNotification(any());
    }

    // =======================================================
    // 2. processEvent() — reglas válidas → crea notificaciones
    // =======================================================
    @Test
    void processEvent_WithRules_ShouldCreateNotifications() throws Exception {
        AlertRule rule = buildRule();

        when(alertRuleService.getRulesByEvent(EventTrigger.TICKET_CREATED))
                .thenReturn(List.of(rule));

        when(objectMapper.readValue(anyString(), eq(Map.class)))
                .thenReturn(Map.of("role", "admin"));

        Map<String, Object> eventData = Map.of("ticket_number", "TK-001");

        alertEventService.processEvent(EventTrigger.TICKET_CREATED, eventData);

        verify(notificationService, times(1))
                .createNotification(any(CreateNotificationRequest.class));
    }

    // =======================================================
    // 3. processEvent() — errores dentro de reglas NO rompen
    // =======================================================
    @Test
    void processEvent_RuleThrowsException_ShouldContinue() {
        AlertRule rule = mock(AlertRule.class);
        when(rule.getId()).thenReturn(99L);
        when(rule.getMessageTemplate()).thenThrow(new RuntimeException("fail"));

        when(alertRuleService.getRulesByEvent(EventTrigger.TICKET_ASSIGNED))
                .thenReturn(List.of(rule));

        alertEventService.processEvent(EventTrigger.TICKET_ASSIGNED, Map.of());

        verify(notificationService, never()).createNotification(any());
    }

    // =======================================================
    // HELPER: Crear una regla válida
    // =======================================================
    private AlertRule buildRule() {
        AlertRule rule = new AlertRule();
        rule.setId(1L);
        rule.setName("Test Rule");
        rule.setChannel(NotificationChannel.valueOf("EMAIL"));
        rule.setPriority(Integer.valueOf("HIGH"));
        rule.setTargetAudience("{\"role\":\"admin\"}");

        MessageTemplate tpl = new MessageTemplate();
        tpl.setName("Test Template");
        tpl.setContent("Hola {ticket_number}");

        rule.setMessageTemplate(tpl);

        return rule;

    }
}
