package com.securecertificate.api.dto.institution;

import com.securecertificate.api.entity.InstitutionStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record InstitutionResponse(
        UUID id,
        String name,
        String registrationNumber,
        String email,
        String address,
        InstitutionStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}