package com.aviva.appointmentsystem.service;

import java.math.BigDecimal;

import com.aviva.appointmentsystem.dto.InsuranceCoverageResult;
import com.aviva.appointmentsystem.entity.PatientInsurance;

public interface InsuranceCoverageCalculator {

    InsuranceCoverageResult calculate(
        BigDecimal baseAmount,
        PatientInsurance patientInsurance
    );
}