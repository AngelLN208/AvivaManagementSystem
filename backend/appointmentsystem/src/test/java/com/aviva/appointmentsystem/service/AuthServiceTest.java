package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.RegisterPatientRequest;
import com.aviva.appointmentsystem.entity.Patient;
import com.aviva.appointmentsystem.entity.Role;
import com.aviva.appointmentsystem.entity.User;
import com.aviva.appointmentsystem.exception.ValidationException;
import com.aviva.appointmentsystem.repository.PatientRepository;
import com.aviva.appointmentsystem.repository.UserRepository;
import com.aviva.appointmentsystem.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;

    @Test
    void registerPatientLinksPortalUserWithPatientProfile() {
        AuthService service = new AuthService(
                userRepository,
                patientRepository,
                passwordEncoder,
                jwtUtil
        );

        RegisterPatientRequest request = new RegisterPatientRequest(
                "maria.portal",
                "secret123",
                "76543210",
                "María",
                "Torres",
                "FEMALE",
                "1995-04-10",
                "999888777",
                "maria@example.com",
                "Lima"
        );

        when(userRepository.findByUsername(request.username())).thenReturn(Optional.empty());
        when(patientRepository.findByDni(request.dni())).thenReturn(Optional.empty());
        when(patientRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(10L);
            return user;
        });
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> {
            Patient patient = invocation.getArgument(0);
            patient.setId(20L);
            return patient;
        });

        service.registerPatient(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<Patient> patientCaptor = ArgumentCaptor.forClass(Patient.class);
        verify(userRepository).save(userCaptor.capture());
        verify(patientRepository).save(patientCaptor.capture());

        User savedUser = userCaptor.getValue();
        Patient savedPatient = patientCaptor.getValue();

        assertEquals(Role.PATIENT, savedUser.getRole());
        assertEquals("encoded-password", savedUser.getPassword());
        assertSame(savedUser, savedPatient.getUser());
    }

    @Test
    void registerPatientRejectsFutureDateOfBirth() {
        AuthService service = new AuthService(
                userRepository,
                patientRepository,
                passwordEncoder,
                jwtUtil
        );
        RegisterPatientRequest request = new RegisterPatientRequest(
                "future.patient",
                "secret123",
                "87654321",
                "Paciente",
                "Futuro",
                "OTHER",
                LocalDate.now().plusDays(1).toString(),
                "999888777",
                "future@example.com",
                "Lima"
        );

        when(userRepository.findByUsername(request.username())).thenReturn(Optional.empty());
        when(patientRepository.findByDni(request.dni())).thenReturn(Optional.empty());
        when(patientRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> service.registerPatient(request));
        verify(userRepository, never()).save(any(User.class));
        verify(patientRepository, never()).save(any(Patient.class));
    }
}
