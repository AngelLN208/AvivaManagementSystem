package com.aviva.appointmentsystem.repository;

import com.aviva.appointmentsystem.entity.Patient;
import com.aviva.appointmentsystem.entity.PatientActivationChallenge;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientActivationChallengeRepository
        extends JpaRepository<PatientActivationChallenge, UUID> {

    Optional<PatientActivationChallenge> findTopByPatientOrderByCreatedAtDesc(Patient patient);

    List<PatientActivationChallenge> findByPatientAndConsumedAtIsNull(Patient patient);

    long countByPatientAndCreatedAtAfter(Patient patient, LocalDateTime createdAfter);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT challenge FROM PatientActivationChallenge challenge " +
           "JOIN FETCH challenge.patient WHERE challenge.id = :id")
    Optional<PatientActivationChallenge> findByIdForUpdate(@Param("id") UUID id);
}
