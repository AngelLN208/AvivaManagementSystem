package com.aviva.appointmentsystem.dto;

import java.math.BigDecimal;

public record InsuranceCoverageResult(
    BigDecimal baseAmount,
    BigDecimal deductibleApplied,
    BigDecimal insuranceCoveredAmount,
    BigDecimal patientAmount,
    BigDecimal remainingAnnualCoverage
) {}
