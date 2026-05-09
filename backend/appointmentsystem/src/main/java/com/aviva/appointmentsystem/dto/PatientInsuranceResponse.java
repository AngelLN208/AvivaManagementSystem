package com.aviva.appointmentsystem.dto;

import java.time.LocalDateTime;

/**
 * DTO para respuestas de seguros de paciente
 */
public record PatientInsuranceResponse(
    Long id,
    Long patientId,
    Long insuranceId,
    String insuranceName,
    String policyNumber,
    String policyHolderName,
    String relationshipToHolder,
    Boolean isPrimary,
    LocalDateTime effectiveDate,
    LocalDateTime expirationDate,
    Boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
