package com.aviva.appointmentsystem.repository;

import com.aviva.appointmentsystem.entity.Doctor;
import com.aviva.appointmentsystem.entity.Specialty;
import com.aviva.appointmentsystem.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Doctor.
 * 
 * Consultas optimizadas:
 * - findByLicenseNumber → Validación de unicidad de colegiatura
 * - findByStatus → Filtrar activos directamente en la DB (no en Java)
 * - findBySpecialtyAndStatus → Buscar por especialidad solo entre activos
 * - findByEmail → Validación de unicidad de email
 */
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    /** Busca doctor por número de licencia (colegiatura) — validación de unicidad */
    Optional<Doctor> findByLicenseNumber(String licenseNumber);

    /** Busca doctor por email — validación de unicidad */
    Optional<Doctor> findByEmail(String email);

    /** Lista doctores por estado — filtro en DB, no en Java */
    List<Doctor> findByStatus(UserStatus status);

    /** Lista doctores por especialidad (objeto) */
    List<Doctor> findBySpecialty(Specialty specialty);

    /** Lista doctores activos de una especialidad — RN-36 */
    List<Doctor> findBySpecialtyAndStatus(Specialty specialty, UserStatus status);

    /** Lista doctores activos por ID de especialidad — evita cargar la entidad Specialty */
    List<Doctor> findBySpecialtyIdAndStatus(Long specialtyId, UserStatus status);
}
