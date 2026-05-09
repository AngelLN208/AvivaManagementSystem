package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.MedicalScheduleRequest;
import com.aviva.appointmentsystem.dto.MedicalScheduleResponse;
import com.aviva.appointmentsystem.entity.Doctor;
import com.aviva.appointmentsystem.entity.MedicalSchedule;
import com.aviva.appointmentsystem.exception.ResourceNotFoundException;
import com.aviva.appointmentsystem.exception.ValidationException;
import com.aviva.appointmentsystem.repository.DoctorRepository;
import com.aviva.appointmentsystem.repository.MedicalScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para gestionar horarios de doctores
 * RN-37, RN-38: Validación de disponibilidad y horarios
 */
@Service
@Transactional
public class MedicalScheduleService {

    private static final Logger logger = LoggerFactory.getLogger(MedicalScheduleService.class);

    @Autowired
    private MedicalScheduleRepository medicalScheduleRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    /**
     * Crea un horario para un doctor
     * RN-37: Define los horarios en que el doctor atiende
     */
    public MedicalScheduleResponse create(Long doctorId, MedicalScheduleRequest request) {
        logger.info("Creando horario para doctor: ID={}, día={}", doctorId, request.dayOfWeek());

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        // Validar que la hora de fin sea posterior a la de inicio
        if (request.endTime().isBefore(request.startTime())) {
            throw new ValidationException("La hora de fin debe ser posterior a la de inicio");
        }

        // Validar duración mínima
        if (request.appointmentDurationMinutes() < 15) {
            throw new ValidationException("La duración mínima de cita es 15 minutos");
        }

        MedicalSchedule schedule = new MedicalSchedule();
        schedule.setDoctor(doctor);
        schedule.setDayOfWeek(request.dayOfWeek());
        schedule.setStartTime(request.startTime());
        schedule.setEndTime(request.endTime());
        schedule.setAppointmentDurationMinutes(request.appointmentDurationMinutes());
        schedule.setMaxAppointmentsPerDay(request.maxAppointmentsPerDay());
        schedule.setActive(true);
        schedule.setNotes(request.notes());
        schedule.setCreatedAt(LocalDateTime.now());
        schedule.setUpdatedAt(LocalDateTime.now());

        MedicalSchedule saved = medicalScheduleRepository.save(schedule);
        logger.info("Horario creado para doctor: ID={}", saved.getId());

        return mapToResponse(saved);
    }

    /**
     * Obtiene todos los horarios de un doctor
     */
    @Transactional(readOnly = true)
    public List<MedicalScheduleResponse> getDoctorSchedules(Long doctorId) {
        logger.debug("Obteniendo horarios del doctor: ID={}", doctorId);

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        return medicalScheduleRepository.findByDoctorAndActive(doctor, true)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Obtiene el horario de un doctor para un día específico
     * RN-38: Se usa para validar disponibilidad de citas
     */
    @Transactional(readOnly = true)
    public List<MedicalScheduleResponse> getDoctorScheduleByDay(Long doctorId, DayOfWeek dayOfWeek) {
        logger.debug("Obteniendo horario del doctor: ID={}, día={}", doctorId, dayOfWeek);

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        return medicalScheduleRepository.findByDoctorAndDayOfWeekAndActive(doctor, dayOfWeek, true)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Actualiza un horario
     */
    public MedicalScheduleResponse update(Long scheduleId, MedicalScheduleRequest request) {
        logger.info("Actualizando horario: ID={}", scheduleId);

        MedicalSchedule schedule = medicalScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Horario", scheduleId));

        if (request.endTime().isBefore(request.startTime())) {
            throw new ValidationException("La hora de fin debe ser posterior a la de inicio");
        }

        schedule.setDayOfWeek(request.dayOfWeek());
        schedule.setStartTime(request.startTime());
        schedule.setEndTime(request.endTime());
        schedule.setAppointmentDurationMinutes(request.appointmentDurationMinutes());
        schedule.setMaxAppointmentsPerDay(request.maxAppointmentsPerDay());
        schedule.setNotes(request.notes());
        schedule.setUpdatedAt(LocalDateTime.now());

        MedicalSchedule updated = medicalScheduleRepository.save(schedule);
        logger.info("Horario actualizado: ID={}", scheduleId);

        return mapToResponse(updated);
    }

    /**
     * Desactiva un horario
     */
    public void deactivate(Long scheduleId) {
        logger.info("Desactivando horario: ID={}", scheduleId);

        MedicalSchedule schedule = medicalScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Horario", scheduleId));

        schedule.setActive(false);
        schedule.setUpdatedAt(LocalDateTime.now());
        medicalScheduleRepository.save(schedule);

        logger.info("Horario desactivado: ID={}", scheduleId);
    }

    /**
     * Mapea entidad a DTO
     */
    private MedicalScheduleResponse mapToResponse(MedicalSchedule schedule) {
        return new MedicalScheduleResponse(
            schedule.getId(),
            schedule.getDoctor().getId(),
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
