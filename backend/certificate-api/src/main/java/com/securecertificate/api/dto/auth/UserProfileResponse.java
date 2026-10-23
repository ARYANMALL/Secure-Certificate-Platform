package com.securecertificate.api.dto.auth;

import com.securecertificate.api.entity.UserRole;

import java.util.UUID;

public record UserProfileResponse(
        UUID userId,
        String email,
        UserRole role) {
}