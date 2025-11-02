package com.telconova.suportsuite.config;

import com.telconova.suportsuite.security.JwtAuthenticationFilter;
import com.telconova.suportsuite.security.SessionRenewalFilter;
import com.telconova.suportsuite.security.JwtTokenProvider; // Nueva Importación
import com.telconova.suportsuite.security.TokenRevocationService; // Nueva Importación
import com.telconova.suportsuite.repository.UserRepository; // Nueva Importación
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService; // Nueva Importación
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    // --- BEANS BÁSICOS ---

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // --- BEANS DE FILTROS (SOLUCIÓN AL NULLPOINTER) ---

    // 1. DEFINIR JwtAuthenticationFilter COMO BEAN
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtTokenProvider tokenProvider,
            UserDetailsService userDetailsService,
            UserRepository userRepository,
            TokenRevocationService tokenRevocationService) {
        // Asumiendo que ahora JwtAuthenticationFilter tiene un constructor con estos 3 argumentos
        return new JwtAuthenticationFilter(tokenProvider, userDetailsService, userRepository, tokenRevocationService);
    }

    // 2. DEFINIR SessionRenewalFilter COMO BEAN
    @Bean
    public SessionRenewalFilter sessionRenewalFilter(
            JwtTokenProvider tokenProvider,
            TokenRevocationService tokenRevocationService) {
        // Se asume que SessionRenewalFilter necesita estas dependencias
        return new SessionRenewalFilter(tokenProvider, tokenRevocationService);
    }


    // --- CADENA DE FILTROS ---

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            // Inyectamos los BEANS definidos arriba
            JwtAuthenticationFilter jwtAuthenticationFilter,
            SessionRenewalFilter sessionRenewalFilter) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configuración de autorización
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register").permitAll()
                        .requestMatchers("/api/health/**").permitAll()
                        .anyRequest().authenticated()
                )

                // 🔗 AÑADIR FILTROS (Usando las instancias Bean inyectadas)
                // Orden de ejecución: SessionRenewalFilter se ejecuta PRIMERO
                // Luego JwtAuthenticationFilter

                .addFilterAfter(sessionRenewalFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, SessionRenewalFilter.class);
        // NOTA: El orden .addFilterBefore(A, B) asegura que A va ANTES de B.
        // Si quieres que JWT vaya ANTES de SessionRenewal, invierte el orden de addFilterBefore:
        // .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        // .addFilterBefore(sessionRenewalFilter, jwtAuthenticationFilter.class);

        return http.build();
    }


    // --- CONFIGURACIÓN CORS ---

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // ... (El código CORS se mantiene exactamente igual)
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "https://telco-nova-p7-f4-front.vercel.app",
                "http://localhost:*",
                "https://localhost:*"
        ));
        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-User-Name",
                "Accept",
                "Origin"
        ));
        configuration.setMaxAge(21600L);
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}