package com.courtlink.payment.dto;

import com.courtlink.booking.entity.BookingStatus;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class PaymentRequest {
    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Booking ID cannot be null")
    private Long bookingId;

    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Payment method cannot be null")
    private String paymentMethod;
} 