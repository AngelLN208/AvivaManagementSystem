package com.aviva.appointmentsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * DTO para vincular seguro a paciente
 */
public record PatientInsuranceRequest(
    @NotNull(message = "El ID del seguro es requerido")
    Long insuranceId,

    @NotBlank(message = "El número de póliza es requerido")
    @Size(min = 3, max = 50, message = "El número de póliza debe tener entre 3 y 50 caracteres")
    String policyNumber,

    @Size(max = 100, message = "El nombre del titular no puede exceder 100 caracteres")
    String policyHolderName,

    @Size(max = 50, message = "La relación no puede exceder 50 caracteres")
    String relationshipToHolder,

    Boolean isPrimary,

    @NotNull(message = "La fecha de inicio es requerida")
    LocalDateTime effectiveDate,

    @NotNull(message = "La fecha de fin es requerida")
    LocalDateTime expirationDate
) {}
