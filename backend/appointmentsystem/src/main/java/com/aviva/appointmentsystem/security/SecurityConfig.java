package com.aviva.appointmentsystem.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
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
 * 1. Rutas de Swagger (/v3/api-docs/**, /swagger-ui/**, /swagger-ui.html) → PÚBLICAS
 * 2. Rutas de Auth (/api/auth/**) → PÚBLICAS (login y registro)
 * 3. PATIENT accede a catálogos y a variantes /me con ownership validado
 * 4. El resto de /api/** requiere un rol de staff
 * 5. CSRF deshabilitado (API REST stateless)
 * 6. Sesiones STATELESS (no se guardan sesiones del servidor)
 * 7. Filtro JWT se ejecuta antes del filtro de autenticación de Spring
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Habilita @PreAuthorize en controladores
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final List<String> allowedOrigins;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            @Value("${app.cors.allowed-origins}") String allowedOrigins
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;

        // Render entrega una lista separada por comas para permitir localhost y Vercel.
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList();
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
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Respuestas JSON coherentes para errores producidos antes del Controller.
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, exception) -> {
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(
                        "{\"success\":false,\"code\":\"UNAUTHORIZED\"," +
                        "\"message\":\"Se requiere autenticación\",\"status\":401}"
                    );
                })
                .accessDeniedHandler((request, response, exception) -> {
                    response.setStatus(403);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(
                        "{\"success\":false,\"code\":\"ACCESS_DENIED\"," +
                        "\"message\":\"No tiene permisos para realizar esta operación\"," +
                        "\"status\":403}"
                    );
                })
            )

            // 4. Reglas de autorización
            .authorizeHttpRequests(auth -> auth
                // ── Rutas PÚBLICAS de Swagger/OpenAPI ──
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()

                // ── Rutas PÚBLICAS de Autenticación ──
                .requestMatchers("/api/auth/**").permitAll()

                // Catálogos de solo lectura que necesita el portal para agendar.
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/specialties",
                    "/api/specialties/**",
                    "/api/doctors",
                    "/api/doctors/**",
                    "/api/insurances"
                ).hasAnyRole("PATIENT", "ADMIN", "RECEPTIONIST", "DOCTOR")

                // Disponibilidad: necesaria tanto para staff como para pacientes.
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/appointments/doctor/*/available-slots"
                ).authenticated()

                // Portal: solo el paciente autenticado; ownership se valida en Service.
                .requestMatchers(
                    "/api/appointments/me",
                    "/api/appointments/me/**",
                    "/api/patient-insurances/me",
                    "/api/patient-insurances/me/**",
                    "/api/payments/me",
                    "/api/payments/me/**",
                    "/api/receipts/me",
                    "/api/receipts/me/**",
                    "/api/notifications/me",
                    "/api/notifications/me/**"
                ).hasRole("PATIENT")

                // Los pagos y comprobantes globales son operaciones de caja.
                // El médico no debe procesarlos ni consultar datos financieros
                // de pacientes fuera de las variantes /me.
                .requestMatchers(
                    "/api/payments",
                    "/api/payments/**",
                    "/api/receipts",
                    "/api/receipts/**"
                ).hasAnyRole("ADMIN", "RECEPTIONIST")

                // Lecturas generales conservadas para el personal autorizado.
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/appointments",
                    "/api/appointments/**"
                ).hasAnyRole("ADMIN", "RECEPTIONIST", "DOCTOR")

                // Cualquier otra operación general de citas es administrativa.
                .requestMatchers(
                    "/api/appointments",
                    "/api/appointments/**"
                ).hasAnyRole("ADMIN", "RECEPTIONIST")

                // Deny-by-default para PATIENT: los módulos clínicos y
                // administrativos requieren un rol de staff hasta que exista
                // una variante /me con validación de ownership.
                .requestMatchers("/api/**")
                .hasAnyRole("ADMIN", "RECEPTIONIST", "DOCTOR")

                // ── Todas las demás rutas requieren autenticación ──
                .anyRequest().authenticated()
            )

            // 5. Agregar filtro JWT antes del filtro de autenticación de Spring
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
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
