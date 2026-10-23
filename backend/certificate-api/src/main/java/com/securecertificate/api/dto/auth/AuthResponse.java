package com.securecertificate.api.dto.auth;

import com.securecertificate.api.entity.UserRole;

import java.util.UUID;

public record AuthResponse(

        String accessToken,

        String tokenType,

        long expiresInSeconds,

        UUID userId,

        String fullName,

        String email,

        UserRole role) {
}