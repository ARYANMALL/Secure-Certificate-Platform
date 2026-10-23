package com.securecertificate.api.dto.certificate;

import java.util.UUID;

public record StudentOptionResponse(
        UUID id,
        String fullName,
        String email) {
}