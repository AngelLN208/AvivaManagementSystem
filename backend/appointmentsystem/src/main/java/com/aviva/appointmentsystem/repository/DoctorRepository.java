package com.aviva.appointmentsystem.repository;

import com.aviva.appointmentsystem.entity.Doctor;
import com.aviva.appointmentsystem.entity.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Doctor
 */
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByLicenseNumber(String licenseNumber);
    List<Doctor> findBySpecialty(Specialty specialty);
    List<Doctor> findByEmail(String email);
}
