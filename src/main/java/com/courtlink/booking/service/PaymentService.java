package com.courtlink.booking.service;

import com.courtlink.booking.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Payment Service Interface
 * 
 * @author CourtLink Team
 * @version 1.0.0
 */
public interface PaymentService {

    /**
     * Create payment
     * 
     * @param payment Payment information
     * @return Created payment
     */
    Payment createPayment(Payment payment);

    /**
     * Process payment
     * 
     * @param paymentNo Payment number
     * @param paymentMethod Payment method
     * @return Processed payment
     */
    Payment processPayment(String paymentNo, Payment.PaymentMethod paymentMethod);

    /**
     * Mock payment processing
     * 
     * @param paymentNo Payment number
     * @return Processed payment
     */
    Payment processMockPayment(String paymentNo);

    /**
     * Handle payment callback
     * 
     * @param paymentNo Payment number
     * @param transactionId Transaction ID
     * @param success Whether successful
     * @return Updated payment
     */
    Payment handlePaymentCallback(String paymentNo, String transactionId, boolean success);

    /**
     * Process refund
     * 
     * @param paymentNo Payment number
     * @param refundAmount Refund amount
     * @param reason Refund reason
     * @return Updated payment
     */
    Payment processRefund(String paymentNo, BigDecimal refundAmount, String reason);

    /**
     * Cancel payment
     * 
     * @param paymentNo Payment number
     * @return Cancelled payment
     */
    Payment cancelPayment(String paymentNo);

    /**
     * Retry failed payment
     * 
     * @param paymentNo Payment number
     * @return Retried payment
     */
    Payment retryFailedPayment(String paymentNo);

    /**
     * Sum payment amount by user and status
     * 
     * @param userId User ID
     * @param status Payment status
     * @return Total amount
     */
    BigDecimal sumAmountByUserIdAndStatus(String userId, Payment.PaymentStatus status);

    /**
     * Get payment by number
     * 
     * @param paymentNo Payment number
     * @return Payment if found
     */
    Payment getPaymentByNo(String paymentNo);

    /**
     * Get payments by user ID
     * 
     * @param userId User ID
     * @param pageable Pagination
     * @return Page of payments
     */
    Page<Payment> getPaymentsByUserId(String userId, Pageable pageable);

    /**
     * Get payments by status
     * 
     * @param status Payment status
     * @param pageable Pagination
     * @return Page of payments
     */
    Page<Payment> getPaymentsByStatus(Payment.PaymentStatus status, Pageable pageable);

    /**
     * Get pending payments
     * 
     * @return List of pending payments
     */
    List<Payment> getPendingPayments();

    /**
     * Payment status enumeration
     */
    enum PaymentStatus {
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
} 
