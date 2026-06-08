package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.AppointmentRequest;
import com.aviva.appointmentsystem.dto.AppointmentResponse;
import com.aviva.appointmentsystem.dto.AvailableSlotResponse;
import com.aviva.appointmentsystem.dto.DoctorResponse;
import com.aviva.appointmentsystem.dto.PatientResponse;
import com.aviva.appointmentsystem.dto.SpecialtyResponse;
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
import com.aviva.appointmentsystem.exception.BusinessRuleException;
import com.aviva.appointmentsystem.exception.ResourceNotFoundException;
import com.aviva.appointmentsystem.exception.ValidationException;
import com.aviva.appointmentsystem.repository.AppointmentRepository;
import com.aviva.appointmentsystem.repository.AuditLogRepository;
import com.aviva.appointmentsystem.repository.DoctorRepository;
import com.aviva.appointmentsystem.repository.MedicalScheduleRepository;
import com.aviva.appointmentsystem.repository.PatientRepository;
import com.aviva.appointmentsystem.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio core para la gestión de citas médicas.
 *
 * Responsabilidades:
 * - RN-13: Estado inicial siempre PENDING al crear
 * - RN-38: Validar que la hora esté dentro del MedicalSchedule del doctor ese día
 * - RN-12: Validar que no exista conflicto de horario exacto con cita activa del doctor
 * - RN-14: Al reprogramar → estado RESCHEDULED (re-valida RN-38 y RN-12)
 * - RN-15: Al cancelar → estado CANCELLED (solo si no está ya cancelada o completada)
 * - RN-21: Al crear → generar Payment con estado PENDING automáticamente
 * - Cálculo de available slots: horarios del doctor − citas activas del día
 *
 * Inyección: Constructor injection (no @Autowired).
 * Notificaciones: Wrapped en try-catch para no romper el flujo principal.
 */
@Service
@Transactional
public class AppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final MedicalScheduleRepository medicalScheduleRepository;
    private final PaymentRepository paymentRepository;
    private final AuditLogRepository auditLogRepository;
    private final NotificationService notificationService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository,
                              MedicalScheduleRepository medicalScheduleRepository,
                              PaymentRepository paymentRepository,
                              AuditLogRepository auditLogRepository,
                              NotificationService notificationService) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.medicalScheduleRepository = medicalScheduleRepository;
        this.paymentRepository = paymentRepository;
        this.auditLogRepository = auditLogRepository;
        this.notificationService = notificationService;
    }

    // ========================================================
    // OPERACIONES PRINCIPALES
    // ========================================================

    /**
     * Crea una nueva cita médica.
     *
     * Flujo completo:
     * 1. Valida que el paciente y el doctor existan
     * 2. RN-38: Valida que la hora esté dentro del horario del doctor ese día
     * 3. RN-12: Valida que no haya conflicto exacto de horario
     * 4. RN-13: Crea la cita con estado PENDING
     * 5. RN-21: Genera un Payment con estado PENDING automáticamente
     * 6. Registra en auditoría y envía notificaciones
     *
     * @param request datos de la cita
     * @return cita creada con datos de paciente y doctor anidados
     */
    public AppointmentResponse create(AppointmentRequest request) {
        logger.info("Creando cita: paciente={}, doctor={}, hora={}",
            request.patientId(), request.doctorId(), request.appointmentDateTime());

        // 1. Validar que el paciente exista
        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", request.patientId()));

        // 1. Validar que el doctor exista
        Doctor doctor = doctorRepository.findById(request.doctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", request.doctorId()));

        LocalDateTime appointmentDateTime = request.appointmentDateTime();

        // 2. RN-38: Validar que la hora esté dentro del schedule activo del doctor ese día
        validateWithinSchedule(doctor, appointmentDateTime);

        // 3. RN-12: Validar que no exista cita activa del doctor en ese datetime exacto
        validateNoConflict(doctor.getId(), appointmentDateTime, null);

        // 4. RN-13: Crear con estado inicial PENDING
        LocalDateTime now = LocalDateTime.now();
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDateTime(appointmentDateTime);
        appointment.setReason(request.reason());
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setCreatedAt(now);
        appointment.setUpdatedAt(now);

        Appointment saved = appointmentRepository.save(appointment);
        logger.info("Cita creada: ID={}, estado=PENDING", saved.getId());

        // 5. RN-21: Generar Payment PENDING automáticamente
        createPaymentForAppointment(saved);

        // 6. Auditoría y notificaciones (no bloquean si fallan)
        registerAudit(saved, "CREATED", "Cita creada exitosamente", "SYSTEM");
        createAppointmentNotifications(saved, "APPOINTMENT_CREATED");

        return mapToResponse(saved);
    }

    /**
     * Reprograma una cita a una nueva fecha/hora.
     *
     * RN-14: El estado cambia a RESCHEDULED.
     * Re-valida RN-38 (dentro del horario) y RN-12 (sin conflicto),
     * excluyendo la propia cita del chequeo de conflictos.
     *
     * @param id ID de la cita a reprogramar
     * @param newDateTimeStr nueva fecha/hora en formato "yyyy-MM-ddTHH:mm:ss"
     * @return cita actualizada
     */
    public AppointmentResponse reschedule(Long id, String newDateTimeStr) {
        logger.info("Reprogramando cita ID={} a {}", id, newDateTimeStr);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita", id));

        // RN-14: Solo se pueden reprogramar citas que no estén canceladas o completadas
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BusinessRuleException("RN-14_CANCELLED",
                "No se puede reprogramar una cita cancelada");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BusinessRuleException("RN-14_COMPLETED",
                "No se puede reprogramar una cita completada");
        }

        LocalDateTime newDateTime = parseDateTime(newDateTimeStr);
        if (!newDateTime.isAfter(LocalDateTime.now())) {
            throw new ValidationException("La nueva fecha y hora debe ser en el futuro");
        }

        Doctor doctor = appointment.getDoctor();

        // RN-38: Validar dentro del horario del doctor en el nuevo día
        validateWithinSchedule(doctor, newDateTime);

        // RN-12: Validar sin conflicto, excluyendo la propia cita
        validateNoConflict(doctor.getId(), newDateTime, id);

        appointment.setAppointmentDateTime(newDateTime);
        appointment.setStatus(AppointmentStatus.RESCHEDULED);
        appointment.setUpdatedAt(LocalDateTime.now());

        Appointment updated = appointmentRepository.save(appointment);
        logger.info("Cita reprogramada: ID={}, nuevoDateTime={}", id, newDateTime);

        registerAudit(updated, "RESCHEDULED", "Reprogramada a: " + newDateTimeStr, "SYSTEM");
        createAppointmentNotifications(updated, "APPOINTMENT_RESCHEDULED");

        return mapToResponse(updated);
    }

    /**
     * Cancela una cita.
     *
     * RN-15: Cambia el estado a CANCELLED.
     * No es posible cancelar citas ya canceladas o completadas.
     *
     * @param id ID de la cita a cancelar
     * @return cita actualizada
     */
    public AppointmentResponse cancel(Long id) {
        logger.info("Cancelando cita ID={}", id);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita", id));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BusinessRuleException("RN-15_ALREADY_CANCELLED",
                "La cita ya se encuentra cancelada");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BusinessRuleException("RN-15_COMPLETED",
                "No se puede cancelar una cita ya completada");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setUpdatedAt(LocalDateTime.now());

        Appointment updated = appointmentRepository.save(appointment);
        logger.info("Cita cancelada: ID={}", id);

        registerAudit(updated, "CANCELLED", "Cita cancelada", "SYSTEM");
        createAppointmentNotifications(updated, "APPOINTMENT_CANCELLED");

        return mapToResponse(updated);
    }

    // ========================================================
    // CONSULTAS
    // ========================================================

    /**
     * Obtiene una cita por ID con datos anidados de paciente y doctor.
     */
    @Transactional(readOnly = true)
    public AppointmentResponse getById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita", id));
        return mapToResponse(appointment);
    }

    /**
     * Lista todas las citas del sistema.
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
     * Lista todas las citas de un paciente.
     *
     * @param patientId ID del paciente
     * @throws ResourceNotFoundException si el paciente no existe
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getByPatient(Long patientId) {
        logger.debug("Obteniendo citas del paciente ID={}", patientId);

        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Paciente", patientId);
        }

        return appointmentRepository.findByPatientId(patientId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Lista todas las citas de un doctor.
     *
     * @param doctorId ID del doctor
     * @throws ResourceNotFoundException si el doctor no existe
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getByDoctor(Long doctorId) {
        logger.debug("Obteniendo citas del doctor ID={}", doctorId);

        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor", doctorId);
        }

        return appointmentRepository.findByDoctorId(doctorId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Lista citas filtradas por estado.
     *
     * @param status estado del AppointmentStatus enum
     * @return lista de citas con ese estado
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
     * Calcula los slots de tiempo disponibles de un doctor para una fecha.
     *
     * Algoritmo:
     * 1. Obtener MedicalSchedules activos del doctor para el día de la semana de la fecha
     * 2. Generar todos los slots posibles (startTime → endTime, paso = appointmentDurationMinutes)
     * 3. Obtener las horas de citas activas (PENDING/CONFIRMED/RESCHEDULED) del doctor ese día
     * 4. Devolver los slots que NO están ocupados
     *
     * RN-38: La base del cálculo es el MedicalSchedule del doctor.
     *
     * @param doctorId ID del doctor
     * @param date fecha específica (ej: 2026-05-20)
     * @return lista de slots disponibles con hora inicio y fin
     */
    @Transactional(readOnly = true)
    public List<AvailableSlotResponse> getAvailableSlots(Long doctorId, LocalDate date) {
        logger.info("Calculando slots disponibles: doctor={}, fecha={}", doctorId, date);

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        // Paso 1: Obtener schedules activos del doctor para ese día de la semana
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<MedicalSchedule> schedules = medicalScheduleRepository
                .findByDoctorAndDayOfWeekAndActive(doctor, dayOfWeek, true);

        if (schedules.isEmpty()) {
            logger.debug("Doctor {} no tiene horarios activos el {}", doctorId, dayOfWeek);
            return List.of();
        }

        // Paso 2: Obtener horas ocupadas (citas activas de ese día)
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay   = date.atTime(LocalTime.MAX);

        Set<LocalTime> horasOcupadas = appointmentRepository
                .findActiveAppointmentsByDoctorAndDate(doctorId, startOfDay, endOfDay)
                .stream()
                .map(a -> a.getAppointmentDateTime().toLocalTime())
                .collect(Collectors.toSet());

        // Paso 3: Generar todos los slots y filtrar los disponibles
        List<AvailableSlotResponse> slotsDisponibles = new ArrayList<>();

        for (MedicalSchedule schedule : schedules) {
            int durationMinutes = schedule.getAppointmentDurationMinutes();
            LocalTime slot = schedule.getStartTime();

            // Generar slots mientras quepan dentro del endTime
            // Un slot es válido si: slot + duration <= endTime
            while (!slot.plusMinutes(durationMinutes).isAfter(schedule.getEndTime())) {
                if (!horasOcupadas.contains(slot)) {
                    slotsDisponibles.add(new AvailableSlotResponse(
                        slot,
                        slot.plusMinutes(durationMinutes)
                    ));
                }
                slot = slot.plusMinutes(durationMinutes);
            }
        }

        logger.info("Slots disponibles para doctor={}, fecha={}: {}", doctorId, date, slotsDisponibles.size());
        return slotsDisponibles;
    }

    // ========================================================
    // VALIDACIONES PRIVADAS
    // ========================================================

    /**
     * RN-38: Valida que la hora de la cita esté dentro del horario activo del doctor ese día.
     *
     * Obtiene los MedicalSchedules activos del doctor para el día de la semana.
     * La hora de la cita debe estar en al menos uno de esos schedules:
     * schedule.startTime <= appointmentTime < schedule.endTime
     *
     * @param doctor Doctor al que se asigna la cita
     * @param appointmentDateTime fecha y hora solicitada
     * @throws BusinessRuleException si no hay schedule activo o la hora está fuera del rango
     */
    private void validateWithinSchedule(Doctor doctor, LocalDateTime appointmentDateTime) {
        DayOfWeek dayOfWeek = appointmentDateTime.getDayOfWeek();
        LocalTime appointmentTime = appointmentDateTime.toLocalTime();

        List<MedicalSchedule> schedules = medicalScheduleRepository
                .findByDoctorAndDayOfWeekAndActive(doctor, dayOfWeek, true);

        if (schedules.isEmpty()) {
            throw new BusinessRuleException("RN-38_NO_SCHEDULE",
                String.format("El doctor no tiene horario activo el día %s", dayOfWeek));
        }

        boolean withinSchedule = schedules.stream().anyMatch(schedule ->
            !appointmentTime.isBefore(schedule.getStartTime()) &&
            appointmentTime.isBefore(schedule.getEndTime())
        );

        if (!withinSchedule) {
            // Construir mensaje descriptivo con los horarios disponibles
            String horariosDisponibles = schedules.stream()
                .map(s -> s.getStartTime() + "-" + s.getEndTime())
                .collect(Collectors.joining(", "));

            throw new BusinessRuleException("RN-38_OUTSIDE_SCHEDULE",
                String.format("La hora %s está fuera del horario del doctor el %s. " +
                              "Horarios disponibles: %s",
                              appointmentTime, dayOfWeek, horariosDisponibles));
        }
    }

    /**
     * RN-12: Valida que no exista otra cita activa del doctor en ese datetime exacto.
     *
     * "Cita activa" = PENDING, CONFIRMED o RESCHEDULED.
     * El parámetro excludeId permite excluir la propia cita en reschedule.
     *
     * @param doctorId ID del doctor
     * @param appointmentDateTime fecha y hora exacta
     * @param excludeId ID de la cita a excluir (null para create)
     * @throws BusinessRuleException si hay conflicto
     */
    private void validateNoConflict(Long doctorId, LocalDateTime appointmentDateTime, Long excludeId) {
        List<Appointment> conflicts = appointmentRepository.findActiveConflict(
            doctorId, appointmentDateTime, excludeId
        );

        if (!conflicts.isEmpty()) {
            throw new BusinessRuleException("RN-12_CONFLICT",
                String.format("El doctor ya tiene una cita activa el %s a las %s",
                    appointmentDateTime.toLocalDate(),
                    appointmentDateTime.toLocalTime()));
        }
    }

    // ========================================================
    // OPERACIONES AUXILIARES PRIVADAS
    // ========================================================

    /**
     * RN-21: Crea automáticamente un Payment en estado PENDING para la cita.
     *
     * Monto base: S/. 100.00 (configurable en futuras versiones).
     * Método de pago inicial: CASH (puede actualizarse en el módulo de Pagos).
     */
    private void createPaymentForAppointment(Appointment appointment) {
        LocalDateTime now = LocalDateTime.now();

        Payment payment = new Payment();
        payment.setAppointment(appointment);
        payment.setAmount(new BigDecimal("100.00"));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setMethod(PaymentMethod.CASH);
        payment.setDescription("Pago generado automáticamente por cita médica ID=" + appointment.getId());
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);

        paymentRepository.save(payment);
        logger.info("RN-21: Payment PENDING creado para cita ID={}", appointment.getId());
    }

    /**
     * Registra un evento en el log de auditoría de la cita.
     */
    private void registerAudit(Appointment appointment, String action, String details, String modifiedBy) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAppointment(appointment);
            auditLog.setAction(action);
            auditLog.setNewStatus(appointment.getStatus());
            auditLog.setDetails(details);
            auditLog.setModifiedBy(modifiedBy);
            auditLog.setCreatedAt(LocalDateTime.now());
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            logger.error("Error registrando auditoría para cita ID={}: {}", appointment.getId(), e.getMessage());
        }
    }

    /**
     * Crea notificaciones para paciente y doctor.
     * Wrapped en try-catch: si falla, NO interrumpe la operación principal.
     */
    private void createAppointmentNotifications(Appointment appointment, String notificationType) {
        try {
            String patientMessage = buildPatientMessage(appointment, notificationType);
            String doctorMessage  = buildDoctorMessage(appointment, notificationType);
            String subject        = buildNotificationSubject(notificationType);

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

            logger.info("Notificaciones enviadas para cita ID={}", appointment.getId());
        } catch (Exception e) {
            logger.error("Error enviando notificaciones para cita ID={}: {}", appointment.getId(), e.getMessage());
        }
    }

    private String buildPatientMessage(Appointment a, String type) {
        return switch (type) {
            case "APPOINTMENT_CREATED" -> String.format(
                "Tu cita con el Dr. %s ha sido creada para el %s a las %s.",
                a.getDoctor().getFirstName(),
                a.getAppointmentDateTime().toLocalDate(),
                a.getAppointmentDateTime().toLocalTime());
            case "APPOINTMENT_RESCHEDULED" -> String.format(
                "Tu cita ha sido reprogramada para el %s a las %s.",
                a.getAppointmentDateTime().toLocalDate(),
                a.getAppointmentDateTime().toLocalTime());
            case "APPOINTMENT_CANCELLED" -> "Tu cita médica ha sido cancelada.";
            default -> "Ha habido un cambio en tu cita médica.";
        };
    }

    private String buildDoctorMessage(Appointment a, String type) {
        return switch (type) {
            case "APPOINTMENT_CREATED" -> String.format(
                "Nueva cita con %s %s para el %s a las %s. Motivo: %s",
                a.getPatient().getFirstName(),
                a.getPatient().getLastName(),
                a.getAppointmentDateTime().toLocalDate(),
                a.getAppointmentDateTime().toLocalTime(),
                a.getReason() != null ? a.getReason() : "No especificado");
            case "APPOINTMENT_RESCHEDULED" -> String.format(
                "La cita con %s ha sido reprogramada para el %s a las %s.",
                a.getPatient().getFirstName(),
                a.getAppointmentDateTime().toLocalDate(),
                a.getAppointmentDateTime().toLocalTime());
            case "APPOINTMENT_CANCELLED" -> String.format(
                "La cita con %s %s ha sido cancelada.",
                a.getPatient().getFirstName(),
                a.getPatient().getLastName());
            default -> "Ha habido un cambio en una cita médica.";
        };
    }

    private String buildNotificationSubject(String type) {
        return switch (type) {
            case "APPOINTMENT_CREATED" -> "Cita médica confirmada";
            case "APPOINTMENT_RESCHEDULED" -> "Cita médica reprogramada";
            case "APPOINTMENT_CANCELLED" -> "Cita médica cancelada";
            default -> "Notificación de cita médica";
        };
    }

    /**
     * Parsea un String a LocalDateTime en formato ISO (yyyy-MM-ddTHH:mm:ss).
     * Usado para el endpoint reschedule que recibe la fecha como @RequestParam String.
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            return LocalDateTime.parse(dateTimeStr);
        } catch (DateTimeParseException e) {
            throw new ValidationException(
                "Formato de fecha-hora inválido: '" + dateTimeStr + "'. Use: yyyy-MM-ddTHH:mm:ss");
        }
    }

    // ========================================================
    // MAPEO Entity → DTO
    // ========================================================

    /**
     * Mapea Appointment a AppointmentResponse con datos anidados de Patient y Doctor.
     * NUNCA devuelve entidades JPA directamente.
     */
    private AppointmentResponse mapToResponse(Appointment appointment) {
        return new AppointmentResponse(
            appointment.getId(),
            mapPatientToResponse(appointment.getPatient()),
            mapDoctorToResponse(appointment.getDoctor()),
            appointment.getAppointmentDateTime(),
            appointment.getReason(),
            appointment.getStatus().name(),
            appointment.getCreatedAt(),
            appointment.getUpdatedAt()
        );
    }

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

    private DoctorResponse mapDoctorToResponse(Doctor doctor) {
        return new DoctorResponse(
            doctor.getId(),
            doctor.getFirstName(),
            doctor.getLastName(),
            doctor.getLicenseNumber(),
            doctor.getPhone(),
            doctor.getEmail(),
            new SpecialtyResponse(
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
}
