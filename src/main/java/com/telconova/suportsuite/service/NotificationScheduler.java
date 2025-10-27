package com.telconova.suportsuite.service;

import com.telconova.suportsuite.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j//Inyecta automáticamente una instancia de logger llamada log en tu clase,
@Component
@RequiredArgsConstructor //Genera automáticamente un constructor que incluye argumentos para todos los campos final

public class NotificationScheduler {

    private final NotificationService notificationService;

    // Procesa la cola automáticamente cada 10 segundos
    @Scheduled(fixedDelay = 10000) //es una herramienta de Spring Framework que se utiliza para programar la ejecución periódica

    public void processNotificationQueue(){
        log.debug("inicicando procesamiento de cola de notificaciones");

        // Obterner notificaciones pendientes ordenadas por prioridad
        List<Notification> pendingNotification =
                notificationService.getPendingNotifications();
        if(pendingNotification.isEmpty()){
            log.debug("No hay notificaciones pendientes en cola");
            return;
        }
        log.info("Procesando {} notificaciones pendientes", pendingNotification.size());

        // Procesar cada notificación
        for(Notification notification : pendingNotification){
            try{
                notificationService.processNotification(notification);
            }catch (Exception e){
                log.error("Error procesando notificación ID {} : {}",notification.getId(),e.getMessage());
                notificationService.handleFailure(notification, e.getMessage());
            }
        }

        log.info("Procesamiento de cola completado ");

    }
    // Reintentar notificaciones fallidas cada 5 minutos
    @Scheduled(fixedDelay = 300000)
    public  void retryFailedNotications(){
        log.debug("Buscando notificaciones para reintentar ");

        // CORRECCIÓN: Usar un método específico. Cambié 'failedNotifications' a 'retryNotifications'
        List <Notification> retryNotifications =
                notificationService.getNotificationsForRetry();

        if(retryNotifications.isEmpty()){
            // ...
        }
        // ...

        for(Notification notification : retryNotifications){
            try {
                notificationService.processNotification(notification);
            }catch (Exception e){
                log.error("Error en el reintento de notificación ID {} : {}",notification.getId(),e.getMessage(),e);
                // IMPORTANTE: Registrar la nueva falla para actualizar el contador de reintentos
                notificationService.handleFailure(notification, e.getMessage());
            }
        }
    }
}
