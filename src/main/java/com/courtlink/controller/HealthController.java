package com.courtlink.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller
 * Provides system health check endpoints
 */
@Slf4j
@RestController
@RequestMapping("/api/health")
@Tag(name = "Health Check", description = "System health check API")
public class HealthController {

    @GetMapping("/simple")
    @Operation(summary = "Simple health check", description = "Simple service availability check")
    public ResponseEntity<Map<String, String>> simpleHealthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/live")
    @Operation(summary = "Liveness check", description = "Service liveness check")
    public ResponseEntity<Map<String, String>> livenessCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ready")
    @Operation(summary = "Readiness check", description = "Service readiness check")
    public ResponseEntity<Map<String, String>> readinessCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        return ResponseEntity.ok(response);
    }
} 