package com.aviva.appointmentsystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

/**
 * Entidad para la relación Paciente-Seguro
 * Un paciente puede tener múltiples seguros
 * Permite asociar número de póliza y datos específicos del paciente
 */
@Entity
@Table(name = "patient_insurances", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"patient_id", "insurance_id"}))
public class PatientInsurance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(optional = false)
    @JoinColumn(name = "insurance_id", nullable = false)
    private Insurance insurance;

    /**
     * Número de póliza del paciente
     * Identificador único en el sistema del seguro
     */
    @Column(nullable = false, length = 50)
    private String policyNumber;

    /**
     * Nombre del titular de la póliza
     * Puede ser diferente al paciente
     */
    @Column(length = 100)
    private String policyHolderName;

    /**
     * Relación del paciente con el titular
     * (SELF, SPOUSE, CHILD, PARENT, etc.)
     */
    @Column(length = 50)
    private String relationshipToHolder;

    /**
     * Indica si es el seguro activo/primario
     * RN-25: Se usa este para calcular costos
     */
    @Column(nullable = false)
    private Boolean isPrimary = false;

    /**
     * Fecha de inicio de vigencia
     */
    @Column(nullable = false)
    private LocalDateTime effectiveDate;

    /**
     * Fecha de fin de vigencia
     */
    @Column(nullable = false)
    private LocalDateTime expirationDate;

    /**
     * Indica si la póliza está activa
     * RN-07: Validar que esté activa antes de usar
     */
    @Column(nullable = false)
    private Boolean active = true;

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
    
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    
    public Insurance getInsurance() { return insurance; }
    public void setInsurance(Insurance insurance) { this.insurance = insurance; }
    
    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }
    
    public String getPolicyHolderName() { return policyHolderName; }
    public void setPolicyHolderName(String policyHolderName) { this.policyHolderName = policyHolderName; }
    
    public String getRelationshipToHolder() { return relationshipToHolder; }
    public void setRelationshipToHolder(String relationshipToHolder) { this.relationshipToHolder = relationshipToHolder; }
    
    public Boolean getIsPrimary() { return isPrimary; }
    public void setIsPrimary(Boolean isPrimary) { this.isPrimary = isPrimary; }
    
    public LocalDateTime getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDateTime effectiveDate) { this.effectiveDate = effectiveDate; }
    
    public LocalDateTime getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDateTime expirationDate) { this.expirationDate = expirationDate; }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
