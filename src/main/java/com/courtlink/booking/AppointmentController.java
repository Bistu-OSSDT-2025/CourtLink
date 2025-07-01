package com.courtlink.booking;

import com.courtlink.booking.dto.AppointmentRequest;
import com.courtlink.booking.dto.AppointmentResponse;
import com.courtlink.booking.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointment Management", description = "Appointment booking API")
public class AppointmentController {
    private final AppointmentService appointmentService;

    @Operation(summary = "Create appointment")
    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(
            @Valid @RequestBody AppointmentRequest request,
            @RequestParam String userId) {
        AppointmentResponse created = appointmentService.createAppointment(userId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Appointment created successfully", created));
    }

    @Operation(summary = "Get appointment by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointment(@PathVariable Long id) {
        AppointmentResponse appointment = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Query successful", appointment));
    }

    @Operation(summary = "Cancel appointment")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancelAppointment(@PathVariable Long id) {
        AppointmentResponse cancelled = appointmentService.cancelAppointment(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Cancellation successful", cancelled));
    }

    @Operation(summary = "Complete appointment")
    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<AppointmentResponse>> completeAppointment(@PathVariable Long id) {
        AppointmentResponse completed = appointmentService.completeAppointment(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Completion successful", completed));
    }

    @Operation(summary = "Get appointments by user ID")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getAppointmentsByUserId(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        Page<AppointmentResponse> appointments = appointmentService.getAppointmentsByUserId(userId, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Query successful", appointments));
    }

    @Operation(summary = "Get appointments by provider ID")
    @GetMapping("/provider/{providerId}")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getAppointmentsByProviderId(
            @PathVariable String providerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        Page<AppointmentResponse> appointments = appointmentService.getAppointmentsByProviderId(providerId, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Query successful", appointments));
    }
} 
