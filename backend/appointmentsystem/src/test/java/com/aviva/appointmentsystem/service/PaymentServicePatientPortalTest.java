package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.PaymentResponse;
import com.aviva.appointmentsystem.entity.Appointment;
import com.aviva.appointmentsystem.entity.AppointmentStatus;
import com.aviva.appointmentsystem.entity.AuditLog;
import com.aviva.appointmentsystem.entity.Insurance;
import com.aviva.appointmentsystem.entity.Patient;
import com.aviva.appointmentsystem.entity.PatientInsurance;
import com.aviva.appointmentsystem.entity.Payment;
import com.aviva.appointmentsystem.entity.PaymentMethod;
import com.aviva.appointmentsystem.entity.PaymentStatus;
import com.aviva.appointmentsystem.entity.Receipt;
import com.aviva.appointmentsystem.entity.User;
import com.aviva.appointmentsystem.entity.UserStatus;
import com.aviva.appointmentsystem.exception.BusinessRuleException;
import com.aviva.appointmentsystem.exception.ResourceNotFoundException;
import com.aviva.appointmentsystem.exception.UserInactiveException;
import com.aviva.appointmentsystem.repository.AppointmentRepository;
import com.aviva.appointmentsystem.repository.AuditLogRepository;
import com.aviva.appointmentsystem.repository.PatientInsuranceRepository;
import com.aviva.appointmentsystem.repository.PatientRepository;
import com.aviva.appointmentsystem.repository.PaymentRepository;
import com.aviva.appointmentsystem.repository.ReceiptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServicePatientPortalTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private ReceiptRepository receiptRepository;
    @Mock private AppointmentRepository appointmentRepository;
    @Mock private AuditLogRepository auditLogRepository;
    @Mock private NotificationService notificationService;
    @Mock private PatientInsuranceRepository patientInsuranceRepository;
    @Mock private PatientRepository patientRepository;

    private PaymentService service;

    @BeforeEach
    void setUp() {
        service = new PaymentService(
                paymentRepository,
                receiptRepository,
                appointmentRepository,
                auditLogRepository,
                notificationService,
                patientInsuranceRepository,
                patientRepository
        );
    }

    @Test
    void listsOnlyPaymentsFromAuthenticatedPatient() {
        Patient patient = activePatient(41L);
        when(patientRepository.findByUserUsername("patient-user"))
                .thenReturn(Optional.of(patient));
        when(paymentRepository.findByAppointmentPatientIdOrderByCreatedAtDesc(41L))
                .thenReturn(List.of());

        List<PaymentResponse> result = service.getForCurrentPatient("patient-user");

        assertTrue(result.isEmpty());
        verify(paymentRepository)
                .findByAppointmentPatientIdOrderByCreatedAtDesc(41L);
    }

    @Test
    void staffProcessingKeepsHistoricalContract() {
        Payment payment = pendingPayment(70L, activePatient(41L));
        when(paymentRepository.findByIdForUpdate(70L))
                .thenReturn(Optional.of(payment));
        persistArguments();

        PaymentResponse response = service.processPayment(70L, PaymentMethod.CASH);

        assertEquals("PAID", response.status());
        assertEquals("CASH", response.method());
        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());
        assertEquals("PAYMENT_CONFIRMED", auditCaptor.getValue().getAction());
        assertEquals("SYSTEM", auditCaptor.getValue().getModifiedBy());
    }

    @Test
    void hidesForeignPaymentAsNotFound() {
        Patient patient = activePatient(41L);
        when(patientRepository.findByUserUsername("patient-user"))
                .thenReturn(Optional.of(patient));
        when(paymentRepository.findOwnedByIdForUpdate(99L, 41L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.payForCurrentPatient(
                        "patient-user", 99L, PaymentMethod.CREDIT_CARD)
        );

        verify(paymentRepository, never()).save(any(Payment.class));
        verify(receiptRepository, never()).save(any(Receipt.class));
    }

    @Test
    void paysOwnedPendingPaymentAndCreatesReceipt() {
        Patient patient = activePatient(41L);
        Payment payment = pendingPayment(70L, patient);
        when(patientRepository.findByUserUsername("patient-user"))
                .thenReturn(Optional.of(patient));
        when(paymentRepository.findOwnedByIdForUpdate(70L, 41L))
                .thenReturn(Optional.of(payment));
        persistArguments();

        PaymentResponse response = service.payForCurrentPatient(
                "patient-user", 70L, PaymentMethod.CREDIT_CARD);

        assertEquals("PAID", response.status());
        assertEquals("CREDIT_CARD", response.method());
        assertEquals(AppointmentStatus.CONFIRMED,
                payment.getAppointment().getStatus());

        ArgumentCaptor<Receipt> receiptCaptor = ArgumentCaptor.forClass(Receipt.class);
        verify(receiptRepository).save(receiptCaptor.capture());
        Receipt receipt = receiptCaptor.getValue();
        assertTrue(receipt.getReceiptNumber().startsWith("RCP-"));
        assertEquals(
                "Comprobante por consulta médica — Cita ID=100",
                receipt.getDescription()
        );

        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());
        assertEquals("PAYMENT_CONFIRMED", auditCaptor.getValue().getAction());
        assertEquals("patient-user", auditCaptor.getValue().getModifiedBy());
    }

    @Test
    void rejectsRefundedPaymentInsteadOfReturningItToPaid() {
        Patient patient = activePatient(41L);
        Payment payment = pendingPayment(70L, patient);
        payment.setStatus(PaymentStatus.REFUNDED);
        when(patientRepository.findByUserUsername("patient-user"))
                .thenReturn(Optional.of(patient));
        when(paymentRepository.findOwnedByIdForUpdate(70L, 41L))
                .thenReturn(Optional.of(payment));

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> service.payForCurrentPatient(
                        "patient-user", 70L, PaymentMethod.CREDIT_CARD)
        );

        assertEquals("RN-26_REFUNDED", exception.getCode());
        verify(receiptRepository, never()).save(any(Receipt.class));
    }

    @Test
    void rejectsPaymentWhenAppointmentWasCancelled() {
        Patient patient = activePatient(41L);
        Payment payment = pendingPayment(70L, patient);
        payment.getAppointment().setStatus(AppointmentStatus.CANCELLED);
        when(patientRepository.findByUserUsername("patient-user"))
                .thenReturn(Optional.of(patient));
        when(paymentRepository.findOwnedByIdForUpdate(70L, 41L))
                .thenReturn(Optional.of(payment));

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> service.payForCurrentPatient(
                        "patient-user", 70L, PaymentMethod.DEBIT_CARD)
        );

        assertEquals("PAYMENT_APPOINTMENT_NOT_PAYABLE", exception.getCode());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void rejectsSelfDeclaredCashPayment() {
        Patient patient = activePatient(41L);
        Payment payment = pendingPayment(70L, patient);
        when(patientRepository.findByUserUsername("patient-user"))
                .thenReturn(Optional.of(patient));
        when(paymentRepository.findOwnedByIdForUpdate(70L, 41L))
                .thenReturn(Optional.of(payment));

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> service.payForCurrentPatient(
                        "patient-user", 70L, PaymentMethod.CASH)
        );

        assertEquals("PATIENT_PAYMENT_METHOD_NOT_ALLOWED", exception.getCode());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void usesInsuranceForZeroBalanceAndLocksCoverage() {
        Patient patient = activePatient(41L);
        Payment payment = pendingPayment(70L, patient);
        payment.setAmount(BigDecimal.ZERO.setScale(2));
        payment.setInsuranceCoveredAmount(new BigDecimal("100.00"));

        PatientInsurance policy = activePolicy(12L, patient,
                payment.getAppointment().getAppointmentDateTime());
        payment.setPatientInsurance(policy);

        when(patientRepository.findByUserUsername("patient-user"))
                .thenReturn(Optional.of(patient));
        when(paymentRepository.findOwnedByIdForUpdate(70L, 41L))
                .thenReturn(Optional.of(payment));
        when(patientInsuranceRepository.findByIdForUpdate(12L))
                .thenReturn(Optional.of(policy));
        persistArguments();

        PaymentResponse response = service.payForCurrentPatient(
                "patient-user", 70L, PaymentMethod.CREDIT_CARD);

        assertEquals("INSURANCE", response.method());
        assertEquals(new BigDecimal("100.00"), policy.getUsedAnnualCoverage());
        verify(patientInsuranceRepository).findByIdForUpdate(12L);
    }

    @Test
    void rejectsPolicyThatExpiresBeforeAppointmentDate() {
        Patient patient = activePatient(41L);
        Payment payment = pendingPayment(70L, patient);
        payment.setInsuranceCoveredAmount(new BigDecimal("50.00"));
        payment.setAmount(new BigDecimal("50.00"));

        PatientInsurance policy = activePolicy(12L, patient,
                payment.getAppointment().getAppointmentDateTime());
        policy.setExpirationDate(
                payment.getAppointment().getAppointmentDateTime().minusDays(1));
        payment.setPatientInsurance(policy);

        when(patientRepository.findByUserUsername("patient-user"))
                .thenReturn(Optional.of(patient));
        when(paymentRepository.findOwnedByIdForUpdate(70L, 41L))
                .thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(patientInsuranceRepository.findByIdForUpdate(12L))
                .thenReturn(Optional.of(policy));

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> service.payForCurrentPatient(
                        "patient-user", 70L, PaymentMethod.DEBIT_CARD)
        );

        assertEquals("INSURANCE_POLICY_NOT_AVAILABLE", exception.getCode());
        verify(receiptRepository, never()).save(any(Receipt.class));
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void rejectsInactiveLinkedUser() {
        Patient patient = activePatient(41L);
        patient.getUser().setStatus(UserStatus.INACTIVE);
        when(patientRepository.findByUserUsername("patient-user"))
                .thenReturn(Optional.of(patient));

        assertThrows(
                UserInactiveException.class,
                () -> service.getForCurrentPatient("patient-user")
        );
        verify(paymentRepository, never())
                .findByAppointmentPatientIdOrderByCreatedAtDesc(any());
    }

    private void persistArguments() {
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(receiptRepository.save(any(Receipt.class)))
                .thenAnswer(invocation -> {
                    Receipt receipt = invocation.getArgument(0);
                    receipt.setId(501L);
                    return receipt;
                });
    }

    private Patient activePatient(Long id) {
        Patient patient = new Patient();
        patient.setId(id);
        patient.setFirstName("María");
        patient.setLastName("Torres");
        patient.setEmail("maria@example.com");
        patient.setStatus(UserStatus.ACTIVE);

        User user = new User();
        user.setUsername("patient-user");
        user.setStatus(UserStatus.ACTIVE);
        patient.setUser(user);
        return patient;
    }

    private Payment pendingPayment(Long id, Patient patient) {
        LocalDateTime now = LocalDateTime.now();
        Appointment appointment = new Appointment();
        appointment.setId(100L);
        appointment.setPatient(patient);
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setAppointmentDateTime(now.plusDays(7));
        appointment.setCreatedAt(now);
        appointment.setUpdatedAt(now);

        Payment payment = new Payment();
        payment.setId(id);
        payment.setAppointment(appointment);
        payment.setBaseAmount(new BigDecimal("100.00"));
        payment.setDeductibleApplied(BigDecimal.ZERO.setScale(2));
        payment.setInsuranceCoveredAmount(BigDecimal.ZERO.setScale(2));
        payment.setAmount(new BigDecimal("100.00"));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setMethod(PaymentMethod.CASH);
        payment.setDescription("Pago particular");
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);
        return payment;
    }

    private PatientInsurance activePolicy(
            Long id,
            Patient patient,
            LocalDateTime appointmentDateTime) {

        Insurance insurance = new Insurance();
        insurance.setId(4L);
        insurance.setName("Seguro Demo");
        insurance.setActive(true);
        insurance.setMaxAnnualCoverage(new BigDecimal("1000.00"));

        PatientInsurance policy = new PatientInsurance();
        policy.setId(id);
        policy.setPatient(patient);
        policy.setInsurance(insurance);
        policy.setActive(true);
        policy.setEffectiveDate(appointmentDateTime.minusMonths(1));
        policy.setExpirationDate(appointmentDateTime.plusMonths(1));
        policy.setUsedAnnualCoverage(BigDecimal.ZERO.setScale(2));
        return policy;
    }
}
