package com.aviva.appointmentsystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Desafio de un solo uso para activar la cuenta de un paciente ya existente.
 *
 * El codigo enviado por correo nunca se persiste en texto plano: esta entidad
 * conserva unicamente el hash generado por el PasswordEncoder.
 */
@Entity
@Table(
    name = "patient_activation_challenges",
    indexes = {
        @Index(
            name = "idx_patient_activation_patient_created",
            columnList = "patient_id, created_at"
        )
    }
)
public class PatientActivationChallenge {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "otp_hash", nullable = false, length = 100)
    private String otpHash;

    @Column(name = "failed_attempts", nullable = false)
    private Integer failedAttempts = 0;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "consumed_at")
    private LocalDateTime consumedAt;

    /** Hash del token temporal emitido despues de validar correctamente el OTP. */
    @Column(name = "activation_token_hash", length = 100)
    private String activationTokenHash;

    /** Momento en que el OTP fue validado; impide emitir mas de un token. */
    @Column(name = "code_verified_at")
    private LocalDateTime codeVerifiedAt;

    /** Limite de uso del token temporal de finalizacion. */
    @Column(name = "activation_token_expires_at")
    private LocalDateTime activationTokenExpiresAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public String getOtpHash() { return otpHash; }
    public void setOtpHash(String otpHash) { this.otpHash = otpHash; }

    public Integer getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(Integer failedAttempts) { this.failedAttempts = failedAttempts; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getConsumedAt() { return consumedAt; }
    public void setConsumedAt(LocalDateTime consumedAt) { this.consumedAt = consumedAt; }

    public String getActivationTokenHash() { return activationTokenHash; }
    public void setActivationTokenHash(String activationTokenHash) {
        this.activationTokenHash = activationTokenHash;
    }

    public LocalDateTime getCodeVerifiedAt() { return codeVerifiedAt; }
    public void setCodeVerifiedAt(LocalDateTime codeVerifiedAt) {
        this.codeVerifiedAt = codeVerifiedAt;
    }

    public LocalDateTime getActivationTokenExpiresAt() { return activationTokenExpiresAt; }
    public void setActivationTokenExpiresAt(LocalDateTime activationTokenExpiresAt) {
        this.activationTokenExpiresAt = activationTokenExpiresAt;
    }
}
