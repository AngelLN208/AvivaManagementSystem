package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.InsuranceCoverageResult;
import com.aviva.appointmentsystem.entity.Insurance;
import com.aviva.appointmentsystem.entity.PatientInsurance;
import com.aviva.appointmentsystem.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas unitarias del calculo de cobertura.
 *
 * Verifica que el deducible, porcentaje y limites del seguro
 * se apliquen correctamente sin utilizar la base de datos.
 */
class DefaultInsuranceCoverageCalculatorTest {

    private final InsuranceCoverageCalculator calculator =
            new DefaultInsuranceCoverageCalculator();

    /**
     * Caso normal: consulta de S/100, deducible S/20 y cobertura del 80%.
     */
    @Test
    void shouldCalculateNormalCoverage() {
        PatientInsurance policy = createPolicy(
                "80.00", "20.00", "500.00", "1000.00", "0.00");

        InsuranceCoverageResult result =
                calculator.calculate(new BigDecimal("100.00"), policy);

        assertEquals(new BigDecimal("20.00"), result.deductibleApplied());
        assertEquals(new BigDecimal("64.00"), result.insuranceCoveredAmount());
        assertEquals(new BigDecimal("36.00"), result.patientAmount());
        assertEquals(new BigDecimal("936.00"),
                result.remainingAnnualCoverage());
    }

    /**
     * La cobertura no puede superar el maximo permitido por consulta.
     */
    @Test
    void shouldRespectMaximumCoveragePerConsultation() {
        PatientInsurance policy = createPolicy(
                "100.00", "0.00", "30.00", "1000.00", "0.00");

        InsuranceCoverageResult result =
                calculator.calculate(new BigDecimal("100.00"), policy);

        assertEquals(new BigDecimal("30.00"), result.insuranceCoveredAmount());
        assertEquals(new BigDecimal("70.00"), result.patientAmount());
    }

    /**
     * La cobertura no puede superar el saldo anual del paciente.
     */
    @Test
    void shouldRespectRemainingAnnualCoverage() {
        PatientInsurance policy = createPolicy(
                "100.00", "0.00", "500.00", "100.00", "90.00");

        InsuranceCoverageResult result =
                calculator.calculate(new BigDecimal("100.00"), policy);

        assertEquals(new BigDecimal("10.00"), result.insuranceCoveredAmount());
        assertEquals(new BigDecimal("90.00"), result.patientAmount());
        assertEquals(new BigDecimal("0.00"),
                result.remainingAnnualCoverage());
    }

    /**
     * Una afiliacion inactiva no debe recibir cobertura.
     */
    @Test
    void shouldRejectInactivePatientInsurance() {
        PatientInsurance policy = createPolicy(
                "80.00", "20.00", "500.00", "1000.00", "0.00");
        policy.setActive(false);

        assertThrows(
                ValidationException.class,
                () -> calculator.calculate(new BigDecimal("100.00"), policy)
        );
    }

    /**
     * Construye una afiliacion vigente para reutilizarla en las pruebas.
     */
    private PatientInsurance createPolicy(
            String percentage,
            String deductible,
            String maxPerConsultation,
            String maxAnnual,
            String usedAnnual) {

        Insurance insurance = new Insurance();
        insurance.setCoveragePercentage(new BigDecimal(percentage));
        insurance.setDeductible(new BigDecimal(deductible));
        insurance.setMaxCoveragePerConsultation(
                new BigDecimal(maxPerConsultation));
        insurance.setMaxAnnualCoverage(new BigDecimal(maxAnnual));
        insurance.setActive(true);

        PatientInsurance policy = new PatientInsurance();
        policy.setInsurance(insurance);
        policy.setUsedAnnualCoverage(new BigDecimal(usedAnnual));
        policy.setActive(true);
        policy.setEffectiveDate(LocalDateTime.now().minusDays(1));
        policy.setExpirationDate(LocalDateTime.now().plusDays(1));

        return policy;
    }
}
