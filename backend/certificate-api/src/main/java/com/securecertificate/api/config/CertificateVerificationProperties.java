package com.securecertificate.api.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.verification")
@Validated
public record CertificateVerificationProperties(

        @NotBlank String baseUrl) {
}