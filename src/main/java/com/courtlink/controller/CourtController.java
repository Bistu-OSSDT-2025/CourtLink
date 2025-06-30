package com.courtlink.controller;

import com.courtlink.dto.CourtRequest;
import com.courtlink.dto.CourtResponse;
import com.courtlink.enums.CourtStatus;
import com.courtlink.service.CourtService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "Court Management")
@RestController
@RequestMapping("/api/courts")
public class CourtController {

    private final CourtService courtService;

    @Autowired
    public CourtController(CourtService courtService) {
        this.courtService = courtService;
    }

    @PostMapping
    @ApiOperation("Create new court")
    public ResponseEntity<CourtResponse> createCourt(
            @ApiParam(value = "Court information", required = true)
            @Valid @RequestBody CourtRequest request) {
        CourtResponse response = courtService.createCourt(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @ApiOperation("Update court information")
    public ResponseEntity<CourtResponse> updateCourt(
            @ApiParam(value = "Court ID", required = true, example = "1")
            @PathVariable Long id,
            @ApiParam(value = "Court information", required = true)
            @Valid @RequestBody CourtRequest request) {
        CourtResponse response = courtService.updateCourt(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("Delete court")
    public ResponseEntity<Void> deleteCourt(
            @ApiParam(value = "Court ID", required = true, example = "1")
            @PathVariable Long id) {
        courtService.deleteCourt(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @ApiOperation("Get court details")
    public ResponseEntity<CourtResponse> getCourt(
            @ApiParam(value = "Court ID", required = true, example = "1")
            @PathVariable Long id) {
        CourtResponse response = courtService.getCourt(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @ApiOperation("Search courts")
    public ResponseEntity<List<CourtResponse>> searchCourts(
            @ApiParam(value = "Search keyword (court name or location)", example = "basketball")
            @RequestParam(required = false) String keyword,
            @ApiParam(value = "Court status", example = "AVAILABLE")
            @RequestParam(required = false) CourtStatus status) {
        List<CourtResponse> responses = courtService.searchCourts(keyword, status);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/all")
    @ApiOperation("Get all courts")
    public ResponseEntity<List<CourtResponse>> getAllCourts() {
        List<CourtResponse> responses = courtService.getAllCourts();
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{id}/status")
    @ApiOperation("Change court status")
    public ResponseEntity<CourtResponse> changeStatus(
            @ApiParam(value = "Court ID", required = true, example = "1")
            @PathVariable Long id,
            @ApiParam(value = "New status", required = true, example = "MAINTENANCE")
            @RequestParam CourtStatus status) {
        CourtResponse response = courtService.changeStatus(id, status);
        return ResponseEntity.ok(response);
    }
} 