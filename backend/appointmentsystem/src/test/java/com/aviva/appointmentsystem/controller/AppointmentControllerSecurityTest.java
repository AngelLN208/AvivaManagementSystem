package com.aviva.appointmentsystem.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AppointmentControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void patientPortalLocalOriginPassesCorsPreflight() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5174")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        "http://localhost:5174"
                ));
    }

    @Test
    void unauthenticatedUserCannotAccessPatientPortal() throws Exception {
        mockMvc.perform(get("/api/appointments/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void patientCanAccessOwnAppointments() throws Exception {
        mockMvc.perform(get("/api/appointments/me")
                        .with(user("paciente1").roles("PATIENT")))
                .andExpect(status().isOk());
    }

    @Test
    void patientCanReachDoctorAvailabilityEndpoint() throws Exception {
        mockMvc.perform(get("/api/appointments/doctor/999/available-slots")
                        .with(user("paciente1").roles("PATIENT"))
                        .param("date", "2099-01-15"))
                .andExpect(status().isNotFound());
    }

    @Test
    void patientCanReadDoctorCatalog() throws Exception {
        mockMvc.perform(get("/api/doctors")
                        .with(user("paciente1").roles("PATIENT")))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "/api/consultations/1",
        "/api/triages/1",
        "/api/patients",
        "/api/payments",
        "/api/audit-logs/1"
    })
    void patientCannotAccessClinicalOrAdministrativeModules(String path) throws Exception {
        mockMvc.perform(get(path)
                        .with(user("paciente1").roles("PATIENT")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void patientCannotAccessLegacyAppointmentList() throws Exception {
        mockMvc.perform(get("/api/appointments")
                        .with(user("paciente1").roles("PATIENT")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void patientCannotCreateAppointmentThroughLegacyEndpoint() throws Exception {
        mockMvc.perform(post("/api/appointments")
                        .with(user("paciente1").roles("PATIENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "patientId": 1,
                                  "doctorId": 1,
                                  "appointmentDateTime": "2099-01-15T10:00:00",
                                  "reason": "Intento mediante ruta de staff"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void receptionistCanContinueUsingLegacyAppointmentList() throws Exception {
        mockMvc.perform(get("/api/appointments")
                        .with(user("recepcion1").roles("RECEPTIONIST")))
                .andExpect(status().isOk());
    }

    @Test
    void receptionistCannotUsePatientMeEndpoint() throws Exception {
        mockMvc.perform(get("/api/appointments/me")
                        .with(user("recepcion1").roles("RECEPTIONIST")))
                .andExpect(status().isForbidden());
    }
}
