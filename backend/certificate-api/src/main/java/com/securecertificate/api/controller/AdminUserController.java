package com.securecertificate.api.controller;

import com.securecertificate.api.dto.admin.CreateIssuerRequest;
import com.securecertificate.api.dto.admin.IssuerResponse;
import com.securecertificate.api.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(
            AdminUserService adminUserService) {

        this.adminUserService = adminUserService;
    }

    @PostMapping("/issuers")
    public ResponseEntity<IssuerResponse> createIssuer(
            @Valid @RequestBody CreateIssuerRequest request) {

        IssuerResponse response = adminUserService.createIssuer(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}