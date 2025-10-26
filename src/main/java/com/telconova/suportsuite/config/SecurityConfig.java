package com.telconova.suportsuite.config;

import com.telconova.suportsuite.security.JwtAuthenticationFilter;
import com.telconova.suportsuite.security.SessionRenewalFilter; // Asumiendo que SessionInactivityFilter se llama ahora SessionRenewalFilter
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // ⭐️ 1. Inyectar el filtro principal de autenticación JWT
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // ⭐️ 2. Usar SessionRenewalFilter (asumiendo que es tu SessionInactivityFilter)
    @Autowired
    private SessionRenewalFilter sessionRenewalFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Uso de BCrypt para el hashing de contraseñas
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Deshabilitar CSRF para APIs REST
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Usar JWT (sin estado)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/login").permitAll() // Login
                        .requestMatchers("/api/v1/auth/register").permitAll() // Registro
                        .requestMatchers("/api/health/**").permitAll() // Salud
                        // ⭐️ Las demás rutas requerirán autenticación JWT
                        .anyRequest().authenticated()
                );

        // ------------------ REGISTRO DE FILTROS JWT ------------------

        // 1. JWT Authentication Filter: Valida el token y autentica al usuario.
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // 2. Session Renewal Filter: Extiende la vida del token si el usuario está activo (HU-003.3).
        // Se ejecuta *después* de que JwtAuthenticationFilter confirme que hay un token válido.
        http.addFilterAfter(sessionRenewalFilter, JwtAuthenticationFilter.class);

        return http.build();
    }
}
