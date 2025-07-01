package com.courtlink.booking;

import com.courtlink.booking.entity.Appointment;
import com.courtlink.booking.entity.Payment;
import com.courtlink.booking.repository.AppointmentRepository;
import com.courtlink.booking.repository.PaymentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "Payment processing and status management API")
public class PaymentController {
    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;

    @Operation(summary = "Process payment")
    @PostMapping("/pay")
    public ResponseEntity<ApiResponse<String>> pay(@RequestParam Long appointmentId, @RequestParam BigDecimal amount) {
        Optional<Appointment> opt = appointmentRepository.findById(appointmentId);
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Appointment not found", null));
        }
        Appointment appointment = opt.get();
        if (appointment.getStatus() != Appointment.AppointmentStatus.CONFIRMED) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Appointment not confirmed, cannot process payment", null));
        }
        // Simulate successful payment
        Payment payment = new Payment();
        payment.setAppointmentId(appointmentId.toString());
        payment.setAmount(amount);
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);
        // Update appointment status
        appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
        appointment.setPaymentId(payment.getId().toString());
        appointmentRepository.save(appointment);
        return ResponseEntity.ok(new ApiResponse<>(true, "Payment successful, appointment completed", null));
    }

    @Operation(summary = "Process refund")
    @PostMapping("/refund")
    public ResponseEntity<ApiResponse<String>> refund(@RequestParam Long appointmentId) {
        Optional<Appointment> opt = appointmentRepository.findById(appointmentId);
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Appointment not found", null));
        }
        Appointment appointment = opt.get();
        if (appointment.getStatus() != Appointment.AppointmentStatus.COMPLETED) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Only completed appointments can be refunded", null));
        }
        // Simulate refund
        Payment payment = paymentRepository.findByAppointmentId(appointmentId.toString()).orElse(null);
        if (payment == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Payment record not found", null));
        }
        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        paymentRepository.save(payment);
        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
        return ResponseEntity.ok(new ApiResponse<>(true, "Refund successful, appointment cancelled", null));
    }

    @Operation(summary = "Payment callback")
    @PostMapping("/callback")
    public ResponseEntity<ApiResponse<String>> paymentCallback(@RequestParam Long appointmentId) {
        Optional<Appointment> opt = appointmentRepository.findById(appointmentId);
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Appointment not found", null));
        }
        Appointment appointment = opt.get();
        // Assume callback indicates successful payment
        appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);
        return ResponseEntity.ok(new ApiResponse<>(true, "Payment callback processed successfully, appointment status updated", null));
    }
} 
