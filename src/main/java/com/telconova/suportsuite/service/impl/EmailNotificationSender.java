package com.telconova.suportsuite.service.impl;

import com.telconova.suportsuite.entity.Notification;
import com.telconova.suportsuite.service.NotificationSender;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.telconova.suportsuite.entity.NotificationChannel;

@Slf4j
@Component
public class EmailNotificationSender implements NotificationSender {

    // 1. IMPLEMENTACIÓN DE send(Notification)
    // Este es el método que realiza el envío y debe retornar un boolean.
    @Override
    public boolean send(Notification notification) {

        // Verificar el canal para evitar enviar emails a números de teléfono
        if (!canSend(notification)) {
            log.warn("El canal de notificación no es EMAIL. No se puede enviar: {}", notification.getId());
            return false;
        }

        try {
            log.info("Iniciando envío de email a: {}", notification.getRecipient()); // Asumo que corregiste el campo a 'destino'

            // --- Lógica real del envío de email iría aquí ---
            // Simulación de envío:
            // if (servicioEmail.send(notification.getDestino(), notification.getMessage()))

            log.info("Email enviado exitosamente a {}", notification.getRecipient());

            // ¡IMPORTANTE! Si el envío es exitoso, retornamos true.
            return true;

        } catch (Exception e) {
            log.error("Error enviando el email a {}: {}", notification, e.getMessage());

            // Si el envío falla, retornamos false.
            return false;
        }
    }

    // 2. IMPLEMENTACIÓN DE canSend(Notification)
    // Este método verifica si este 'sender' es adecuado para la notificación dada.
    @Override
    public boolean canSend(Notification notification) {

        return notification.getChannel() == NotificationChannel.EMAIL;
    }
}