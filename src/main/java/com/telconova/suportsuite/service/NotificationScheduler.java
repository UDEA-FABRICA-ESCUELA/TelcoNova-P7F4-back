package com.telconova.suportsuite.service;

import com.telconova.suportsuite.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;

    /**
     * Procesa la cola automáticamente cada minuto.
     * Esta tarea verifica los estados PENDIENTE y REINTENTANDO.
     * fixedDelay: Asegura que haya 1 minuto de descanso después de que termina la ejecución anterior.
     */
    @Scheduled(fixedDelay = 60000)
    public void processNotificationQueue(){
        log.debug("Iniciando procesamiento de cola de notificaciones (PENDIENTES y REINTENTANDO).");

        List<Notification> queueNotifications =
                notificationService.getQueueNotifications();

        if(queueNotifications.isEmpty()){
            log.debug("No hay notificaciones pendientes ni reintentando en cola.");
            return;
        }
        log.info("Procesando {} notificaciones en cola.", queueNotifications.size());

        // Procesar cada notificación en orden
        for(Notification notification : queueNotifications){
            try{
                notificationService.processNotification(notification);
            }catch (Exception e){
                log.error("Error procesando notificación ID {} : {}",notification.getId(),e.getMessage());
                // IMPORTANTE: Registrar el fallo para actualizar el contador de reintentos
                notificationService.handleFailure(notification, e.getMessage());
            }
        }

        log.info("Procesamiento de cola completado.");
    }

    @Scheduled(fixedDelay = 300000)
    public  void retryFailedNotications(){
        log.debug("Buscando notificaciones para reintentar (policy 5 min). ");

        List <Notification> retryNotifications =
                notificationService.getNotificationsForRetry();

        if(retryNotifications.isEmpty()){
            log.debug("No hay notificaciones para reintentar en este ciclo.");
            return;
        }
        log.info("Reintentando {} notificaciones.", retryNotifications.size());


        for(Notification notification : retryNotifications){
            try {
                notificationService.processNotification(notification);
            }catch (Exception e){
                log.error("Error en el reintento de notificación ID {} : {}",notification.getId(),e.getMessage(),e);
                notificationService.handleFailure(notification, e.getMessage());
            }
        }
        log.info("Ciclo de reintento de notificaciones completado.");
    }
}