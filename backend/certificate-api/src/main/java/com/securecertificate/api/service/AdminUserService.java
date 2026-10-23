package com.securecertificate.api.service;

import com.securecertificate.api.dto.admin.CreateIssuerRequest;
import com.securecertificate.api.dto.admin.IssuerResponse;
import com.securecertificate.api.entity.Institution;
import com.securecertificate.api.entity.InstitutionStatus;
import com.securecertificate.api.entity.User;
import com.securecertificate.api.entity.UserRole;
import com.securecertificate.api.exception.BusinessRuleException;
import com.securecertificate.api.exception.DuplicateResourceException;
import com.securecertificate.api.exception.ResourceNotFoundException;
import com.securecertificate.api.repository.InstitutionRepository;
import com.securecertificate.api.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(
            UserRepository userRepository,
            InstitutionRepository institutionRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.institutionRepository = institutionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public IssuerResponse createIssuer(
            CreateIssuerRequest request) {

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        /*
         * 1. Make sure the email isn't already registered.
         */
        if (userRepository.existsByEmailIgnoreCase(email)) {

            throw new DuplicateResourceException(
                    "User with this email already exists");
        }

        /*
         * 2. Find the institution.
         */
        Institution institution = institutionRepository
                .findById(request.institutionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Institution not found: "
                                + request.institutionId()));

        /*
         * 3. Only active institutions can have issuers.
         */
        if (institution.getStatus() != InstitutionStatus.ACTIVE) {

            throw new BusinessRuleException(
                    "Issuer can only be created for an active institution");
        }

        /*
         * 4. Hash the password.
         *
         * NEVER store the plain-text password.
         */
        String passwordHash = passwordEncoder.encode(
                request.password());

        /*
         * 5. Create the user.
         *
         * The role is controlled by the backend.
         */
        User issuer = new User(
                request.fullName().trim(),
                email,
                passwordHash,
                UserRole.ISSUER,
                institution);

        /*
         * 6. Save to PostgreSQL.
         */
        User savedIssuer = userRepository.save(issuer);

        /*
         * 7. Return a safe response.
         */
        return new IssuerResponse(
                savedIssuer.getId(),
                savedIssuer.getFullName(),
                savedIssuer.getEmail(),
                savedIssuer.getRole(),
                savedIssuer.getStatus(),
                institution.getId());
    }
}
