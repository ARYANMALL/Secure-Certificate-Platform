package com.securecertificate.api.controller;

import com.securecertificate.api.dto.auth.UserProfileResponse;
import com.securecertificate.api.entity.UserRole;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public UserProfileResponse me(
            JwtAuthenticationToken authentication) {

        var jwt = authentication.getToken();

        return new UserProfileResponse(
                UUID.fromString(jwt.getSubject()),
                jwt.getClaimAsString("email"),
                UserRole.valueOf(
                        jwt.getClaimAsString("role")));
    }
}