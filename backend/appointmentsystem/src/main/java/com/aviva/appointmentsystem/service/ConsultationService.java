package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.ConsultationRequest;
import com.aviva.appointmentsystem.dto.ConsultationResponse;
import com.aviva.appointmentsystem.entity.Appointment;
import com.aviva.appointmentsystem.entity.AppointmentStatus;
import com.aviva.appointmentsystem.entity.Consultation;
import com.aviva.appointmentsystem.exception.ResourceNotFoundException;
import com.aviva.appointmentsystem.exception.ValidationException;
import com.aviva.appointmentsystem.repository.AppointmentRepository;
import com.aviva.appointmentsystem.repository.ConsultationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Servicio para gestionar consultas (diagnósticos y tratamientos)
 */
@Service
@Transactional
public class ConsultationService {

    private static final Logger logger = LoggerFactory.getLogger(ConsultationService.class);

    @Autowired
    private ConsultationRepository consultationRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    /**
     * Registra una consulta (diagnóstico y tratamiento) para una cita
     * Valida que:
     * - La cita exista
     * - La cita esté CONFIRMADA
     * - No exista una consulta previa para esa cita
     */
    public ConsultationResponse create(Long appointmentId, ConsultationRequest request) {
        logger.info("Registrando consulta para cita ID={}", appointmentId);

        // Validar que la cita exista
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita", appointmentId));

        // Validar que la cita esté CONFIRMADA
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            logger.warn("Intento de crear consulta para cita no confirmada. Estado: {}", appointment.getStatus());
            throw new ValidationException("La cita debe estar CONFIRMADA para registrar una consulta");
        }

        // Validar que no exista una consulta previa
        if (consultationRepository.findByAppointmentId(appointmentId).isPresent()) {
            throw new ValidationException("Ya existe una consulta para esta cita");
        }

        // Crear consulta
        Consultation consultation = new Consultation();
        consultation.setAppointment(appointment);
        consultation.setDiagnosis(request.diagnosis());
        consultation.setTreatment(request.treatment());
        consultation.setNotes(request.notes());
        consultation.setCreatedAt(LocalDateTime.now());
        consultation.setUpdatedAt(LocalDateTime.now());

        Consultation saved = consultationRepository.save(consultation);
        logger.info("Consulta registrada exitosamente: ID={}", saved.getId());

        return mapToResponse(saved);
    }

    /**
     * Obtiene la consulta de una cita
     */
    @Transactional(readOnly = true)
    public ConsultationResponse getByAppointment(Long appointmentId) {
        logger.debug("Obteniendo consulta de cita ID={}", appointmentId);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita", appointmentId));

        Consultation consultation = consultationRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta no encontrada para cita: " + appointmentId));

        return mapToResponse(consultation);
    }

    /**
     * Mapea entidad a DTO
     */
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
