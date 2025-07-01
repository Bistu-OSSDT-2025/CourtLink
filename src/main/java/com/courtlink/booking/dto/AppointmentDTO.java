package com.courtlink.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.courtlink.booking.entity.Appointment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Appointment Data Transfer Object
 * 
 * @author CourtLink Team
 * @version 1.0.0
 */
@Data
@Schema(description = "Appointment data transfer object")
public class AppointmentDTO {

    @Schema(description = "Appointment ID")
    private Long id;

    @NotBlank(message = "User ID cannot be empty")
    @Schema(description = "User ID", required = true)
    private String userId;

    @NotBlank(message = "Provider ID cannot be empty")
    @Schema(description = "Service Provider ID", required = true)
    private String providerId;

    @NotBlank(message = "Service type cannot be empty")
    @Schema(description = "Service Type", required = true)
    private String serviceType;

    @NotNull(message = "Appointment start time cannot be empty")
    @Future(message = "Appointment start time must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Appointment Start Time", required = true)
    private LocalDateTime startTime;

    @NotNull(message = "Appointment end time cannot be empty")
    @Future(message = "Appointment end time must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Appointment End Time", required = true)
    private LocalDateTime endTime;

    @Schema(description = "Appointment Status")
    private Appointment.AppointmentStatus status;

    @DecimalMin(value = "0.0", message = "Amount cannot be negative")
    @Schema(description = "Appointment Amount")
    private BigDecimal amount;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Schema(description = "Notes")
    private String notes;

    @Schema(description = "Payment ID")
    private String paymentId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Created At")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Updated At")
    private LocalDateTime updatedAt;

    @Schema(description = "Version")
    private Long version;

    /**
     * Convert to entity object
     * 
     * @return Appointment entity
     */
    public Appointment toEntity() {
        Appointment appointment = new Appointment();
        appointment.setId(this.id);
        appointment.setUserId(this.userId);
        appointment.setProviderId(this.providerId);
        appointment.setServiceType(this.serviceType);
        appointment.setStartTime(this.startTime);
        appointment.setEndTime(this.endTime);
        appointment.setStatus(this.status);
        appointment.setAmount(this.amount);
        appointment.setNotes(this.notes);
        appointment.setPaymentId(this.paymentId);
        appointment.setVersion(this.version);
        return appointment;
    }

    /**
     * Create DTO from entity object
     * 
     * @param appointment Appointment entity
     * @return Appointment DTO
     */
    public static AppointmentDTO fromEntity(Appointment appointment) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(appointment.getId());
        dto.setUserId(appointment.getUserId());
        dto.setProviderId(appointment.getProviderId());
        dto.setServiceType(appointment.getServiceType());
        dto.setStartTime(appointment.getStartTime());
        dto.setEndTime(appointment.getEndTime());
        dto.setStatus(appointment.getStatus());
        dto.setAmount(appointment.getAmount());
        dto.setNotes(appointment.getNotes());
        dto.setPaymentId(appointment.getPaymentId());
        dto.setCreatedAt(appointment.getCreatedAt());
        dto.setUpdatedAt(appointment.getUpdatedAt());
        dto.setVersion(appointment.getVersion());
        return dto;
    }
} 