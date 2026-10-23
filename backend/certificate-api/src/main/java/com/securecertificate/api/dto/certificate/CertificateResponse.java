package com.securecertificate.api.dto.certificate;

import com.securecertificate.api.entity.CertificateStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CertificateResponse(

        UUID id,

        String certificateNumber,

        UUID studentId,

        String studentName,

        UUID issuerId,

        String issuerName,

        UUID institutionId,

        String institutionName,

        String courseName,

        String description,

        LocalDate issueDate,

        CertificateStatus status,

        String certificateHash,

        String signatureAlgorithm,

        OffsetDateTime revokedAt,

        String revocationReason,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt) {
}