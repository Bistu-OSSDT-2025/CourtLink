package com.courtlink.payment.dto;

import com.courtlink.booking.entity.Payment;
import com.courtlink.booking.entity.BookingStatus;
import lombok.Data;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentDTO {
    private Long id;
    private Long userId;
    private Long bookingId;
    private String paymentNo;
    private BookingStatus status;
    private String paymentMethod;
    private BigDecimal amount;
    private LocalDateTime paymentTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 