package com.example.appointment;

import com.example.appointment.entity.Appointment;
import com.example.appointment.entity.Payment;
import com.example.appointment.repository.AppointmentRepository;
import com.example.appointment.repository.PaymentRepository;
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
@Tag(name = "支付管理", description = "模拟支付与订单状态管理")
public class PaymentController {
    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;

    @Operation(summary = "模拟支付")
    @PostMapping("/pay")
    public ResponseEntity<?> pay(@RequestParam Long appointmentId, @RequestParam BigDecimal amount) {
        Optional<Appointment> opt = appointmentRepository.findById(appointmentId);
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().body("预约不存在");
        }
        Appointment appointment = opt.get();
        if (appointment.getStatus() != Appointment.AppointmentStatus.CONFIRMED) {
            return ResponseEntity.badRequest().body("预约未确认，无法支付");
        }
        // 模拟支付成功
        Payment payment = new Payment();
        payment.setAppointmentId(appointmentId);
        payment.setAmount(amount);
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);
        // 更新预约状态
        appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
        appointment.setPaymentId(payment.getId().toString());
        appointmentRepository.save(appointment);
        return ResponseEntity.ok("支付成功，预约已完成");
    }

    @Operation(summary = "模拟退款")
    @PostMapping("/refund")
    public ResponseEntity<?> refund(@RequestParam Long appointmentId) {
        Optional<Appointment> opt = appointmentRepository.findById(appointmentId);
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().body("预约不存在");
        }
        Appointment appointment = opt.get();
        if (appointment.getStatus() != Appointment.AppointmentStatus.COMPLETED) {
            return ResponseEntity.badRequest().body("只有已完成预约才能退款");
        }
        // 模拟退款
        Payment payment = paymentRepository.findByAppointmentId(appointmentId).orElse(null);
        if (payment == null) {
            return ResponseEntity.badRequest().body("未找到支付记录");
        }
        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        paymentRepository.save(payment);
        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
        return ResponseEntity.ok("退款成功，预约已取消");
    }

    @Operation(summary = "模拟支付回调")
    @PostMapping("/callback")
    public ResponseEntity<?> paymentCallback(@RequestParam Long appointmentId) {
        Optional<Appointment> opt = appointmentRepository.findById(appointmentId);
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().body("预约不存在");
        }
        Appointment appointment = opt.get();
        // 假设回调即支付成功
        appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);
        return ResponseEntity.ok("支付回调处理成功，预约状态已更新");
    }
} 