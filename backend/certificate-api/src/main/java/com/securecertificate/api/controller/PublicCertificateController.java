package com.securecertificate.api.controller;

import com.securecertificate.api.dto.verification.CertificateVerificationResponse;
import com.securecertificate.api.service.CertificateVerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/certificates")
public class PublicCertificateController {

    private final CertificateVerificationService verificationService;

    public PublicCertificateController(
            CertificateVerificationService verificationService) {

        this.verificationService = verificationService;
    }

    /**
     * Public certificate verification.
     *
     * No login required.
     *
     * GET
     * /api/public/certificates/{certificateNumber}/verify
     */
    @GetMapping("/{certificateNumber}/verify")
    public ResponseEntity<CertificateVerificationResponse> verify(
            @PathVariable String certificateNumber) {

        return ResponseEntity.ok(
                verificationService.verify(
                        certificateNumber));
    }
}