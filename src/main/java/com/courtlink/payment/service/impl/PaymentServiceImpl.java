package com.courtlink.payment.service.impl;

import com.courtlink.payment.entity.Payment;
import com.courtlink.payment.repository.PaymentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl {
    
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * 创建支付订单
     */
    public Payment createPayment(Long userId, BigDecimal amount, Payment.PaymentMethod paymentMethod, 
                               List<Map<String, Object>> bookingSlots, String description) {
        try {
            Payment payment = new Payment();
            payment.setPaymentId(generatePaymentId());
            payment.setUserId(userId);
            payment.setAmount(amount);
            payment.setPaymentMethod(paymentMethod);
            payment.setStatus(Payment.PaymentStatus.PENDING);
            payment.setDescription(description != null ? description : "场地预约支付");
            
            // 将预约信息转换为JSON存储
            if (bookingSlots != null && !bookingSlots.isEmpty()) {
                String appointmentJson = objectMapper.writeValueAsString(bookingSlots);
                payment.setAppointmentIds(appointmentJson);
            }
            
            return paymentRepository.save(payment);
        } catch (JsonProcessingException e) {
            log.error("创建支付订单时JSON转换失败", e);
            throw new RuntimeException("创建支付订单失败", e);
        }
    }
    
    /**
     * 处理支付
     */
    public Payment processPayment(String paymentId, String paymentMethod) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
            .orElseThrow(() -> new RuntimeException("支付订单不存在"));
        
        if (payment.getStatus() != Payment.PaymentStatus.PENDING) {
            throw new RuntimeException("支付订单状态错误");
        }
        
        try {
            // 更新支付状态为处理中
            payment.setStatus(Payment.PaymentStatus.PROCESSING);
            paymentRepository.save(payment);
            
            // 模拟支付处理
            boolean paymentSuccess = simulatePaymentProcess(payment, paymentMethod);
            
            if (paymentSuccess) {
                payment.setStatus(Payment.PaymentStatus.COMPLETED);
                payment.setCompletedAt(LocalDateTime.now());
                payment.setExternalTransactionId(generateExternalTransactionId());
                log.info("支付成功: paymentId={}, amount={}", paymentId, payment.getAmount());
            } else {
                payment.setStatus(Payment.PaymentStatus.FAILED);
                log.warn("支付失败: paymentId={}", paymentId);
            }
            
            return paymentRepository.save(payment);
        } catch (Exception e) {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            paymentRepository.save(payment);
            log.error("处理支付时发生错误: paymentId={}", paymentId, e);
            throw new RuntimeException("支付处理失败", e);
        }
    }
    
    /**
     * 获取支付状态
     */
    public Payment getPaymentStatus(String paymentId) {
        return paymentRepository.findByPaymentId(paymentId)
            .orElseThrow(() -> new RuntimeException("支付订单不存在"));
    }
    
    /**
     * 获取用户的支付记录
     */
    public List<Payment> getUserPayments(Long userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * 取消支付
     */
    public Payment cancelPayment(String paymentId) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
            .orElseThrow(() -> new RuntimeException("支付订单不存在"));
        
        if (payment.getStatus() != Payment.PaymentStatus.PENDING) {
            throw new RuntimeException("只能取消待支付状态的订单");
        }
        
        payment.setStatus(Payment.PaymentStatus.CANCELLED);
        return paymentRepository.save(payment);
    }
    
    /**
     * 模拟支付处理过程
     */
    private boolean simulatePaymentProcess(Payment payment, String paymentMethod) {
        try {
            // 模拟支付处理延时
            Thread.sleep(1000);
            
            // 模拟90%的支付成功率
            return Math.random() > 0.1;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    /**
     * 生成支付ID
     */
    private String generatePaymentId() {
        return "PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * 生成第三方交易ID
     */
    private String generateExternalTransactionId() {
        return "TXN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
} 