package com.telconova.suportsuite.security;

import com.telconova.suportsuite.entity.User;
import com.telconova.suportsuite.exception.LockedAccountException;
import com.telconova.suportsuite.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
import java.util.Collection;
import java.util.Arrays;
import java.util.stream.Collectors;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Log logger = LogFactory.getLog(JwtAuthenticationFilter.class);

    private final TokenRevocationService tokenRevocationService;
    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService; // Lo mantendremos
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
            String jwt = tokenProvider.getJwtFromRequest(request);

            if (jwt != null) {
                if (tokenProvider.validateToken(jwt)) {

                    // ... (Lógica de token revocado y username/userEntity/isLocked permanece IGUAL) ...

                    String username = tokenProvider.getUsernameFromJWT(jwt);

                    // 1. OBTENER EL ESTADO ACTUAL DEL USUARIO para verificación de bloqueo (MANTENER)
                    User userEntity = userRepository.findByUsername(username)
                            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

                    if (userEntity.isLocked()) {
                        throw new LockedAccountException("Acceso denegado. Su cuenta está bloqueada.");
                    }

                    // 2. Cargar UserDetails para cumplir con la inyección (MANTENER)
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);


                    // 🟢 MODIFICACIÓN CRÍTICA: EXTRACCIÓN ROBUSTA DE ROLES

                    // Obtener el objeto de la claim "roles". Puede ser String o List<String>.
                    Object rolesObject = tokenProvider.getClaims(jwt).get("roles");
                    Collection<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();

                    if (rolesObject instanceof String) {
                        // Caso 1: El rol viene como una cadena (ej: "ADMIN")
                        String rolesString = (String) rolesObject;
                        // Aseguramos que si hay comas (ej: "ADMIN,SUPERVISOR") también funcione.
                        authorities = Arrays.stream(rolesString.split(","))
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.trim()))
                                .collect(Collectors.toList());
                    } else if (rolesObject instanceof java.util.List) {
                        // Caso 2: El rol viene como una lista (ej: ["ADMIN", "SUPERVISOR"])
                        authorities = (Collection<SimpleGrantedAuthority>) ((java.util.List<?>) rolesObject).stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + ((String) role).trim()))
                                .collect(Collectors.toList());
                    }


                    // 3. Crear el Token de Autenticación con las authorities CORREGIDAS
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            authorities // Usamos las authorities ya corregidas con ROLE_
                    );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        // ... (Manejo de excepciones)
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
            // Este catch general es peligroso, pero nos permite pasar al siguiente filtro
            logger.error("Error general al establecer la autenticación JWT para la petición.", ex);
        }

        filterChain.doFilter(request, response);
    }
}
