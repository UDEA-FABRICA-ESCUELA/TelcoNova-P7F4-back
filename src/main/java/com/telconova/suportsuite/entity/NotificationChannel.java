package com.telconova.suportsuite.entity;

public enum NotificationChannel {// Enum Solo existen las constantes predefinidas
    EMAIL("Correo Electrónico"),
    SMS ("Mensaje de Texto"),
    PUSH("Notificación Push"),
    WHATSAPP("WhatsApp");

    private final String description;

    NotificationChannel (String description){
        this.description = description;
    }
    public String getDescription(){
        return description;
    }
}
