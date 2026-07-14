package com.aviva.appointmentsystem.repository;

import com.aviva.appointmentsystem.entity.Payment;
import com.aviva.appointmentsystem.entity.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Payment
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByStatus(PaymentStatus status);
    List<Payment> findByAppointmentId(Long appointmentId);

    List<Payment> findByAppointmentPatientIdOrderByCreatedAtDesc(Long patientId);

    Optional<Payment> findByIdAndAppointmentPatientId(Long id, Long patientId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT p
        FROM Payment p
        JOIN FETCH p.appointment a
        WHERE p.id = :paymentId
        """)
    Optional<Payment> findByIdForUpdate(@Param("paymentId") Long paymentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT p
        FROM Payment p
        JOIN FETCH p.appointment a
        WHERE p.id = :paymentId
          AND a.patient.id = :patientId
        """)
    Optional<Payment> findOwnedByIdForUpdate(
            @Param("paymentId") Long paymentId,
            @Param("patientId") Long patientId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT p
        FROM Payment p
        WHERE p.appointment.id = :appointmentId
        """)
    Optional<Payment> findByAppointmentIdForUpdate(
            @Param("appointmentId") Long appointmentId);
}
