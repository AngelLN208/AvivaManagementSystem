package com.aviva.appointmentsystem.repository;

import com.aviva.appointmentsystem.entity.Patient;
import com.aviva.appointmentsystem.entity.Insurance;
import com.aviva.appointmentsystem.entity.PatientInsurance;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
    Optional<PatientInsurance> findByPatientAndInsurance(Patient patient, Insurance insurance);
    List<PatientInsurance> findByPatientIdAndInsuranceIdAndActive(Long patientId, Long insuranceId, Boolean active);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pi FROM PatientInsurance pi WHERE pi.id = :id")
    Optional<PatientInsurance> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pi FROM PatientInsurance pi WHERE pi.id = :id AND pi.patient = :patient")
    Optional<PatientInsurance> findOwnedByIdForUpdate(
            @Param("id") Long id,
            @Param("patient") Patient patient);

    @Query("""
    SELECT pi
    FROM PatientInsurance pi
    WHERE pi.patient = :patient
      AND pi.isPrimary = true
      AND pi.active = true
      AND pi.insurance.active = true
      AND pi.effectiveDate <= :now
      AND pi.expirationDate >= :now
        """)
    Optional<PatientInsurance> findValidPrimaryInsurance(
            @Param("patient") Patient patient,
            @Param("now") LocalDateTime now
    );
}
