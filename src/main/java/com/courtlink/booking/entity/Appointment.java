package com.courtlink.booking.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Appointment Entity
 * 
 * @author CourtLink Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "appointments")
@Schema(description = "Appointment Information")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Appointment ID")
    private Long id;

    @NotBlank(message = "User ID cannot be empty")
    @Column(name = "user_id", nullable = false)
    @Schema(description = "User ID")
    private String userId;

    @NotBlank(message = "Provider ID cannot be empty")
    @Column(name = "provider_id", nullable = false)
    @Schema(description = "Service Provider ID")
    private String providerId;

    @Column(name = "court_id")
    @Schema(description = "Court ID")
    private Long courtId;

    @NotBlank(message = "Service type cannot be empty")
    @Column(name = "service_type", nullable = false)
    @Schema(description = "Service Type")
    private String serviceType;

    @NotNull(message = "Appointment start time cannot be empty")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "start_time", nullable = false)
    @Schema(description = "Appointment Start Time")
    private LocalDateTime startTime;

    @NotNull(message = "Appointment end time cannot be empty")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "end_time", nullable = false)
    @Schema(description = "Appointment End Time")
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Schema(description = "Appointment Status")
    private AppointmentStatus status = AppointmentStatus.PENDING;

    @DecimalMin(value = "0.0", message = "Amount cannot be negative")
    @Column(name = "amount", precision = 10, scale = 2)
    @Schema(description = "Appointment Amount")
    private BigDecimal amount;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Column(name = "notes", length = 500)
    @Schema(description = "Notes")
    private String notes;

    @Column(name = "payment_id")
    @Schema(description = "Payment ID")
    private String paymentId;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(description = "Created At")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "updated_at", nullable = false)
    @Schema(description = "Updated At")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    @Schema(description = "Version")
    private Long version;

    /**
     * Appointment Status Enumeration
     */
    public enum AppointmentStatus {
        PENDING("Pending"),
        CONFIRMED("Confirmed"),
        CANCELLED("Cancelled"),
        COMPLETED("Completed"),
        EXPIRED("Expired");

        private final String description;

        AppointmentStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
} 
