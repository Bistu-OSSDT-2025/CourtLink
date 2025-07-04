package com.courtlink.payment.controller;

import com.courtlink.payment.entity.Payment;
import com.courtlink.payment.entity.WeChatPayOrder;
import com.courtlink.payment.service.impl.PaymentServiceImpl;
import com.courtlink.payment.service.impl.WeChatPayServiceImpl;
import com.courtlink.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    
    private final PaymentServiceImpl paymentService;
    private final WeChatPayServiceImpl weChatPayService;
    private final JwtService jwtService;
    
    /**
     * 创建支付订单
     */
    @PostMapping
    public ResponseEntity<?> createPayment(@RequestBody Map<String, Object> request, 
                                         HttpServletRequest httpRequest) {
        try {
            // 从JWT token中获取用户ID
            String authHeader = httpRequest.getHeader("Authorization");
            String token = null;
            String username = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                username = jwtService.extractUsername(token);
            }
            // 这里简化处理，实际应该从用户服务获取用户ID
            Long userId = 1L; // 临时硬编码，实际项目中应该从用户服务获取
            
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String paymentMethodStr = request.get("paymentMethod").toString().toUpperCase();
            Payment.PaymentMethod paymentMethod = Payment.PaymentMethod.valueOf(paymentMethodStr);
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> bookingSlots = (List<Map<String, Object>>) request.get("bookingSlots");
            
            String description = (String) request.get("description");
            
            Payment payment = paymentService.createPayment(userId, amount, paymentMethod, bookingSlots, description);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("paymentId", payment.getPaymentId());
            response.put("amount", payment.getAmount());
            response.put("status", payment.getStatus());
            response.put("message", "支付订单创建成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("创建支付订单失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建支付订单失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 处理支付
     */
    @PostMapping("/{paymentId}/process")
    public ResponseEntity<?> processPayment(@PathVariable String paymentId, 
                                          @RequestBody Map<String, String> request) {
        try {
            String paymentMethod = request.get("paymentMethod");
            
            // 处理微信支付
            if ("WECHAT".equalsIgnoreCase(paymentMethod)) {
                return processWeChatPayment(paymentId, request);
            }
            
            // 处理其他支付方式
            Payment payment = paymentService.processPayment(paymentId, paymentMethod);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("paymentId", payment.getPaymentId());
            response.put("status", payment.getStatus());
            response.put("amount", payment.getAmount());
            
            if (payment.getStatus() == Payment.PaymentStatus.COMPLETED) {
                response.put("message", "支付成功");
                response.put("transactionId", payment.getExternalTransactionId());
            } else {
                response.put("message", "支付失败，请重试");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("处理支付失败: paymentId={}", paymentId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "支付处理失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 处理微信支付
     */
    private ResponseEntity<?> processWeChatPayment(String paymentId, Map<String, String> request) {
        try {
            // 获取支付记录
            Payment payment = paymentService.getPaymentStatus(paymentId);
            
            // 构建商品描述
            String description = String.format("羽毛球场预约支付 - 订单号:%s", paymentId);
            
            // 创建微信支付订单
            WeChatPayOrder weChatOrder = weChatPayService.createPayOrder(payment, description);
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("paymentId", payment.getPaymentId());
            response.put("paymentMethod", "WECHAT");
            response.put("status", "PENDING");
            response.put("message", "微信支付订单创建成功，请扫描二维码完成支付");
            
            // 微信支付相关信息
            Map<String, Object> wechatInfo = new HashMap<>();
            wechatInfo.put("outTradeNo", weChatOrder.getOutTradeNo());
            wechatInfo.put("codeUrl", weChatOrder.getCodeUrl());
            wechatInfo.put("qrCodeImage", weChatOrder.getQrCodeImage());
            wechatInfo.put("expireTime", weChatOrder.getExpireTime());
            wechatInfo.put("amount", weChatOrder.getAmountDecimal());
            wechatInfo.put("isMock", weChatOrder.getIsMock());
            
            response.put("wechatPayInfo", wechatInfo);
            
            log.info("微信支付订单创建成功: paymentId={}, outTradeNo={}", paymentId, weChatOrder.getOutTradeNo());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("处理微信支付失败: paymentId={}", paymentId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "微信支付处理失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取支付状态
     */
    @GetMapping("/{paymentId}/status")
    public ResponseEntity<?> getPaymentStatus(@PathVariable String paymentId) {
        try {
            Payment payment = paymentService.getPaymentStatus(paymentId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("paymentId", payment.getPaymentId());
            response.put("status", payment.getStatus().toString().toLowerCase());
            response.put("amount", payment.getAmount());
            response.put("paymentMethod", payment.getPaymentMethod());
            response.put("createdAt", payment.getCreatedAt());
            response.put("updatedAt", payment.getUpdatedAt());
            response.put("completedAt", payment.getCompletedAt());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取支付状态失败: paymentId={}", paymentId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取支付状态失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 取消支付
     */
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<?> cancelPayment(@PathVariable String paymentId) {
        try {
            Payment payment = paymentService.cancelPayment(paymentId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("paymentId", payment.getPaymentId());
            response.put("status", payment.getStatus());
            response.put("message", "支付已取消");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("取消支付失败: paymentId={}", paymentId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "取消支付失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取用户的支付记录
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyPayments(HttpServletRequest httpRequest) {
        try {
            // 从JWT token中获取用户ID
            String authHeader = httpRequest.getHeader("Authorization");
            String token = null;
            String username = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                username = jwtService.extractUsername(token);
            }
            // 这里简化处理，实际应该从用户服务获取用户ID
            Long userId = 1L; // 临时硬编码
            
            List<Payment> payments = paymentService.getUserPayments(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("payments", payments);
            response.put("total", payments.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取用户支付记录失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取支付记录失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 