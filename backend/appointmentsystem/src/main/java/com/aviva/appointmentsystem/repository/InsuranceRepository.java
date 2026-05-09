package com.aviva.appointmentsystem.repository;

import com.aviva.appointmentsystem.entity.Insurance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar seguros médicos
 */
@Repository
public interface InsuranceRepository extends JpaRepository<Insurance, Long> {
    Optional<Insurance> findByName(String name);
    List<Insurance> findByActive(Boolean active);
}
