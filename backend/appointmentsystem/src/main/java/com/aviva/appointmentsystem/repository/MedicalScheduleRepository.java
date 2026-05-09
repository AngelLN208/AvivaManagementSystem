package com.aviva.appointmentsystem.repository;

import com.aviva.appointmentsystem.entity.Doctor;
import com.aviva.appointmentsystem.entity.MedicalSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

/**
 * Repositorio para gestionar horarios médicos
 */
@Repository
public interface MedicalScheduleRepository extends JpaRepository<MedicalSchedule, Long> {
    List<MedicalSchedule> findByDoctor(Doctor doctor);
    List<MedicalSchedule> findByDoctorAndActive(Doctor doctor, Boolean active);
    List<MedicalSchedule> findByDoctorAndDayOfWeek(Doctor doctor, DayOfWeek dayOfWeek);
    List<MedicalSchedule> findByDoctorAndDayOfWeekAndActive(Doctor doctor, DayOfWeek dayOfWeek, Boolean active);
}
