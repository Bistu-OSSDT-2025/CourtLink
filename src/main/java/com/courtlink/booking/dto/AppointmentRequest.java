package com.courtlink.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Appointment Request DTO
 * 
 * @author CourtLink Team
 * @version 1.0.0
 */
@Data
@Schema(description = "Appointment request data")
public class AppointmentRequest {

    @NotBlank(message = "User ID cannot be empty")
    @Schema(description = "User ID", required = true, example = "user_001")
    private String userId;

    @NotBlank(message = "Provider ID cannot be empty")
    @Schema(description = "Service provider ID", required = true, example = "provider_001")
    private String providerId;

    @NotBlank(message = "Service type cannot be empty")
    @Schema(description = "Service type", required = true, example = "BADMINTON")
    private String serviceType;

    @NotNull(message = "Appointment start time cannot be empty")
    @Future(message = "Appointment start time must be in the future")
    @Schema(description = "Appointment start time", required = true, example = "2024-03-25T10:00:00")
    private LocalDateTime startTime;

    @NotNull(message = "Appointment end time cannot be empty")
    @Future(message = "Appointment end time must be in the future")
    @Schema(description = "Appointment end time", required = true, example = "2024-03-25T12:00:00")
    private LocalDateTime endTime;

    @DecimalMin(value = "0.0", message = "Amount cannot be negative")
    @Schema(description = "Appointment amount", example = "100.00")
    private BigDecimal amount;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Schema(description = "Appointment notes", example = "Regular booking")
    private String notes;
} 
