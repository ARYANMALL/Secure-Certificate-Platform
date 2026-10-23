package com.securecertificate.api.controller;

import com.securecertificate.api.dto.certificate.CertificateResponse;
import com.securecertificate.api.dto.certificate.CreateCertificateRequest;
import com.securecertificate.api.dto.certificate.RevokeCertificateRequest;
import com.securecertificate.api.entity.Certificate;
import com.securecertificate.api.service.CertificatePdfService;
import com.securecertificate.api.service.CertificateService;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {

    private final CertificateService certificateService;
    private final CertificatePdfService certificatePdfService;

    public CertificateController(
            CertificateService certificateService,
            CertificatePdfService certificatePdfService) {

        this.certificateService = certificateService;

        this.certificatePdfService = certificatePdfService;
    }

    @PostMapping
    public ResponseEntity<CertificateResponse> issueCertificate(
            @Valid @RequestBody CreateCertificateRequest request,

            JwtAuthenticationToken authentication) {

        UUID issuerId = extractUserId(authentication);

        CertificateResponse response = certificateService.issueCertificate(
                issuerId,
                request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CertificateResponse> findById(
            @PathVariable UUID id,
            JwtAuthenticationToken authentication) {

        UUID issuerId = extractUserId(authentication);

        return ResponseEntity.ok(
                certificateService.findById(
                        id,
                        issuerId));
    }

    @GetMapping
    public ResponseEntity<List<CertificateResponse>> findForIssuer(
            JwtAuthenticationToken authentication) {

        UUID issuerId = extractUserId(authentication);

        return ResponseEntity.ok(
                certificateService.findForIssuer(
                        issuerId));
    }

    @PatchMapping("/{id}/revoke")
    public ResponseEntity<CertificateResponse> revoke(
            @PathVariable UUID id,

            @Valid @RequestBody RevokeCertificateRequest request,

            JwtAuthenticationToken authentication) {

        UUID issuerId = extractUserId(authentication);

        CertificateResponse response = certificateService.revokeCertificate(
                id,
                issuerId,
                request.reason());

        return ResponseEntity.ok(response);
    }

    /*
     * Download certificate PDF.
     *
     * GET /api/certificates/{id}/pdf
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(
            @PathVariable UUID id,
            JwtAuthenticationToken authentication) {

        UUID issuerId = extractUserId(authentication);

        Certificate certificate = certificateService
                .getCertificateEntityForPdf(
                        id,
                        issuerId);

        byte[] pdf = certificatePdfService.generate(
                certificate);

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_PDF);

        headers.setContentDisposition(
                ContentDisposition
                        .attachment()
                        .filename(
                                certificate
                                        .getCertificateNumber()
                                        + ".pdf")
                        .build());

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(pdf);
    }

    private UUID extractUserId(
            JwtAuthenticationToken authentication) {

        String subject = authentication
                .getToken()
                .getSubject();

        try {

            return UUID.fromString(subject);

        } catch (IllegalArgumentException exception) {

            throw new IllegalStateException(
                    "Authenticated user ID is invalid",
                    exception);
        }
    }
}