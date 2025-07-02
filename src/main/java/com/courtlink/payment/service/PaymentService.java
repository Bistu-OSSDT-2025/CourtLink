package com.courtlink.payment.service;

import com.courtlink.booking.entity.Payment;
import com.courtlink.booking.entity.BookingStatus;
import com.courtlink.payment.dto.PaymentRequest;
import com.courtlink.payment.dto.PaymentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PaymentService {
    PaymentDTO findById(Long id);
    
    List<PaymentDTO> findByUserId(Long userId);
    
    Page<PaymentDTO> findByStatus(BookingStatus status, Pageable pageable);
    
    PaymentDTO createPayment(PaymentRequest request);
    
    PaymentDTO updatePayment(Long id, PaymentRequest request);
    
    PaymentDTO updateStatus(Long id, BookingStatus status);
} 