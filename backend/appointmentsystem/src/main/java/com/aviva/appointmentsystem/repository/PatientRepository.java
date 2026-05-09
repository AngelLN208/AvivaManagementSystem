package com.aviva.appointmentsystem.repository;

import com.aviva.appointmentsystem.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Patient
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByDni(String dni);
    Optional<Patient> findByEmail(String email);
    List<Patient> findByFirstNameContainingIgnoreCase(String firstName);
    List<Patient> findByLastNameContainingIgnoreCase(String lastName);
    List<Patient> findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(String firstName, String lastName);
}
