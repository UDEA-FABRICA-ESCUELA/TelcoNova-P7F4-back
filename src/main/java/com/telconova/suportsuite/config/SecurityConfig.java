package com.telconova.suportsuite.config;

import com.telconova.suportsuite.security.JwtAuthenticationFilter;
import com.telconova.suportsuite.security.SessionRenewalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CONFIGURACIÓN DEFINITIVA DE SEGURIDAD - BASADA EN ENDPOINTS REALES DEL PROYECTO
 * Actualizado: 2025-11-01
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    // NUEVO BEAN AÑADIDO: Define el cifrador de contraseñas.
    // Esto resuelve el error 'required a bean of type PasswordEncoder that could not be found'.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 🚫 Deshabilitar CSRF para APIs REST
                .csrf(AbstractHttpConfigurer::disable)

                // 🌐 Configuración CORS para frontend en Vercel
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 🏗️ Configuración de sesiones
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 🔒 Configuración de autorización - RUTAS REALES CONFIRMADAS
                .authorizeHttpRequests(auth -> auth
                        // 🔐 RUTAS DE AUTENTICACIÓN (sin autenticación)
                        .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register").permitAll()

                        // ❤️‍🩹 RUTAS DE SALUD (para monitoreo)
                        .requestMatchers("/api/health/**").permitAll()

                        // 📧 HU-004 (Plantillas de mensajes) - UNIFICADAS A PERMITALL
                        .requestMatchers("/api/v1/templates/**").permitAll()

                        // 🚨 HU-005 (Reglas de alertas) - UNIFICADAS A PERMITALL
                        .requestMatchers("/api/v1/alert-rules/**").permitAll()

                        // 📬 HU-004 (Notificaciones) - UNIFICADAS A PERMITALL
                        // Incluye /api/v1/notifications y /api/v1/notifications/stats
                        .requestMatchers("/api/v1/notifications/**").permitAll()

                        // ⭐️ Otras rutas requieren autenticación
                        .anyRequest().authenticated()
                )

                // 🔗 Añadir filtros de autenticación
                .addFilterBefore(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new SessionRenewalFilter(), JwtAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configuración CORS para permitir peticiones del frontend en Vercel
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 🌍 Orígenes permitidos
        configuration.setAllowedOriginPatterns(List.of(
                "https://telco-nova-p7-f4-front.vercel.app",
                "http://localhost:*",
                "https://localhost:*"
        ));

        // 📋 Métodos HTTP permitidos
        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // 📋 Headers permitidos
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-User-Name",
                "Accept",
                "Origin"
        ));

        // ⏱️ Tiempo de preflight (6 horas)
        configuration.setMaxAge(21600L);

        // 🔓 Permitir credenciales
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}


