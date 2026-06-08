package com.aviva.appointmentsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para crear o actualizar una especialidad médica.
 * 
 * Usado en:
 * - POST /api/specialties (crear)
 * - PUT /api/specialties/{id} (actualizar)
 * 
 * Validaciones:
 * - name: obligatorio, entre 3 y 100 caracteres
 * - description: opcional, máximo 500 caracteres
 */
public record SpecialtyRequest(

    @NotBlank(message = "El nombre de la especialidad no puede estar vacío")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    String name,

    @Size(max = 500, message = "La descripción no debe exceder 500 caracteres")
    String description

) {
}
