package com.telconova.suportsuite.entity;

public enum AuditAction {
    CREATE("Regla creada"),
    UPDATE("Regla modificada"),
    DELETE("Regla eliminada"),
    ACTIVATE("Regla activada"),
    DEACTIVATE("Regla desactivada");

    private final String description;

    AuditAction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
