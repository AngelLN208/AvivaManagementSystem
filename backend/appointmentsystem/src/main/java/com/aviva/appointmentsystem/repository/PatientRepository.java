package com.aviva.appointmentsystem.repository;

import com.aviva.appointmentsystem.entity.Patient;
import com.aviva.appointmentsystem.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Patient.
 * 
 * Consultas optimizadas:
 * - findByDni / findByEmail → Validaciones de unicidad
 * - findByStatus → Filtrar activos directamente en la DB (no en Java)
 * - searchByFilters → Búsqueda combinada DNI + nombre + apellido en una sola query
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    /** Busca paciente por DNI exacto */
    Optional<Patient> findByDni(String dni);

    /** Busca paciente por email exacto */
    Optional<Patient> findByEmail(String email);

    /** Resuelve el perfil asociado a la identidad autenticada del portal. */
    Optional<Patient> findByUserUsername(String username);

    /** Lista pacientes por estado (ACTIVE, INACTIVE, etc.) — sin filtrar en Java */
    List<Patient> findByStatus(UserStatus status);

    /** Busca pacientes por nombre (LIKE, case-insensitive) */
    List<Patient> findByFirstNameContainingIgnoreCase(String firstName);

    /** Busca pacientes por apellido (LIKE, case-insensitive) */
    List<Patient> findByLastNameContainingIgnoreCase(String lastName);

    /** Busca pacientes por nombre Y apellido (LIKE, case-insensitive) */
    List<Patient> findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(
            String firstName, String lastName);

    /**
     * Búsqueda flexible de pacientes activos.
     * Soporta filtrar por DNI exacto, nombre parcial y/o apellido parcial.
     * Los parámetros nulos son tratados como "sin filtro" mediante la cláusula OR.
     */
    @Query("""
        SELECT p FROM Patient p
        WHERE p.status = :status
          AND (:dni IS NULL OR p.dni = :dni)
          AND (:firstName IS NULL OR LOWER(p.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')))
          AND (:lastName IS NULL OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :lastName, '%')))
        ORDER BY p.lastName, p.firstName
        """)
    List<Patient> searchByFilters(
            @Param("status") UserStatus status,
            @Param("dni") String dni,
            @Param("firstName") String firstName,
            @Param("lastName") String lastName);
}
