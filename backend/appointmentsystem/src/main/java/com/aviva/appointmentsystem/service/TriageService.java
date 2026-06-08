package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.TriageRequest;
import com.aviva.appointmentsystem.dto.TriageResponse;
import com.aviva.appointmentsystem.entity.Appointment;
import com.aviva.appointmentsystem.entity.Triage;
import com.aviva.appointmentsystem.exception.BusinessRuleException;
import com.aviva.appointmentsystem.exception.ResourceNotFoundException;
import com.aviva.appointmentsystem.exception.ValidationException;
import com.aviva.appointmentsystem.repository.AppointmentRepository;
import com.aviva.appointmentsystem.repository.TriageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Servicio para la gestión del triaje (signos vitales) de citas.
 *
 * Validaciones en create():
 * 1. Que la cita exista
 * 2. Que no exista un triaje previo para esa cita (1 triaje por cita, como 1-to-1)
 * 3. Que los signos vitales estén dentro de rangos clínicos razonables
 *    (rangos basados en parámetros médicos estándar)
 *
 * Inyección: Constructor injection (no @Autowired).
 */
@Service
@Transactional
public class TriageService {

    private static final Logger logger = LoggerFactory.getLogger(TriageService.class);

    private final TriageRepository triageRepository;
    private final AppointmentRepository appointmentRepository;

    public TriageService(TriageRepository triageRepository,
                         AppointmentRepository appointmentRepository) {
        this.triageRepository = triageRepository;
        this.appointmentRepository = appointmentRepository;
    }

    /**
     * Registra los signos vitales (triaje) para una cita.
     *
     * Flujo:
     * 1. Validar que la cita exista
     * 2. Validar que no exista triaje previo para esa cita
     * 3. Validar rangos clínicos de los signos vitales
     * 4. Persistir el Triage
     *
     * @param appointmentId ID de la cita médica
     * @param request datos vitales del paciente
     * @return triaje creado
     */
    public TriageResponse create(Long appointmentId, TriageRequest request) {
        logger.info("Registrando triaje para cita ID={}", appointmentId);

        // 1. Validar que la cita exista
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita", appointmentId));

        // 2. Validar que no exista triaje previo (relación OneToOne)
        if (triageRepository.findByAppointmentId(appointmentId).isPresent()) {
            throw new BusinessRuleException("TRIAGE_ALREADY_EXISTS",
                "Ya existe un registro de triaje para la cita ID=" + appointmentId);
        }

        // 3. Validar rangos clínicos
        validateVitalSigns(request);

        // 4. Crear y persistir
        LocalDateTime now = LocalDateTime.now();
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
        triage.setCreatedAt(now);
        triage.setUpdatedAt(now);

        Triage saved = triageRepository.save(triage);
        logger.info("Triaje registrado: ID={}, cita ID={}", saved.getId(), appointmentId);

        return mapToResponse(saved);
    }

    /**
     * Obtiene el triaje de una cita por ID de cita.
     *
     * @param appointmentId ID de la cita
     * @throws ResourceNotFoundException si la cita o el triaje no existen
     */
    @Transactional(readOnly = true)
    public TriageResponse getByAppointment(Long appointmentId) {
        logger.debug("Obteniendo triaje de cita ID={}", appointmentId);

        if (!appointmentRepository.existsById(appointmentId)) {
            throw new ResourceNotFoundException("Cita", appointmentId);
        }

        Triage triage = triageRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Triaje no encontrado para cita ID: " + appointmentId));

        return mapToResponse(triage);
    }

    // ========================================================
    // VALIDACIONES PRIVADAS
    // ========================================================

    /**
     * Valida que los signos vitales estén dentro de rangos clínicos razonables.
     *
     * Rangos utilizados (estándares médicos generales):
     * - Presión sistólica:    70–200 mmHg
     * - Presión diastólica:   40–130 mmHg
     * - Temperatura:          34.0–42.0 °C
     * - Frecuencia cardíaca:  30–220 lpm
     * - Frecuencia resp.:      8–60 rpm
     * - Peso:                  2–500 kg   (neonatos hasta obesidad extrema)
     * - Altura:               30–250 cm
     *
     * Nota: Los rangos del service original (90-180, 60-120) eran demasiado restrictivos
     * para casos clínicos límite. Se amplían para reducir falsos rechazos.
     */
    private void validateVitalSigns(TriageRequest request) {

        // Presión sistólica: 70–200 mmHg
        if (request.bloodPressureSystolic() < 70 || request.bloodPressureSystolic() > 200) {
            throw new ValidationException(String.format(
                "Presión sistólica %d fuera de rango válido (70–200 mmHg)",
                request.bloodPressureSystolic()));
        }

        // Presión diastólica: 40–130 mmHg
        if (request.bloodPressureDiastolic() < 40 || request.bloodPressureDiastolic() > 130) {
            throw new ValidationException(String.format(
                "Presión diastólica %d fuera de rango válido (40–130 mmHg)",
                request.bloodPressureDiastolic()));
        }

        // Sistólica debe ser mayor que diastólica
        if (request.bloodPressureSystolic() <= request.bloodPressureDiastolic()) {
            throw new ValidationException(
                "La presión sistólica debe ser mayor que la diastólica");
        }

        // Temperatura: 34.0–42.0 °C
        if (request.temperature() < 34.0 || request.temperature() > 42.0) {
            throw new ValidationException(String.format(
                "Temperatura %.1f °C fuera de rango válido (34.0–42.0 °C)",
                request.temperature()));
        }

        // Frecuencia cardíaca: 30–220 lpm
        if (request.heartRate() < 30 || request.heartRate() > 220) {
            throw new ValidationException(String.format(
                "Frecuencia cardíaca %d fuera de rango válido (30–220 lpm)",
                request.heartRate()));
        }

        // Frecuencia respiratoria: 8–60 rpm
        if (request.respiratoryRate() < 8 || request.respiratoryRate() > 60) {
            throw new ValidationException(String.format(
                "Frecuencia respiratoria %d fuera de rango válido (8–60 rpm)",
                request.respiratoryRate()));
        }

        // Peso: 2–500 kg
        if (request.weight() < 2.0 || request.weight() > 500.0) {
            throw new ValidationException(String.format(
                "Peso %.1f fuera de rango válido (2–500 kg)",
                request.weight()));
        }

        // Altura: 30–250 cm
        if (request.height() < 30.0 || request.height() > 250.0) {
            throw new ValidationException(String.format(
                "Altura %.1f fuera de rango válido (30–250 cm)",
                request.height()));
        }
    }

    // ========================================================
    // MAPEO Entity → DTO
    // ========================================================

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
