package com.securecertificate.api.service;

import com.securecertificate.api.dto.certificate.CertificateResponse;
import com.securecertificate.api.dto.certificate.CreateCertificateRequest;
import com.securecertificate.api.entity.Certificate;
import com.securecertificate.api.entity.Institution;
import com.securecertificate.api.entity.InstitutionStatus;
import com.securecertificate.api.entity.User;
import com.securecertificate.api.entity.UserRole;
import com.securecertificate.api.entity.UserStatus;
import com.securecertificate.api.exception.BusinessRuleException;
import com.securecertificate.api.exception.ResourceNotFoundException;
import com.securecertificate.api.repository.CertificateRepository;
import com.securecertificate.api.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class CertificateService {

    private static final String CERTIFICATE_NUMBER_PREFIX = "SC";

    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private final CertificateCanonicalizer canonicalizer;
    private final CertificateSigningService signingService;

    public CertificateService(
            CertificateRepository certificateRepository,
            UserRepository userRepository,
            CertificateCanonicalizer canonicalizer,
            CertificateSigningService signingService) {

        this.certificateRepository = certificateRepository;
        this.userRepository = userRepository;
        this.canonicalizer = canonicalizer;
        this.signingService = signingService;
    }

    /**
     * Issue a new digitally signed certificate.
     *
     * Only an ISSUER can perform this operation.
     */
    @PreAuthorize("hasRole('ISSUER')")
    @Transactional
    public CertificateResponse issueCertificate(
            UUID issuerId,
            CreateCertificateRequest request) {

        /*
         * 1. Find the issuer.
         */
        User issuer = userRepository
                .findById(issuerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Issuer not found: "
                                + issuerId));

        /*
         * 2. Make sure this user really is an ISSUER.
         */
        if (issuer.getRole() != UserRole.ISSUER) {

            throw new BusinessRuleException(
                    "Only issuer accounts can issue certificates");
        }

        /*
         * 3. Suspended issuers cannot issue certificates.
         */
        if (issuer.getStatus() != UserStatus.ACTIVE) {

            throw new BusinessRuleException(
                    "Suspended issuers cannot issue certificates");
        }

        /*
         * 4. Get the institution belonging to the issuer.
         */
        Institution institution = issuer.getInstitution();

        if (institution == null) {

            throw new BusinessRuleException(
                    "Issuer is not associated with an institution");
        }

        /*
         * 5. Institution must be active.
         */
        if (institution.getStatus() != InstitutionStatus.ACTIVE) {

            throw new BusinessRuleException(
                    "Certificates cannot be issued by "
                            + "an inactive institution");
        }

        /*
         * 6. Find the student.
         */
        User student = userRepository
                .findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found: "
                                + request.studentId()));

        /*
         * 7. The certificate recipient must
         * actually be a STUDENT.
         */
        if (student.getRole() != UserRole.STUDENT) {

            throw new BusinessRuleException(
                    "Certificate recipient must be a student");
        }

        /*
         * 8. Suspended students cannot receive
         * new certificates.
         */
        if (student.getStatus() != UserStatus.ACTIVE) {

            throw new BusinessRuleException(
                    "Suspended students cannot receive certificates");
        }

        /*
         * 9. Clean user input.
         */
        String courseName = request.courseName().trim();

        String description = normalizeDescription(
                request.description());

        /*
         * 10. Certificate issue date.
         */
        LocalDate issueDate = LocalDate.now(ZoneOffset.UTC);

        /*
         * 11. Generate unique certificate number.
         *
         * Example:
         * SC-2026-000001
         */
        long sequence = certificateRepository
                .nextCertificateSequence();

        String certificateNumber = String.format(
                Locale.ROOT,
                "%s-%d-%06d",
                CERTIFICATE_NUMBER_PREFIX,
                issueDate.getYear(),
                sequence);

        /*
         * 12. Create the exact canonical data
         * that will be signed.
         */
        String canonicalData = canonicalizer.canonicalize(

                certificateNumber,

                student.getId(),
                student.getFullName(),

                issuer.getId(),
                issuer.getFullName(),

                institution.getId(),
                institution.getName(),

                courseName,
                description,
                issueDate);

        /*
         * 13. Calculate SHA-256 hash.
         */
        String certificateHash = signingService.calculateSha256(
                canonicalData);

        /*
         * 14. Create RSA digital signature.
         */
        String digitalSignature = signingService.sign(
                canonicalData);

        /*
         * 15. Create certificate entity.
         */
        Certificate certificate = new Certificate(

                certificateNumber,

                student,
                issuer,
                institution,

                /*
                 * Historical snapshots.
                 */
                student.getFullName(),
                issuer.getFullName(),
                institution.getName(),

                courseName,
                description,

                issueDate,

                certificateHash,
                digitalSignature,

                signingService
                        .getSignatureAlgorithm());

        /*
         * 16. Save certificate.
         */
        Certificate savedCertificate = certificateRepository.save(
                certificate);

        /*
         * 17. Return safe response.
         */
        return toResponse(
                savedCertificate);
    }

    /**
     * Get a certificate by ID.
     *
     * An issuer can only access certificates
     * belonging to their own institution.
     */
    @PreAuthorize("hasRole('ISSUER')")
    @Transactional(readOnly = true)
    public CertificateResponse findById(
            UUID certificateId,
            UUID issuerId) {

        Certificate certificate = certificateRepository
                .findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Certificate not found: "
                                + certificateId));

        User issuer = userRepository
                .findById(issuerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Issuer not found: "
                                + issuerId));

        ensureIssuerOwnsCertificate(
                issuer,
                certificate);

        return toResponse(certificate);
    }

    /**
     * Get all certificates belonging to
     * the issuer's institution.
     */
    @PreAuthorize("hasRole('ISSUER')")
    @Transactional(readOnly = true)
    public List<CertificateResponse> findForIssuer(
            UUID issuerId) {

        User issuer = userRepository
                .findById(issuerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Issuer not found: "
                                + issuerId));

        if (issuer.getInstitution() == null) {

            throw new BusinessRuleException(
                    "Issuer is not associated with an institution");
        }

        UUID institutionId = issuer.getInstitution().getId();

        return certificateRepository
                .findAll()
                .stream()
                .filter(certificate -> certificate
                        .getInstitution()
                        .getId()
                        .equals(institutionId))
                .map(this::toResponse)
                .toList();
    }

    /**
     * Revoke an existing certificate.
     */
    @PreAuthorize("hasRole('ISSUER')")
    @Transactional
    public CertificateResponse revokeCertificate(
            UUID certificateId,
            UUID issuerId,
            String reason) {

        Certificate certificate = certificateRepository
                .findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Certificate not found: "
                                + certificateId));

        User issuer = userRepository
                .findById(issuerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Issuer not found: "
                                + issuerId));

        /*
         * Issuer can only revoke certificates
         * belonging to their institution.
         */
        ensureIssuerOwnsCertificate(
                issuer,
                certificate);

        /*
         * Suspended issuers cannot revoke certificates.
         */
        if (issuer.getStatus() != UserStatus.ACTIVE) {

            throw new BusinessRuleException(
                    "Suspended issuers cannot revoke certificates");
        }

        /*
         * Change:
         *
         * ACTIVE → REVOKED
         */
        certificate.revoke(reason);

        return toResponse(certificate);
    }

    /**
     * Get the actual Certificate entity for PDF generation.
     *
     * This method is used internally by the PDF endpoint.
     */
    @PreAuthorize("hasRole('ISSUER')")
    @Transactional(readOnly = true)
    public Certificate getCertificateEntityForPdf(
            UUID certificateId,
            UUID issuerId) {

        Certificate certificate = certificateRepository
                .findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Certificate not found: "
                                + certificateId));

        User issuer = userRepository
                .findById(issuerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Issuer not found: "
                                + issuerId));

        /*
         * Reuse the same authorization rule.
         */
        ensureIssuerOwnsCertificate(
                issuer,
                certificate);

        return certificate;
    }

    /**
     * Makes sure the issuer belongs to the
     * same institution as the certificate.
     */
    private void ensureIssuerOwnsCertificate(
            User issuer,
            Certificate certificate) {

        if (issuer.getInstitution() == null) {

            throw new BusinessRuleException(
                    "Issuer is not associated with an institution");
        }

        if (!issuer.getInstitution()
                .getId()
                .equals(
                        certificate
                                .getInstitution()
                                .getId())) {

            throw new BusinessRuleException(
                    "You are not authorized to access "
                            + "this certificate");
        }
    }

    /**
     * Convert blank descriptions to null.
     */
    private String normalizeDescription(
            String description) {

        if (description == null) {
            return null;
        }

        String trimmed = description.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }

    /**
     * Convert entity into API response.
     */
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