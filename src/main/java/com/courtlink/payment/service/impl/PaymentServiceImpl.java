package com.courtlink.payment.service.impl;

import com.courtlink.booking.entity.Payment;
import com.courtlink.booking.entity.Payment.PaymentStatus;
import com.courtlink.booking.entity.Payment.PaymentMethod;
import com.courtlink.booking.entity.BookingStatus;
import com.courtlink.payment.dto.PaymentDTO;
import com.courtlink.payment.dto.PaymentRequest;
import com.courtlink.payment.repository.PaymentRepository;
import com.courtlink.payment.service.PaymentService;
import com.courtlink.payment.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional(readOnly = true)
    public PaymentDTO findById(Long id) {
        return paymentRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDTO> findByUserId(Long userId) {
        return paymentRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDTO> findByStatus(BookingStatus status, Pageable pageable) {
        return paymentRepository.findByStatus(PaymentStatus.valueOf(status.name()), pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    public PaymentDTO createPayment(PaymentRequest request) {
        Payment payment = new Payment();
        payment.setPaymentNo(generatePaymentNo());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaidAt(LocalDateTime.now());
        payment.setTransactionId(generateTransactionId());
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        
        return convertToDTO(paymentRepository.save(payment));
    }

    @Override
    @Transactional
    public PaymentDTO updatePayment(Long id, PaymentRequest request) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()));
        payment.setUpdatedAt(LocalDateTime.now());
        
        return convertToDTO(paymentRepository.save(payment));
    }

    @Override
    @Transactional
    public PaymentDTO updateStatus(Long id, BookingStatus status) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        
        payment.setStatus(PaymentStatus.valueOf(status.name()));
        payment.setUpdatedAt(LocalDateTime.now());
        
        return convertToDTO(paymentRepository.save(payment));
    }

    private PaymentDTO convertToDTO(Payment payment) {
        return PaymentDTO.builder()
                .id(payment.getId())
                .userId(payment.getUserId())
                .bookingId(payment.getBookingId())
                .paymentNo(payment.getPaymentNo())
                .status(BookingStatus.valueOf(payment.getStatus().name()))
                .paymentMethod(payment.getPaymentMethod().name())
                .amount(payment.getAmount())
                .paymentTime(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    private String generateTransactionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String generatePaymentNo() {
        return "PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
} 