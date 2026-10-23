package com.securecertificate.api.controller;

import com.securecertificate.api.dto.certificate.CertificateResponse;
import com.securecertificate.api.entity.Certificate;
import com.securecertificate.api.service.CertificatePdfService;
import com.securecertificate.api.service.StudentCertificateService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/student/certificates")
public class StudentCertificateController {

    private final StudentCertificateService studentCertificateService;
    private final CertificatePdfService certificatePdfService;

    public StudentCertificateController(
            StudentCertificateService studentCertificateService,
            CertificatePdfService certificatePdfService) {

        this.studentCertificateService = studentCertificateService;

        this.certificatePdfService = certificatePdfService;
    }

    @GetMapping
    public ResponseEntity<List<CertificateResponse>> findMyCertificates(
            JwtAuthenticationToken authentication) {

        UUID studentId = extractUserId(authentication);

        return ResponseEntity.ok(
                studentCertificateService
                        .findMyCertificates(studentId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CertificateResponse> findMyCertificate(
            @PathVariable UUID id,
            JwtAuthenticationToken authentication) {

        UUID studentId = extractUserId(authentication);

        Certificate certificate = studentCertificateService
                .getMyCertificate(
                        studentId,
                        id);

        return ResponseEntity.ok(
                toResponse(certificate));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(
            @PathVariable UUID id,
            JwtAuthenticationToken authentication) {

        UUID studentId = extractUserId(authentication);

        Certificate certificate = studentCertificateService
                .getMyCertificate(
                        studentId,
                        id);

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

        try {

            return UUID.fromString(
                    authentication
                            .getToken()
                            .getSubject());

        } catch (IllegalArgumentException exception) {

            throw new IllegalStateException(
                    "Authenticated user ID is invalid",
                    exception);
        }
    }

    private CertificateResponse toResponse(
            Certificate certificate) {

        return new CertificateResponse(

                certificate.getId(),

                certificate.getCertificateNumber(),

                certificate.getStudent().getId(),
                certificate.getStudentName(),

                certificate.getIssuer().getId(),
                certificate.getIssuerName(),

                certificate.getInstitution().getId(),
                certificate.getInstitutionName(),

                certificate.getCourseName(),

                certificate.getDescription(),

                certificate.getIssueDate(),

                certificate.getStatus(),

                certificate.getCertificateHash(),

                certificate.getSignatureAlgorithm(),

                certificate.getRevokedAt(),

                certificate.getRevocationReason(),

                certificate.getCreatedAt(),

                certificate.getUpdatedAt());
    }
}