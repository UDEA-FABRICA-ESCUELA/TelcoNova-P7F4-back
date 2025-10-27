package com.telconova.suportsuite.repository;

import com.telconova.suportsuite.entity.Notification;

import com.telconova.suportsuite.entity.Notification.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification,Long> {


    List<Notification> findByStatusOrderByPriorityAscCreatedAtAsc(NotificationStatus status);

    // Enum anidado
    Long countByStatus(NotificationStatus status);

    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.reintentosCount < n.maxReintentos")
    List<Notification> findNotificationsEligibleForReattempt(
            @Param("status") NotificationStatus status
    );

    @Query("SELECT n FROM Notification n WHERE (n.status = 'FALLIDA' OR n.status = 'REINTENTANDO') AND n.reintentosCount < n.maxReintentos")
    List<Notification> findNotificationsEligibleForRetry();


    List<Notification> findByStatusIn(List<NotificationStatus> statuses);

}
