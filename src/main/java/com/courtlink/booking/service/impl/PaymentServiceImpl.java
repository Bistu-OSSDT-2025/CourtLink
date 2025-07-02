package com.courtlink.booking.service.impl;

import com.courtlink.booking.entity.Payment;
import com.courtlink.booking.entity.Appointment;
import com.courtlink.booking.repository.PaymentRepository;
import com.courtlink.booking.repository.AppointmentRepository;
import com.courtlink.booking.service.PaymentService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Set;

/**
 * Payment Service Implementation
 * 
 * @author CourtLink Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final Validator validator;

    @Override
    @Transactional
    public Payment createPayment(Payment payment) {
        log.info("Creating payment: {}", payment);

        // 验证支付对象
        Set<ConstraintViolation<Payment>> violations = validator.validate(payment);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        // 设置默认值
        if (payment.getStatus() == null) {
            payment.setStatus(Payment.PaymentStatus.PENDING);
        }
        if (payment.getCreatedAt() == null) {
            payment.setCreatedAt(LocalDateTime.now());
        }
        if (payment.getUpdatedAt() == null) {
            payment.setUpdatedAt(LocalDateTime.now());
        }

        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public Payment processPayment(String paymentNo, Payment.PaymentMethod paymentMethod) {
        Payment payment = getPaymentByNo(paymentNo);
        if (payment == null) {
            throw new IllegalArgumentException("Payment not found: " + paymentNo);
        }

        if (payment.getStatus() != Payment.PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment cannot be processed: " + payment.getStatus());
        }

        // 模拟支付处理
        Payment processedPayment = processMockPayment(paymentNo);

        // 如果支付成功，更新预约的支付ID
        if (processedPayment.getStatus() == Payment.PaymentStatus.SUCCESS) {
            Appointment appointment = appointmentRepository.findById(Long.valueOf(processedPayment.getAppointmentId()))
                    .orElseThrow(() -> new IllegalStateException("Appointment not found: " + processedPayment.getAppointmentId()));
            appointment.setPaymentId(processedPayment.getId().toString());
            appointmentRepository.save(appointment);
        }

        return processedPayment;
    }

    @Override
    @Transactional
    public Payment processMockPayment(String paymentNo) {
        Payment payment = getPaymentByNo(paymentNo);
        if (payment == null) {
            throw new IllegalArgumentException("Payment not found: " + paymentNo);
        }

        // 模拟支付成功率80%
        boolean success = Math.random() < 0.8;
        String transactionId = UUID.randomUUID().toString();

        return handlePaymentCallback(paymentNo, transactionId, success);
    }

    @Override
    @Transactional
    public Payment handlePaymentCallback(String paymentNo, String transactionId, boolean success) {
        Payment payment = getPaymentByNo(paymentNo);
        if (payment == null) {
            throw new IllegalArgumentException("Payment not found: " + paymentNo);
        }

        payment.setTransactionId(transactionId);
        payment.setStatus(success ? Payment.PaymentStatus.SUCCESS : Payment.PaymentStatus.FAILED);
        payment.setPaidAt(success ? LocalDateTime.now() : null);
        payment.setUpdatedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public Payment processRefund(String paymentNo, BigDecimal refundAmount, String reason) {
        Payment payment = getPaymentByNo(paymentNo);
        if (payment == null) {
            throw new IllegalArgumentException("Payment not found: " + paymentNo);
        }

        if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Payment cannot be refunded: " + payment.getStatus());
        }

        if (refundAmount.compareTo(payment.getAmount()) > 0) {
            throw new IllegalArgumentException("Refund amount cannot exceed payment amount");
        }

        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        payment.setRefundAmount(refundAmount);
        payment.setRefundAt(LocalDateTime.now());
        payment.setNotes(reason);
        payment.setUpdatedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public Payment cancelPayment(String paymentNo) {
        Payment payment = getPaymentByNo(paymentNo);
        if (payment == null) {
            throw new IllegalArgumentException("Payment not found: " + paymentNo);
        }

        if (payment.getStatus() != Payment.PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment cannot be cancelled: " + payment.getStatus());
        }

        payment.setStatus(Payment.PaymentStatus.CANCELLED);
        payment.setUpdatedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public Payment retryFailedPayment(String paymentNo) {
        Payment payment = getPaymentByNo(paymentNo);
        if (payment == null) {
            throw new IllegalArgumentException("Payment not found: " + paymentNo);
        }

        if (payment.getStatus() != Payment.PaymentStatus.FAILED) {
            throw new IllegalStateException("Only failed payments can be retried");
        }

        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        return processPayment(paymentNo, payment.getPaymentMethod());
    }

    @Override
    public BigDecimal sumAmountByUserIdAndStatus(String userId, Payment.PaymentStatus status) {
        return paymentRepository.sumAmountByUserIdAndStatus(userId, status);
    }

    @Override
    public Payment getPaymentByNo(String paymentNo) {
        return paymentRepository.findByPaymentNo(paymentNo).orElse(null);
    }

    @Override
    public Page<Payment> getPaymentsByUserId(String userId, Pageable pageable) {
        log.debug("Getting payments by user ID: userId={}", userId);
        return paymentRepository.findByUserId(userId, pageable);
    }

    @Override
    public Page<Payment> getPaymentsByStatus(Payment.PaymentStatus status, Pageable pageable) {
        log.debug("Getting payments by status: status={}", status);
        return paymentRepository.findByStatus(status, pageable);
    }

    @Override
    public List<Payment> getPendingPayments() {
        log.debug("Getting pending payments");
        return paymentRepository.findByStatus(Payment.PaymentStatus.PENDING);
    }

    private String generatePaymentNo() {
        return "PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
} 