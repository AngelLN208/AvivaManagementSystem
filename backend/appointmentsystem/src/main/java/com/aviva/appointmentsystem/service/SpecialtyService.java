package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.SpecialtyRequest;
import com.aviva.appointmentsystem.dto.SpecialtyResponse;
import com.aviva.appointmentsystem.entity.Specialty;
import com.aviva.appointmentsystem.exception.ResourceAlreadyExistsException;
import com.aviva.appointmentsystem.exception.ResourceNotFoundException;
import com.aviva.appointmentsystem.repository.SpecialtyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para gestionar especialidades médicas
 */
@Service
@Transactional
public class SpecialtyService {

    private static final Logger logger = LoggerFactory.getLogger(SpecialtyService.class);

    @Autowired
    private SpecialtyRepository specialtyRepository;

    /**
     * Crea una nueva especialidad
     */
    public SpecialtyResponse create(SpecialtyRequest request) {
        logger.info("Creando nueva especialidad: {}", request.name());

        // Validar que no exista ya
        if (specialtyRepository.findByName(request.name()).isPresent()) {
            logger.warn("La especialidad {} ya existe", request.name());
            throw new ResourceAlreadyExistsException("La especialidad '" + request.name() + "' ya existe");
        }

        Specialty specialty = new Specialty();
        specialty.setName(request.name());
        specialty.setDescription(request.description());
        specialty.setActive(true);
        specialty.setCreatedAt(LocalDateTime.now());
        specialty.setUpdatedAt(LocalDateTime.now());

        Specialty saved = specialtyRepository.save(specialty);
        logger.info("Especialidad creada: ID={}", saved.getId());

        return mapToResponse(saved);
    }

    /**
     * Actualiza una especialidad
     */
    public SpecialtyResponse update(Long id, SpecialtyRequest request) {
        logger.info("Actualizando especialidad ID={}", id);

        Specialty specialty = specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Especialidad", id));

        // Validar que no haya otra con el mismo nombre
        if (!specialty.getName().equalsIgnoreCase(request.name()) &&
            specialtyRepository.findByName(request.name()).isPresent()) {
            throw new ResourceAlreadyExistsException("La especialidad '" + request.name() + "' ya existe");
        }

        specialty.setName(request.name());
        specialty.setDescription(request.description());
        specialty.setUpdatedAt(LocalDateTime.now());

        Specialty updated = specialtyRepository.save(specialty);
        logger.info("Especialidad actualizada: ID={}", id);

        return mapToResponse(updated);
    }

    /**
     * Obtiene una especialidad por ID
     */
    @Transactional(readOnly = true)
    public SpecialtyResponse getById(Long id) {
        logger.debug("Obteniendo especialidad ID={}", id);

        Specialty specialty = specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Especialidad", id));

        return mapToResponse(specialty);
    }

    /**
     * Lista todas las especialidades activas
     */
    @Transactional(readOnly = true)
    public List<SpecialtyResponse> getAll() {
        logger.debug("Obteniendo todas las especialidades");
        return specialtyRepository.findAll()
                .stream()
                .filter(Specialty::getActive)
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Desactiva una especialidad
     */
    public void deactivate(Long id) {
        logger.info("Desactivando especialidad ID={}", id);

        Specialty specialty = specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Especialidad", id));

        specialty.setActive(false);
        specialty.setUpdatedAt(LocalDateTime.now());
        specialtyRepository.save(specialty);

        logger.info("Especialidad desactivada: ID={}", id);
    }

    /**
     * Mapea entidad a DTO
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
