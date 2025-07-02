package com.courtlink.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller
 * Provides system health check endpoints
 */
@Slf4j
@RestController
@Tag(name = "Health Check", description = "System health check API")
public class HealthController {

    @GetMapping("/api/health/simple")
    @Operation(summary = "Simple health check", description = "Simple service availability check")
    public ResponseEntity<Map<String, String>> simpleHealthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/health/live")
    @Operation(summary = "Liveness check", description = "Service liveness check")
    public ResponseEntity<Map<String, String>> livenessCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/health/ready")
    @Operation(summary = "Readiness check", description = "Service readiness check")
    public ResponseEntity<Map<String, String>> readinessCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/public/health")
    @Operation(summary = "Public health check", description = "Public health check endpoint for testing")
    public ResponseEntity<Map<String, Object>> publicHealthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "CourtLink Booking System");
        return ResponseEntity.ok(response);
    }
} 