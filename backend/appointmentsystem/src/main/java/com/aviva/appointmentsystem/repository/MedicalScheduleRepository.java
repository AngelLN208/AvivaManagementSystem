package com.aviva.appointmentsystem.repository;

import com.aviva.appointmentsystem.entity.Doctor;
import com.aviva.appointmentsystem.entity.MedicalSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/**
 * Repositorio para gestionar horarios médicos.
 * 
 * Consultas:
 * - findByDoctorAndActive → Horarios activos de un doctor
 * - findByDoctorAndDayOfWeekAndActive → Horarios por día
 * - findOverlappingSchedules → RN-37: Detectar cruces de horario
 */
@Repository
public interface MedicalScheduleRepository extends JpaRepository<MedicalSchedule, Long> {

    /** Lista todos los horarios de un doctor */
    List<MedicalSchedule> findByDoctor(Doctor doctor);

    /** Lista horarios activos de un doctor */
    List<MedicalSchedule> findByDoctorAndActive(Doctor doctor, Boolean active);

    /** Lista horarios por doctor y día */
    List<MedicalSchedule> findByDoctorAndDayOfWeek(Doctor doctor, DayOfWeek dayOfWeek);

    /** Lista horarios activos de un doctor por día */
    List<MedicalSchedule> findByDoctorAndDayOfWeekAndActive(Doctor doctor, DayOfWeek dayOfWeek, Boolean active);

    /** Lista horarios activos de un doctor por ID de doctor */
    List<MedicalSchedule> findByDoctorIdAndActive(Long doctorId, Boolean active);

    /** Lista horarios activos de un doctor por ID de doctor y día */
    List<MedicalSchedule> findByDoctorIdAndDayOfWeekAndActive(Long doctorId, DayOfWeek dayOfWeek, Boolean active);

    /**
     * RN-37: Detecta cruces de horario para un doctor en un día específico.
     * 
     * Dos horarios se superponen si:
     * - El nuevo empieza antes de que termine el existente, Y
     * - El nuevo termina después de que empiece el existente.
     * 
     * Opcionalmente excluye un horario por ID (para validar en update sin
     * que el horario actual se detecte como "conflicto consigo mismo").
     */
    @Query("""
        SELECT ms FROM MedicalSchedule ms
        WHERE ms.doctor.id = :doctorId
          AND ms.dayOfWeek = :dayOfWeek
          AND ms.active = true
          AND ms.startTime < :endTime
          AND ms.endTime > :startTime
          AND (:excludeId IS NULL OR ms.id <> :excludeId)
        """)
    List<MedicalSchedule> findOverlappingSchedules(
            @Param("doctorId") Long doctorId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId);
}
