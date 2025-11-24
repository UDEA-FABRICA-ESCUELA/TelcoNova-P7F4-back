package com.telconova.suportsuite.service.impl;

import com.telconova.suportsuite.entity.Notification;
import com.telconova.suportsuite.service.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailNotificationSender implements NotificationSender {

    @Override
    public boolean canSend(Notification notification) {
        return "EMAIL".equals(notification.getChannel().name());
    }

    @Override
    public boolean send(Notification notification) {

        // LÓGICA DE SIMULACIÓN CONTROLADA POR DATO
        String recipient = notification.getRecipient().toLowerCase();

        if (recipient.contains("fallo") || recipient.contains("failure")) {
            log.warn("SIMULACIÓN DE FALLO ACTIVA: El destinatario '{}' contiene palabra clave de fallo. Forzando error.", notification.getRecipient());
            return false; // Simula un fallo de envío (HU-005.3)
        }

        // --- Lógica de Envío Real (o simulación de éxito) ---
        log.info("Enviando Email a: {} con asunto: {}", notification.getRecipient(), notification.getSubject());

        // Simulación de éxito
        return true;
    }
}