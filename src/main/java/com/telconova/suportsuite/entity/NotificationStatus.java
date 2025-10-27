package com.telconova.suportsuite.entity;

public enum NotificationStatus { //Los enum se deben usar cuando necesitas un conjunto de valores fijos, finitos y conocidos de antemano.
    PENDIENTE("Pendiente"),
    PROCESANDO("Procesando"),
    ENVIADO("Enviado"),
    FALLIDA("Fallida"),
    REINTENTADO("Reintentando");

    private final String description;

    NotificationStatus (String description){
        this.description = description;

    }

    public  String getDescription(){
        return  description;
    }
}
