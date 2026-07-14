package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.PatientInsuranceResponse;
import com.aviva.appointmentsystem.dto.PortalPatientInsuranceRequest;
import com.aviva.appointmentsystem.entity.Insurance;
import com.aviva.appointmentsystem.entity.Patient;
import com.aviva.appointmentsystem.entity.PatientInsurance;
import com.aviva.appointmentsystem.entity.Role;
import com.aviva.appointmentsystem.entity.User;
import com.aviva.appointmentsystem.entity.UserStatus;
import com.aviva.appointmentsystem.exception.BusinessRuleException;
import com.aviva.appointmentsystem.exception.ResourceNotFoundException;
import com.aviva.appointmentsystem.repository.InsuranceRepository;
import com.aviva.appointmentsystem.repository.PatientInsuranceRepository;
import com.aviva.appointmentsystem.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientInsuranceServicePatientPortalTest {

    @Mock private PatientInsuranceRepository patientInsuranceRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private InsuranceRepository insuranceRepository;

    @InjectMocks private PatientInsuranceService service;

    @Test
    void linksFirstInsuranceToPatientResolvedFromAuthenticatedUsername() {
        Patient patient = activePatient(41L);
        Insurance insurance = activeInsurance(7L);
        PortalPatientInsuranceRequest request = new PortalPatientInsuranceRequest(
                7L,
                " POL-123 ",
                "",
                "",
                LocalDate.now().minusMonths(1),
                LocalDate.now().plusYears(1)
        );

        when(patientRepository.findByUserUsername("patient-user"))
                .thenReturn(Optional.of(patient));
        when(patientRepository.findByIdForUpdate(41L)).thenReturn(Optional.of(patient));
        when(insuranceRepository.findById(7L)).thenReturn(Optional.of(insurance));
        when(patientInsuranceRepository.findByPatientAndActive(patient, true))
                .thenReturn(List.of());
        when(patientInsuranceRepository.findByPatientAndInsurance(patient, insurance))
                .thenReturn(Optional.empty());
        when(patientInsuranceRepository.save(any(PatientInsurance.class)))
                .thenAnswer(invocation -> {
                    PatientInsurance value = invocation.getArgument(0);
                    value.setId(80L);
                    return value;
                });

        PatientInsuranceResponse response = service
                .linkInsuranceForCurrentPatient("patient-user", request);

        ArgumentCaptor<PatientInsurance> captor =
                ArgumentCaptor.forClass(PatientInsurance.class);
        verify(patientInsuranceRepository).save(captor.capture());
        PatientInsurance saved = captor.getValue();

        assertEquals(patient, saved.getPatient());
        assertEquals("POL-123", saved.getPolicyNumber());
        assertEquals("Ana Portal", saved.getPolicyHolderName());
        assertEquals("SELF", saved.getRelationshipToHolder());
        assertTrue(saved.getIsPrimary());
        assertTrue(saved.getActive());
        assertEquals(41L, response.patientId());
    }

    @Test
    void rejectsSecondActiveInsuranceFromPortal() {
        Patient patient = activePatient(41L);
        Insurance insurance = activeInsurance(7L);
        PatientInsurance existing = new PatientInsurance();
        existing.setPatient(patient);
        existing.setInsurance(insurance);

        when(patientRepository.findByUserUsername("patient-user"))
                .thenReturn(Optional.of(patient));
        when(patientRepository.findByIdForUpdate(41L)).thenReturn(Optional.of(patient));
        when(insuranceRepository.findById(7L)).thenReturn(Optional.of(insurance));
        when(patientInsuranceRepository.findByPatientAndActive(patient, true))
                .thenReturn(List.of(existing));

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> service.linkInsuranceForCurrentPatient(
                        "patient-user",
                        validRequest(7L)
                )
        );

        assertEquals("PATIENT_INSURANCE_ALREADY_LINKED", exception.getCode());
        verify(patientInsuranceRepository, never()).save(any());
    }

    @Test
    void doesNotRevealOrDeleteInsuranceOwnedByAnotherPatient() {
        Patient patient = activePatient(41L);
        when(patientRepository.findByUserUsername("patient-user"))
                .thenReturn(Optional.of(patient));
        when(patientRepository.findByIdForUpdate(41L)).thenReturn(Optional.of(patient));
        when(patientInsuranceRepository.findOwnedByIdForUpdate(99L, patient))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.unlinkInsuranceForCurrentPatient("patient-user", 99L)
        );

        verify(patientInsuranceRepository, never()).save(any());
    }

    private PortalPatientInsuranceRequest validRequest(Long insuranceId) {
        return new PortalPatientInsuranceRequest(
                insuranceId,
                "POL-123",
                "Ana Portal",
                "SELF",
                LocalDate.now().minusMonths(1),
                LocalDate.now().plusYears(1)
        );
    }

    private Patient activePatient(Long id) {
        User user = new User();
        user.setUsername("patient-user");
        user.setRole(Role.PATIENT);
        user.setStatus(UserStatus.ACTIVE);

        Patient patient = new Patient();
        patient.setId(id);
        patient.setFirstName("Ana");
        patient.setLastName("Portal");
        patient.setStatus(UserStatus.ACTIVE);
        patient.setUser(user);
        return patient;
    }

    private Insurance activeInsurance(Long id) {
        Insurance insurance = new Insurance();
        insurance.setId(id);
        insurance.setName("Seguro Demo");
        insurance.setActive(true);
        return insurance;
    }
}
