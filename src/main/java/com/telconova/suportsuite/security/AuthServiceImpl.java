package com.telconova.suportsuite.security;

import com.telconova.suportsuite.DTO.AuthResponse;
import com.telconova.suportsuite.DTO.LoginRequest;
import com.telconova.suportsuite.service.AuditService; // Importación ajustada al paquete service
import com.telconova.suportsuite.entity.User;
import com.telconova.suportsuite.repository.UserRepository;
import com.telconova.suportsuite.exception.AccountLockedException;
import com.telconova.suportsuite.service.ISecurityPersistenceService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements IAuthService {

    private static final int MAX_FAILED_ATTEMPTS = 3; // Límite de intentos fallidos

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtTokenProvider tokenProvider;
    @Autowired private AuditService auditService; // 👈 Servicio de Auditoría
    @Autowired private ISecurityPersistenceService securityPersistenceService;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public AuthResponse login(LoginRequest request, String ipAddress) {

        // 1. Buscar usuario
        User user = userRepository.findByUsername(request.getUsername())
                .orElse(null);

        if (user == null) {
            // 🚨 AUDITORÍA: Usuario no existe
            auditService.recordEvent(null, "LOGIN_FAILED", "Intento de login fallido. Usuario no encontrado: " + request.getUsername(), ipAddress);
            throw new BadCredentialsException("Usuario o contraseña incorrectos.");
        }


        // 2. Verificar Bloqueo Temporal (HU-003.2)
        if (user.isLocked()) {

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime lockoutEnd = user.getLockoutEndTime();

            // Desbloqueo automático si el tiempo ha expirado
            if (lockoutEnd == null || now.isAfter(lockoutEnd)) {
                // Llama a REQUIRES_NEW para desbloquear.
                securityPersistenceService.autoUnlockAccount(user, ipAddress);

                // CRÍTICO: Limpiar la caché y recargar el usuario
                entityManager.clear();
                user = userRepository.findById(user.getId())
                        .orElseThrow(() -> new IllegalStateException("Error: Usuario no encontrado después de recarga."));
            } else {
                // Si la cuenta sigue bloqueada
                // 🚨 AUDITORÍA: LOGIN_BLOCKED_TEMPORAL (HU-003.4)
                auditService.recordEvent(user.getId(), "LOGIN_BLOCKED_TEMPORAL",
                        "Cuenta bloqueada temporalmente. Tiempo restante hasta: " + lockoutEnd.toLocalTime(), ipAddress);
                throw new AccountLockedException("Cuenta bloqueada por intentos fallidos. Intente de nuevo después de " + lockoutEnd.toLocalTime() + ".");
            }
        } // Fin del if (user.isLocked())


        // 3. Validar Contraseña
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {

            // 🚨 PERSISTENCIA Y AUDITORÍA: Llama a REQUIRES_NEW para registrar el fallo e iniciar el bloqueo si aplica.
            // NOTA: Se asume que handleFailedLogin llama a auditService.recordEvent("LOGIN_FAILED" y/o "ACCOUNT_LOCKED").
            boolean accountLocked = securityPersistenceService.handleFailedLogin(user, ipAddress);

            // CRÍTICO: Limpiar la caché y recargar el usuario para ver el estado actual
            entityManager.clear();
            user = userRepository.findById(user.getId())
                    .orElseThrow(() -> new IllegalStateException("Error: Usuario no encontrado después de recarga."));

            if (accountLocked) {
                // La cuenta ha sido BLOQUEADA en esta llamada (HU-003.2).
                throw new AccountLockedException("Cuenta bloqueada por alcanzar " + MAX_FAILED_ATTEMPTS + " intentos fallidos.");
            }

            // Si retorna FALSE (fallo de credenciales sin bloqueo)
            throw new BadCredentialsException("Usuario o contraseña incorrectos.");
        }


        // 4. Login Exitoso
        // 🚨 PERSISTENCIA Y AUDITORÍA: Llama a REQUIRES_NEW para persistir el éxito y auditar el LOGIN_SUCCESS.
        securityPersistenceService.handleSuccessfulLogin(user, ipAddress);

        // Generar Token y respuesta (HU-003.1, HU-003.3)
        String jwtToken = tokenProvider.generateToken(user);

        return AuthResponse.builder()
                .jwtToken(jwtToken)
                .welcomeMessage("¡Bienvenido, Administrador de Alertas!")
                .expirationTime(LocalDateTime.now().plusMinutes(30))
                .build();
    }
}