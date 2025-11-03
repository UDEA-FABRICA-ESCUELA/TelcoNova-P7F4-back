package com.telconova.suportsuite.service.impl;

import com.telconova.suportsuite.entity.Notification;
import com.telconova.suportsuite.entity.NotificationChannel;
import com.telconova.suportsuite.service.NotificationSender; // Asumiendo esta ruta
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service // 1. Componente de Spring: Necesario para que sea detectado e inyectado
public class SmsNotificationSender implements NotificationSender {

    /**
     * Verifica si este sender puede manejar el canal SMS.
     * Esto es crucial para que NotificationService lo encuentre.
     */
    @Override
    public boolean canSend(Notification notification) {
        // 🟢 Devuelve TRUE solo si el canal de la notificación es SMS
        return notification.getChannel() == NotificationChannel.SMS;
    }

    /**
     * Simula el proceso de envío del mensaje de texto.
     */
    @Override
    public boolean send(Notification notification) {
        log.info("-----------------------------------------------------");
        log.info("✅ INICIANDO ENVÍO SMS (Simulación)");
        log.info("   Destinatario: {}", notification.getRecipient());
        log.info("   Asunto: {}", notification.getSubject());
        log.info("   Mensaje: {}...", notification.getContent().substring(0, Math.min(notification.getContent().length(), 50)));
        log.info("-----------------------------------------------------");

        // --- LÓGICA DE NEGOCIO REAL (Twilio, Amazon SNS, etc.) ---

        // En un entorno real:
        // 1. Llamada a una API externa (ej. Twilio, ClickSend).
        // 2. Manejo de excepciones de la API externa.
        // 3. Devolución de TRUE si la API externa confirma el envío.

        // Para fines de prueba y dado que el error era la falta del sender:
        return true; // 🟢 Asumimos éxito para que pase a estado 'ENVIADO'
    }
}