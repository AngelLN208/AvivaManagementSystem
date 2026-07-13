package com.aviva.appointmentsystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad para aseguradoras/seguros médicos
 * Almacena información de convenios y coberturas
 * (RN-04 a RN-08: Reglas de validación de seguros)
 */
@Entity
@Table(name = "insurances")
public class Insurance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private BigDecimal coveragePercentage;

    @Column(nullable = false)
    private BigDecimal deductible;

    @Column(nullable = false)
    private BigDecimal maxCoveragePerConsultation;

    @Column(nullable = false)
    private BigDecimal maxAnnualCoverage;

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
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getCoveragePercentage() { return coveragePercentage; }
    public void setCoveragePercentage(BigDecimal coveragePercentage) { this.coveragePercentage = coveragePercentage; }
    
    public BigDecimal getDeductible() { return deductible; }
    public void setDeductible(BigDecimal deductible) { this.deductible = deductible; }
    
    public BigDecimal getMaxCoveragePerConsultation() { return maxCoveragePerConsultation; }
    public void setMaxCoveragePerConsultation(BigDecimal maxCoveragePerConsultation) { this.maxCoveragePerConsultation = maxCoveragePerConsultation; }
    
    public BigDecimal getMaxAnnualCoverage() { return maxAnnualCoverage; }
    public void setMaxAnnualCoverage(BigDecimal maxAnnualCoverage) { this.maxAnnualCoverage = maxAnnualCoverage; }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
