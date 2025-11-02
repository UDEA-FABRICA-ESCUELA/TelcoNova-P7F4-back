package com.telconova.suportsuite.security;

import com.telconova.suportsuite.entity.User;
import com.telconova.suportsuite.exception.LockedAccountException;
import com.telconova.suportsuite.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component; // Mantener para SpringBoot (aunque no será el punto de inyección en SecurityConfig)
import org.springframework.web.filter.OncePerRequestFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Log logger = LogFactory.getLog(JwtAuthenticationFilter.class);

    private final TokenRevocationService tokenRevocationService;
    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    // Constructor para Inyección de Dependencias
    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserDetailsService userDetailsService, UserRepository userRepository, TokenRevocationService tokenRevocationService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.tokenRevocationService = tokenRevocationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            // Línea que fallaba: ahora 'tokenProvider' ya no es null
            String jwt = tokenProvider.getJwtFromRequest(request);

            if (jwt != null) {
                if (tokenProvider.validateToken(jwt)) {


                    if (tokenRevocationService.isRevoked(jwt)) {
                        logger.warn("Intento de acceso con token revocado: " + jwt.substring(0, 10) + "...");
                        // Usamos SC_FORBIDDEN (403) o SC_UNAUTHORIZED (401).
                        // 403 suele ser más claro para token válido pero no autorizado.
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\": \"Acceso denegado. Su token ha sido revocado o ha expirado.\"}");
                        return; // DETENER LA CADENA DE FILTROS AQUÍ
                    }
                    String username = tokenProvider.getUsernameFromJWT(jwt);
                    //aca string username
                    // 1. OBTENER EL ESTADO ACTUAL DEL USUARIO
                    User userEntity = userRepository.findByUsername(username)
                            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

                    // 2. VERIFICAR SI LA CUENTA ESTÁ BLOQUEADA
                    if (userEntity.isLocked()) {
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
        // ... (El manejo de excepciones sigue igual)
        catch (LockedAccountException ex) {
            logger.warn("Intento de acceso con cuenta bloqueada", ex);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Acceso denegado. Su cuenta está bloqueada.\"}");
            return;
        }
        catch (ExpiredJwtException ex) {
            logger.warn("Token JWT expirado", ex);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Su sesión ha expirado por inactividad\"}");
            return;
        }
        catch (MalformedJwtException | IllegalArgumentException ex) {
            logger.error("Token JWT inválido", ex);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Token JWT inválido\"}");
            return;
        }

        catch (Exception ex) {
            logger.error("Error general al establecer la autenticación JWT para la petición.", ex);
        }

        filterChain.doFilter(request, response);
    }
}