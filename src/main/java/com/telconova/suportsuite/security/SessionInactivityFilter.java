package com.telconova.suportsuite.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filtro de Inactividad de Sesión que utiliza un servicio de revocación en memoria
 * para bloquear tokens inactivos y el problema del "segundo intento".
 */
@Component
public class SessionInactivityFilter extends OncePerRequestFilter {

    // INYECCIÓN DEL SERVICIO DE REVOCACIÓN
    @Autowired
    private TokenRevocationService tokenRevocationService;

    // Mapa para rastrear el último acceso (solo para medir la inactividad)
    private final ConcurrentHashMap<String, LocalDateTime> lastAccessMap = new ConcurrentHashMap<>();

    @Value("${session.inactivity.minutes:15}")
    private long inactivityMinutes;

    // Ruta de autenticación para excluir
    private static final String AUTH_PATH = "/api/v1/auth/login";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. VERIFICACIÓN DE EXCLUSIÓN (Salta el filtro si es la petición de login)
        if (request.getServletPath().equals(AUTH_PATH) || request.getServletPath().equals("/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);

        if (token != null && !token.isEmpty()) {

            // 2. VERIFICACIÓN DE REVOCACIÓN (Bloquea el "segundo intento" o tokens revocados por nuevo login)
            if (tokenRevocationService.isRevoked(token)) {
                // Token ya fue marcado como inactivo o revocado, lo bloqueamos inmediatamente.
                sendExpiredResponse(response);
                return;
            }

            // 3. VERIFICACIÓN DE INACTIVIDAD POR TIEMPO
            LocalDateTime lastAccess = lastAccessMap.get(token);
            LocalDateTime now = LocalDateTime.now();
            boolean isInactive = false;

            if (lastAccess != null) {
                Duration duration = Duration.between(lastAccess, now);

                if (duration.toMinutes() >= inactivityMinutes) {
                    isInactive = true;
                }
            }

            if (isInactive) {
                // Sesión expirada por inactividad

                // Lo removemos del mapa de acceso (para no seguir rastreándolo)
                lastAccessMap.remove(token);

                // CLAVE: Lo agregamos a la lista negra para bloquear cualquier reintento (segundo intento)
                tokenRevocationService.revokeToken(token);

                sendExpiredResponse(response);
                return; // Detener la cadena
            }

            // 4. Mantener Sesión Activa (Actualizar marca de tiempo).
            // Si `lastAccess == null`, el token se registra por primera vez aquí.
            lastAccessMap.put(token, now);
        }

        filterChain.doFilter(request, response);
    }

    // --- Métodos Auxiliares ---

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void sendExpiredResponse(HttpServletResponse response) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"Su sesión ha expirado por inactividad.\"}");
    }
}