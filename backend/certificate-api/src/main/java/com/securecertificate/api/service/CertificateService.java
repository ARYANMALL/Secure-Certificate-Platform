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

        /*
         * =========================================================
         * DIRECT ISSUANCE
         * =========================================================
         *
         * Existing issuer endpoint.
         *
         * An active issuer can issue certificates
         * for students from ANY active institution.
         */
        @PreAuthorize("hasRole('ISSUER')")
        @Transactional
        public CertificateResponse issueCertificate(
                        UUID issuerId,
                        CreateCertificateRequest request) {

                Certificate certificate = createCertificate(
                                issuerId,
                                request.studentId(),
                                request.courseName(),
                                request.description());

                return toResponse(certificate);
        }

        /*
         * =========================================================
         * ADMIN APPROVED ISSUANCE
         * =========================================================
         *
         * Called by CertificateRequestService after
         * the administrator approves a request.
         */
        @PreAuthorize("hasRole('ADMIN')")
        @Transactional
        public Certificate issueApprovedRequest(
                        UUID issuerId,
                        UUID studentId,
                        String courseName,
                        String description) {

                return createCertificate(
                                issuerId,
                                studentId,
                                courseName,
                                description);
        }

        /*
         * =========================================================
         * CREATE CERTIFICATE
         * =========================================================
         *
         * IMPORTANT ARCHITECTURE:
         *
         * issuer = Amit
         *
         * institution = student's institution
         *
         * Therefore:
         *
         * Amit + DIT student
         * ↓
         * DIT certificate
         *
         * Amit + Tech Institute student
         * ↓
         * Tech Institute certificate
         */
        private Certificate createCertificate(
                        UUID issuerId,
                        UUID studentId,
                        String courseNameInput,
                        String descriptionInput) {

                /*
                 * -----------------------------------------------------
                 * 1. FIND ISSUER
                 * -----------------------------------------------------
                 */
                User issuer = userRepository
                                .findById(issuerId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Issuer not found: "
                                                                + issuerId));

                /*
                 * -----------------------------------------------------
                 * 2. VERIFY ISSUER ROLE
                 * -----------------------------------------------------
                 */
                if (issuer.getRole() != UserRole.ISSUER) {

                        throw new BusinessRuleException(
                                        "Only issuer accounts can "
                                                        + "issue certificates");
                }

                /*
                 * -----------------------------------------------------
                 * 3. VERIFY ISSUER ACTIVE
                 * -----------------------------------------------------
                 */
                if (issuer.getStatus() != UserStatus.ACTIVE) {

                        throw new BusinessRuleException(
                                        "Suspended issuers cannot "
                                                        + "issue certificates");
                }

                /*
                 * -----------------------------------------------------
                 * 4. FIND STUDENT
                 * -----------------------------------------------------
                 */
                User student = userRepository
                                .findById(studentId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Student not found: "
                                                                + studentId));

                /*
                 * -----------------------------------------------------
                 * 5. VERIFY STUDENT ROLE
                 * -----------------------------------------------------
                 */
                if (student.getRole() != UserRole.STUDENT) {

                        throw new BusinessRuleException(
                                        "Certificate recipient must "
                                                        + "be a student");
                }

                /*
                 * -----------------------------------------------------
                 * 6. VERIFY STUDENT ACTIVE
                 * -----------------------------------------------------
                 */
                if (student.getStatus() != UserStatus.ACTIVE) {

                        throw new BusinessRuleException(
                                        "Suspended students cannot "
                                                        + "receive certificates");
                }

                /*
                 * -----------------------------------------------------
                 * 7. GET STUDENT'S INSTITUTION
                 * -----------------------------------------------------
                 *
                 * THIS IS THE IMPORTANT CHANGE.
                 *
                 * We do NOT use:
                 *
                 * issuer.getInstitution()
                 *
                 * because Amit is a central issuer.
                 */
                Institution institution = student.getInstitution();

                if (institution == null) {

                        throw new BusinessRuleException(
                                        "Student is not associated "
                                                        + "with an institution");
                }

                /*
                 * -----------------------------------------------------
                 * 8. INSTITUTION MUST BE ACTIVE
                 * -----------------------------------------------------
                 */
                if (institution.getStatus() != InstitutionStatus.ACTIVE) {

                        throw new BusinessRuleException(
                                        "Certificates cannot be issued "
                                                        + "for an inactive institution");
                }

                /*
                 * -----------------------------------------------------
                 * 9. COURSE NAME
                 * -----------------------------------------------------
                 */
                if (courseNameInput == null ||
                                courseNameInput.isBlank()) {

                        throw new BusinessRuleException(
                                        "Course name is required");
                }

                String courseName = courseNameInput.trim();

                /*
                 * -----------------------------------------------------
                 * 10. DESCRIPTION
                 * -----------------------------------------------------
                 */
                String description = normalizeDescription(
                                descriptionInput);

                /*
                 * -----------------------------------------------------
                 * 11. ISSUE DATE
                 * -----------------------------------------------------
                 */
                LocalDate issueDate = LocalDate.now(
                                ZoneOffset.UTC);

                /*
                 * -----------------------------------------------------
                 * 12. GENERATE CERTIFICATE NUMBER
                 * -----------------------------------------------------
                 *
                 * Example:
                 *
                 * SC-2026-000007
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
                 * -----------------------------------------------------
                 * 13. CANONICAL DATA
                 * -----------------------------------------------------
                 *
                 * These exact values are used for both:
                 *
                 * SHA-256
                 * RSA signature
                 */
                String canonicalData = canonicalizer.canonicalize(

                                certificateNumber,

                                /*
                                 * Student
                                 */
                                student.getId(),
                                student.getFullName(),

                                /*
                                 * Central issuer
                                 */
                                issuer.getId(),
                                issuer.getFullName(),

                                /*
                                 * Student's institution
                                 */
                                institution.getId(),
                                institution.getName(),

                                /*
                                 * Course
                                 */
                                courseName,

                                description,

                                /*
                                 * Date
                                 */
                                issueDate);

                /*
                 * -----------------------------------------------------
                 * 14. SHA-256
                 * -----------------------------------------------------
                 */
                String certificateHash = signingService.calculateSha256(
                                canonicalData);

                /*
                 * -----------------------------------------------------
                 * 15. RSA DIGITAL SIGNATURE
                 * -----------------------------------------------------
                 */
                String digitalSignature = signingService.sign(
                                canonicalData);

                /*
                 * -----------------------------------------------------
                 * 16. CREATE CERTIFICATE ENTITY
                 * -----------------------------------------------------
                 */
                Certificate certificate = new Certificate(

                                certificateNumber,

                                /*
                                 * Student
                                 */
                                student,

                                /*
                                 * Central issuer
                                 */
                                issuer,

                                /*
                                 * STUDENT'S institution
                                 */
                                institution,

                                /*
                                 * Historical snapshots
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
                 * -----------------------------------------------------
                 * 17. SAVE
                 * -----------------------------------------------------
                 */
                return certificateRepository.save(
                                certificate);
        }

        /*
         * =========================================================
         * FIND CERTIFICATE BY ID
         * =========================================================
         *
         * Central issuer can access certificates
         * from every institution.
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

                ensureActiveIssuer(issuer);

                return toResponse(certificate);
        }

        /*
         * =========================================================
         * GET ALL CERTIFICATES
         * =========================================================
         *
         * Central issuer can see certificates
         * from ALL institutions.
         */
        @PreAuthorize("hasRole('ISSUER')")
        @Transactional(readOnly = true)
        public List<CertificateResponse> findForIssuer(UUID issuerId) {

                User issuer = userRepository
                                .findById(issuerId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Issuer not found: "
                                                                + issuerId));

                ensureActiveIssuer(issuer);

                return certificateRepository
                                .findAll()
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        /*
         * =========================================================
         * REVOKE
         * =========================================================
         *
         * Central issuer can revoke certificates
         * from any institution.
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

                ensureActiveIssuer(issuer);

                /*
                 * NO institution comparison.
                 *
                 * Central issuer can revoke certificates
                 * from any institution.
                 */
                certificate.revoke(reason);

                return toResponse(certificate);
        }

        /*
         * =========================================================
         * PDF
         * =========================================================
         *
         * Central issuer can download PDFs
         * from any institution.
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

                ensureActiveIssuer(issuer);

                return certificate;
        }

        /*
         * =========================================================
         * ISSUER VALIDATION
         * =========================================================
         *
         * Notice:
         *
         * There is NO institution check.
         */
        private void ensureActiveIssuer(
                        User issuer) {

                if (issuer.getRole() != UserRole.ISSUER) {

                        throw new BusinessRuleException(
                                        "Only issuer accounts can "
                                                        + "manage certificates");
                }

                if (issuer.getStatus() != UserStatus.ACTIVE) {

                        throw new BusinessRuleException(
                                        "Suspended issuers cannot "
                                                        + "manage certificates");
                }
        }

        /*
         * =========================================================
         * DESCRIPTION NORMALIZATION
         * =========================================================
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

        /*
         * =========================================================
         * ENTITY → RESPONSE
         * =========================================================
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