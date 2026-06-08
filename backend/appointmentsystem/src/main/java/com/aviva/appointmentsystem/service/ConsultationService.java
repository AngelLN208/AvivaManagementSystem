package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.ConsultationRequest;
import com.aviva.appointmentsystem.dto.ConsultationResponse;
import com.aviva.appointmentsystem.entity.Appointment;
import com.aviva.appointmentsystem.entity.AppointmentStatus;
import com.aviva.appointmentsystem.entity.Consultation;
import com.aviva.appointmentsystem.exception.BusinessRuleException;
import com.aviva.appointmentsystem.exception.ResourceNotFoundException;
import com.aviva.appointmentsystem.repository.AppointmentRepository;
import com.aviva.appointmentsystem.repository.ConsultationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Servicio para la gestión de consultas médicas (diagnóstico y tratamiento).
 *
 * Reglas de negocio críticas:
 *
 * RN-31: Solo se puede registrar una consulta si la cita está en estado CONFIRMED.
 *        Un paciente debe haber pagado (→CONFIRMED via RN-16) antes de la consulta.
 *        Si el estado es PENDING, RESCHEDULED, CANCELLED u otro → BusinessRuleException (409).
 *
 * RN-33: Impide registrar más de una consulta por cita.
 *        La entidad Consultation tiene @OneToOne con Appointment (unique constraint).
 *        Se valida en service antes de llegar a la DB para dar un mensaje descriptivo.
 *
 * Inyección: Constructor injection (no @Autowired).
 */
@Service
@Transactional
public class ConsultationService {

    private static final Logger logger = LoggerFactory.getLogger(ConsultationService.class);

    private final ConsultationRepository consultationRepository;
    private final AppointmentRepository appointmentRepository;

    public ConsultationService(ConsultationRepository consultationRepository,
                                AppointmentRepository appointmentRepository) {
        this.consultationRepository = consultationRepository;
        this.appointmentRepository = appointmentRepository;
    }

    /**
     * Registra la consulta médica (diagnóstico + tratamiento) para una cita.
     *
     * Flujo obligatorio:
     * 1. Validar que la cita exista
     * 2. RN-31: Validar que la cita esté en estado CONFIRMED (no PENDING, CANCELLED, etc.)
     * 3. RN-33: Validar que no exista ya una consulta para esa cita
     * 4. Persistir la Consultation
     *
     * @param appointmentId ID de la cita (debe estar CONFIRMED)
     * @param request datos del diagnóstico y tratamiento
     * @return consulta registrada
     */
    public ConsultationResponse create(Long appointmentId, ConsultationRequest request) {
        logger.info("Registrando consulta para cita ID={}", appointmentId);

        // 1. Validar que la cita exista
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita", appointmentId));

        // 2. RN-31: La cita DEBE estar CONFIRMED
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            logger.warn("RN-31: Intento de consulta en cita con estado={}", appointment.getStatus());
            throw new BusinessRuleException("RN-31_NOT_CONFIRMED",
                String.format(
                    "RN-31: Solo se puede registrar consulta para citas CONFIRMADAS. " +
                    "Estado actual de la cita ID=%d: %s",
                    appointmentId, appointment.getStatus().getDisplayName()));
        }

        // 3. RN-33: No puede existir consulta previa para la misma cita
        if (consultationRepository.findByAppointmentId(appointmentId).isPresent()) {
            throw new BusinessRuleException("RN-33_DUPLICATE_CONSULTATION",
                "RN-33: Ya existe una consulta registrada para la cita ID=" + appointmentId);
        }

        // 4. Crear y persistir
        LocalDateTime now = LocalDateTime.now();
        Consultation consultation = new Consultation();
        consultation.setAppointment(appointment);
        consultation.setDiagnosis(request.diagnosis());
        consultation.setTreatment(request.treatment());
        consultation.setNotes(request.notes());
        consultation.setCreatedAt(now);
        consultation.setUpdatedAt(now);

        Consultation saved = consultationRepository.save(consultation);
        logger.info("Consulta registrada: ID={}, cita ID={}", saved.getId(), appointmentId);

        return mapToResponse(saved);
    }

    /**
     * Obtiene la consulta médica de una cita.
     *
     * @param appointmentId ID de la cita
     * @throws ResourceNotFoundException si la cita o la consulta no existen
     */
    @Transactional(readOnly = true)
    public ConsultationResponse getByAppointment(Long appointmentId) {
        logger.debug("Obteniendo consulta de cita ID={}", appointmentId);

        if (!appointmentRepository.existsById(appointmentId)) {
            throw new ResourceNotFoundException("Cita", appointmentId);
        }

        Consultation consultation = consultationRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Consulta no encontrada para cita ID: " + appointmentId));

        return mapToResponse(consultation);
    }

    // ========================================================
    // MAPEO Entity → DTO
    // ========================================================

    private ConsultationResponse mapToResponse(Consultation consultation) {
        return new ConsultationResponse(
            consultation.getId(),
            consultation.getAppointment().getId(),
            consultation.getDiagnosis(),
            consultation.getTreatment(),
            consultation.getNotes(),
            consultation.getCreatedAt(),
            consultation.getUpdatedAt()
        );
    }
}
