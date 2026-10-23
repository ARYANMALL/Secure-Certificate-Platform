package com.securecertificate.api.controller;

import com.securecertificate.api.dto.certificate.StudentOptionResponse;
import com.securecertificate.api.service.IssuerStudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/issuer/students")
public class IssuerStudentController {

    private final IssuerStudentService issuerStudentService;

    public IssuerStudentController(
            IssuerStudentService issuerStudentService) {

        this.issuerStudentService = issuerStudentService;
    }

    @GetMapping
    public ResponseEntity<List<StudentOptionResponse>> findStudents(
            JwtAuthenticationToken authentication) {

        UUID issuerId = UUID.fromString(
                authentication
                        .getToken()
                        .getSubject());

        return ResponseEntity.ok(
                issuerStudentService
                        .findActiveStudents(issuerId));
    }
}