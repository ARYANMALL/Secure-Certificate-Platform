package com.securecertificate.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController  // This class contains HTTP API endpoints.
public class HealthController {

    @GetMapping("/api/health")   // "When someone sends a GET request to /api/health, call this method."
    public String health() {
        return "Secure Certificate API is running";
    }
}