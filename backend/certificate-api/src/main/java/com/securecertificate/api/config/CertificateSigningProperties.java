package com.securecertificate.api.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.certificate-signing")
@Validated
public record CertificateSigningProperties(

        @NotBlank String keystorePath,

        @NotBlank String keystorePassword,

        @NotBlank String keyAlias,

        @NotBlank String signatureAlgorithm) {
}