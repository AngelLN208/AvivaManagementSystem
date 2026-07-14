package com.aviva.appointmentsystem.service;

/**
 * Contenido completo de un correo transaccional.
 *
 * textContent es el respaldo accesible y htmlContent contiene exclusivamente
 * presentación; ninguna regla de negocio depende del HTML.
 */
public record EmailContent(
        String subject,
        String textContent,
        String htmlContent
) {}
