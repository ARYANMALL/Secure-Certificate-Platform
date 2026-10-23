package com.securecertificate.api.dto.certificate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RevokeCertificateRequest(

                @NotBlank(message = "Revocation reason is required") @Size(max = 500, message = "Revocation reason must not exceed 500 characters") String reason) {
}