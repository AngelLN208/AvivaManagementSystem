package com.aviva.appointmentsystem.repository;

import com.aviva.appointmentsystem.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Receipt
 */
@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    Optional<Receipt> findByReceiptNumber(String receiptNumber);
    Optional<Receipt> findByPaymentId(Long paymentId);

    List<Receipt> findByPaymentAppointmentPatientIdOrderByCreatedAtDesc(
            Long patientId);

    Optional<Receipt> findByIdAndPaymentAppointmentPatientId(
            Long id,
            Long patientId);

    Optional<Receipt> findByPaymentIdAndPaymentAppointmentPatientId(
            Long paymentId,
            Long patientId);
}
