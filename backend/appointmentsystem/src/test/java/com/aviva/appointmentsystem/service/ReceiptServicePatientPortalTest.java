package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.ReceiptResponse;
import com.aviva.appointmentsystem.entity.Appointment;
import com.aviva.appointmentsystem.entity.Patient;
import com.aviva.appointmentsystem.entity.Payment;
import com.aviva.appointmentsystem.entity.Receipt;
import com.aviva.appointmentsystem.entity.User;
import com.aviva.appointmentsystem.entity.UserStatus;
import com.aviva.appointmentsystem.exception.ResourceNotFoundException;
import com.aviva.appointmentsystem.repository.PatientRepository;
import com.aviva.appointmentsystem.repository.ReceiptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceiptServicePatientPortalTest {

    @Mock private ReceiptRepository receiptRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private ReceiptPdfService receiptPdfService;

    private ReceiptService service;

    @BeforeEach
    void setUp() {
        service = new ReceiptService(
                receiptRepository,
                patientRepository,
                receiptPdfService
        );
    }

    @Test
    void listsOnlyReceiptsFromAuthenticatedPatient() {
        Patient patient = activePatient(41L);
        Receipt receipt = receiptForPatient(9L, patient);
        when(patientRepository.findByUserUsername("patient-user"))
                .thenReturn(Optional.of(patient));
        when(receiptRepository
                .findByPaymentAppointmentPatientIdOrderByCreatedAtDesc(41L))
                .thenReturn(List.of(receipt));

        List<ReceiptResponse> result = service.getForCurrentPatient("patient-user");

        assertEquals(1, result.size());
        assertEquals(70L, result.get(0).paymentId());
        verify(receiptRepository)
                .findByPaymentAppointmentPatientIdOrderByCreatedAtDesc(41L);
    }

    @Test
    void hidesForeignReceiptAsNotFound() {
        Patient patient = activePatient(41L);
        when(patientRepository.findByUserUsername("patient-user"))
                .thenReturn(Optional.of(patient));
        when(receiptRepository.findByIdAndPaymentAppointmentPatientId(99L, 41L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.getByIdForCurrentPatient("patient-user", 99L)
        );
    }

    @Test
    void findsReceiptByOwnedPayment() {
        Patient patient = activePatient(41L);
        Receipt receipt = receiptForPatient(9L, patient);
        when(patientRepository.findByUserUsername("patient-user"))
                .thenReturn(Optional.of(patient));
        when(receiptRepository
                .findByPaymentIdAndPaymentAppointmentPatientId(70L, 41L))
                .thenReturn(Optional.of(receipt));

        ReceiptResponse response = service.getByPaymentForCurrentPatient(
                "patient-user", 70L);

        assertEquals(9L, response.id());
        assertEquals("Comprobante por consulta médica — Cita ID=100",
                response.description());
    }

    @Test
    void generatesPdfOnlyAfterResolvingOwnedReceipt() {
        Patient patient = activePatient(41L);
        Receipt receipt = receiptForPatient(9L, patient);
        ReceiptPdfDocument expected = new ReceiptPdfDocument(
                "Constancia-RCP-20260714-ABCDEF12.pdf",
                new byte[] {1, 2, 3}
        );
        when(patientRepository.findByUserUsername("patient-user"))
                .thenReturn(Optional.of(patient));
        when(receiptRepository.findByIdAndPaymentAppointmentPatientId(9L, 41L))
                .thenReturn(Optional.of(receipt));
        when(receiptPdfService.generate(receipt)).thenReturn(expected);

        ReceiptPdfDocument result = service.generatePdfForCurrentPatient(
                "patient-user",
                9L
        );

        assertEquals(expected, result);
        verify(receiptPdfService).generate(receipt);
    }

    private Patient activePatient(Long id) {
        Patient patient = new Patient();
        patient.setId(id);
        patient.setStatus(UserStatus.ACTIVE);
        User user = new User();
        user.setUsername("patient-user");
        user.setStatus(UserStatus.ACTIVE);
        patient.setUser(user);
        return patient;
    }

    private Receipt receiptForPatient(Long id, Patient patient) {
        Appointment appointment = new Appointment();
        appointment.setId(100L);
        appointment.setPatient(patient);

        Payment payment = new Payment();
        payment.setId(70L);
        payment.setAppointment(appointment);

        Receipt receipt = new Receipt();
        receipt.setId(id);
        receipt.setPayment(payment);
        receipt.setReceiptNumber("RCP-20260714-ABCDEF12");
        receipt.setDescription("Comprobante por consulta médica — Cita ID=100");
        receipt.setTotalAmount(new BigDecimal("100.00"));
        receipt.setCreatedAt(LocalDateTime.now());
        return receipt;
    }
}
