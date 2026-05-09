package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.TriageRequest;
import com.aviva.appointmentsystem.dto.TriageResponse;
import com.aviva.appointmentsystem.entity.Appointment;
import com.aviva.appointmentsystem.entity.Triage;
import com.aviva.appointmentsystem.exception.ResourceNotFoundException;
import com.aviva.appointmentsystem.exception.ValidationException;
import com.aviva.appointmentsystem.repository.AppointmentRepository;
import com.aviva.appointmentsystem.repository.TriageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Servicio para gestionar triaje (signos vitales)
 */
@Service
@Transactional
public class TriageService {

    private static final Logger logger = LoggerFactory.getLogger(TriageService.class);

    @Autowired
    private TriageRepository triageRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    /**
     * Registra los signos vitales (triaje) de una cita
     */
    public TriageResponse create(Long appointmentId, TriageRequest request) {
        logger.info("Registrando triaje para cita ID={}", appointmentId);

        // Validar que la cita exista
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita", appointmentId));

        // Validar que no exista un triaje previo
        if (triageRepository.findByAppointmentId(appointmentId).isPresent()) {
            throw new ValidationException("Ya existe un registro de triaje para esta cita");
        }

        // Validaciones de rangos normales (recomendaciones médicas)
        validateVitalSigns(request);

        // Crear triaje
        Triage triage = new Triage();
        triage.setAppointment(appointment);
        triage.setBloodPressureSystolic(request.bloodPressureSystolic());
        triage.setBloodPressureDiastolic(request.bloodPressureDiastolic());
        triage.setTemperature(request.temperature());
        triage.setHeartRate(request.heartRate());
        triage.setRespiratoryRate(request.respiratoryRate());
        triage.setWeight(request.weight());
        triage.setHeight(request.height());
        triage.setNotes(request.notes());
        triage.setCreatedAt(LocalDateTime.now());
        triage.setUpdatedAt(LocalDateTime.now());

        Triage saved = triageRepository.save(triage);
        logger.info("Triaje registrado exitosamente: ID={}", saved.getId());

        return mapToResponse(saved);
    }

    /**
     * Obtiene el triaje de una cita
     */
    @Transactional(readOnly = true)
    public TriageResponse getByAppointment(Long appointmentId) {
        logger.debug("Obteniendo triaje de cita ID={}", appointmentId);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita", appointmentId));

        Triage triage = triageRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Triaje no encontrado para cita: " + appointmentId));

        return mapToResponse(triage);
    }

    /**
     * Valida que los signos vitales estén dentro de rangos razonables
     */
    private void validateVitalSigns(TriageRequest request) {
        // Presión arterial sistólica: 90-180 mmHg
        if (request.bloodPressureSystolic() < 90 || request.bloodPressureSystolic() > 180) {
            logger.warn("Presión sistólica fuera de rango: {}", request.bloodPressureSystolic());
            throw new ValidationException("Presión sistólica debe estar entre 90-180 mmHg");
        }

        // Presión arterial diastólica: 60-120 mmHg
        if (request.bloodPressureDiastolic() < 60 || request.bloodPressureDiastolic() > 120) {
            logger.warn("Presión diastólica fuera de rango: {}", request.bloodPressureDiastolic());
            throw new ValidationException("Presión diastólica debe estar entre 60-120 mmHg");
        }

        // Temperatura: 35-42°C
        if (request.temperature().doubleValue() < 35 || request.temperature().doubleValue() > 42) {
            logger.warn("Temperatura fuera de rango: {}", request.temperature());
            throw new ValidationException("Temperatura debe estar entre 35-42°C");
        }

        // Frecuencia cardíaca: 40-200 lpm
        if (request.heartRate() < 40 || request.heartRate() > 200) {
            logger.warn("Frecuencia cardíaca fuera de rango: {}", request.heartRate());
            throw new ValidationException("Frecuencia cardíaca debe estar entre 40-200 lpm");
        }

        // Frecuencia respiratoria: 10-50 rpm
        if (request.respiratoryRate() < 10 || request.respiratoryRate() > 50) {
            logger.warn("Frecuencia respiratoria fuera de rango: {}", request.respiratoryRate());
            throw new ValidationException("Frecuencia respiratoria debe estar entre 10-50 rpm");
        }

        // Peso: 20-300 kg
        if (request.weight().doubleValue() < 20 || request.weight().doubleValue() > 300) {
            logger.warn("Peso fuera de rango: {}", request.weight());
            throw new ValidationException("Peso debe estar entre 20-300 kg");
        }

        // Altura: 50-220 cm
        if (request.height().doubleValue() < 50 || request.height().doubleValue() > 220) {
            logger.warn("Altura fuera de rango: {}", request.height());
            throw new ValidationException("Altura debe estar entre 50-220 cm");
        }
    }

    /**
     * Mapea entidad a DTO
     */
    private TriageResponse mapToResponse(Triage triage) {
        return new TriageResponse(
            triage.getId(),
            triage.getBloodPressureSystolic(),
            triage.getBloodPressureDiastolic(),
            triage.getTemperature(),
            triage.getHeartRate(),
            triage.getRespiratoryRate(),
            triage.getWeight(),
            triage.getHeight(),
            triage.getNotes(),
            triage.getCreatedAt(),
            triage.getUpdatedAt()
        );
    }
}
