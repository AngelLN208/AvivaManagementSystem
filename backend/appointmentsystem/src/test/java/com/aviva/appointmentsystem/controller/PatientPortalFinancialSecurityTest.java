package com.aviva.appointmentsystem.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica la frontera HTTP entre las rutas /me del portal y las rutas legacy
 * que permiten consultar informacion financiera o de seguros de terceros.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PatientPortalFinancialSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void anonymousUserCannotReadPatientPayments() throws Exception {
        mockMvc.perform(get("/api/payments/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "/api/insurances",
        "/api/patient-insurances/me",
        "/api/payments/me",
        "/api/receipts/me"
    })
    void patientCanReadPortalCatalogAndOwnResources(String path) throws Exception {
        mockMvc.perform(get(path)
                        .with(user("paciente1").roles("PATIENT")))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "/api/patient-insurances/me",
        "/api/payments/me",
        "/api/receipts/me",
        "/api/receipts/me/1/pdf"
    })
    void receptionistCannotUsePatientMeRoutes(String path) throws Exception {
        mockMvc.perform(get(path)
                        .with(user("recepcion1").roles("RECEPTIONIST")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void patientPaymentRouteUsesTheNeutralContractAndValidatesItsBody()
            throws Exception {
        mockMvc.perform(post("/api/payments/me/1/pay")
                        .with(user("paciente1").roles("PATIENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void receptionistCannotUsePatientPaymentRoute() throws Exception {
        mockMvc.perform(post("/api/payments/me/1/pay")
                        .with(user("recepcion1").roles("RECEPTIONIST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"method\":\"DEBIT_CARD\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "/api/insurances/1",
        "/api/patient-insurances/patient/1",
        "/api/payments",
        "/api/payments/1",
        "/api/receipts",
        "/api/receipts/1"
    })
    void patientCannotUseLegacyInsuranceOrFinancialRoutes(String path) throws Exception {
        mockMvc.perform(get(path)
                        .with(user("paciente1").roles("PATIENT")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "/api/payments",
        "/api/payments/1",
        "/api/receipts",
        "/api/receipts/1"
    })
    void doctorCannotReadGlobalPaymentsOrReceipts(String path) throws Exception {
        mockMvc.perform(get(path)
                        .with(user("doctor1").roles("DOCTOR")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "/api/auth/patient-activation/request",
        "/api/auth/patient-activation/verify-code",
        "/api/auth/patient-activation/complete"
    })
    void activationStepsRemainPublicAndInvalidPayloadReachesValidation(String path)
            throws Exception {
        mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
