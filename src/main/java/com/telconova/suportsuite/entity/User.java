package com.telconova.suportsuite.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "tn_user")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    public String getPassword() {
        return passwordHash;
    }
    public void setPasswordHash(String passwordHash) {}

    // Campos para HU-03.2: Bloqueo por fallos
    @Column(name = "is_locked")
    private boolean isLocked = false;

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    // Campo para HU-03.3 (Opcional en la entidad, pero útil para auditoría)
    @Column(name = "last_successful_login")
    private LocalDateTime lastSuccessfulLogin;

    @Column(nullable = false)
    private String roles; // Ejemplo: "ADMIN_ALERTS"

    @Column(name = "lockout_end_time")
    private LocalDateTime lockoutEndTime;
}