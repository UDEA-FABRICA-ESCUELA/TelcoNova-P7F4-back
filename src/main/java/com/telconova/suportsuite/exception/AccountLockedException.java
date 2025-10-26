package com.telconova.suportsuite.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Mapea el error 401/403 automáticamente para el frontend
// HttpStatus.UNAUTHORIZED (401) o FORBIDDEN (403) son apropiados aquí.
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AccountLockedException extends RuntimeException {
    public AccountLockedException(String message) {
        super(message);
    }
}