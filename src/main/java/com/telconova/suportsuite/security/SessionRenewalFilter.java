package com.telconova.suportsuite.security;

import com.telconova.suportsuite.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    private final JwtTokenProvider tokenProvider;
    private final TokenRevocationService tokenRevocationService; // Requerido para el Bean en SecurityConfig

    // Umbral de Renovación: 5 minutos (300,000 ms).
    private static final long RENEWAL_THRESHOLD_MS = 300000L;


    public SessionRenewalFilter(JwtTokenProvider tokenProvider, TokenRevocationService tokenRevocationService) {
        this.tokenProvider = tokenProvider;
        this.tokenRevocationService = tokenRevocationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Solo procesar si el usuario ya fue autenticado por JwtAuthenticationFilter
        if (SecurityContextHolder.getContext().getAuthentication() != null) {

            // La línea que fallaba antes ahora usa la variable 'tokenProvider' que NO es null.
            String jwt = tokenProvider.getJwtFromRequest(request);

            if (jwt != null) {

                long remainingTimeMs = tokenProvider.getRemainingTimeInMs(jwt);

                // Verificar si el token es válido y está dentro del umbral de renovación
                if (remainingTimeMs > 0 && remainingTimeMs < RENEWAL_THRESHOLD_MS) {

                    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

                    // ⚠NOTA: El principal en Spring Security suele ser un UserDetails, no tu entidad User.
                    // Si estás seguro de que tu UserDetailsService devuelve la entidad User directamente, está bien.
                    // Si devuelve un UserDetails de Spring, tendrías que obtener el username y cargarlo de nuevo.
                    if (principal instanceof User) {

                        User user = (User) principal;

                        // Generar el nuevo token
                        String newJwt = tokenProvider.generateToken(user);

                        // Devolver el nuevo token en un encabezado CUSTOM (X-New-Token).
                        response.setHeader("X-New-Token", newJwt);
                        logger.info("Token JWT renovado para el usuario: " + user.getUsername());
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}