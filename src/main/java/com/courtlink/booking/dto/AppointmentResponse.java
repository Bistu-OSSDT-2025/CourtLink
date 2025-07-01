package com.courtlink.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.courtlink.booking.entity.Appointment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Appointment Response DTO
 * 
 * @author CourtLink Team
 * @version 1.0.0
 */
@Data
@Schema(description = "Appointment response data")
public class AppointmentResponse {

    @Schema(description = "Appointment ID")
    private Long id;

    @Schema(description = "User ID")
    private String userId;

    @Schema(description = "Service provider ID")
    private String providerId;

    @Schema(description = "Service type")
    private String serviceType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Appointment start time")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Appointment end time")
    private LocalDateTime endTime;

    @Schema(description = "Appointment status")
    private Appointment.AppointmentStatus status;

    @Schema(description = "Appointment amount")
    private BigDecimal amount;

    @Schema(description = "Notes")
    private String notes;

    @Schema(description = "Payment ID")
    private String paymentId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Created at")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Updated at")
    private LocalDateTime updatedAt;

    /**
     * Create from entity
     * 
     * @param appointment Appointment entity
     * @return AppointmentResponse
     */
    public static AppointmentResponse fromEntity(Appointment appointment) {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(appointment.getId());
        response.setUserId(appointment.getUserId());
        response.setProviderId(appointment.getProviderId());
        response.setServiceType(appointment.getServiceType());
        response.setStartTime(appointment.getStartTime());
        response.setEndTime(appointment.getEndTime());
        response.setStatus(appointment.getStatus());
        response.setAmount(appointment.getAmount());
        response.setNotes(appointment.getNotes());
        response.setPaymentId(appointment.getPaymentId());
        response.setCreatedAt(appointment.getCreatedAt());
        response.setUpdatedAt(appointment.getUpdatedAt());
        return response;
    }
} 
