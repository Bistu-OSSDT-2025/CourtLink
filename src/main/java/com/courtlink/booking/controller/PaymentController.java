package com.courtlink.booking.controller;

import com.courtlink.booking.ApiResponse;
import com.courtlink.booking.entity.Appointment;
import com.courtlink.booking.entity.Payment;
import com.courtlink.booking.repository.AppointmentRepository;
import com.courtlink.booking.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "Payment processing and status management API")
public class PaymentController {
    private final PaymentService paymentService;
    private final AppointmentRepository appointmentRepository;

    @Operation(summary = "Process payment for appointment")
    @PostMapping("/pay")
    public ResponseEntity<ApiResponse<String>> pay(
            @Parameter(description = "Appointment ID") @RequestParam Long appointmentId, 
            @Parameter(description = "Payment amount") @RequestParam BigDecimal amount,
            @Parameter(description = "Payment method") @RequestParam(defaultValue = "CREDIT_CARD") Payment.PaymentMethod paymentMethod) {
        
        Optional<Appointment> opt = appointmentRepository.findById(appointmentId);
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Appointment not found", null));
        }
        
        Appointment appointment = opt.get();
        if (appointment.getStatus() != Appointment.AppointmentStatus.CONFIRMED) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Appointment not confirmed, cannot process payment", null));
        }
        
        try {
            // Create payment record
            Payment payment = new Payment();
            payment.setAppointmentId(appointmentId.toString());
            payment.setUserId(appointment.getUserId());
            payment.setAmount(amount);
            payment.setPaymentMethod(paymentMethod);
            
            Payment createdPayment = paymentService.createPayment(payment);
            
            // Process payment
            Payment processedPayment = paymentService.processPayment(createdPayment.getPaymentNo(), paymentMethod);
            
            if (processedPayment.getStatus() == Payment.PaymentStatus.SUCCESS) {
                // Update appointment status
                appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
                appointment.setPaymentId(processedPayment.getId().toString());
                appointmentRepository.save(appointment);
                
                return ResponseEntity.ok(new ApiResponse<>(true, "Payment successful, appointment completed", 
                        processedPayment.getPaymentNo()));
            } else {
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Payment failed", null));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Payment processing error: " + e.getMessage(), null));
        }
    }

    @Operation(summary = "Process refund for payment")
    @PostMapping("/{paymentNo}/refund")
    public ResponseEntity<ApiResponse<String>> refund(
            @Parameter(description = "Payment number") @PathVariable String paymentNo,
            @Parameter(description = "Refund amount") @RequestParam(name = "amount", required = false) BigDecimal refundAmount,
            @Parameter(description = "Refund reason") @RequestParam(required = false) String reason) {
        
        try {
            Payment payment = paymentService.getPaymentByNo(paymentNo);
            if (payment == null) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Payment not found", null));
            }
            
            // Use full payment amount if refund amount not specified
            BigDecimal actualRefundAmount = refundAmount != null ? refundAmount : payment.getAmount();
            String actualReason = reason != null ? reason : "Customer requested refund";
            
            Payment refundedPayment = paymentService.processRefund(paymentNo, actualRefundAmount, actualReason);
            
            // Update appointment status if full refund
            if (actualRefundAmount.equals(payment.getAmount())) {
                Optional<Appointment> opt = appointmentRepository.findById(Long.valueOf(payment.getAppointmentId()));
                if (opt.isPresent()) {
                    Appointment appointment = opt.get();
                    appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
                    appointmentRepository.save(appointment);
                }
            }
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Refund processed successfully", refundedPayment.getPaymentNo()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Refund processing error: " + e.getMessage(), null));
        }
    }

    @Operation(summary = "Handle payment callback from payment gateway")
    @PostMapping("/callback")
    public ResponseEntity<ApiResponse<String>> paymentCallback(
            @Parameter(description = "Payment number") @RequestParam String paymentNo,
            @Parameter(description = "Transaction ID") @RequestParam String transactionId,
            @Parameter(description = "Payment success status") @RequestParam boolean success) {
        
        try {
            Payment payment = paymentService.handlePaymentCallback(paymentNo, transactionId, success);
            
            if (success && payment.getStatus() == Payment.PaymentStatus.SUCCESS) {
                // Update appointment status
                Optional<Appointment> opt = appointmentRepository.findById(Long.valueOf(payment.getAppointmentId()));
                if (opt.isPresent()) {
                    Appointment appointment = opt.get();
                    appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
                    appointment.setPaymentId(payment.getId().toString());
                    appointmentRepository.save(appointment);
                }
            }
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Payment callback processed successfully", payment.getPaymentNo()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Callback processing error: " + e.getMessage(), null));
        }
    }

    @Operation(summary = "Get payment by payment number")
    @GetMapping("/{paymentNo}")
    public ResponseEntity<ApiResponse<Payment>> getPayment(
            @Parameter(description = "Payment number") @PathVariable String paymentNo) {
        
        Payment payment = paymentService.getPaymentByNo(paymentNo);
        if (payment == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Payment not found", null));
        }
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Payment retrieved successfully", payment));
    }

    @Operation(summary = "Get payments by user ID")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<Payment>>> getUserPayments(
            @Parameter(description = "User ID") @PathVariable String userId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Payment> payments = paymentService.getPaymentsByUserId(userId, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "User payments retrieved successfully", payments));
    }

    @Operation(summary = "Get payments by status")
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<Page<Payment>>> getPaymentsByStatus(
            @Parameter(description = "Payment status") @PathVariable Payment.PaymentStatus status,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Payment> payments = paymentService.getPaymentsByStatus(status, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Payments by status retrieved successfully", payments));
    }

    @Operation(summary = "Cancel payment")
    @PostMapping("/{paymentNo}/cancel")
    public ResponseEntity<ApiResponse<String>> cancelPayment(
            @Parameter(description = "Payment number") @PathVariable String paymentNo) {
        
        try {
            Payment payment = paymentService.cancelPayment(paymentNo);
            return ResponseEntity.ok(new ApiResponse<>(true, "Payment cancelled successfully", payment.getPaymentNo()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Cancel payment error: " + e.getMessage(), null));
        }
    }

    @Operation(summary = "Retry failed payment")
    @PostMapping("/{paymentNo}/retry")
    public ResponseEntity<ApiResponse<String>> retryPayment(
            @Parameter(description = "Payment number") @PathVariable String paymentNo) {
        
        try {
            Payment payment = paymentService.retryFailedPayment(paymentNo);
            return ResponseEntity.ok(new ApiResponse<>(true, "Payment retry initiated successfully", payment.getPaymentNo()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Retry payment error: " + e.getMessage(), null));
        }
    }

    @Operation(summary = "Get pending payments")
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<Payment>>> getPendingPayments() {
        List<Payment> payments = paymentService.getPendingPayments();
        return ResponseEntity.ok(new ApiResponse<>(true, "Pending payments retrieved successfully", payments));
    }
} 