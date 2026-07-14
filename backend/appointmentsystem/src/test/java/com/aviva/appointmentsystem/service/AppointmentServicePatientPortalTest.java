package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.AppointmentResponse;
import com.aviva.appointmentsystem.dto.PatientAppointmentRequest;
import com.aviva.appointmentsystem.entity.Appointment;
import com.aviva.appointmentsystem.entity.AppointmentStatus;
import com.aviva.appointmentsystem.entity.AuditLog;
import com.aviva.appointmentsystem.entity.Doctor;
import com.aviva.appointmentsystem.entity.Gender;
import com.aviva.appointmentsystem.entity.MedicalSchedule;
import com.aviva.appointmentsystem.entity.Patient;
import com.aviva.appointmentsystem.entity.Payment;
import com.aviva.appointmentsystem.entity.PaymentMethod;
import com.aviva.appointmentsystem.entity.PaymentStatus;
import com.aviva.appointmentsystem.entity.Specialty;
import com.aviva.appointmentsystem.entity.User;
import com.aviva.appointmentsystem.entity.UserStatus;
import com.aviva.appointmentsystem.exception.BusinessRuleException;
import com.aviva.appointmentsystem.exception.ResourceNotFoundException;
import com.aviva.appointmentsystem.exception.UserInactiveException;
import com.aviva.appointmentsystem.repository.AppointmentRepository;
import com.aviva.appointmentsystem.repository.AuditLogRepository;
import com.aviva.appointmentsystem.repository.DoctorRepository;
import com.aviva.appointmentsystem.repository.MedicalScheduleRepository;
import com.aviva.appointmentsystem.repository.PatientInsuranceRepository;
import com.aviva.appointmentsystem.repository.PatientRepository;
import com.aviva.appointmentsystem.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServicePatientPortalTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private MedicalScheduleRepository medicalScheduleRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private AuditLogRepository auditLogRepository;
    @Mock private NotificationService notificationService;
    @Mock private PatientInsuranceRepository patientInsuranceRepository;
    @Mock private InsuranceCoverageCalculator insuranceCoverageCalculator;

    private AppointmentService service;

    @BeforeEach
    void setUp() {
        service = new AppointmentService(
                appointmentRepository,
                patientRepository,
                doctorRepository,
                medicalScheduleRepository,
                paymentRepository,
                auditLogRepository,
                notificationService,
                patientInsuranceRepository,
                insuranceCoverageCalculator
        );
    }

    @Test
    void listsOnlyAppointmentsFromAuthenticatedPatient() {
        Patient patient = activePatient(41L);
        when(patientRepository.findByUserUsername("patient-user"))
                .thenReturn(Optional.of(patient));
        when(appointmentRepository.findByPatientId(patient.getId())).thenReturn(List.of());

        List<AppointmentResponse> result = service.getForCurrentPatient("patient-user");

        assertTrue(result.isEmpty());
        verify(appointmentRepository).findByPatientId(41L);
    }

    @Test
    void returnsNotFoundWhenAppointmentDoesNotBelongToAuthenticatedPatient() {
        Patient patient = activePatient(41L);
        when(patientRepository.findByUserUsername("patient-user"))
                .thenReturn(Optional.of(patient));
        when(appointmentRepository.findByIdAndPatientId(99L, 41L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.cancelForCurrentPatient("patient-user", 99L)
        );

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void cancellingAppointmentAlsoCancelsItsPendingPayment() {
        Patient patient = activePatient(41L);
        Appointment appointment = payableAppointment(100L, patient);
        Payment payment = paymentFor(70L, appointment, PaymentStatus.PENDING);
        when(patientRepository.findByUserUsername("patient-user"))
                .thenReturn(Optional.of(patient));
        when(appointmentRepository.findByIdAndPatientId(100L, 41L))
                .thenReturn(Optional.of(appointment));
        when(paymentRepository.findByAppointmentIdForUpdate(100L))
                .thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.cancelForCurrentPatient("patient-user", 100L);

        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
        assertEquals(PaymentStatus.CANCELLED, payment.getStatus());
        verify(paymentRepository).save(payment);
    }

    @Test
    void realPaidAppointmentRequiresRefundBeforeCancellation() {
        Patient patient = activePatient(41L);
        Appointment appointment = payableAppointment(100L, patient);
        Payment payment = paymentFor(70L, appointment, PaymentStatus.PAID);
        when(patientRepository.findByUserUsername("patient-user"))
                .thenReturn(Optional.of(patient));
        when(appointmentRepository.findByIdAndPatientId(100L, 41L))
                .thenReturn(Optional.of(appointment));
        when(paymentRepository.findByAppointmentIdForUpdate(100L))
                .thenReturn(Optional.of(payment));

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> service.cancelForCurrentPatient("patient-user", 100L)
        );

        assertEquals("PAID_APPOINTMENT_REQUIRES_REFUND", exception.getCode());
        assertEquals(AppointmentStatus.PENDING, appointment.getStatus());
        verify(appointmentRepository, never()).save(any(Appointment.class));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void staffCancellationPreservesLegacyFlowWithoutChangingPayments() {
        Patient patient = activePatient(41L);
        Appointment appointment = payableAppointment(100L, patient);
        when(appointmentRepository.findById(100L))
                .thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.cancel(100L);

        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
        verify(paymentRepository, never()).findByAppointmentIdForUpdate(100L);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void rejectsPortalAccessWhenPatientProfileIsInactive() {
        Patient patient = activePatient(41L);
        patient.setStatus(UserStatus.INACTIVE);
        when(patientRepository.findByUserUsername("patient-user"))
                .thenReturn(Optional.of(patient));

        assertThrows(
                UserInactiveException.class,
                () -> service.getForCurrentPatient("patient-user")
        );
    }

    @Test
    void rejectsPortalAccessWhenLinkedUserIsInactive() {
        Patient patient = activePatient(41L);
        patient.getUser().setStatus(UserStatus.INACTIVE);
        when(patientRepository.findByUserUsername("patient-user"))
                .thenReturn(Optional.of(patient));

        assertThrows(
                UserInactiveException.class,
                () -> service.getForCurrentPatient("patient-user")
        );
    }

    @Test
    void createsAppointmentUsingPatientResolvedFromAuthenticatedUsername() {
        Patient patient = activePatient(41L);
        Doctor doctor = activeDoctor(7L);
        LocalDateTime requestedDateTime = LocalDate.now()
                .plusWeeks(2)
                .atTime(10, 0);
        MedicalSchedule schedule = scheduleFor(doctor, requestedDateTime);

        when(patientRepository.findByUserUsername("patient-user"))
                .thenReturn(Optional.of(patient));
        when(patientRepository.findById(41L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(7L)).thenReturn(Optional.of(doctor));
        when(medicalScheduleRepository.findByDoctorAndDayOfWeekAndActive(
                doctor, requestedDateTime.getDayOfWeek(), true))
                .thenReturn(List.of(schedule));
        when(appointmentRepository.findActiveConflict(7L, requestedDateTime, null))
                .thenReturn(List.of());
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment appointment = invocation.getArgument(0);
            appointment.setId(100L);
            return appointment;
        });
        when(patientInsuranceRepository.findValidPrimaryInsurance(
                patient, requestedDateTime))
                .thenReturn(Optional.empty());

        PatientAppointmentRequest request = new PatientAppointmentRequest(
                7L,
                requestedDateTime,
                "Control preventivo"
        );

        AppointmentResponse response =
                service.createForCurrentPatient("patient-user", request);

        ArgumentCaptor<Appointment> appointmentCaptor =
                ArgumentCaptor.forClass(Appointment.class);
        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(appointmentRepository).save(appointmentCaptor.capture());
        verify(auditLogRepository).save(auditCaptor.capture());

        assertEquals(41L, appointmentCaptor.getValue().getPatient().getId());
        assertEquals(41L, response.patient().id());
        assertEquals("patient-user", auditCaptor.getValue().getModifiedBy());
        verify(patientInsuranceRepository)
                .findValidPrimaryInsurance(patient, requestedDateTime);
    }

    @Test
    void rejectsCreatingAppointmentWithInactiveDoctor() {
        Patient patient = activePatient(41L);
        Doctor doctor = activeDoctor(7L);
        doctor.setStatus(UserStatus.INACTIVE);
        LocalDateTime requestedDateTime = LocalDate.now().plusWeeks(2).atTime(10, 0);

        when(patientRepository.findByUserUsername("patient-user"))
                .thenReturn(Optional.of(patient));
        when(patientRepository.findById(41L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(7L)).thenReturn(Optional.of(doctor));

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> service.createForCurrentPatient(
                        "patient-user",
                        new PatientAppointmentRequest(7L, requestedDateTime, null)
                )
        );

        assertEquals("DOCTOR_INACTIVE", exception.getCode());
        verify(medicalScheduleRepository, never())
                .findByDoctorAndDayOfWeekAndActive(any(), any(), eq(true));
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void rejectsAvailabilityForInactiveDoctor() {
        Doctor doctor = activeDoctor(7L);
        doctor.setStatus(UserStatus.INACTIVE);
        when(doctorRepository.findById(7L)).thenReturn(Optional.of(doctor));

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> service.getAvailableSlots(7L, LocalDate.now().plusDays(1))
        );

        assertEquals("DOCTOR_INACTIVE", exception.getCode());
        verify(medicalScheduleRepository, never())
                .findByDoctorAndDayOfWeekAndActive(any(), any(), eq(true));
    }

    @Test
    void rejectsReschedulingWhenAssignedDoctorIsInactive() {
        Patient patient = activePatient(41L);
        Doctor doctor = activeDoctor(7L);
        doctor.setStatus(UserStatus.INACTIVE);
        Appointment appointment = new Appointment();
        appointment.setId(100L);
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setAppointmentDateTime(LocalDate.now().plusDays(2).atTime(9, 0));

        when(patientRepository.findByUserUsername("patient-user"))
                .thenReturn(Optional.of(patient));
        when(appointmentRepository.findByIdAndPatientId(100L, 41L))
                .thenReturn(Optional.of(appointment));

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> service.rescheduleForCurrentPatient(
                        "patient-user",
                        100L,
                        LocalDate.now().plusWeeks(1).atTime(11, 0).toString()
                )
        );

        assertEquals("DOCTOR_INACTIVE", exception.getCode());
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    private Patient activePatient(Long id) {
        LocalDateTime now = LocalDateTime.now();
        Patient patient = new Patient();
        patient.setId(id);
        User user = new User();
        user.setUsername("patient-user");
        user.setStatus(UserStatus.ACTIVE);
        patient.setUser(user);
        patient.setDni("76543210");
        patient.setFirstName("María");
        patient.setLastName("Torres");
        patient.setGender(Gender.FEMALE);
        patient.setDateOfBirth(LocalDate.of(1995, 4, 10));
        patient.setPhone("999888777");
        patient.setEmail("maria@example.com");
        patient.setAddress("Lima");
        patient.setStatus(UserStatus.ACTIVE);
        patient.setCreatedAt(now);
        patient.setUpdatedAt(now);
        return patient;
    }

    private Doctor activeDoctor(Long id) {
        LocalDateTime now = LocalDateTime.now();
        Specialty specialty = new Specialty();
        specialty.setId(3L);
        specialty.setName("Medicina general");
        specialty.setDescription("Consulta general");
        specialty.setActive(true);
        specialty.setCreatedAt(now);
        specialty.setUpdatedAt(now);

        Doctor doctor = new Doctor();
        doctor.setId(id);
        doctor.setFirstName("Carlos");
        doctor.setLastName("Mendoza");
        doctor.setLicenseNumber("CMP-12345");
        doctor.setPhone("988777666");
        doctor.setEmail("doctor@example.com");
        doctor.setSpecialty(specialty);
        doctor.setStatus(UserStatus.ACTIVE);
        doctor.setCreatedAt(now);
        doctor.setUpdatedAt(now);
        return doctor;
    }

    private Appointment payableAppointment(Long id, Patient patient) {
        LocalDateTime now = LocalDateTime.now();
        Appointment appointment = new Appointment();
        appointment.setId(id);
        appointment.setPatient(patient);
        appointment.setDoctor(activeDoctor(7L));
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setAppointmentDateTime(now.plusDays(5));
        appointment.setCreatedAt(now);
        appointment.setUpdatedAt(now);
        return appointment;
    }

    private Payment paymentFor(
            Long id,
            Appointment appointment,
            PaymentStatus status) {

        LocalDateTime now = LocalDateTime.now();
        Payment payment = new Payment();
        payment.setId(id);
        payment.setAppointment(appointment);
        payment.setBaseAmount(new java.math.BigDecimal("100.00"));
        payment.setDeductibleApplied(java.math.BigDecimal.ZERO);
        payment.setInsuranceCoveredAmount(java.math.BigDecimal.ZERO);
        payment.setAmount(new java.math.BigDecimal("100.00"));
        payment.setStatus(status);
        payment.setMethod(PaymentMethod.CREDIT_CARD);
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);
        return payment;
    }

    private MedicalSchedule scheduleFor(Doctor doctor, LocalDateTime requestedDateTime) {
        MedicalSchedule schedule = new MedicalSchedule();
        schedule.setDoctor(doctor);
        schedule.setDayOfWeek(requestedDateTime.getDayOfWeek());
        schedule.setStartTime(LocalTime.of(8, 0));
        schedule.setEndTime(LocalTime.of(17, 0));
        schedule.setAppointmentDurationMinutes(30);
        schedule.setMaxAppointmentsPerDay(18);
        schedule.setActive(true);
        return schedule;
    }
}
