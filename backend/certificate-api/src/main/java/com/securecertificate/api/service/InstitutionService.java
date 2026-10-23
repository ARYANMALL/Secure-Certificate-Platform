package com.securecertificate.api.service;

import com.securecertificate.api.dto.institution.CreateInstitutionRequest;
import com.securecertificate.api.dto.institution.InstitutionResponse;
import com.securecertificate.api.entity.Institution;
import com.securecertificate.api.exception.DuplicateResourceException;
import com.securecertificate.api.exception.ResourceNotFoundException;
import com.securecertificate.api.repository.InstitutionRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class InstitutionService {

    private final InstitutionRepository institutionRepository;

    public InstitutionService(
            InstitutionRepository institutionRepository) {

        this.institutionRepository = institutionRepository;
    }

    /*
     * Create a new institution.
     */
    @Transactional
    public InstitutionResponse create(
            CreateInstitutionRequest request) {

        String registrationNumber = request.registrationNumber().trim();

        /*
         * Check whether the registration number
         * is already being used.
         */
        if (institutionRepository
                .findByRegistrationNumber(
                        registrationNumber)
                .isPresent()) {

            throw new DuplicateResourceException(
                    "Institution with registration number '"
                            + registrationNumber
                            + "' already exists");
        }

        /*
         * Convert the request DTO into
         * our JPA entity.
         */
        Institution institution = new Institution(
                request.name().trim(),
                registrationNumber,
                request.email(),
                request.address());

        /*
         * Save the entity to PostgreSQL.
         */
        Institution savedInstitution = institutionRepository.save(
                institution);

        /*
         * Convert entity into response DTO.
         */
        return toResponse(savedInstitution);
    }

    /*
     * Get all institutions.
     */
    @Transactional(readOnly = true)
    public List<InstitutionResponse> findAll() {

        return institutionRepository
                .findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /*
     * Get one institution by UUID.
     */
    @Transactional(readOnly = true)
    public InstitutionResponse findById(
            UUID id) {

        Institution institution = institutionRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Institution not found: "
                                + id));

        return toResponse(institution);
    }

    /*
     * Only an ADMIN can activate
     * an institution.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public InstitutionResponse activate(
            UUID id) {

        /*
         * Find the institution.
         */
        Institution institution = institutionRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Institution not found: "
                                + id));

        /*
         * Change status:
         *
         * PENDING → ACTIVE
         */
        institution.activate();

        /*
         * Because the entity is inside a transaction,
         * JPA will persist the change.
         */
        return toResponse(institution);
    }

    /*
     * Converts the database entity into
     * the API response DTO.
     */
    private InstitutionResponse toResponse(
            Institution institution) {

        return new InstitutionResponse(
                institution.getId(),
                institution.getName(),
                institution.getRegistrationNumber(),
                institution.getEmail(),
                institution.getAddress(),
                institution.getStatus(),
                institution.getCreatedAt(),
                institution.getUpdatedAt());
    }
}