package com.telconova.suportsuite.service;

import com.telconova.suportsuite.entity.User;

public interface ISecurityPersistenceService {

    // Retorna TRUE si la cuenta fue bloqueada.
    boolean handleFailedLogin(User user, String ipAddress);

    void handleSuccessfulLogin(User user, String ipAddress);

    // *NUEVO*: Maneja el desbloqueo automático en una transacción independiente.
    void autoUnlockAccount(User user, String ipAddress);
}