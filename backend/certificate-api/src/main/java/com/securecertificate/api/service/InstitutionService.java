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
import java.util.Locale;
import java.util.UUID;

@Service
public class InstitutionService {

        private final InstitutionRepository institutionRepository;

        public InstitutionService(
                        InstitutionRepository institutionRepository) {

                this.institutionRepository = institutionRepository;
        }

        /*
         * Create institution application.
         *
         * This method is intentionally available to the
         * public registration endpoint.
         *
         * New institutions remain PENDING until an ADMIN
         * activates them.
         */
        @Transactional
        public InstitutionResponse create(
                        CreateInstitutionRequest request) {

                String registrationNumber = request.registrationNumber()
                                .trim();

                if (institutionRepository
                                .findByRegistrationNumber(
                                                registrationNumber)
                                .isPresent()) {

                        throw new DuplicateResourceException(
                                        "Institution with registration number '"
                                                        + registrationNumber
                                                        + "' already exists");
                }

                String email = request.email()
                                .trim()
                                .toLowerCase(Locale.ROOT);

                Institution institution = new Institution(
                                request.name().trim(),
                                registrationNumber,
                                email,
                                request.address().trim());

                Institution savedInstitution = institutionRepository.save(
                                institution);

                return toResponse(savedInstitution);
        }

        /*
         * ADMIN: get all institutions.
         */
        @PreAuthorize("hasRole('ADMIN')")
        @Transactional(readOnly = true)
        public List<InstitutionResponse> findAll() {

                return institutionRepository
                                .findAll()
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        /*
         * ADMIN: get one institution.
         */
        @PreAuthorize("hasRole('ADMIN')")
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
         * ADMIN: activate institution.
         *
         * PENDING → ACTIVE
         */
        @PreAuthorize("hasRole('ADMIN')")
        @Transactional
        public InstitutionResponse activate(
                        UUID id) {

                Institution institution = institutionRepository
                                .findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Institution not found: "
                                                                + id));

                institution.activate();

                return toResponse(institution);
        }

        /*
         * Entity → response DTO.
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