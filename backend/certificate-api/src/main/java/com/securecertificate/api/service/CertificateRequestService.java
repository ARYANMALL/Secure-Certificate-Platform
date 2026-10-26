package com.securecertificate.api.service;

import com.securecertificate.api.dto.certificate.CertificateRequestResponse;
import com.securecertificate.api.dto.certificate.CreateCertificateRequestRequest;
import com.securecertificate.api.dto.certificate.IssuerCreateCertificateRequest;
import com.securecertificate.api.entity.Certificate;
import com.securecertificate.api.entity.CertificateRequest;
import com.securecertificate.api.entity.CertificateRequestStatus;
import com.securecertificate.api.entity.Institution;
import com.securecertificate.api.entity.InstitutionStatus;
import com.securecertificate.api.entity.User;
import com.securecertificate.api.entity.UserRole;
import com.securecertificate.api.entity.UserStatus;
import com.securecertificate.api.exception.BusinessRuleException;
import com.securecertificate.api.exception.ResourceNotFoundException;
import com.securecertificate.api.repository.CertificateRequestRepository;
import com.securecertificate.api.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CertificateRequestService {

    private final CertificateRequestRepository requestRepository;

    private final UserRepository userRepository;

    private final CertificateService certificateService;

    public CertificateRequestService(
            CertificateRequestRepository requestRepository,
            UserRepository userRepository,
            CertificateService certificateService) {

        this.requestRepository = requestRepository;

        this.userRepository = userRepository;

        this.certificateService = certificateService;
    }

    /*
     * =========================================================
     * STUDENT
     * CREATE CERTIFICATE REQUEST
     * =========================================================
     */

    @PreAuthorize("hasRole('STUDENT')")
    @Transactional
    public CertificateRequestResponse create(
            UUID studentId,
            CreateCertificateRequestRequest request) {

        User student = userRepository
                .findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found: "
                                + studentId));

        if (student.getRole() != UserRole.STUDENT) {

            throw new BusinessRuleException(
                    "Only students can create certificate requests");
        }

        if (student.getStatus() != UserStatus.ACTIVE) {

            throw new BusinessRuleException(
                    "Suspended students cannot create requests");
        }

        Institution institution = student.getInstitution();

        if (institution == null) {

            throw new BusinessRuleException(
                    "Student is not associated with an institution");
        }

        if (institution.getStatus() != InstitutionStatus.ACTIVE) {

            throw new BusinessRuleException(
                    "Your institution is not active");
        }

        if (request.courseName() == null ||
                request.courseName().isBlank()) {

            throw new BusinessRuleException(
                    "Course name is required");
        }

        String courseName = request.courseName().trim();

        String description = normalizeDescription(
                request.description());

        CertificateRequest certificateRequest = new CertificateRequest(
                student,
                institution,
                courseName,
                description);

        CertificateRequest saved = requestRepository.save(
                certificateRequest);

        return toResponse(saved);
    }

    /*
     * =========================================================
     * STUDENT
     * GET MY REQUESTS
     * =========================================================
     */

    @PreAuthorize("hasRole('STUDENT')")
    @Transactional(readOnly = true)
    public List<CertificateRequestResponse> findForStudent(UUID studentId) {

        return requestRepository
                .findByStudent_IdOrderByCreatedAtDesc(
                        studentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /*
     * =========================================================
     * CENTRAL ISSUER
     * GET ALL PENDING REQUESTS
     * =========================================================
     */

    @PreAuthorize("hasRole('ISSUER')")
    @Transactional(readOnly = true)
    public List<CertificateRequestResponse> findForIssuer(UUID issuerId) {

        User issuer = getUser(issuerId);

        ensureActiveIssuer(issuer);

        /*
         * IMPORTANT:
         *
         * There is intentionally NO institution filter.
         *
         * Amit is the central issuer.
         */
        return requestRepository
                .findByStatusOrderByCreatedAtAsc(
                        CertificateRequestStatus.PENDING_ISSUER)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /*
     * =========================================================
     * CENTRAL ISSUER
     * ACCEPT STUDENT REQUEST
     * =========================================================
     */

    @PreAuthorize("hasRole('ISSUER')")
    @Transactional
    public CertificateRequestResponse acceptByIssuer(
            UUID requestId,
            UUID issuerId) {

        User issuer = getUser(issuerId);

        ensureActiveIssuer(issuer);

        CertificateRequest request = getRequest(requestId);

        if (request.getStatus() != CertificateRequestStatus.PENDING_ISSUER) {

            throw new BusinessRuleException(
                    "Request is no longer awaiting issuer review");
        }

        /*
         * No institution comparison.
         *
         * Amit can process requests from every institution.
         */
        request.acceptByIssuer(issuer);

        return toResponse(request);
    }

    /*
     * =========================================================
     * CENTRAL ISSUER
     * CREATE CERTIFICATE REQUEST DIRECTLY
     * =========================================================
     * 
     * Amit selects:
     * 
     * Institution
     * Student
     * Course
     * Description
     * 
     * The request goes directly to:
     * 
     * PENDING_ADMIN
     * 
     * The actual certificate is NOT created until
     * the administrator approves it.
     * =========================================================
     */

    @PreAuthorize("hasRole('ISSUER')")
    @Transactional
    public CertificateRequestResponse createByIssuer(
            UUID issuerId,
            IssuerCreateCertificateRequest request) {

        User issuer = getUser(issuerId);

        ensureActiveIssuer(issuer);

        /*
         * -----------------------------------------------------
         * FIND SELECTED STUDENT
         * -----------------------------------------------------
         */

        User student = userRepository
                .findById(
                        request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found: "
                                + request.studentId()));

        /*
         * -----------------------------------------------------
         * STUDENT MUST ACTUALLY BE A STUDENT
         * -----------------------------------------------------
         */

        if (student.getRole() != UserRole.STUDENT) {

            throw new BusinessRuleException(
                    "Certificate recipient must be a student");
        }

        /*
         * -----------------------------------------------------
         * STUDENT MUST BE ACTIVE
         * -----------------------------------------------------
         */

        if (student.getStatus() != UserStatus.ACTIVE) {

            throw new BusinessRuleException(
                    "Suspended students cannot receive certificates");
        }

        /*
         * -----------------------------------------------------
         * GET STUDENT'S INSTITUTION
         * -----------------------------------------------------
         *
         * We deliberately use:
         *
         * student.getInstitution()
         *
         * NOT:
         *
         * issuer.getInstitution()
         *
         * because Amit is a central issuer.
         */

        Institution institution = student.getInstitution();

        if (institution == null) {

            throw new BusinessRuleException(
                    "Student is not associated with an institution");
        }

        /*
         * -----------------------------------------------------
         * INSTITUTION MUST BE ACTIVE
         * -----------------------------------------------------
         */

        if (institution.getStatus() != InstitutionStatus.ACTIVE) {

            throw new BusinessRuleException(
                    "Certificates cannot be prepared for an inactive institution");
        }

        /*
         * -----------------------------------------------------
         * COURSE
         * -----------------------------------------------------
         */

        if (request.courseName() == null ||
                request.courseName().isBlank()) {

            throw new BusinessRuleException(
                    "Course name is required");
        }

        String courseName = request.courseName().trim();

        /*
         * -----------------------------------------------------
         * DESCRIPTION
         * -----------------------------------------------------
         */

        String description = normalizeDescription(
                request.description());

        /*
         * -----------------------------------------------------
         * CREATE REQUEST
         * -----------------------------------------------------
         */

        CertificateRequest certificateRequest = new CertificateRequest(
                student,
                institution,
                courseName,
                description);

        /*
         * Amit is already the issuer who prepared it.
         *
         * Therefore:
         *
         * PENDING_ISSUER
         * ↓
         * PENDING_ADMIN
         */

        certificateRequest.acceptByIssuer(
                issuer);

        CertificateRequest saved = requestRepository.save(
                certificateRequest);

        return toResponse(saved);
    }

    /*
     * =========================================================
     * ADMIN
     * GET PENDING APPROVALS
     * =========================================================
     */

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<CertificateRequestResponse> findForAdmin() {

        return requestRepository
                .findByStatusOrderByCreatedAtAsc(
                        CertificateRequestStatus.PENDING_ADMIN)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /*
     * =========================================================
     * ADMIN
     * APPROVE
     * =========================================================
     */

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public CertificateRequestResponse approveByAdmin(UUID requestId) {

        CertificateRequest request = getRequest(requestId);

        if (request.getStatus() != CertificateRequestStatus.PENDING_ADMIN) {

            throw new BusinessRuleException(
                    "Request is not awaiting admin approval");
        }

        if (request.getIssuer() == null) {

            throw new BusinessRuleException(
                    "Request has no assigned issuer");
        }

        /*
         * CertificateService creates the real certificate.
         *
         * Student's institution is preserved.
         */
        Certificate certificate = certificateService
                .issueApprovedRequest(
                        request.getIssuer().getId(),
                        request.getStudent().getId(),
                        request.getCourseName(),
                        request.getDescription());

        request.complete(
                certificate);

        return toResponse(request);
    }

    /*
     * =========================================================
     * ADMIN
     * REJECT
     * =========================================================
     */

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public CertificateRequestResponse rejectByAdmin(
            UUID requestId,
            String reason) {

        CertificateRequest request = getRequest(requestId);

        if (request.getStatus() != CertificateRequestStatus.PENDING_ADMIN) {

            throw new BusinessRuleException(
                    "Request is not awaiting admin approval");
        }

        request.reject(reason);

        return toResponse(request);
    }

    /*
     * =========================================================
     * HELPERS
     * =========================================================
     */

    private User getUser(UUID id) {

        return userRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: "
                                + id));
    }

    private CertificateRequest getRequest(UUID id) {

        return requestRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Certificate request not found: "
                                + id));
    }

    private void ensureActiveIssuer(
            User issuer) {

        if (issuer.getRole() != UserRole.ISSUER) {

            throw new BusinessRuleException(
                    "Only issuer accounts can process certificate requests");
        }

        if (issuer.getStatus() != UserStatus.ACTIVE) {

            throw new BusinessRuleException(
                    "Suspended issuers cannot process requests");
        }
    }

    private String normalizeDescription(
            String description) {

        if (description == null) {
            return null;
        }

        String value = description.trim();

        return value.isEmpty()
                ? null
                : value;
    }

    private CertificateRequestResponse toResponse(
            CertificateRequest request) {

        Certificate certificate = request.getCertificate();

        return new CertificateRequestResponse(

                request.getId(),

                request.getStudent().getId(),

                request.getStudent().getFullName(),

                request.getIssuer() == null
                        ? null
                        : request
                                .getIssuer()
                                .getId(),

                request.getIssuer() == null
                        ? null
                        : request
                                .getIssuer()
                                .getFullName(),

                request
                        .getInstitution()
                        .getId(),

                request
                        .getInstitution()
                        .getName(),

                request.getCourseName(),

                request.getDescription(),

                request.getStatus(),

                certificate == null
                        ? null
                        : certificate.getId(),

                certificate == null
                        ? null
                        : certificate
                                .getCertificateNumber(),

                request.getRejectionReason(),

                request.getCreatedAt(),

                request.getUpdatedAt());
    }
}