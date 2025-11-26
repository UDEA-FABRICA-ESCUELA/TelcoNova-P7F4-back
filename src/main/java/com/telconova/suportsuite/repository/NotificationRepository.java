package com.telconova.suportsuite.repository;

import com.telconova.suportsuite.entity.Notification;
import com.telconova.suportsuite.entity.Notification.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification,Long> {

    // 1. HU-005.4: Métodos de monitoreo y conteo.
    Long countByStatus(NotificationStatus status);

    // 2. HU-005.2: Cola Activa (PENDIENTE y REINTENTANDO)
    List<Notification> findByStatusInOrderByPriorityAscCreatedAtAsc(List<NotificationStatus> statuses);

    // 3. HU-005.4: Obtener notificaciones para la visualización de errores (FALLIDA y REINTENTANDO)
    List<Notification> findByStatusInOrderByCreatedAtDesc(List<NotificationStatus> statuses);

    @Query("SELECT n FROM Notification n WHERE n.status = 'FALLIDA' AND n.reintentosCount < n.maxReintentos")
    List<Notification> findNotificationsEligibleForRetry();

}