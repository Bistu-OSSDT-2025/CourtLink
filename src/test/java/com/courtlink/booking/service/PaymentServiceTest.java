package com.courtlink.booking.service;

import com.courtlink.booking.entity.Payment;
import com.courtlink.booking.repository.PaymentRepository;
import com.courtlink.booking.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PaymentService Unit Tests
 * 
 * @author CourtLink Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Payment Service Tests")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = new Payment();
        payment.setId(1L);
        payment.setPaymentNo("PAY123456789");
        payment.setAppointmentId("1");
        payment.setUserId("user123");
        payment.setAmount(new BigDecimal("50.00"));
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should create payment successfully")
    void shouldCreatePaymentSuccessfully() {
        // Given
        Payment newPayment = new Payment();
        newPayment.setAppointmentId("1");
        newPayment.setUserId("user123");
        newPayment.setAmount(new BigDecimal("50.00"));
        
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        Payment result = paymentService.createPayment(newPayment);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(Payment.PaymentStatus.PENDING);
        
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should process payment successfully")
    void shouldProcessPaymentSuccessfully() {
        // Given
        lenient().when(paymentRepository.findByPaymentNo("PAY123456789")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        Payment result = paymentService.processPayment("PAY123456789", Payment.PaymentMethod.CREDIT_CARD);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPaymentMethod()).isEqualTo(Payment.PaymentMethod.CREDIT_CARD);
        
        verify(paymentRepository, atLeastOnce()).findByPaymentNo("PAY123456789");
        verify(paymentRepository, atLeastOnce()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw exception when payment not found for processing")
    void shouldThrowExceptionWhenPaymentNotFoundForProcessing() {
        // Given
        when(paymentRepository.findByPaymentNo("PAY123456789")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> paymentService.processPayment("PAY123456789", Payment.PaymentMethod.CREDIT_CARD))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");

        verify(paymentRepository).findByPaymentNo("PAY123456789");
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should process mock payment successfully")
    void shouldProcessMockPaymentSuccessfully() {
        // Given
        lenient().when(paymentRepository.findByPaymentNo("PAY123456789")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        Payment result = paymentService.processMockPayment("PAY123456789");

        // Then
        assertThat(result).isNotNull();
        
        verify(paymentRepository, atLeastOnce()).findByPaymentNo("PAY123456789");
        verify(paymentRepository, atLeastOnce()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should handle payment callback successfully")
    void shouldHandlePaymentCallbackSuccessfully() {
        // Given
        when(paymentRepository.findByPaymentNo("PAY123456789")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        Payment result = paymentService.handlePaymentCallback("PAY123456789", "TXN123", true);

        // Then
        assertThat(result).isNotNull();
        
        verify(paymentRepository).findByPaymentNo("PAY123456789");
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should process refund successfully")
    void shouldProcessRefundSuccessfully() {
        // Given
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        when(paymentRepository.findByPaymentNo("PAY123456789")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        Payment result = paymentService.processRefund("PAY123456789", new BigDecimal("30.00"), "Customer request");

        // Then
        assertThat(result).isNotNull();
        
        verify(paymentRepository).findByPaymentNo("PAY123456789");
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw exception when refund amount exceeds payment amount")
    void shouldThrowExceptionWhenRefundAmountExceedsPaymentAmount() {
        // Given
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        when(paymentRepository.findByPaymentNo("PAY123456789")).thenReturn(Optional.of(payment));

        // When & Then
        assertThatThrownBy(() -> paymentService.processRefund("PAY123456789", new BigDecimal("100.00"), "Customer request"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("cannot exceed");

        verify(paymentRepository).findByPaymentNo("PAY123456789");
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should cancel payment successfully")
    void shouldCancelPaymentSuccessfully() {
        // Given
        when(paymentRepository.findByPaymentNo("PAY123456789")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        Payment result = paymentService.cancelPayment("PAY123456789");

        // Then
        assertThat(result).isNotNull();
        
        verify(paymentRepository).findByPaymentNo("PAY123456789");
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should retry failed payment successfully")
    void shouldRetryFailedPaymentSuccessfully() {
        // Given
        payment.setStatus(Payment.PaymentStatus.FAILED);
        lenient().when(paymentRepository.findByPaymentNo("PAY123456789")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        Payment result = paymentService.retryFailedPayment("PAY123456789");

        // Then
        assertThat(result).isNotNull();
        
        verify(paymentRepository, atLeastOnce()).findByPaymentNo("PAY123456789");
        verify(paymentRepository, atLeastOnce()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should get payment by number")
    void shouldGetPaymentByNumber() {
        // Given
        when(paymentRepository.findByPaymentNo("PAY123456789")).thenReturn(Optional.of(payment));

        // When
        Payment result = paymentService.getPaymentByNo("PAY123456789");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPaymentNo()).isEqualTo("PAY123456789");
        
        verify(paymentRepository).findByPaymentNo("PAY123456789");
    }

    @Test
    @DisplayName("Should get payments by user ID")
    void shouldGetPaymentsByUserId() {
        // Given
        List<Payment> payments = Arrays.asList(payment);
        Page<Payment> page = new PageImpl<>(payments);
        Pageable pageable = PageRequest.of(0, 10);
        
        when(paymentRepository.findByUserId("user123", pageable)).thenReturn(page);

        // When
        Page<Payment> result = paymentService.getPaymentsByUserId("user123", pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUserId()).isEqualTo("user123");
        
        verify(paymentRepository).findByUserId("user123", pageable);
    }

    @Test
    @DisplayName("Should get payments by status")
    void shouldGetPaymentsByStatus() {
        // Given
        List<Payment> payments = Arrays.asList(payment);
        Page<Payment> page = new PageImpl<>(payments);
        Pageable pageable = PageRequest.of(0, 10);
        
        when(paymentRepository.findByStatus(Payment.PaymentStatus.PENDING, pageable)).thenReturn(page);

        // When
        Page<Payment> result = paymentService.getPaymentsByStatus(Payment.PaymentStatus.PENDING, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(Payment.PaymentStatus.PENDING);
        
        verify(paymentRepository).findByStatus(Payment.PaymentStatus.PENDING, pageable);
    }

    @Test
    @DisplayName("Should get pending payments")
    void shouldGetPendingPayments() {
        // Given
        List<Payment> payments = Arrays.asList(payment);
        when(paymentRepository.findByStatus(Payment.PaymentStatus.PENDING)).thenReturn(payments);

        // When
        List<Payment> result = paymentService.getPendingPayments();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(Payment.PaymentStatus.PENDING);
        
        verify(paymentRepository).findByStatus(Payment.PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("Should sum amount by user ID and status")
    void shouldSumAmountByUserIdAndStatus() {
        // Given
        when(paymentRepository.sumAmountByUserIdAndStatus("user123", Payment.PaymentStatus.SUCCESS))
                .thenReturn(new BigDecimal("150.00"));

        // When
        BigDecimal result = paymentService.sumAmountByUserIdAndStatus("user123", Payment.PaymentStatus.SUCCESS);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(new BigDecimal("150.00"));
        
        verify(paymentRepository).sumAmountByUserIdAndStatus("user123", Payment.PaymentStatus.SUCCESS);
    }
} 