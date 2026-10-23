package com.securecertificate.api.service;

import com.securecertificate.api.dto.certificate.StudentOptionResponse;
import com.securecertificate.api.entity.User;
import com.securecertificate.api.entity.UserRole;
import com.securecertificate.api.entity.UserStatus;
import com.securecertificate.api.exception.BusinessRuleException;
import com.securecertificate.api.exception.ResourceNotFoundException;
import com.securecertificate.api.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class IssuerStudentService {

    private final UserRepository userRepository;

    public IssuerStudentService(
            UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    @PreAuthorize("hasRole('ISSUER')")
    @Transactional(readOnly = true)
    public List<StudentOptionResponse> findActiveStudents(
            UUID issuerId) {

        User issuer = userRepository
                .findById(issuerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Issuer not found: " + issuerId));

        if (issuer.getRole() != UserRole.ISSUER) {

            throw new BusinessRuleException(
                    "Only issuers can access students");
        }

        if (issuer.getInstitution() == null) {

            throw new BusinessRuleException(
                    "Issuer is not associated with an institution");
        }

        UUID institutionId = issuer.getInstitution().getId();

        return userRepository
                .findByRoleAndStatusAndInstitution_IdOrderByFullNameAsc(
                        UserRole.STUDENT,
                        UserStatus.ACTIVE,
                        institutionId)
                .stream()
                .map(user -> new StudentOptionResponse(
                        user.getId(),
                        user.getFullName(),
                        user.getEmail()))
                .toList();
    }
}