package com.aviva.appointmentsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para crear datos de consulta (diagnóstico y tratamiento)
 */
public record ConsultationRequest(
    @NotBlank(message = "El diagnóstico no puede estar vacío")
    @Size(min = 10, max = 2000, message = "El diagnóstico debe tener entre 10 y 2000 caracteres")
    String diagnosis,

    @NotBlank(message = "El tratamiento no puede estar vacío")
    @Size(min = 10, max = 2000, message = "El tratamiento debe tener entre 10 y 2000 caracteres")
    String treatment,

    @Size(max = 500, message = "Las notas no deben exceder 500 caracteres")
    String notes
) {
}
