package com.aviva.appointmentsystem.repository;

import com.aviva.appointmentsystem.entity.Triage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Triage
 */
@Repository
public interface TriageRepository extends JpaRepository<Triage, Long> {
    Optional<Triage> findByAppointmentId(Long appointmentId);
}
