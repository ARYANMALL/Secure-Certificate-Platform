package com.securecertificate.api.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateIssuerRequest(

        @NotBlank(message = "Full name is required") @Size(max = 150, message = "Full name must not exceed 150 characters") String fullName,

        @NotBlank(message = "Email is required") @Email(message = "Email must be valid") @Size(max = 320, message = "Email must not exceed 320 characters") String email,

        @NotBlank(message = "Password is required") @Size(min = 12, max = 72, message = "Password must be between 12 and 72 characters") String password,

        @NotNull(message = "Institution ID is required") UUID institutionId) {
}