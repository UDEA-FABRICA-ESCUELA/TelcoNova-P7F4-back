package com.telconova.suportsuite.DTO;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuthResponse {
    private String jwtToken;
    private String welcomeMessage; // Mensaje de bienvenida (HU-03.1)
    private LocalDateTime expirationTime;
}
