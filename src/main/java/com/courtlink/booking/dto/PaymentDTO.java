package com.courtlink.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.courtlink.booking.entity.Payment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment Data Transfer Object
 * 
 * @author CourtLink Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(description = "Payment data transfer object")
@Slf4j
public class PaymentDTO {

    @Schema(description = "Payment ID")
    private Long id;

    @Schema(description = "Payment Number")
    private String paymentNo;

    @NotBlank(message = "User ID cannot be empty")
    @Schema(description = "User ID", required = true)
    private String userId;

    @NotBlank(message = "Appointment ID cannot be empty")
    @Schema(description = "Associated Appointment ID", required = true)
    private String appointmentId;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.0", message = "Amount must be greater than 0")
    @Schema(description = "Payment Amount", required = true)
    private BigDecimal amount;

    @NotNull(message = "Payment status cannot be null")
    @Schema(description = "Payment Status", required = true)
    private Payment.PaymentStatus status;

    @Schema(description = "Transaction ID")
    private String transactionId;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Schema(description = "Notes")
    private String notes;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Created At")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Updated At")
    private LocalDateTime updatedAt;

    @Schema(description = "Version")
    private Long version;

    @Schema(description = "Error Message")
    private String errorMessage;

    @NotNull(message = "Payment method cannot be null")
    @Schema(description = "Payment Method", required = true)
    private Payment.PaymentMethod paymentMethod;

    /**
     * Convert to entity object
     * 
     * @return Payment entity
     */
    public Payment toEntity() {
        return Payment.builder()
                .id(this.id)
                .paymentNo(this.paymentNo)
                .userId(this.userId)
                .appointmentId(this.appointmentId)
                .amount(this.amount)
                .status(this.status)
                .paymentMethod(this.paymentMethod)
                .transactionId(this.transactionId)
                .notes(this.notes)
                .version(this.version)
                .errorMessage(this.errorMessage)
                .build();
    }

    /**
     * Create DTO from entity object
     * 
     * @param payment Payment entity
     * @return Payment DTO
     */
    public static PaymentDTO fromEntity(Payment payment) {
        return PaymentDTO.builder()
                .id(payment.getId())
                .paymentNo(payment.getPaymentNo())
                .userId(payment.getUserId())
                .appointmentId(payment.getAppointmentId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .notes(payment.getNotes())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .version(payment.getVersion())
                .errorMessage(payment.getErrorMessage())
                .build();
    }
} 
