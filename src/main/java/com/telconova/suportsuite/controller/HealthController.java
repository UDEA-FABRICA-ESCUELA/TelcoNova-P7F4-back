package com.telconova.suportsuite.controller;

import com.telconova.suportsuite.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    // Inyectamos el UserRepository para acceder al método de verificación.
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/db")
    public ResponseEntity<String> checkDbHealth() {
        try {
            // Intentamos ejecutar la consulta simple
            userRepository.checkDatabaseConnection();

            // Si llega aquí, la conexión fue exitosa
            return ResponseEntity.ok("✅ Conexión a la BD exitosa. HikariPool funcional.");

        } catch (Exception e) {
            // Si ocurre un error (ej. FATAL: Max client connections reached)
            // devolvemos un código de error 503 (Service Unavailable)
            System.err.println("Error al intentar obtener conexión para /api/health/db: " + e.getMessage());

            String errorMessage = "❌ Falló la conexión a la BD. Causa: " + e.getMessage();

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(errorMessage);
        }
    }
}