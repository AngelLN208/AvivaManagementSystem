package com.aviva.appointmentsystem.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI / Swagger para documentación interactiva.
 *
 * - Accede a la documentación en: http://localhost:8080/swagger-ui.html
 * - Accede al JSON en: http://localhost:8080/v3/api-docs
 *
 * @SecurityScheme configura el botón "Authorize" en Swagger para inyectar el Bearer Token JWT.
 */
@Configuration
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Ingrese su token JWT. Ejemplo: eyJhbGciOiJIUzI1NiIs..."
)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Clínica Áviva - API de Gestión de Citas Médicas")
                .version("2.0.0")
                .description("""
                    Sistema de gestión de citas médicas de la Clínica Áviva.
                    
                    ## Módulos disponibles:
                    - **Autenticación**: Login y registro de pacientes
                    - **Pacientes**: CRUD y búsquedas
                    - **Especialidades**: Gestión de especialidades médicas
                    - **Médicos**: Registro y horarios
                    - **Citas**: Programación, reprogramación y cancelación
                    - **Atención**: Triaje y consultas médicas
                    - **Pagos y Facturación**: Procesamiento de pagos y comprobantes
                    - **Auditoría**: Historial de cambios
                    
                    ## Autenticación:
                    Todos los endpoints (excepto login y registro) requieren un token JWT.
                    Use el botón **Authorize** para inyectar su token.
                    """)
                .contact(new Contact()
                    .name("Equipo Áviva")
                    .email("soporte@clinicaaviva.com"))
                .license(new License()
                    .name("Uso Académico")
                    .url("https://github.com/AngelLN208/AvivaManagementSystem"))
            );
    }
}
