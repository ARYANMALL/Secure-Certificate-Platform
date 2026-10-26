package com.securecertificate.api.dto.certificate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record IssuerCreateCertificateRequest(

        @NotNull(message = "Student is required") UUID studentId,

        @NotBlank(message = "Course name is required") @Size(max = 200, message = "Course name must not exceed 200 characters") String courseName,

        @Size(max = 2000, message = "Description must not exceed 2000 characters") String description

) {
}
