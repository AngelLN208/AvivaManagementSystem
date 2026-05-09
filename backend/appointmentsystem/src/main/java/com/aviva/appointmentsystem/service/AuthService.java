package com.aviva.appointmentsystem.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.aviva.appointmentsystem.entity.User;
import com.aviva.appointmentsystem.exception.InvalidCredentialsException;
import com.aviva.appointmentsystem.exception.UserInactiveException;
import com.aviva.appointmentsystem.exception.UserNotFoundException;
import com.aviva.appointmentsystem.repository.UserRepository;
import com.aviva.appointmentsystem.security.JwtUtil;

/**
 * Servicio de autenticación
 * Maneja login y generación de tokens JWT
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Autentica un usuario y devuelve un token JWT
     *
     * @param username el nombre de usuario
     * @param password la contraseña
     * @return mapa con token, rol y usuario
     * @throws UserNotFoundException si el usuario no existe
     * @throws InvalidCredentialsException si la contraseña es incorrecta
     * @throws UserInactiveException si el usuario está inactivo
     */
    public Map<String, String> login(String username, String password) {
        logger.info("Intento de login para usuario: {}", username);

        // Buscar usuario por nombre de usuario
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Usuario no encontrado: {}", username);
                    return new UserNotFoundException(username);
                });

        // Validar contraseña
        if (!passwordEncoder.matches(password, user.getPassword())) {
            logger.warn("Contraseña incorrecta para usuario: {}", username);
            throw new InvalidCredentialsException();
        }

        // Validar que el usuario esté activo
        if (user.getStatus() != com.aviva.appointmentsystem.entity.UserStatus.ACTIVE) {
            logger.warn("Usuario inactivo intenta acceder: {}", username);
            throw new UserInactiveException(username);
        }

        // Generar token JWT
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        logger.info("Login exitoso para usuario: {}", username);

        // Construir respuesta
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("role", user.getRole().name());
        response.put("username", user.getUsername());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());

        return response;
    }

    /**
     * Autentica un usuario y devuelve solo el token JWT
     *
     * @param username el nombre de usuario
     * @param password la contraseña
     * @return token JWT
     * @throws UserNotFoundException si el usuario no existe
     * @throws InvalidCredentialsException si la contraseña es incorrecta
     * @throws UserInactiveException si el usuario está inactivo
     */
    public String authenticate(String username, String password) {
        Map<String, String> loginResponse = login(username, password);
        return loginResponse.get("token");
    }
}