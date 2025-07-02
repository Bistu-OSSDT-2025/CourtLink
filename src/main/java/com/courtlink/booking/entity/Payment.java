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
 * Payment Entity
 * 
 * @author CourtLink Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "payments")
@Schema(description = "Payment Information")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Payment ID")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    @Schema(description = "Associated Booking")
    private Booking booking;

    @NotNull(message = "支付编号不能为空")
    @Column(name = "payment_no", nullable = false, unique = true)
    @Schema(description = "Payment Number")
    private String paymentNo;

    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于0")
    @DecimalMax(value = "99999.99", message = "支付金额不能超过99999.99")
    @Digits(integer = 5, fraction = 2, message = "支付金额格式不正确，整数部分不能超过5位，小数部分不能超过2位")
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    @Schema(description = "Payment Amount")
    private BigDecimal amount;

    @DecimalMin(value = "0.00", message = "退款金额不能为负数")
    @DecimalMax(value = "99999.99", message = "退款金额不能超过99999.99")
    @Digits(integer = 5, fraction = 2, message = "退款金额格式不正确，整数部分不能超过5位，小数部分不能超过2位")
    @Column(name = "refund_amount", precision = 10, scale = 2)
    @Schema(description = "Refund Amount")
    private BigDecimal refundAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Schema(description = "Payment Status")
    private PaymentStatus status = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    @Schema(description = "Payment Method")
    private PaymentMethod paymentMethod;

    @Column(name = "transaction_id")
    @Schema(description = "Transaction ID")
    private String transactionId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "paid_at")
    @Schema(description = "Payment Time")
    private LocalDateTime paidAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "refund_at")
    @Schema(description = "Refund Time")
    private LocalDateTime refundAt;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Column(name = "notes", length = 500)
    @Schema(description = "Notes")
    private String notes;

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

    public void setBooking(Booking booking) {
        this.booking = booking;
        if (booking != null && booking.getPayment() != this) {
            booking.setPayment(this);
        }
    }

    public Long getBookingId() {
        return booking != null ? booking.getId() : null;
    }

    public Long getUserId() {
        return booking != null ? booking.getUserId() : null;
    }

    /**
     * Payment Status Enumeration
     */
    public enum PaymentStatus {
        PENDING("Pending"),
        PROCESSING("Processing"),
        SUCCESS("Success"),
        FAILED("Failed"),
        CANCELLED("Cancelled"),
        REFUNDED("Refunded");

        private final String description;

        PaymentStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Payment Method Enumeration
     */
    public enum PaymentMethod {
        CREDIT_CARD("Credit Card"),
        DEBIT_CARD("Debit Card"),
        PAYPAL("PayPal"),
        BANK_TRANSFER("Bank Transfer"),
        CASH("Cash"),
        OTHER("Other");

        private final String description;

        PaymentMethod(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
} 
