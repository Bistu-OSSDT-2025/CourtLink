package com.courtlink.booking.dto;

import com.courtlink.booking.entity.Payment;
import com.courtlink.booking.enums.AppointmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payment Response DTO
 * 
 * @author CourtLink Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payment response data")
public class PaymentResponse {

    @Schema(description = "Payment ID")
    private String paymentId;

    @Schema(description = "Payment status")
    private Payment.PaymentStatus status;

    @Schema(description = "Appointment status")
    private AppointmentStatus appointmentStatus;

    @Schema(description = "Response message")
    private String message;

    @Schema(description = "Error message")
    private String errorMessage;

    @Schema(description = "Transaction ID")
    private String transactionId;
} 