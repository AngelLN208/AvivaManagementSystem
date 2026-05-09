package com.aviva.appointmentsystem.repository;

import com.aviva.appointmentsystem.entity.Patient;
import com.aviva.appointmentsystem.entity.PatientInsurance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar seguros de pacientes
 */
@Repository
public interface PatientInsuranceRepository extends JpaRepository<PatientInsurance, Long> {
    List<PatientInsurance> findByPatient(Patient patient);
    List<PatientInsurance> findByPatientAndActive(Patient patient, Boolean active);
    Optional<PatientInsurance> findByPatientAndIsPrimary(Patient patient, Boolean isPrimary);
    List<PatientInsurance> findByPatientAndInsuranceIdAndActive(Long patientId, Long insuranceId, Boolean active);
}
