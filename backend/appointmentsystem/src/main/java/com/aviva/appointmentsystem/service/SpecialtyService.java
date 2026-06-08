package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.SpecialtyRequest;
import com.aviva.appointmentsystem.dto.SpecialtyResponse;
import com.aviva.appointmentsystem.entity.Specialty;
import com.aviva.appointmentsystem.exception.ResourceAlreadyExistsException;
import com.aviva.appointmentsystem.exception.ResourceNotFoundException;
import com.aviva.appointmentsystem.repository.SpecialtyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para gestionar especialidades médicas.
 * 
 * Responsabilidades (Service Layer):
 * - TODA la lógica de negocio reside aquí
 * - Validaciones de unicidad (nombre no duplicado)
 * - Búsqueda por ID con excepción si no existe
 * - Conversión Entity → DTO (mapToResponse)
 * 
 * El Controller NO hace lógica, solo delega aquí.
 */
@Service
@Transactional
public class SpecialtyService {

    private static final Logger logger = LoggerFactory.getLogger(SpecialtyService.class);

    private final SpecialtyRepository specialtyRepository;

    public SpecialtyService(SpecialtyRepository specialtyRepository) {
        this.specialtyRepository = specialtyRepository;
    }

    /**
     * Crea una nueva especialidad.
     * Valida que no exista otra con el mismo nombre (case-insensitive).
     */
    public SpecialtyResponse create(SpecialtyRequest request) {
        logger.info("Creando nueva especialidad: {}", request.name());

        // Validar unicidad del nombre (case-insensitive)
        if (specialtyRepository.findByNameIgnoreCase(request.name()).isPresent()) {
            logger.warn("La especialidad '{}' ya existe", request.name());
            throw new ResourceAlreadyExistsException(
                "La especialidad '" + request.name() + "' ya existe"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        Specialty specialty = new Specialty();
        specialty.setName(request.name());
        specialty.setDescription(request.description());
        specialty.setActive(true);
        specialty.setCreatedAt(now);
        specialty.setUpdatedAt(now);

        Specialty saved = specialtyRepository.save(specialty);
        logger.info("Especialidad creada: ID={}, nombre='{}'", saved.getId(), saved.getName());

        return mapToResponse(saved);
    }

    /**
     * Actualiza una especialidad existente.
     * 
     * @param id ID de la especialidad a actualizar
     * @param request datos actualizados
     * @return especialidad actualizada
     * @throws ResourceNotFoundException si el ID no existe
     * @throws ResourceAlreadyExistsException si el nuevo nombre ya está en uso por OTRA especialidad
     */
    public SpecialtyResponse update(Long id, SpecialtyRequest request) {
        logger.info("Actualizando especialidad ID={}", id);

        // 1. Buscar la especialidad o lanzar 404
        Specialty specialty = specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Especialidad", id));

        // 2. Si cambió el nombre, verificar que no exista otra con el mismo nombre
        if (!specialty.getName().equalsIgnoreCase(request.name())) {
            specialtyRepository.findByNameIgnoreCase(request.name()).ifPresent(existing -> {
                throw new ResourceAlreadyExistsException(
                    "La especialidad '" + request.name() + "' ya existe"
                );
            });
        }

        // 3. Actualizar campos
        specialty.setName(request.name());
        specialty.setDescription(request.description());
        specialty.setUpdatedAt(LocalDateTime.now());

        Specialty updated = specialtyRepository.save(specialty);
        logger.info("Especialidad actualizada: ID={}", id);

        return mapToResponse(updated);
    }

    /**
     * Obtiene una especialidad por ID.
     * 
     * @param id ID de la especialidad
     * @return especialidad encontrada
     * @throws ResourceNotFoundException si el ID no existe
     */
    @Transactional(readOnly = true)
    public SpecialtyResponse getById(Long id) {
        logger.debug("Obteniendo especialidad ID={}", id);

        Specialty specialty = specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Especialidad", id));

        return mapToResponse(specialty);
    }

    /**
     * Lista todas las especialidades activas.
     * 
     * Usa findByActiveTrue() para filtrar directamente en la DB
     * (evita cargar todas y filtrar en Java — antipatrón).
     */
    @Transactional(readOnly = true)
    public List<SpecialtyResponse> getAll() {
        logger.debug("Obteniendo todas las especialidades activas");

        return specialtyRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Desactiva una especialidad (soft delete).
     * No la elimina físicamente de la base de datos.
     * 
     * @param id ID de la especialidad a desactivar
     * @throws ResourceNotFoundException si el ID no existe
     */
    public void deactivate(Long id) {
        logger.info("Desactivando especialidad ID={}", id);

        Specialty specialty = specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Especialidad", id));

        specialty.setActive(false);
        specialty.setUpdatedAt(LocalDateTime.now());
        specialtyRepository.save(specialty);

        logger.info("Especialidad desactivada: ID={}, nombre='{}'", id, specialty.getName());
    }

    /**
     * Mapea entidad Specialty a DTO SpecialtyResponse.
     * Método privado — la conversión es responsabilidad del Service.
     */
    private SpecialtyResponse mapToResponse(Specialty specialty) {
        return new SpecialtyResponse(
            specialty.getId(),
            specialty.getName(),
            specialty.getDescription(),
            specialty.getActive(),
            specialty.getCreatedAt(),
            specialty.getUpdatedAt()
        );
    }
}
