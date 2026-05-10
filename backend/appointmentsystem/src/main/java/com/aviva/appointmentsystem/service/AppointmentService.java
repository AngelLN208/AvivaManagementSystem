package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.AppointmentRequest;
import com.aviva.appointmentsystem.dto.AppointmentResponse;
import com.aviva.appointmentsystem.dto.AvailableSlotResponse;
import com.aviva.appointmentsystem.dto.DoctorResponse;
import com.aviva.appointmentsystem.dto.PatientResponse;
import com.aviva.appointmentsystem.entity.Appointment;
import com.aviva.appointmentsystem.entity.AppointmentStatus;
import com.aviva.appointmentsystem.entity.AuditLog;
import com.aviva.appointmentsystem.entity.Doctor;
import com.aviva.appointmentsystem.entity.MedicalSchedule;
import com.aviva.appointmentsystem.entity.Notification;
import com.aviva.appointmentsystem.entity.Patient;
import com.aviva.appointmentsystem.entity.Payment;
import com.aviva.appointmentsystem.entity.PaymentMethod;
import com.aviva.appointmentsystem.entity.PaymentStatus;
import com.aviva.appointmentsystem.exception.ResourceNotFoundException;
import com.aviva.appointmentsystem.exception.ValidationException;
import com.aviva.appointmentsystem.repository.AppointmentRepository;
import com.aviva.appointmentsystem.repository.AuditLogRepository;
import com.aviva.appointmentsystem.repository.DoctorRepository;
import com.aviva.appointmentsystem.repository.PatientRepository;
import com.aviva.appointmentsystem.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;
import com.aviva.appointmentsystem.repository.MedicalScheduleRepository;

/**
 * Servicio para gestionar citas médicas
 */
@Service
@Transactional
public class AppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentService.class);

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MedicalScheduleRepository medicalScheduleRepository;

    /**
     * Crea una nueva cita médica
     * Automáticamente genera un pago en estado PENDIENTE
     */
    public AppointmentResponse create(AppointmentRequest request) {
        logger.info("Creando nueva cita para paciente ID={}, doctor ID={}", 
            request.patientId(), request.doctorId());

        // Validar que el paciente exista
        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", request.patientId()));

        // Validar que el doctor exista
        Doctor doctor = doctorRepository.findById(request.doctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", request.doctorId()));

        // Validar que la fecha sea válida
        LocalDateTime appointmentDateTime = parseDateTime(request.appointmentDateTime());
        if (appointmentDateTime.isBefore(LocalDateTime.now())) {
            throw new ValidationException("La fecha y hora de la cita no puede ser en el pasado");
        }

        // Validar que no haya conflicto de horario
        validateNoConflict(doctor, appointmentDateTime);

        // Crear cita
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDateTime(appointmentDateTime);
        appointment.setReason(request.reason());
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setUpdatedAt(LocalDateTime.now());

        Appointment saved = appointmentRepository.save(appointment);
        logger.info("Cita creada exitosamente: ID={}", saved.getId());

        // Crear pago automáticamente
        createPaymentForAppointment(saved);

        // Registrar en auditoría
        registerAudit(saved, "CREATED", "Cita creada", "SYSTEM");

        // Crear notificaciones
        createAppointmentNotifications(saved, "APPOINTMENT_CREATED");

        return mapToResponse(saved);
    }

    /**
     * Reprograma una cita a una nueva fecha
     */
    public AppointmentResponse reschedule(Long id, String newDateTime) {
        logger.info("Reprogramando cita ID={}", id);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita", id));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new ValidationException("No se puede reprogramar una cita cancelada");
        }

        LocalDateTime newAppointmentDateTime = parseDateTime(newDateTime);
        if (newAppointmentDateTime.isBefore(LocalDateTime.now())) {
            throw new ValidationException("La fecha y hora de la cita no puede ser en el pasado");
        }

        // Validar que no haya conflicto
        validateNoConflict(appointment.getDoctor(), newAppointmentDateTime);

        appointment.setAppointmentDateTime(newAppointmentDateTime);
        appointment.setStatus(AppointmentStatus.RESCHEDULED);
        appointment.setUpdatedAt(LocalDateTime.now());

        Appointment updated = appointmentRepository.save(appointment);
        logger.info("Cita reprogramada: ID={}", id);

        // Crear notificaciones
        createAppointmentNotifications(updated, "APPOINTMENT_RESCHEDULED");

        // Registrar en auditoría
        registerAudit(updated, "RESCHEDULED", "Cita reprogramada a: " + newDateTime, "SYSTEM");

        return mapToResponse(updated);
    }

    /**
     * Cancela una cita
     */
    public AppointmentResponse cancel(Long id) {
        logger.info("Cancelando cita ID={}", id);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita", id));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new ValidationException("La cita ya está cancelada");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new ValidationException("No se puede cancelar una cita completada");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setUpdatedAt(LocalDateTime.now());

        Appointment updated = appointmentRepository.save(appointment);
        logger.info("Cita cancelada: ID={}", id);

        // Crear notificaciones
        createAppointmentNotifications(updated, "APPOINTMENT_CANCELLED");

        // Registrar en auditoría
        registerAudit(updated, "CANCELLED", "Cita cancelada", "SYSTEM");

        return mapToResponse(updated);
    }

    /**
     * Obtiene una cita por ID
     */
    @Transactional(readOnly = true)
    public AppointmentResponse getById(Long id) {
        logger.debug("Obteniendo cita ID={}", id);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita", id));

        return mapToResponse(appointment);
    }

    /**
     * Lista todas las citas
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAll() {
        logger.debug("Obteniendo todas las citas");
        return appointmentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Lista citas de un paciente
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getByPatient(Long patientId) {
        logger.debug("Obteniendo citas del paciente ID={}", patientId);

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", patientId));

        return appointmentRepository.findByPatient(patient)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Lista citas de un doctor
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getByDoctor(Long doctorId) {
        logger.debug("Obteniendo citas del doctor ID={}", doctorId);

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        return appointmentRepository.findByDoctor(doctor)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Lista citas por estado
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getByStatus(AppointmentStatus status) {
        logger.debug("Obteniendo citas con estado: {}", status);

        return appointmentRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Obtiene los slots disponibles de un doctor para una fecha específica
     * Calcula los horarios libres restando las citas ya agendadas
     * RN-38: Solo muestra horarios dentro del schedule activo del doctor
     */

    @Transactional(readOnly = true)
    public List<AvailableSlotResponse> getAvailableSlots(Long doctorId, LocalDate date) {
        logger.info("Obteniendo slots disponibles para doctor: ID={}, fecha={}", doctorId, date);

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        DayOfWeek dayOfWeek = date.getDayOfWeek();

        List<MedicalSchedule> schedules = medicalScheduleRepository
                .findByDoctorAndDayOfWeekAndActive(doctor, dayOfWeek, true);

        if (schedules.isEmpty()) {
            return List.of();
        }

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay   = date.atTime(LocalTime.MAX);

        List<Appointment> citasDelDia = appointmentRepository
                .findByDoctorAndAppointmentDateTimeBetweenAndStatusNot(
                    doctor, startOfDay, endOfDay, AppointmentStatus.CANCELLED
                );

        Set<LocalTime> horasOcupadas = citasDelDia.stream()
                .map(a -> a.getAppointmentDateTime().toLocalTime())
                .collect(Collectors.toSet());

        List<AvailableSlotResponse> slotsDisponibles = new ArrayList<>();

        for (MedicalSchedule schedule : schedules) {
            LocalTime slot = schedule.getStartTime();
            while (slot.plusMinutes(schedule.getAppointmentDurationMinutes())
                    .compareTo(schedule.getEndTime()) <= 0) {
                if (!horasOcupadas.contains(slot)) {
                    slotsDisponibles.add(new AvailableSlotResponse(
                        slot,
                        slot.plusMinutes(schedule.getAppointmentDurationMinutes())
                    ));
                }
                slot = slot.plusMinutes(schedule.getAppointmentDurationMinutes());
            }
        }

        return slotsDisponibles;
    }
    /**
     * Valida que no haya conflicto de horario para el doctor
     */
    private void validateNoConflict(Doctor doctor, LocalDateTime appointmentDateTime) {
        // Permitir 30 minutos antes y después del tiempo
        LocalDateTime startWindow = appointmentDateTime.minusMinutes(30);
        LocalDateTime endWindow = appointmentDateTime.plusMinutes(30);

        List<Appointment> conflictingAppointments = appointmentRepository
                .findByDoctorAndAppointmentDateTimeBetween(doctor, startWindow, endWindow)
                .stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .toList();

        if (!conflictingAppointments.isEmpty()) {
            logger.warn("Conflicto de horario detectado para doctor ID={}", doctor.getId());
            throw new ValidationException("El doctor ya tiene una cita en ese horario");
        }
    }

    /**
     * Crea un pago automáticamente para la cita
     */
    private void createPaymentForAppointment(Appointment appointment) {
        Payment payment = new Payment();
        payment.setAppointment(appointment);
        payment.setAmount(new BigDecimal("100.00")); // Monto default
        payment.setStatus(PaymentStatus.PENDING);
        payment.setMethod(PaymentMethod.CASH);
        payment.setDescription("Pago por cita médica");
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        paymentRepository.save(payment);
        logger.debug("Pago creado para cita ID={}", appointment.getId());
    }

    /**
     * Registra un evento en la auditoría
     */
    private void registerAudit(Appointment appointment, String action, String details, String modifiedBy) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAppointment(appointment);
        auditLog.setAction(action);
        auditLog.setNewStatus(appointment.getStatus());
        auditLog.setDetails(details);
        auditLog.setModifiedBy(modifiedBy);
        auditLog.setCreatedAt(LocalDateTime.now());

        auditLogRepository.save(auditLog);
    }

    /**
     * Crea notificaciones para paciente y doctor
     */
    private void createAppointmentNotifications(Appointment appointment, String notificationType) {
        try {
            String patientMessage = buildPatientMessage(appointment, notificationType);
            String doctorMessage = buildDoctorMessage(appointment, notificationType);
            String subject = buildNotificationSubject(notificationType);

            // Notificación al paciente
            notificationService.createNotification(
                Notification.NotificationType.valueOf(notificationType),
                appointment.getPatient().getEmail(),
                appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName(),
                appointment,
                subject,
                patientMessage,
                Notification.NotificationChannel.EMAIL,
                LocalDateTime.now()
            );

            // Notificación al doctor
            notificationService.createNotification(
                Notification.NotificationType.valueOf(notificationType),
                appointment.getDoctor().getEmail(),
                appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName(),
                appointment,
                "[Doctor] " + subject,
                doctorMessage,
                Notification.NotificationChannel.EMAIL,
                LocalDateTime.now()
            );

            logger.info("Notificaciones creadas para cita: ID={}", appointment.getId());
        } catch (Exception e) {
            logger.error("Error creando notificaciones para cita: ID={}", appointment.getId(), e);
        }
    }

    /**
     * Construye mensaje para el paciente
     */
    private String buildPatientMessage(Appointment appointment, String notificationType) {
        return switch (notificationType) {
            case "APPOINTMENT_CREATED" -> 
                String.format("Tu cita ha sido creada con el Dr. %s el %s a las %s.",
                    appointment.getDoctor().getFirstName(),
                    appointment.getAppointmentDateTime().toLocalDate(),
                    appointment.getAppointmentDateTime().toLocalTime());
            case "APPOINTMENT_RESCHEDULED" ->
                String.format("Tu cita ha sido reprogramada para el %s a las %s.",
                    appointment.getAppointmentDateTime().toLocalDate(),
                    appointment.getAppointmentDateTime().toLocalTime());
            case "APPOINTMENT_CANCELLED" ->
                "Tu cita ha sido cancelada.";
            default -> "Cambio en tu cita médica.";
        };
    }

    /**
     * Construye mensaje para el doctor
     */
    private String buildDoctorMessage(Appointment appointment, String notificationType) {
        return switch (notificationType) {
            case "APPOINTMENT_CREATED" ->
                String.format("Nueva cita con %s el %s a las %s. Motivo: %s",
                    appointment.getPatient().getFirstName(),
                    appointment.getAppointmentDateTime().toLocalDate(),
                    appointment.getAppointmentDateTime().toLocalTime(),
                    appointment.getReason());
            case "APPOINTMENT_RESCHEDULED" ->
                String.format("La cita con %s ha sido reprogramada para %s a las %s.",
                    appointment.getPatient().getFirstName(),
                    appointment.getAppointmentDateTime().toLocalDate(),
                    appointment.getAppointmentDateTime().toLocalTime());
            case "APPOINTMENT_CANCELLED" ->
                String.format("La cita con %s ha sido cancelada.",
                    appointment.getPatient().getFirstName());
            default -> "Cambio en cita médica.";
        };
    }

    /**
     * Construye el asunto de la notificación
     */
    private String buildNotificationSubject(String notificationType) {
        return switch (notificationType) {
            case "APPOINTMENT_CREATED" -> "Tu cita ha sido creada";
            case "APPOINTMENT_RESCHEDULED" -> "Tu cita ha sido reprogramada";
            case "APPOINTMENT_CANCELLED" -> "Tu cita ha sido cancelada";
            default -> "Notificación de cita";
        };
    }

    /**
     * Mapea entidad a DTO
     */
    private AppointmentResponse mapToResponse(Appointment appointment) {
        PatientResponse patientResponse = mapPatientToResponse(appointment.getPatient());
        DoctorResponse doctorResponse = mapDoctorToResponse(appointment.getDoctor());

        return new AppointmentResponse(
            appointment.getId(),
            patientResponse,
            doctorResponse,
            appointment.getAppointmentDateTime(),
            appointment.getReason(),
            appointment.getStatus().name(),
            appointment.getCreatedAt(),
            appointment.getUpdatedAt()
        );
    }

    /**
     * Mapea Patient a PatientResponse
     */
    private PatientResponse mapPatientToResponse(Patient patient) {
        return new PatientResponse(
            patient.getId(),
            patient.getDni(),
            patient.getFirstName(),
            patient.getLastName(),
            patient.getGender().name(),
            patient.getDateOfBirth(),
            patient.getPhone(),
            patient.getEmail(),
            patient.getAddress(),
            patient.getStatus().name(),
            patient.getCreatedAt(),
            patient.getUpdatedAt()
        );
    }

    /**
     * Mapea Doctor a DoctorResponse
     */
    private DoctorResponse mapDoctorToResponse(Doctor doctor) {
        return new DoctorResponse(
            doctor.getId(),
            doctor.getFirstName(),
            doctor.getLastName(),
            doctor.getLicenseNumber(),
            doctor.getPhone(),
            doctor.getEmail(),
            new com.aviva.appointmentsystem.dto.SpecialtyResponse(
                doctor.getSpecialty().getId(),
                doctor.getSpecialty().getName(),
                doctor.getSpecialty().getDescription(),
                doctor.getSpecialty().getActive(),
                doctor.getSpecialty().getCreatedAt(),
                doctor.getSpecialty().getUpdatedAt()
            ),
            doctor.getStatus().name(),
            doctor.getCreatedAt(),
            doctor.getUpdatedAt()
        );
    }

    /**
     * Parsea una fecha en formato ISO String a LocalDateTime
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            return LocalDateTime.parse(dateTimeStr, formatter);
        } catch (DateTimeParseException e) {
            logger.error("Error parseando fecha-hora: {}", dateTimeStr);
            throw new ValidationException("Formato de fecha-hora inválido. Use: yyyy-MM-ddTHH:mm:ss");
        }
    }
}
