package com.aviva.appointmentsystem.repository;

import com.aviva.appointmentsystem.entity.Appointment;
import com.aviva.appointmentsystem.entity.AppointmentStatus;
import com.aviva.appointmentsystem.entity.Doctor;
import com.aviva.appointmentsystem.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Appointment.
 *
 * Consultas optimizadas:
 * - findByPatientId → Citas de un paciente (sin cargar entidad Patient)
 * - findByDoctorId → Citas de un doctor (sin cargar entidad Doctor)
 * - findByStatus → Citas por estado
 * - findActiveConflict → RN-12: Detecta cita exacta en ese datetime para ese doctor
 * - findByDoctorIdAndDateRange → Slots ocupados en una fecha (para available-slots)
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /** Citas de un paciente (por objeto) */
    List<Appointment> findByPatient(Patient patient);

    /** Citas de un doctor (por objeto) */
    List<Appointment> findByDoctor(Doctor doctor);

    /** Citas por estado */
    List<Appointment> findByStatus(AppointmentStatus status);

    /** Citas de un paciente filtradas por estado */
    List<Appointment> findByPatientAndStatus(Patient patient, AppointmentStatus status);

    /** Citas de un doctor filtradas por estado */
    List<Appointment> findByDoctorAndStatus(Doctor doctor, AppointmentStatus status);

    /** Citas de un doctor por ID (sin cargar la entidad Doctor) */
    List<Appointment> findByDoctorId(Long doctorId);

    /** Citas de un paciente por ID (sin cargar la entidad Patient) */
    List<Appointment> findByPatientId(Long patientId);

    /**
     * Busca una cita únicamente cuando pertenece al paciente indicado.
     * Se usa en el portal para validar ownership sin exponer citas ajenas.
     */
    Optional<Appointment> findByIdAndPatientId(Long id, Long patientId);

    /**
     * RN-12: Detecta conflicto exacto de horario para un doctor.
     *
     * Un conflicto existe si el doctor ya tiene una cita activa
     * (PENDING, CONFIRMED o RESCHEDULED) en ese datetime exacto.
     *
     * Excluye opcionalmente una cita por ID (para no auto-conflictuar en reschedule).
     *
     * @param doctorId ID del doctor
     * @param appointmentDateTime fecha y hora exacta
     * @param excludeId ID de cita a excluir (null para create, ID para reschedule)
     */
    @Query("""
        SELECT a FROM Appointment a
        WHERE a.doctor.id = :doctorId
          AND a.appointmentDateTime = :appointmentDateTime
          AND a.status IN (
              com.aviva.appointmentsystem.entity.AppointmentStatus.PENDING,
              com.aviva.appointmentsystem.entity.AppointmentStatus.CONFIRMED,
              com.aviva.appointmentsystem.entity.AppointmentStatus.RESCHEDULED
          )
          AND (:excludeId IS NULL OR a.id <> :excludeId)
        """)
    List<Appointment> findActiveConflict(
            @Param("doctorId") Long doctorId,
            @Param("appointmentDateTime") LocalDateTime appointmentDateTime,
            @Param("excludeId") Long excludeId);

    /**
     * Obtiene los datetimes de citas activas de un doctor en una fecha específica.
     * Usado para calcular slots disponibles (available-slots endpoint).
     *
     * Solo incluye citas PENDING, CONFIRMED o RESCHEDULED (no CANCELLED, COMPLETED, NO_SHOW).
     *
     * @param doctorId ID del doctor
     * @param start inicio del día (00:00:00)
     * @param end fin del día (23:59:59)
     */
    @Query("""
        SELECT a FROM Appointment a
        WHERE a.doctor.id = :doctorId
          AND a.appointmentDateTime >= :start
          AND a.appointmentDateTime <= :end
          AND a.status IN (
              com.aviva.appointmentsystem.entity.AppointmentStatus.PENDING,
              com.aviva.appointmentsystem.entity.AppointmentStatus.CONFIRMED,
              com.aviva.appointmentsystem.entity.AppointmentStatus.RESCHEDULED
          )
        """)
    List<Appointment> findActiveAppointmentsByDoctorAndDate(
            @Param("doctorId") Long doctorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Queries legacy (mantenidas para compatibilidad con otros servicios)
    List<Appointment> findByAppointmentDateTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Appointment> findByDoctorAndAppointmentDateTimeBetween(Doctor doctor, LocalDateTime start, LocalDateTime end);
    List<Appointment> findByDoctorAndAppointmentDateTimeBetweenAndStatusNot(
            Doctor doctor, LocalDateTime start, LocalDateTime end, AppointmentStatus status);
}
