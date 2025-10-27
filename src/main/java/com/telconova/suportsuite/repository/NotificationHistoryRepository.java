package com.telconova.suportsuite.repository;

import com.telconova.suportsuite.entity.NotificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, Long> {

    // CORRECCIÓN: Cambiar findByNotification a findByNotificationId
    List<NotificationHistory> findByNotificationIdOrderByTimestampDesc(Long notificationId);

}