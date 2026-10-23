package com.securecertificate.api.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;

@Service
public class CertificateCanonicalizer {

    private static final String CANONICAL_VERSION = "1";

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    /**
     * Creates the exact representation of certificate data
     * that will be hashed and digitally signed.
     *
     * IMPORTANT:
     * The order of these fields must remain stable.
     *
     * If this format changes in the future, increase
     * CANONICAL_VERSION and support both versions during
     * verification if old certificates still exist.
     */
    public String canonicalize(
            String certificateNumber,
            UUID studentId,
            String studentName,
            UUID issuerId,
            String issuerName,
            UUID institutionId,
            String institutionName,
            String courseName,
            String description,
            LocalDate issueDate) {

        return String.join(
                "\n",

                "version=" + CANONICAL_VERSION,

                "certificateNumber=" +
                        encode(certificateNumber),

                "studentId=" +
                        encode(studentId.toString()),

                "studentName=" +
                        encode(studentName),

                "issuerId=" +
                        encode(issuerId.toString()),

                "issuerName=" +
                        encode(issuerName),

                "institutionId=" +
                        encode(institutionId.toString()),

                "institutionName=" +
                        encode(institutionName),

                "courseName=" +
                        encode(courseName),

                "description=" +
                        encodeNullable(description),

                "issueDate=" +
                        encode(issueDate.toString()));
    }

    private String encode(String value) {

        if (value == null) {
            throw new IllegalArgumentException(
                    "Canonical certificate value cannot be null");
        }

        return ENCODER.encodeToString(
                value.getBytes(StandardCharsets.UTF_8));
    }

    private String encodeNullable(String value) {

        if (value == null) {
            return "-";
        }

        return encode(value);
    }
}