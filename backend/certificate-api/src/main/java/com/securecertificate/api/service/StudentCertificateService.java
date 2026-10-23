package com.securecertificate.api.service;

import com.securecertificate.api.dto.certificate.CertificateResponse;
import com.securecertificate.api.entity.Certificate;
import com.securecertificate.api.entity.User;
import com.securecertificate.api.entity.UserRole;
import com.securecertificate.api.exception.BusinessRuleException;
import com.securecertificate.api.exception.ResourceNotFoundException;
import com.securecertificate.api.repository.CertificateRepository;
import com.securecertificate.api.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class StudentCertificateService {

    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;

    public StudentCertificateService(
            CertificateRepository certificateRepository,
            UserRepository userRepository) {

        this.certificateRepository = certificateRepository;

        this.userRepository = userRepository;
    }

    @PreAuthorize("hasRole('STUDENT')")
    @Transactional(readOnly = true)
    public List<CertificateResponse> findMyCertificates(
            UUID studentId) {

        User student = userRepository
                .findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found: "
                                + studentId));

        if (student.getRole() != UserRole.STUDENT) {

            throw new BusinessRuleException(
                    "Only student accounts can access "
                            + "student certificates");
        }

        return certificateRepository
                .findByStudent_IdOrderByIssueDateDesc(
                        studentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PreAuthorize("hasRole('STUDENT')")
    @Transactional(readOnly = true)
    public Certificate getMyCertificate(
            UUID studentId,
            UUID certificateId) {

        User student = userRepository
                .findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found: "
                                + studentId));

        if (student.getRole() != UserRole.STUDENT) {

            throw new BusinessRuleException(
                    "Only student accounts can access "
                            + "student certificates");
        }

        return certificateRepository
                .findByIdAndStudent_Id(
                        certificateId,
                        studentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Certificate not found"));
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