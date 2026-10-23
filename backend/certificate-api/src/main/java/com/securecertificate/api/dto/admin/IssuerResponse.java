package com.securecertificate.api.dto.admin;

import com.securecertificate.api.entity.UserRole;
import com.securecertificate.api.entity.UserStatus;

import java.util.UUID;

public record IssuerResponse(

        UUID userId,

        String fullName,

        String email,

        UserRole role,

        UserStatus status,

        UUID institutionId) {
}