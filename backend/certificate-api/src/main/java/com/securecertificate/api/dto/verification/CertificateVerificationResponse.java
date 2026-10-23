package com.securecertificate.api.dto.verification;

import com.securecertificate.api.entity.CertificateStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record CertificateVerificationResponse(

        boolean valid,

        boolean integrityValid,

        boolean signatureValid,

        String message,

        String certificateNumber,

        String studentName,

        String issuerName,

        String institutionName,

        String courseName,

        LocalDate issueDate,

        CertificateStatus status,

        OffsetDateTime verifiedAt) {
}