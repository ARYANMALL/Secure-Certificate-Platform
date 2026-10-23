package com.securecertificate.api.controller;

import com.securecertificate.api.dto.institution.CreateInstitutionRequest;
import com.securecertificate.api.dto.institution.InstitutionResponse;
import com.securecertificate.api.service.InstitutionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/institutions")
public class InstitutionController {

    private final InstitutionService institutionService;

    public InstitutionController(
            InstitutionService institutionService) {

        this.institutionService = institutionService;
    }

    /*
     * POST /api/institutions
     *
     * Creates a new institution.
     */
    @PostMapping
    public ResponseEntity<InstitutionResponse> create(
            @Valid @RequestBody CreateInstitutionRequest request) {

        InstitutionResponse response = institutionService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /*
     * GET /api/institutions
     *
     * Returns all institutions.
     */
    @GetMapping
    public ResponseEntity<List<InstitutionResponse>> findAll() {

        return ResponseEntity.ok(
                institutionService.findAll());
    }

    /*
     * GET /api/institutions/{id}
     *
     * Returns one institution.
     */
    @GetMapping("/{id}")
    public ResponseEntity<InstitutionResponse> findById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                institutionService.findById(id));
    }

    /*
     * PATCH /api/institutions/{id}/activate
     *
     * Changes:
     *
     * PENDING → ACTIVE
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<InstitutionResponse> activate(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                institutionService.activate(id));
    }
}