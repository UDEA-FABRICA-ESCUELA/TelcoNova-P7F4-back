package com.telconova.suportsuite.security;

import com.telconova.suportsuite.entity.User;
import com.telconova.suportsuite.exception.LockedAccountException; // 👈 NECESARIO
import com.telconova.suportsuite.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;

/**
 * Filtro que valida el JWT y establece la autenticación.
 * Implementa la verificación de estado de bloqueo y maneja la expiración del token.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Log logger = LogFactory.getLog(JwtAuthenticationFilter.class);

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository; // Renombrado a minúsculas por convención

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = tokenProvider.getJwtFromRequest(request);

            if (jwt != null) {
                if (tokenProvider.validateToken(jwt)) {
                    String username = tokenProvider.getUsernameFromJWT(jwt);

                    // 1. OBTENER EL ESTADO ACTUAL DEL USUARIO (HU-003.2)
                    User userEntity = userRepository.findByUsername(username)
                            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

                    // 2. VERIFICAR SI LA CUENTA ESTÁ BLOQUEADA
                    if (userEntity.isLocked()) {
                        // Si está bloqueada, lanzamos nuestra excepción customizada para que el catch la maneje.
                        throw new LockedAccountException("Acceso denegado. Su cuenta está bloqueada.");
                    }

                    // 3. Si no está bloqueado, procede con la autenticación normal
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        // 🚨 4. MANEJO DE BLOQUEO DE CUENTA
        catch (LockedAccountException ex) {
            logger.warn("Intento de acceso con cuenta bloqueada", ex);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Acceso denegado. Su cuenta está bloqueada.\"}");
            return; // Detiene el flujo
        }
        // 5. MANEJO DE EXPIRACIÓN (HU-003.3)
        catch (ExpiredJwtException ex) {
            logger.warn("Token JWT expirado", ex);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Su sesión ha expirado por inactividad\"}");
            return;
        }
        // 6. MANEJO DE OTROS ERRORES DE TOKEN INVÁLIDO
        catch (MalformedJwtException | IllegalArgumentException ex) {
            logger.error("Token JWT inválido", ex);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Token JWT inválido\"}");
            return;
        }

        catch (Exception ex) {
            logger.error("Error al establecer la autenticación JWT para la petición.", ex);
        }

        filterChain.doFilter(request, response);
    }
}
