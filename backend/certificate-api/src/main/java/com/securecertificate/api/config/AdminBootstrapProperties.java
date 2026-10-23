package com.securecertificate.api.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.bootstrap.admin")
@Validated
public record AdminBootstrapProperties(

        @NotBlank String fullName,

        @NotBlank @Email String email,

        @NotBlank @Size(min = 12, max = 72) String password) {
}