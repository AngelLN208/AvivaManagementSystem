package com.aviva.appointmentsystem.controller;

import com.aviva.appointmentsystem.dto.ApiResponse;
import com.aviva.appointmentsystem.dto.PatientInsuranceRequest;
import com.aviva.appointmentsystem.dto.PatientInsuranceResponse;
import com.aviva.appointmentsystem.service.PatientInsuranceService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * Controlador de seguros de pacientes
 * Gestiona la relación paciente-seguro
 */
@RestController
@RequestMapping("/api/patient-insurances")
public class PatientInsuranceController {

    private static final Logger logger = LoggerFactory.getLogger(PatientInsuranceController.class);

    @Autowired
    private PatientInsuranceService patientInsuranceService;

    /**
     * Vincula un seguro a un paciente
     * POST /api/patient-insurances/patient/{patientId}
     */
    @PostMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<PatientInsuranceResponse>> linkInsurance(
            @PathVariable Long patientId,
            @Valid @RequestBody PatientInsuranceRequest request) {
        logger.info("POST /api/patient-insurances/patient/{} - Vincular seguro", patientId);

        PatientInsuranceResponse response = patientInsuranceService.linkInsurance(patientId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Seguro vinculado al paciente exitosamente"));
    }

    /**
     * Obtiene seguros de un paciente
     * GET /api/patient-insurances/patient/{patientId}
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<PatientInsuranceResponse>>> getPatientInsurances(
            @PathVariable Long patientId) {
        logger.info("GET /api/patient-insurances/patient/{} - Obtener seguros del paciente", patientId);

        List<PatientInsuranceResponse> response = patientInsuranceService.getPatientInsurances(patientId);

        return ResponseEntity
                .ok(ApiResponse.success(response, "Seguros obtenidos: " + response.size()));
    }

    /**
     * Obtiene el seguro primario de un paciente
     * GET /api/patient-insurances/patient/{patientId}/primary
     */
    @GetMapping("/patient/{patientId}/primary")
    public ResponseEntity<ApiResponse<?>> getPrimaryInsurance(
            @PathVariable Long patientId) {
        logger.info("GET /api/patient-insurances/patient/{}/primary - Obtener seguro primario", patientId);

        Optional<PatientInsuranceResponse> response = patientInsuranceService.getPrimaryInsurance(patientId);

        if (response.isPresent()) {
            return ResponseEntity
                    .ok(ApiResponse.success(response.get(), "Seguro primario obtenido"));
        } else {
            return ResponseEntity
                    .ok(ApiResponse.success(null, "El paciente no tiene seguro primario"));
        }
    }

    /**
     * Desvincula un seguro de un paciente
     * DELETE /api/patient-insurances/{patientInsuranceId}
     */
    @DeleteMapping("/{patientInsuranceId}")
    public ResponseEntity<ApiResponse<String>> unlinkInsurance(
            @PathVariable Long patientInsuranceId) {
        logger.info("DELETE /api/patient-insurances/{} - Desvincula seguro", patientInsuranceId);

        patientInsuranceService.unlinkInsurance(patientInsuranceId);

        return ResponseEntity
                .ok(ApiResponse.success("Seguro desvinculado exitosamente"));
    }
}
