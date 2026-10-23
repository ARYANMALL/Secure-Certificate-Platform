package com.securecertificate.api.service;

import com.securecertificate.api.dto.verification.CertificateVerificationResponse;
import com.securecertificate.api.entity.Certificate;
import com.securecertificate.api.entity.CertificateStatus;
import com.securecertificate.api.exception.ResourceNotFoundException;
import com.securecertificate.api.repository.CertificateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;

@Service
public class CertificateVerificationService {

    private final CertificateRepository certificateRepository;
    private final CertificateCanonicalizer canonicalizer;
    private final CertificateSigningService signingService;

    public CertificateVerificationService(
            CertificateRepository certificateRepository,
            CertificateCanonicalizer canonicalizer,
            CertificateSigningService signingService) {

        this.certificateRepository = certificateRepository;

        this.canonicalizer = canonicalizer;

        this.signingService = signingService;
    }

    @Transactional(readOnly = true)
    public CertificateVerificationResponse verify(
            String certificateNumber) {

        Certificate certificate = certificateRepository
                .findByCertificateNumber(
                        certificateNumber.trim())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Certificate not found: "
                                + certificateNumber));

        /*
         * Rebuild the exact data that was originally signed.
         */
        String canonicalData = canonicalizer.canonicalize(

                certificate.getCertificateNumber(),

                certificate.getStudent().getId(),
                certificate.getStudentName(),

                certificate.getIssuer().getId(),
                certificate.getIssuerName(),

                certificate.getInstitution().getId(),
                certificate.getInstitutionName(),

                certificate.getCourseName(),

                certificate.getDescription(),

                certificate.getIssueDate());

        /*
         * Calculate a fresh SHA-256 hash.
         */
        String calculatedHash = signingService.calculateSha256(
                canonicalData);

        /*
         * Compare the calculated hash with the
         * hash stored when the certificate was issued.
         */
        boolean integrityValid = MessageDigest.isEqual(
                calculatedHash.getBytes(
                        StandardCharsets.US_ASCII),
                certificate.getCertificateHash()
                        .getBytes(
                                StandardCharsets.US_ASCII));

        /*
         * Independently verify the RSA signature.
         */
        boolean signatureValid = signingService.verify(
                canonicalData,
                certificate.getDigitalSignature());

        /*
         * A certificate is publicly considered valid
         * only if:
         *
         * 1. Data hasn't changed
         * 2. Signature is valid
         * 3. Certificate hasn't been revoked
         */
        boolean valid = integrityValid
                && signatureValid
                && certificate.getStatus() == CertificateStatus.ACTIVE;

        String message;

        if (certificate.getStatus() == CertificateStatus.REVOKED) {

            message = "Certificate has been revoked.";

        } else if (!integrityValid) {

            message = "Certificate data integrity verification failed.";

        } else if (!signatureValid) {

            message = "Digital signature verification failed.";

        } else {

            message = "Certificate is authentic and active.";
        }

        return new CertificateVerificationResponse(

                valid,

                integrityValid,

                signatureValid,

                message,

                certificate.getCertificateNumber(),

                certificate.getStudentName(),

                certificate.getIssuerName(),

                certificate.getInstitutionName(),

                certificate.getCourseName(),

                certificate.getIssueDate(),

                certificate.getStatus(),

                OffsetDateTime.now());
    }
}