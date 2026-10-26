package com.securecertificate.api.service;

import com.securecertificate.api.dto.auth.AuthResponse;
import com.securecertificate.api.dto.auth.LoginRequest;
import com.securecertificate.api.dto.auth.RegisterRequest;
import com.securecertificate.api.entity.Institution;
import com.securecertificate.api.entity.InstitutionStatus;
import com.securecertificate.api.entity.User;
import com.securecertificate.api.entity.UserRole;
import com.securecertificate.api.exception.DuplicateResourceException;
import com.securecertificate.api.exception.InvalidCredentialsException;
import com.securecertificate.api.exception.ResourceNotFoundException;
import com.securecertificate.api.repository.InstitutionRepository;
import com.securecertificate.api.repository.UserRepository;
import com.securecertificate.api.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            InstitutionRepository institutionRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.institutionRepository = institutionRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmailIgnoreCase(email)) {

            throw new DuplicateResourceException(
                    "User with this email already exists");
        }

        Institution institution = institutionRepository
                .findById(request.institutionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Institution not found"));

        if (institution.getStatus() != InstitutionStatus.ACTIVE) {

            throw new IllegalStateException(
                    "Selected institution is not active");
        }

        String passwordHash = passwordEncoder.encode(
                request.password());

        User user = new User(
                request.fullName().trim(),
                email,
                passwordHash,
                UserRole.STUDENT,
                institution);

        User savedUser = userRepository.save(user);

        return createAuthResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(
            LoginRequest request) {

        String email = normalizeEmail(request.email());

        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            request.password()));

        } catch (AuthenticationException exception) {

            throw new InvalidCredentialsException(
                    "Invalid email or password");
        }

        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new InvalidCredentialsException(
                        "Invalid email or password"));

        return createAuthResponse(user);
    }

    private AuthResponse createAuthResponse(
            User user) {

        String token = jwtService.generateAccessToken(user);

        return new AuthResponse(
                token,
                "Bearer",
                jwtService.getExpirationSeconds(),
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole());
    }

    private String normalizeEmail(
            String email) {

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}