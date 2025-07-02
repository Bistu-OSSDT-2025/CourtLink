package com.courtlink.payment.controller;

import com.courtlink.booking.entity.BookingStatus;
import com.courtlink.payment.dto.PaymentDTO;
import com.courtlink.payment.dto.PaymentRequest;
import com.courtlink.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment API", description = "支付管理接口")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "创建支付")
    public ResponseEntity<PaymentDTO> createPayment(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.createPayment(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取支付详情")
    public ResponseEntity<PaymentDTO> getPayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.findById(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户支付列表")
    public ResponseEntity<List<PaymentDTO>> getUserPayments(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentService.findByUserId(userId));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "按状态查询支付")
    public ResponseEntity<Page<PaymentDTO>> getPaymentsByStatus(
            @PathVariable BookingStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(paymentService.findByStatus(status, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新支付信息")
    public ResponseEntity<PaymentDTO> updatePayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.updatePayment(id, request));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新支付状态")
    public ResponseEntity<PaymentDTO> updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam BookingStatus status) {
        return ResponseEntity.ok(paymentService.updateStatus(id, status));
    }
} 