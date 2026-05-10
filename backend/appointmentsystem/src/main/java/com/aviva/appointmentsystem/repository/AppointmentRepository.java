package com.aviva.appointmentsystem.repository;

import com.aviva.appointmentsystem.entity.Appointment;
import com.aviva.appointmentsystem.entity.AppointmentStatus;
import com.aviva.appointmentsystem.entity.Doctor;
import com.aviva.appointmentsystem.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para la entidad Appointment
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatient(Patient patient);
    List<Appointment> findByDoctor(Doctor doctor);
    List<Appointment> findByStatus(AppointmentStatus status);
    List<Appointment> findByPatientAndStatus(Patient patient, AppointmentStatus status);
    List<Appointment> findByDoctorAndStatus(Doctor doctor, AppointmentStatus status);
    List<Appointment> findByAppointmentDateTimeBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);
    List<Appointment> findByDoctorAndAppointmentDateTimeBetween(Doctor doctor, LocalDateTime startDateTime, LocalDateTime endDateTime);
    //Busca citas de un doctor en un rango de fechas, excluyendo un estado específico.
    List<Appointment> findByDoctorAndAppointmentDateTimeBetweenAndStatusNot(Doctor doctor, LocalDateTime start, LocalDateTime end, AppointmentStatus status);
}
