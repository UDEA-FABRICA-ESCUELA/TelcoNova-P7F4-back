package com.telconova.suportsuite.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 1. Maneja la excepción de cuenta bloqueada (AccountLockedException)
    // Spring ya devuelve 401 por la anotación, pero con este Handler garantizamos el formato JSON
    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<Map<String, Object>> handleAccountLockedException(AccountLockedException ex) {
        return createErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage());
    }

    // 2. Maneja la excepción estándar de credenciales inválidas (Login fallido simple)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentialsException(BadCredentialsException ex) {
        // Por seguridad, siempre se devuelve un mensaje genérico para no dar pistas al atacante.
        return createErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", "Usuario o contraseña incorrectos.");
    }

    // 3. Método auxiliar para crear una respuesta de error JSON limpia
    private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String error, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", error);
        response.put("message", message);

        return new ResponseEntity<>(response, status);
    }
}
