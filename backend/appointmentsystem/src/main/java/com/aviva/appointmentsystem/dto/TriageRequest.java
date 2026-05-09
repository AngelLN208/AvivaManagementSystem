package com.aviva.appointmentsystem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO para crear datos de triaje (vitales)
 */
public record TriageRequest(
    @NotNull(message = "La presión sistólica no puede estar vacía")
    @Positive(message = "La presión sistólica debe ser positiva")
    Integer bloodPressureSystolic,

    @NotNull(message = "La presión diastólica no puede estar vacía")
    @Positive(message = "La presión diastólica debe ser positiva")
    Integer bloodPressureDiastolic,

    @NotNull(message = "La temperatura no puede estar vacía")
    @Positive(message = "La temperatura debe ser positiva")
    Double temperature,

    @NotNull(message = "La frecuencia cardíaca no puede estar vacía")
    @Positive(message = "La frecuencia cardíaca debe ser positiva")
    Integer heartRate,

    @NotNull(message = "La frecuencia respiratoria no puede estar vacía")
    @Positive(message = "La frecuencia respiratoria debe ser positiva")
    Integer respiratoryRate,

    @NotNull(message = "El peso no puede estar vacío")
    @Positive(message = "El peso debe ser positivo")
    Double weight,

    @NotNull(message = "La altura no puede estar vacía")
    @Positive(message = "La altura debe ser positiva")
    Double height,

    String notes
) {
}
