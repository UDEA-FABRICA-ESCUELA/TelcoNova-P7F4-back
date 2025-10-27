package com.telconova.suportsuite.service;

import com.telconova.suportsuite.entity.Notification;

public interface NotificationSender {

    boolean send (Notification notification);

    // Valida si el canal puede enviar la notificaión

    boolean canSend(Notification notification);


}
