package com.aviva.appointmentsystem.repository;

import com.aviva.appointmentsystem.entity.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Specialty.
 * 
 * Consultas optimizadas usando Spring Data JPA derived queries
 * para evitar cargar todas las especialidades y filtrar en Java.
 */
@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {

    /** Busca una especialidad por nombre exacto (para validación de unicidad) */
    Optional<Specialty> findByName(String name);

    /** Busca una especialidad por nombre exacto ignorando mayúsculas */
    Optional<Specialty> findByNameIgnoreCase(String name);

    /** Lista todas las especialidades activas (consulta directa en DB, sin filtrar en Java) */
    List<Specialty> findByActiveTrue();
}
