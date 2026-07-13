package com.aviva.appointmentsystem.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuración de Spring Security.
 * 
 * Reglas:
 * 1. Rutas de Swagger (/v3/api-docs/**, /swagger-ui/**, /swagger-ui.html) →
 * PÚBLICAS
 * 2. Rutas de Auth (/api/auth/**) → PÚBLICAS (login y registro)
 * 3. Todas las demás rutas /api/** → REQUIEREN AUTENTICACIÓN JWT
 * 4. CSRF deshabilitado (API REST stateless)
 * 5. Sesiones STATELESS (no se guardan sesiones del servidor)
 * 6. Filtro JWT se ejecuta antes del filtro de autenticación de Spring
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Habilita @PreAuthorize en controladores
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Deshabilitar CSRF (API REST stateless no necesita CSRF)
                .csrf(csrf -> csrf.disable())

                // 2. Configurar CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 3. Sesiones STATELESS (JWT, sin sesiones del servidor)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. Reglas de autorización
                .authorizeHttpRequests(auth -> auth
                        // ── Rutas PÚBLICAS de Swagger/OpenAPI ──
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**")
                        .permitAll()

                        // ── Rutas PÚBLICAS de Autenticación ──
                        .requestMatchers("/api/auth/**").permitAll()

                        // ── Todas las demás rutas requieren autenticación ──
                        .anyRequest().authenticated())

                // 5. Agregar filtro JWT antes del filtro de autenticación de Spring
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:8080",
                "http://localhost:5050",
                "http://127.0.0.1:5500",
                "http://localhost:5173",
                "https://zany-space-broccoli-v6jgg4jvxppghrrq-5173.app.github.dev"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}