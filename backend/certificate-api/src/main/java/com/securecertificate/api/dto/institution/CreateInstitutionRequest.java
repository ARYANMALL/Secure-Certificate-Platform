package com.securecertificate.api.dto.institution;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateInstitutionRequest(

                @NotBlank(message = "Institution name is required") @Size(max = 200, message = "Institution name must not exceed 200 characters") String name,

                @NotBlank(message = "Registration number is required") @Size(max = 100, message = "Registration number must not exceed 100 characters") String registrationNumber,

                @NotBlank(message = "Institution email is required") @Email(message = "Email must be valid") @Size(max = 320, message = "Email must not exceed 320 characters") String email,

                @NotBlank(message = "Address is required") @Size(max = 500, message = "Address must not exceed 500 characters") String address) {
}