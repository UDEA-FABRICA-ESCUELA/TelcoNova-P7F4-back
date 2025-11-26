package com.telconova.suportsuite.config;

import com.telconova.suportsuite.security.JwtAuthenticationFilter;
import com.telconova.suportsuite.security.SessionRenewalFilter;
import com.telconova.suportsuite.security.JwtTokenProvider;
import com.telconova.suportsuite.security.TokenRevocationService;
import com.telconova.suportsuite.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
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

    // --- BEANS DE FILTROS ---

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtTokenProvider tokenProvider,
            UserDetailsService userDetailsService,
            UserRepository userRepository,
            TokenRevocationService tokenRevocationService) {
        return new JwtAuthenticationFilter(tokenProvider, userDetailsService, userRepository, tokenRevocationService);
    }

    @Bean
    public SessionRenewalFilter sessionRenewalFilter(
            JwtTokenProvider tokenProvider,
            TokenRevocationService tokenRevocationService) {
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

                // Configuración de autorización (MANTENER ES CORRECTA)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register").permitAll()
                        .requestMatchers("/api/health/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/prometheus", "/error").permitAll()
                        .requestMatchers("/api/v1/alert-rules/**", "/api/v1/templates/**").hasAnyRole("ADMIN", "SUPERVISOR")
                        .requestMatchers("/api/v1/notifications/**").hasAnyRole("ADMIN", "SUPERVISOR")
                        .anyRequest().authenticated()
                )

                // 🟢 MODIFICACIÓN CLAVE DE ORDEN DE FILTROS

                // 1. Añadimos el JwtAuthenticationFilter DESPUÉS del filtro de persistencia de contexto
                //    (SecurityContextHolderFilter o su predecesor), lo cual se hace colocándolo
                //    ANTES del filtro de Autenticación de Spring.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 2. Reinsertamos el SessionRenewalFilter después del filtro JWT.
                //    Si el filtro JWT funciona, este debería funcionar.
                .addFilterAfter(sessionRenewalFilter, JwtAuthenticationFilter.class);


        return http.build();
    }


    // --- CONFIGURACIÓN CORS (SIN CAMBIOS - ES CORRECTA) ---

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
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