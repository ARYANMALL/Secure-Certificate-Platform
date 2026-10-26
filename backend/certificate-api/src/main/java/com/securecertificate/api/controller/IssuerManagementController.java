package com.securecertificate.api.controller;

import com.securecertificate.api.dto.certificate.CertificateRequestResponse;
import com.securecertificate.api.dto.certificate.IssuerCreateCertificateRequest;
import com.securecertificate.api.entity.InstitutionStatus;
import com.securecertificate.api.entity.UserRole;
import com.securecertificate.api.entity.UserStatus;
import com.securecertificate.api.repository.InstitutionRepository;
import com.securecertificate.api.repository.UserRepository;
import com.securecertificate.api.service.CertificateRequestService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/issuer/manage")
@PreAuthorize("hasRole('ISSUER')")
public class IssuerManagementController {

    private final InstitutionRepository institutionRepository;

    private final UserRepository userRepository;

    private final CertificateRequestService certificateRequestService;

    public IssuerManagementController(
            InstitutionRepository institutionRepository,
            UserRepository userRepository,
            CertificateRequestService certificateRequestService) {

        this.institutionRepository = institutionRepository;

        this.userRepository = userRepository;

        this.certificateRequestService = certificateRequestService;
    }

    /*
     * =========================================================
     * GET ACTIVE INSTITUTIONS
     * =========================================================
     *
     * Amit can issue certificates for every
     * active institution.
     */

    @GetMapping("/institutions")
    public List<InstitutionOption> institutions() {

        return institutionRepository
                .findAll()
                .stream()
                .filter(institution -> institution.getStatus() == InstitutionStatus.ACTIVE)
                .map(institution -> new InstitutionOption(
                        institution.getId(),
                        institution.getName(),
                        institution
                                .getRegistrationNumber()))
                .toList();
    }

    /*
     * =========================================================
     * GET ACTIVE STUDENTS
     * =========================================================
     *
     * The frontend selects an institution first.
     *
     * Then it asks for the active students
     * belonging to that institution.
     */

    @GetMapping("/students")
    public List<StudentOption> students(
            @RequestParam UUID institutionId) {

        return userRepository
                .findByRoleAndStatusAndInstitution_IdOrderByFullNameAsc(
                        UserRole.STUDENT,
                        UserStatus.ACTIVE,
                        institutionId)
                .stream()
                .map(student -> new StudentOption(
                        student.getId(),
                        student.getFullName(),
                        student.getEmail(),
                        institutionId))
                .toList();
    }

    /*
     * =========================================================
     * CENTRAL ISSUER CREATES CERTIFICATE REQUEST
     * =========================================================
     *
     * Amit selects:
     *
     * Institution
     * Student
     * Course
     * Description
     *
     * The request goes to PENDING_ADMIN.
     */

    @PostMapping("/certificate-requests")
    public CertificateRequestResponse createCertificateRequest(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody IssuerCreateCertificateRequest request) {

        UUID issuerId = currentUserId(jwt);

        return certificateRequestService
                .createByIssuer(
                        issuerId,
                        request);
    }

    /*
     * =========================================================
     * CURRENT USER ID
     * =========================================================
     */

    private UUID currentUserId(Jwt jwt) {

        if (jwt == null ||
                jwt.getSubject() == null) {

            throw new IllegalStateException(
                    "Authenticated user ID is missing from JWT");
        }

        try {

            return UUID.fromString(
                    jwt.getSubject());

        } catch (IllegalArgumentException exception) {

            throw new IllegalStateException(
                    "Authenticated user ID is invalid",
                    exception);
        }
    }

    /*
     * =========================================================
     * INSTITUTION DROPDOWN RESPONSE
     * =========================================================
     */

    public record InstitutionOption(
            UUID id,
            String name,
            String registrationNumber) {
    }

    /*
     * =========================================================
     * STUDENT DROPDOWN RESPONSE
     * =========================================================
     */

    public record StudentOption(
            UUID id,
            String fullName,
            String email,
            UUID institutionId) {
    }
}