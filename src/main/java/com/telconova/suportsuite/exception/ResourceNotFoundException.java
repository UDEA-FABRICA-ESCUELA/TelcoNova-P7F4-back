package com.telconova.suportsuite.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción para manejar peticiones donde el recurso solicitado (por ID) no existe.
 */
@ResponseStatus(HttpStatus.NOT_FOUND) // ⬅️ Esto asegura la respuesta HTTP 404
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}