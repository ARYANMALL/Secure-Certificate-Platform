package com.securecertificate.api.controller;

import com.securecertificate.api.dto.institution.CreateInstitutionRequest;
import com.securecertificate.api.dto.institution.InstitutionResponse;
import com.securecertificate.api.entity.InstitutionStatus;
import com.securecertificate.api.repository.InstitutionRepository;
import com.securecertificate.api.service.InstitutionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/institutions")
public class PublicInstitutionController {

        private final InstitutionRepository institutionRepository;
        private final InstitutionService institutionService;

        public PublicInstitutionController(
                        InstitutionRepository institutionRepository,
                        InstitutionService institutionService) {

                this.institutionRepository = institutionRepository;

                this.institutionService = institutionService;
        }

        /*
         * Public list of active institutions.
         *
         * Used by student registration.
         */
        @GetMapping
        public ResponseEntity<List<InstitutionOption>> getActiveInstitutions() {

                List<InstitutionOption> institutions = institutionRepository
                                .findByStatusOrderByNameAsc(
                                                InstitutionStatus.ACTIVE)
                                .stream()
                                .map(institution -> new InstitutionOption(
                                                institution.getId(),
                                                institution.getName()))
                                .toList();

                return ResponseEntity.ok(institutions);
        }

        /*
         * Public institution application.
         *
         * A newly submitted institution remains
         * PENDING until an ADMIN activates it.
         */
        @PostMapping
        public ResponseEntity<InstitutionResponse> registerInstitution(
                        @Valid @RequestBody CreateInstitutionRequest request) {

                InstitutionResponse response = institutionService.create(request);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(response);
        }

        public record InstitutionOption(
                        UUID id,
                        String name) {
        }
}