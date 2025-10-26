package com.telconova.suportsuite.service;

import com.telconova.suportsuite.service.AuditService;
import com.telconova.suportsuite.entity.User;
import com.telconova.suportsuite.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importación más simple
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class SecurityPersistenceServiceImpl implements ISecurityPersistenceService {

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final int LOCKOUT_DURATION_MINUTES = 5;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditService auditService;

    // ⭐️ CAMBIO: Se elimina REQUIRES_NEW para evitar solicitar una segunda conexión.
    @Override
    @Transactional
    public boolean handleFailedLogin(User user, String ipAddress) {

        // 1. Incrementar el contador
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        boolean needsLocking = user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS;

        if (needsLocking) {
            user.setLocked(true);
            user.setLockoutEndTime(LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES).truncatedTo(ChronoUnit.SECONDS));

            // 2. Persistir el bloqueo y forzar el commit.
            // (userRepository.save(user) + userRepository.flush() es correcto)
            userRepository.saveAndFlush(user); // Alternativa más concisa y que hace lo mismo

            // 3. Registrar auditoría
            auditService.recordEvent(user.getId(), "ACCOUNT_LOCKED", "Máx. intentos fallidos alcanzado. Bloqueado.", ipAddress);
            return true;

        } else {
            // Persistir solo el contador incrementado
            userRepository.saveAndFlush(user); // Alternativa más concisa

            auditService.recordEvent(user.getId(), "LOGIN_FAILURE", "Contraseña incorrecta. Intento N°" + user.getFailedLoginAttempts(), ipAddress);
            return false;
        }
    }

    // ⭐️ CAMBIO: Se elimina REQUIRES_NEW.
    @Override
    @Transactional
    public void handleSuccessfulLogin(User user, String ipAddress) {
        user.setFailedLoginAttempts(0);
        user.setLastSuccessfulLogin(LocalDateTime.now());
        user.setLockoutEndTime(null);
        user.setLocked(false);

        // Forzar el commit inmediato.
        userRepository.saveAndFlush(user); // Alternativa más concisa
        auditService.recordEvent(user.getId(), "LOGIN_SUCCESS", "Acceso exitoso.", ipAddress);
    }

    // ⭐️ CAMBIO: Se elimina REQUIRES_NEW.
    @Override
    @Transactional
    public void autoUnlockAccount(User user, String ipAddress) {

        // 1. Aplicar el desbloqueo en el objeto
        user.setLocked(false);
        user.setFailedLoginAttempts(0);
        user.setLockoutEndTime(null);

        // 2. Persistir el estado y forzar el commit
        //userRepository.saveAndFlush(user); // Alternativa más concisa

        userRepository.save(user);

        // 3. Registrar auditoría
        auditService.recordEvent(user.getId(), "ACCOUNT_UNLOCKED_AUTO", "Cuenta desbloqueada automáticamente por tiempo.", ipAddress);
    }
}