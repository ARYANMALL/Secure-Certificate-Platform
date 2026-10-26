package com.securecertificate.api.dto.certificate;

import com.securecertificate.api.entity.CertificateRequestStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CertificateRequestResponse(

        UUID id,

        UUID studentId,

        String studentName,

        UUID issuerId,

        String issuerName,

        UUID institutionId,

        String institutionName,

        String courseName,

        String description,

        CertificateRequestStatus status,

        UUID certificateId,

        String certificateNumber,

        String rejectionReason,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt

) {
}