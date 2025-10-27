package com.telconova.suportsuite.controller;

import com.telconova.suportsuite.DTO.AuthResponse;
import com.telconova.suportsuite.DTO.LoginRequest;
import com.telconova.suportsuite.security.IAuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private IAuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        // Obtener la dirección IP del cliente (requerida para la auditoría HU-03.4)
        String ipAddress = request.getRemoteAddr();

        AuthResponse response = authService.login(loginRequest, ipAddress);

        // Retorna la respuesta con el JWT (HU-03.1)
        return ResponseEntity.ok(response);
    }
}
