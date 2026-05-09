package com.aviva.appointmentsystem.repository;

import com.aviva.appointmentsystem.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad AuditLog
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByAppointmentIdOrderByCreatedAtDesc(Long appointmentId);
}
