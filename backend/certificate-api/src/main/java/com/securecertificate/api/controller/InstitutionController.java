package com.securecertificate.api.controller;

import com.securecertificate.api.dto.institution.InstitutionResponse;
import com.securecertificate.api.service.InstitutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/institutions")
@PreAuthorize("hasRole('ADMIN')")
public class InstitutionController {

    private final InstitutionService institutionService;

    public InstitutionController(
            InstitutionService institutionService) {

        this.institutionService = institutionService;
    }

    /*
     * ADMIN: view all institutions.
     */
    @GetMapping
    public ResponseEntity<List<InstitutionResponse>> findAll() {

        return ResponseEntity.ok(
                institutionService.findAll());
    }

    /*
     * ADMIN: view one institution.
     */
    @GetMapping("/{id}")
    public ResponseEntity<InstitutionResponse> findById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                institutionService.findById(id));
    }

    /*
     * ADMIN: approve/activate institution.
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