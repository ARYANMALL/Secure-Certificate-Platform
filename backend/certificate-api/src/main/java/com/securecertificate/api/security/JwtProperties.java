package com.securecertificate.api.security;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "security.jwt")
@Validated
public record JwtProperties(

        @NotBlank String issuer,

        @NotBlank String secret,

        @Min(1) long expirationMinutes) {
}