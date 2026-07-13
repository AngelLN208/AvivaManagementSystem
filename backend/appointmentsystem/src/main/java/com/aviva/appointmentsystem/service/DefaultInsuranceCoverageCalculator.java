package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.InsuranceCoverageResult;
import com.aviva.appointmentsystem.entity.Insurance;
import com.aviva.appointmentsystem.entity.PatientInsurance;
import com.aviva.appointmentsystem.exception.ValidationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class DefaultInsuranceCoverageCalculator implements InsuranceCoverageCalculator {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    /**
     * Calcula la distribución del costo de una consulta entre la aseguradora
     * y el paciente, respetando el deducible y los límites del plan.
     *
     * Este método no actualiza el consumo anual. Esa operación se realiza
     * únicamente cuando el pago queda confirmado.
     *
     * @param baseAmount costo original de la consulta
     * @param patientInsurance afiliación utilizada por el paciente
     * @return desglose de la cobertura y del monto que pagará el paciente
     * @throws ValidationException si el seguro está inactivo o fuera de vigencia
     */
    @Override
    public InsuranceCoverageResult calculate(BigDecimal baseAmount, PatientInsurance patientInsurance) {

        validateBaseAmount(baseAmount);
        // Primero se comprueba que la afiliación pueda utilizarse.
        validatePatientInsurance(patientInsurance);
        
        Insurance insurance = patientInsurance.getInsurance();

        BigDecimal base = money(baseAmount);
        BigDecimal deductible = nonNegative(insurance.getDeductible());
        BigDecimal maxPerConsultation = nonNegative(insurance.getMaxCoveragePerConsultation());
        BigDecimal maxAnnual = nonNegative(insurance.getMaxAnnualCoverage());
        BigDecimal usedAnnual = nonNegative(patientInsurance.getUsedAnnualCoverage());
        // Se calcula el saldo que todavía puede cubrir el seguro durante el año.
        BigDecimal remainingAnnual = maxAnnual.subtract(usedAnnual).max(BigDecimal.ZERO);

        BigDecimal deductibleApplied = deductible.min(base);

        BigDecimal applicableAmount = base.subtract(deductibleApplied).max(BigDecimal.ZERO);

        BigDecimal coverageRate = insurance.getCoveragePercentage()
                .divide(ONE_HUNDRED, 6, RoundingMode.HALF_UP);
        // La cobertura final nunca puede superar los límites del plan.
        BigDecimal calculatedCoverage = applicableAmount
                .multiply(coverageRate)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal finalCoverage = calculatedCoverage
                .min(maxPerConsultation)
                .min(remainingAnnual)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal patientAmount = base
                .subtract(finalCoverage)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal remainingAfterCoverage = remainingAnnual
                .subtract(finalCoverage)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        return new InsuranceCoverageResult(
                base,
                deductibleApplied,
                finalCoverage,
                patientAmount,
                remainingAfterCoverage
        );
    }

    private void validateBaseAmount(BigDecimal baseAmount) {
        if (baseAmount == null || baseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(
                    "El monto base debe ser mayor que cero");
        }
    }

    private void validatePatientInsurance(
            PatientInsurance patientInsurance) {

        if (patientInsurance == null) {
            throw new ValidationException(
                    "La afiliación del paciente es requerida");
        }

        if (!Boolean.TRUE.equals(patientInsurance.getActive())) {
            throw new ValidationException(
                    "La afiliación del paciente no está activa");
        }

        Insurance insurance = patientInsurance.getInsurance();

        if (insurance == null || !Boolean.TRUE.equals(insurance.getActive())) {
            throw new ValidationException(
                    "El seguro no está activo");
        }

        LocalDateTime now = LocalDateTime.now();

        if (patientInsurance.getEffectiveDate() == null ||
                patientInsurance.getExpirationDate() == null ||
                now.isBefore(patientInsurance.getEffectiveDate()) ||
                now.isAfter(patientInsurance.getExpirationDate())) {

            throw new ValidationException(
                    "La póliza no se encuentra vigente");
        }

        BigDecimal percentage = insurance.getCoveragePercentage();

        if (percentage == null ||
                percentage.compareTo(BigDecimal.ZERO) < 0 ||
                percentage.compareTo(ONE_HUNDRED) > 0) {

            throw new ValidationException(
                    "El porcentaje debe encontrarse entre 0 y 100");
        }
    }

    private BigDecimal nonNegative(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.max(BigDecimal.ZERO);
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
