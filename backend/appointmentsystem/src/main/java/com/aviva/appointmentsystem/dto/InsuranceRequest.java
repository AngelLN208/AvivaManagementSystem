package com.aviva.appointmentsystem.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * DTO para crear/actualizar seguros
 */
public record InsuranceRequest(
    @NotBlank(message = "El nombre del seguro es requerido")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    String name,

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    String description,

    @NotNull(message = "El porcentaje de cobertura es requerido")
    @DecimalMin(value = "0.0", message = "El porcentaje debe ser mayor o igual a 0")
    BigDecimal coveragePercentage,

    @NotNull(message = "El deducible es requerido")
    @DecimalMin(value = "0.0", message = "El deducible debe ser mayor o igual a 0")
    BigDecimal deductible,

    @NotNull(message = "El máximo por consulta es requerido")
    @DecimalMin(value = "0.01", message = "El máximo por consulta debe ser mayor a 0")
    BigDecimal maxCoveragePerConsultation,

    @NotNull(message = "El máximo anual es requerido")
    @DecimalMin(value = "0.01", message = "El máximo anual debe ser mayor a 0")
    BigDecimal maxAnnualCoverage
) {}
