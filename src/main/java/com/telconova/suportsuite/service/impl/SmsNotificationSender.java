package com.telconova.suportsuite.service.impl;

import com.telconova.suportsuite.entity.Notification;
import com.telconova.suportsuite.entity.NotificationChannel;
import com.telconova.suportsuite.service.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsNotificationSender implements NotificationSender {

    /**
     * Verifica si este sender puede manejar el canal SMS.
     */
    @Override
    public boolean canSend(Notification notification) {
        // Devuelve TRUE solo si el canal de la notificación es SMS
        return notification.getChannel() == NotificationChannel.SMS;
    }

    /**
     * Simula el proceso de envío del mensaje de texto, incluyendo la lógica de simulación
     * de fallo basada en el destinatario.
     */
    @Override
    public boolean send(Notification notification) {

        // LÓGICA DE SIMULACIÓN CONTROLADA POR DATO (Para pruebas)
        String recipient = notification.getRecipient().toLowerCase();

        if (recipient.contains("fallo") || recipient.contains("failure")) {
            log.warn("SIMULACIÓN DE FALLO ACTIVA: El destinatario '{}' contiene palabra clave de fallo. Forzando error SMS.", notification.getRecipient());
            return false; // Retorna FALLO para activar el ciclo de reintentos
        }

        //  (Simulación de Éxito) ---

        log.info("-----------------------------------------------------");
        log.info("INICIANDO ENVÍO SMS (Simulación)");
        log.info("   Destinatario: {}", notification.getRecipient());
        log.info("   Asunto: {}", notification.getSubject());
        log.info("   Mensaje: {}...", notification.getContent().substring(0, Math.min(notification.getContent().length(), 50)));
        log.info("-----------------------------------------------------");

        return true; // Asumimos éxito si no se fuerza el fallo
    }
}