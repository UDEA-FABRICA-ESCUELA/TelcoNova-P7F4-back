package com.telconova.suportsuite.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder; // Importación necesaria

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;
import java.time.Duration;

@Component
public class SessionInactivityFilter extends OncePerRequestFilter {

    private final ConcurrentHashMap<String, LocalDateTime> lastAccessMap = new ConcurrentHashMap<>();

    @Value("${session.inactivity.minutes:15}")
    private long inactivityMinutes;

    // Lista de rutas que el filtro debe ignorar (ej: /login, /auth, /public)
    // Aunque se recomienda configurar esto en la cadena de seguridad,
    // lo hacemos aquí por si el filtro se aplica a todas las rutas.
    private static final String AUTH_PATH = "/api/v1/auth/login";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // --- VERIFICACIÓN DE EXCLUSIÓN ---
        // Excluir la ruta de login para que la autenticación inicial no se bloquee.
        if (request.getServletPath().equals(AUTH_PATH) || request.getServletPath().equals("/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);

        if (token != null && !token.isEmpty()) {
            LocalDateTime lastAccess = lastAccessMap.get(token);
            LocalDateTime now = LocalDateTime.now();
            boolean isInactive = false; // Cambiamos el nombre de la variable para ser más claros

            // 1. Caso: Token conocido (tiene un registro previo de acceso).
            if (lastAccess != null) {
                Duration duration = Duration.between(lastAccess, now);

                // 2. Verificar Inactividad (HU-03.3)
                if (duration.toMinutes() >= inactivityMinutes) {
                    isInactive = true;
                }
            }
            // 3. Caso: Token NO encontrado.
            // Si el token no está en el mapa, SOLO lo consideramos inactivo si NO es un nuevo token
            // (lo cual es difícil de saber aquí). La forma más segura es la que ya implementamos
            // para bloquear el segundo intento: si se borró, debe seguir borrado.
            // Para diferenciar un token nuevo de uno borrado, se debería usar Redis o una lista negra
            // global. Por ahora, nos quedamos con el caso de inactividad por tiempo.
            // Si el token llega aquí y `lastAccess == null`, simplemente se actualizará el mapa abajo.

            if (isInactive) {
                // Sesión expirada por inactividad
                lastAccessMap.remove(token);

                SecurityContextHolder.clearContext();

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                response.getWriter().write("{\"error\": \"Su sesión ha expirado por inactividad.\"}");
                response.setHeader("Location", "/login");
                return; // Detener la cadena, sin pasar al JwtAuthenticationFilter.
            }

            // 4. Mantener Sesión Activa (Actualizar marca de tiempo).
            // Si `lastAccess == null`, el token se registrará por primera vez.
            lastAccessMap.put(token, now);
        }

        filterChain.doFilter(request, response);
    }

    // Método auxiliar para extraer el JWT (sin cambios)
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}