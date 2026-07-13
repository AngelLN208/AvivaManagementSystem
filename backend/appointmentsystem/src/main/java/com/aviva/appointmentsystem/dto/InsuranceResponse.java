package com.aviva.appointmentsystem.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para respuestas de seguros
 */
public record InsuranceResponse(
    Long id,
    String name,
    String description,
    BigDecimal coveragePercentage,
    BigDecimal deductible,
    BigDecimal maxCoveragePerConsultation,
    BigDecimal maxAnnualCoverage,
    Boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
