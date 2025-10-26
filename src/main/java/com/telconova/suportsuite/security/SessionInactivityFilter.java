package com.telconova.suportsuite.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;
import java.time.Duration;

@Component
public class SessionInactivityFilter extends OncePerRequestFilter {

    // Almacenamiento temporal para el último acceso. En un entorno real, esto debería estar en Redis.
    private final ConcurrentHashMap<String, LocalDateTime> lastAccessMap = new ConcurrentHashMap<>();

    // Tiempo de inactividad establecido (15 minutos) (HU-03.3)
    @Value("${session.inactivity.minutes:15}")
    private long inactivityMinutes;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Obtener la identificación de la sesión (ej: el JWT del header)
        String token = extractToken(request);

        if (token != null && !token.isEmpty()) {
            // Usamos el token como clave (proxy para el ID de sesión)

            LocalDateTime lastAccess = lastAccessMap.get(token);
            LocalDateTime now = LocalDateTime.now();

            if (lastAccess != null) {
                Duration duration = Duration.between(lastAccess, now);

                // 2. Verificar Inactividad (HU-03.3)
                if (duration.toMinutes() >= inactivityMinutes) {
                    // Sesión expirada por inactividad
                    lastAccessMap.remove(token); // Eliminar la sesión

                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                    response.getWriter().write("{\"error\": \"Su sesión ha expirado por inactividad.\"}");
                    response.setHeader("Location", "/login"); // Redirigir a la pantalla de login
                    return;
                }
            }

            // 3. Mantener Sesión Activa (Actualizar marca de tiempo)
            lastAccessMap.put(token, now);
        }

        filterChain.doFilter(request, response);
    }

    // Método auxiliar para extraer el JWT
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
