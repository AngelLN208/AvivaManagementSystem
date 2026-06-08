package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.MedicalScheduleRequest;
import com.aviva.appointmentsystem.dto.MedicalScheduleResponse;
import com.aviva.appointmentsystem.dto.MedicalScheduleUpdateRequest;
import com.aviva.appointmentsystem.entity.Doctor;
import com.aviva.appointmentsystem.entity.MedicalSchedule;
import com.aviva.appointmentsystem.exception.ResourceNotFoundException;
import com.aviva.appointmentsystem.exception.ValidationException;
import com.aviva.appointmentsystem.repository.DoctorRepository;
import com.aviva.appointmentsystem.repository.MedicalScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/**
 * Servicio para gestionar horarios médicos.
 * 
 * Responsabilidades (Service Layer):
 * - TODA la lógica de negocio reside aquí
 * - RN-37: Validar que doctorId exista antes de asignar horario
 * - RN-37: Detectar cruces de horario (solapamiento) en el mismo día/doctor
 * - Validación cruzada: startTime < endTime
 * - Validación de duración mínima (15 minutos)
 * - Conversión Entity → DTO (mapToResponse)
 * 
 * El Controller NO hace lógica, solo delega aquí.
 */
@Service
@Transactional
public class MedicalScheduleService {

    private static final Logger logger = LoggerFactory.getLogger(MedicalScheduleService.class);

    private final MedicalScheduleRepository medicalScheduleRepository;
    private final DoctorRepository doctorRepository;

    public MedicalScheduleService(MedicalScheduleRepository medicalScheduleRepository,
                                  DoctorRepository doctorRepository) {
        this.medicalScheduleRepository = medicalScheduleRepository;
        this.doctorRepository = doctorRepository;
    }

    /**
     * Crea un horario para un doctor.
     * 
     * RN-37: Define los horarios en que el doctor atiende.
     * Validaciones:
     * 1. Que el doctor exista
     * 2. Que startTime < endTime
     * 3. Que la duración sea >= 15 minutos
     * 4. Que NO haya superposición con horarios existentes del mismo doctor/día
     * 
     * @param doctorId ID del doctor
     * @param request datos del horario
     * @return horario creado
     * @throws ResourceNotFoundException si el doctor no existe
     * @throws ValidationException si los horarios son inválidos o hay superposición
     */
    public MedicalScheduleResponse create(Long doctorId, MedicalScheduleRequest request) {
        logger.info("Creando horario para doctor ID={}, día={}", doctorId, request.dayOfWeek());

        // 1. Validar que el doctor exista (RN-37)
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        // 2. Validar startTime < endTime
        validateTimeRange(request.startTime(), request.endTime());

        // 3. Validar duración mínima
        if (request.appointmentDurationMinutes() < 15) {
            throw new ValidationException("La duración mínima de cita es 15 minutos");
        }

        // 4. RN-37: Validar que no exista superposición de horarios
        validateNoOverlap(doctorId, request.dayOfWeek(), request.startTime(), request.endTime(), null);

        MedicalSchedule schedule = new MedicalSchedule();
        schedule.setDoctor(doctor);
        schedule.setDayOfWeek(request.dayOfWeek());
        schedule.setStartTime(request.startTime());
        schedule.setEndTime(request.endTime());
        schedule.setAppointmentDurationMinutes(request.appointmentDurationMinutes());
        schedule.setMaxAppointmentsPerDay(request.maxAppointmentsPerDay());
        schedule.setActive(true);
        schedule.setNotes(request.notes());
        // createdAt y updatedAt se setean via @PrePersist en la entidad

        MedicalSchedule saved = medicalScheduleRepository.save(schedule);
        logger.info("Horario creado: ID={}, doctor={}, día={}, {}-{}",
            saved.getId(), doctorId, request.dayOfWeek(), request.startTime(), request.endTime());

        return mapToResponse(saved);
    }

    /**
     * Actualiza un horario existente (actualización parcial).
     * 
     * Solo actualiza los campos que vienen como no-nulos en el request.
     * Tras aplicar los cambios parciales, re-valida:
     * - startTime < endTime (con valores finales mergeados)
     * - Sin superposición con otros horarios (excluyendo el propio)
     * 
     * @param scheduleId ID del horario a actualizar
     * @param request datos parciales
     * @return horario actualizado
     * @throws ResourceNotFoundException si el horario no existe
     * @throws ValidationException si los tiempos son inválidos o hay superposición
     */
    public MedicalScheduleResponse update(Long scheduleId, MedicalScheduleUpdateRequest request) {
        logger.info("Actualizando horario ID={}", scheduleId);

        MedicalSchedule schedule = medicalScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Horario médico", scheduleId));

        // Aplicar solo campos presentes (no nulos) — merge parcial
        if (request.dayOfWeek() != null) {
            schedule.setDayOfWeek(request.dayOfWeek());
        }
        if (request.startTime() != null) {
            schedule.setStartTime(request.startTime());
        }
        if (request.endTime() != null) {
            schedule.setEndTime(request.endTime());
        }
        if (request.appointmentDurationMinutes() != null) {
            if (request.appointmentDurationMinutes() < 15) {
                throw new ValidationException("La duración mínima de cita es 15 minutos");
            }
            schedule.setAppointmentDurationMinutes(request.appointmentDurationMinutes());
        }
        if (request.maxAppointmentsPerDay() != null) {
            schedule.setMaxAppointmentsPerDay(request.maxAppointmentsPerDay());
        }
        if (request.notes() != null) {
            schedule.setNotes(request.notes());
        }

        // Re-validar con los valores finales (mergeados)
        validateTimeRange(schedule.getStartTime(), schedule.getEndTime());

        // Validar superposición excluyendo el propio horario
        validateNoOverlap(
            schedule.getDoctor().getId(),
            schedule.getDayOfWeek(),
            schedule.getStartTime(),
            schedule.getEndTime(),
            scheduleId  // excluir el horario actual de la validación
        );

        // updatedAt se setea via @PreUpdate en la entidad
        MedicalSchedule updated = medicalScheduleRepository.save(schedule);
        logger.info("Horario actualizado: ID={}", scheduleId);

        return mapToResponse(updated);
    }

    /**
     * Obtiene todos los horarios activos de un doctor.
     * 
     * @param doctorId ID del doctor
     * @return lista de horarios activos
     * @throws ResourceNotFoundException si el doctor no existe
     */
    @Transactional(readOnly = true)
    public List<MedicalScheduleResponse> getDoctorSchedules(Long doctorId) {
        logger.debug("Obteniendo horarios del doctor ID={}", doctorId);

        // Validar que el doctor exista
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor", doctorId);
        }

        return medicalScheduleRepository.findByDoctorIdAndActive(doctorId, true)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Obtiene los horarios activos de un doctor para un día específico.
     * RN-38: Se usa para validar disponibilidad de citas.
     * 
     * @param doctorId ID del doctor
     * @param dayOfWeek día de la semana
     * @return horarios activos para ese día
     * @throws ResourceNotFoundException si el doctor no existe
     */
    @Transactional(readOnly = true)
    public List<MedicalScheduleResponse> getDoctorScheduleByDay(Long doctorId, DayOfWeek dayOfWeek) {
        logger.debug("Obteniendo horario del doctor ID={}, día={}", doctorId, dayOfWeek);

        // Validar que el doctor exista
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor", doctorId);
        }

        return medicalScheduleRepository.findByDoctorIdAndDayOfWeekAndActive(doctorId, dayOfWeek, true)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Desactiva un horario (soft delete).
     * 
     * @param scheduleId ID del horario a desactivar
     * @throws ResourceNotFoundException si el horario no existe
     */
    public void deactivate(Long scheduleId) {
        logger.info("Desactivando horario ID={}", scheduleId);

        MedicalSchedule schedule = medicalScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Horario médico", scheduleId));

        schedule.setActive(false);
        // updatedAt se setea via @PreUpdate en la entidad

        medicalScheduleRepository.save(schedule);
        logger.info("Horario desactivado: ID={}, doctor={}", scheduleId, schedule.getDoctor().getId());
    }

    // ===============================
    // MÉTODOS PRIVADOS DE VALIDACIÓN
    // ===============================

    /**
     * Valida que startTime sea estrictamente anterior a endTime.
     * 
     * @throws ValidationException si startTime >= endTime
     */
    private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new ValidationException(
                "La hora de inicio (" + startTime + ") debe ser estrictamente anterior " +
                "a la hora de fin (" + endTime + ")"
            );
        }
    }

    /**
     * RN-37: Valida que no exista superposición de horarios.
     * 
     * Dos rangos [A, B) y [C, D) se superponen si A < D && C < B.
     * 
     * @param doctorId ID del doctor
     * @param dayOfWeek día de la semana
     * @param startTime hora de inicio del nuevo horario
     * @param endTime hora de fin del nuevo horario
     * @param excludeId ID del horario a excluir (para updates), o null para creates
     * @throws ValidationException si hay superposición
     */
    private void validateNoOverlap(Long doctorId, DayOfWeek dayOfWeek,
                                   LocalTime startTime, LocalTime endTime,
                                   Long excludeId) {
        List<MedicalSchedule> overlapping = medicalScheduleRepository.findOverlappingSchedules(
            doctorId, dayOfWeek, startTime, endTime, excludeId
        );

        if (!overlapping.isEmpty()) {
            MedicalSchedule conflict = overlapping.get(0);
            throw new ValidationException(
                String.format(
                    "El horario %s-%s se superpone con un horario existente (%s-%s) " +
                    "del mismo doctor el día %s",
                    startTime, endTime,
                    conflict.getStartTime(), conflict.getEndTime(),
                    dayOfWeek
                )
            );
        }
    }

    // ===============================
    // MÉTODOS PRIVADOS DE CONVERSIÓN
    // ===============================

    /**
     * Mapea entidad MedicalSchedule a DTO MedicalScheduleResponse.
     * Incluye doctorFirstName y doctorLastName.
     */
    private MedicalScheduleResponse mapToResponse(MedicalSchedule schedule) {
        return new MedicalScheduleResponse(
            schedule.getId(),
            schedule.getDoctor().getId(),
            schedule.getDoctor().getFirstName(),
            schedule.getDoctor().getLastName(),
            schedule.getDayOfWeek(),
            schedule.getStartTime(),
            schedule.getEndTime(),
            schedule.getAppointmentDurationMinutes(),
            schedule.getMaxAppointmentsPerDay(),
            schedule.getActive(),
            schedule.getNotes(),
            schedule.getCreatedAt(),
            schedule.getUpdatedAt()
        );
    }
}
