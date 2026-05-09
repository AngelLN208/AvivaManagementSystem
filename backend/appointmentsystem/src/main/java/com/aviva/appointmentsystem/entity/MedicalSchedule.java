package com.aviva.appointmentsystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Entidad para los horarios de atención de doctores
 * Define qué días y horas atiende cada doctor
 * RN-37: Los doctores deben tener horarios definidos
 * RN-38: Las citas solo pueden ser dentro de estos horarios
 */
@Entity
@Table(name = "medical_schedules")
public class MedicalSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    /**
     * Día de la semana (MONDAY, TUESDAY, etc.)
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    /**
     * Hora de inicio de atención
     * Ejemplo: 08:00
     */
    @Column(nullable = false)
    private LocalTime startTime;

    /**
     * Hora de fin de atención
     * Ejemplo: 17:00
     */
    @Column(nullable = false)
    private LocalTime endTime;

    /**
     * Duración de cada cita en minutos
     * RN-37: Define slots de 30, 45 o 60 minutos
     */
    @Column(nullable = false)
    private Integer appointmentDurationMinutes;

    /**
     * Máximo número de citas en este horario
     * Ejemplo: 20 citas de 30 min entre 08:00-17:00
     */
    @Column(nullable = false)
    private Integer maxAppointmentsPerDay;

    /**
     * Indica si está habilitado
     * Permite desactivar temporalmente sin eliminar
     */
    @Column(nullable = false)
    private Boolean active = true;

    /**
     * Notas especiales
     * Ejemplo: "Almuerzo de 12:00-13:00", "Solo urgencias"
     */
    @Column(length = 500)
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
    
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    
    public Integer getAppointmentDurationMinutes() { return appointmentDurationMinutes; }
    public void setAppointmentDurationMinutes(Integer appointmentDurationMinutes) { this.appointmentDurationMinutes = appointmentDurationMinutes; }
    
    public Integer getMaxAppointmentsPerDay() { return maxAppointmentsPerDay; }
    public void setMaxAppointmentsPerDay(Integer maxAppointmentsPerDay) { this.maxAppointmentsPerDay = maxAppointmentsPerDay; }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
