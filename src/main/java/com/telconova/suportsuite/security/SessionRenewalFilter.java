package com.telconova.suportsuite.security;

import com.telconova.suportsuite.entity.User; // Asume que esta es tu entidad User
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que maneja la inactividad de la sesión (HU-003.3).
 * Renueva el token si el usuario está activo (hace peticiones) y el token está cerca de expirar.
 */
@Component
public class SessionRenewalFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider;

    // Umbral de Renovación: 5 minutos (300,000 ms).
    // Si quedan menos de 5 min para expirar, se renueva la sesión.
    private static final long RENEWAL_THRESHOLD_MS = 300000L;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Solo procesar si el usuario ya fue autenticado por JwtAuthenticationFilter
        if (SecurityContextHolder.getContext().getAuthentication() != null) {

            String jwt = tokenProvider.getJwtFromRequest(request);

            if (jwt != null) {

                long remainingTimeMs = tokenProvider.getRemainingTimeInMs(jwt);

                // Verificar si el token es válido y está dentro del umbral de renovación
                if (remainingTimeMs > 0 && remainingTimeMs < RENEWAL_THRESHOLD_MS) {

                    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

                    // Solo renovar si el principal es una instancia de User
                    if (principal instanceof User) {

                        User user = (User) principal;

                        // Generar el nuevo token
                        String newJwt = tokenProvider.generateToken(user);

                        // Devolver el nuevo token en un encabezado CUSTOM (X-New-Token).
                        // El frontend es responsable de capturar y reemplazar este nuevo token.
                        response.setHeader("X-New-Token", newJwt);
                        logger.info("Token JWT renovado para el usuario: " + user.getUsername());
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
