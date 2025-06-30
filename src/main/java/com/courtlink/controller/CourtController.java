package com.courtlink.controller;

import com.courtlink.dto.CourtRequest;
import com.courtlink.dto.CourtResponse;
import com.courtlink.enums.CourtStatus;
import com.courtlink.service.CourtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Tag(name = "Court Management", description = "Court management APIs")
@RestController
@RequestMapping("/api/courts")
public class CourtController {

    private final CourtService courtService;

    @Autowired
    public CourtController(CourtService courtService) {
        this.courtService = courtService;
    }

    @PostMapping
    @Operation(summary = "Create new court", description = "Create a new court with provided information")
    public ResponseEntity<CourtResponse> createCourt(
            @Parameter(description = "Court information", required = true)
            @Valid @RequestBody CourtRequest request) {
        CourtResponse response = courtService.createCourt(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update court information", description = "Update existing court information")
    public ResponseEntity<CourtResponse> updateCourt(
            @Parameter(description = "Court ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Court information", required = true)
            @Valid @RequestBody CourtRequest request) {
        CourtResponse response = courtService.updateCourt(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete court", description = "Delete a court by ID")
    public ResponseEntity<Void> deleteCourt(
            @Parameter(description = "Court ID", required = true, example = "1")
            @PathVariable Long id) {
        courtService.deleteCourt(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get court details", description = "Get detailed information of a specific court")
    public ResponseEntity<CourtResponse> getCourt(
            @Parameter(description = "Court ID", required = true, example = "1")
            @PathVariable Long id) {
        CourtResponse response = courtService.getCourt(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Search courts", description = "Search courts by keyword and status")
    public ResponseEntity<List<CourtResponse>> searchCourts(
            @Parameter(description = "Search keyword (court name or location)", example = "basketball")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Court status", example = "AVAILABLE")
            @RequestParam(required = false) CourtStatus status) {
        List<CourtResponse> responses = courtService.searchCourts(keyword, status);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all courts", description = "Retrieve all courts in the system")
    public ResponseEntity<List<CourtResponse>> getAllCourts() {
        List<CourtResponse> responses = courtService.getAllCourts();
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change court status", description = "Update the status of a specific court")
    public ResponseEntity<CourtResponse> changeStatus(
            @Parameter(description = "Court ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "New status", required = true, example = "MAINTENANCE")
            @RequestParam CourtStatus status) {
        CourtResponse response = courtService.changeStatus(id, status);
        return ResponseEntity.ok(response);
    }
} 