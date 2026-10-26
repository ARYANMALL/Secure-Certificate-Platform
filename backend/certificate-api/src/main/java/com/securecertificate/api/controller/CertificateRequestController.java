package com.securecertificate.api.controller;

import com.securecertificate.api.dto.certificate.CertificateRequestResponse;
import com.securecertificate.api.dto.certificate.CreateCertificateRequestRequest;
import com.securecertificate.api.service.CertificateRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/certificate-requests")
public class CertificateRequestController {

    private final CertificateRequestService requestService;

    public CertificateRequestController(
            CertificateRequestService requestService) {

        this.requestService = requestService;
    }

    /*
     * STUDENT
     * POST /api/certificate-requests
     */
    @PostMapping
    public ResponseEntity<CertificateRequestResponse> create(
            @Valid @RequestBody CreateCertificateRequestRequest request,

            JwtAuthenticationToken authentication) {

        UUID studentId = extractUserId(authentication);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        requestService.create(
                                studentId,
                                request));
    }

    /*
     * STUDENT
     * GET /api/certificate-requests/my
     */
    @GetMapping("/my")
    public ResponseEntity<List<CertificateRequestResponse>> myRequests(
            JwtAuthenticationToken authentication) {

        UUID studentId = extractUserId(authentication);

        return ResponseEntity.ok(
                requestService
                        .findForStudent(studentId));
    }

    /*
     * ISSUER
     * GET /api/certificate-requests/issuer
     */
    @GetMapping("/issuer")
    public ResponseEntity<List<CertificateRequestResponse>> issuerRequests(
            JwtAuthenticationToken authentication) {

        UUID issuerId = extractUserId(authentication);

        return ResponseEntity.ok(
                requestService
                        .findForIssuer(issuerId));
    }

    /*
     * ISSUER
     * PATCH /api/certificate-requests/{id}/accept
     */
    @PatchMapping("/{id}/accept")
    public ResponseEntity<CertificateRequestResponse> accept(
            @PathVariable UUID id,
            JwtAuthenticationToken authentication) {

        UUID issuerId = extractUserId(authentication);

        return ResponseEntity.ok(
                requestService.acceptByIssuer(
                        id,
                        issuerId));
    }

    /*
     * ADMIN
     * GET /api/certificate-requests/admin
     */
    @GetMapping("/admin")
    public ResponseEntity<List<CertificateRequestResponse>> adminRequests() {

        return ResponseEntity.ok(
                requestService.findForAdmin());
    }

    /*
     * ADMIN
     * PATCH /api/certificate-requests/{id}/approve
     */
    @PatchMapping("/{id}/approve")
    public ResponseEntity<CertificateRequestResponse> approve(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                requestService.approveByAdmin(id));
    }

    /*
     * ADMIN
     * PATCH /api/certificate-requests/{id}/reject
     */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<CertificateRequestResponse> reject(
            @PathVariable UUID id,
            @RequestParam String reason) {

        return ResponseEntity.ok(
                requestService.rejectByAdmin(
                        id,
                        reason));
    }

    private UUID extractUserId(
            JwtAuthenticationToken authentication) {

        return UUID.fromString(
                authentication
                        .getToken()
                        .getSubject());
    }
}