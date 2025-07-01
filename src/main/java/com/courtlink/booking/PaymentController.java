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
@Tag(name = "֧������", description = "ģ��֧���붩��״̬����")
public class PaymentController {
    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;

    @Operation(summary = "ģ��֧��")
    @PostMapping("/pay")
    public ResponseEntity<ApiResponse<String>> pay(@RequestParam Long appointmentId, @RequestParam BigDecimal amount) {
        Optional<Appointment> opt = appointmentRepository.findById(appointmentId);
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "ԤԼ������", null));
        }
        Appointment appointment = opt.get();
        if (appointment.getStatus() != Appointment.AppointmentStatus.CONFIRMED) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "ԤԼδȷ�ϣ��޷�֧��", null));
        }
        // ģ��֧���ɹ�
        Payment payment = new Payment();
        payment.setAppointmentId(appointmentId);
        payment.setAmount(amount);
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);
        // ����ԤԼ״̬
        appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
        appointment.setPaymentId(payment.getId().toString());
        appointmentRepository.save(appointment);
        return ResponseEntity.ok(new ApiResponse<>(true, "֧���ɹ���ԤԼ�����?, null));
    }

    @Operation(summary = "ģ���˿�")
    @PostMapping("/refund")
    public ResponseEntity<ApiResponse<String>> refund(@RequestParam Long appointmentId) {
        Optional<Appointment> opt = appointmentRepository.findById(appointmentId);
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "ԤԼ������", null));
        }
        Appointment appointment = opt.get();
        if (appointment.getStatus() != Appointment.AppointmentStatus.COMPLETED) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "ֻ�������ԤԼ�����˿�?, null));
        }
        // ģ���˿�
        Payment payment = paymentRepository.findByAppointmentId(appointmentId).orElse(null);
        if (payment == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "δ�ҵ�֧����¼", null));
        }
        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        paymentRepository.save(payment);
        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
        return ResponseEntity.ok(new ApiResponse<>(true, "�˿�ɹ���ԤԼ��ȡ��?, null));
    }

    @Operation(summary = "ģ��֧���ص�")
    @PostMapping("/callback")
    public ResponseEntity<ApiResponse<String>> paymentCallback(@RequestParam Long appointmentId) {
        Optional<Appointment> opt = appointmentRepository.findById(appointmentId);
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "ԤԼ������", null));
        }
        Appointment appointment = opt.get();
        // ����ص���֧���ɹ�?
        appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);
        return ResponseEntity.ok(new ApiResponse<>(true, "֧���ص������ɹ���ԤԼ״̬�Ѹ���", null));
    }
} 
