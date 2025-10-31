package com.telconova.suportsuite.entity;

public enum EventTrigger {

    // Eventos del sistema
    USER_REGISTERED("Nuevo usuario registrado"),
    USER_LOGIN("Usuario inició sesión"),
    USER_LOGOUT("Usuario cerró sesión "),

    // Eventos Tickets
    TICKET_CREATED ("Nuevo ticket creado"),
    TICKET_ASSIGNED("Ticket asignado a técnico "),
    TICKET_STATUS_CHANGED("Estado de ticket cambiado"),
    TICKET_CLOSED("Ticket cerrado "),
    TICKET_REOPENED("Ticket reabierto"),

    // Eventos de alerta de tiempo crítico (SLA)
    SLA_WARNING("Advertencia de SLA  próximo a vercer"),
    SLA_BREACHED("SLA violado"),

    //Eventos de mantenimiento
    MAINTENANCE_SCHEDULED ("Mantenimiento programado "),
    MAINTENANCE_STARTED("Mantenimiento iniciado"),
    MAINTENANCE_COMPLETED("Mantenimiento completado "),

    //Eventos de facturación
    INVOICE_GENERATED("Factura generada"),
    PAYMENT_RECEIVED("Pago recibido"),
    PAYMENT_OVERDUE("Pago vencido") ,

    //Eventos personalizados
    CUSTOM_EVENT("Evento personalizado");

    private final String description;

    EventTrigger (String description){
        this.description = description;
    }
    public  String getDescription(){
        return description;
    }

}
