package com.courtlink.payment.controller;

import com.courtlink.payment.entity.WeChatPayOrder;
import com.courtlink.payment.service.impl.WeChatPayServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payment/wechat")
@RequiredArgsConstructor
public class WeChatPayCallbackController {

    private final WeChatPayServiceImpl weChatPayService;

    /**
     * 微信支付结果通知回调
     */
    @PostMapping("/notify")
    public ResponseEntity<Map<String, String>> paymentNotify(HttpServletRequest request) {
        try {
            // 读取请求体
            String requestBody = readRequestBody(request);
            
            // 获取请求头
            Map<String, String> headers = getRequestHeaders(request);
            
            log.info("收到微信支付回调通知");
            log.debug("请求体：{}", requestBody);
            log.debug("请求头：{}", headers);

            // 处理支付回调
            boolean success = weChatPayService.handlePaymentNotify(requestBody, headers);

            // 构建响应
            Map<String, String> response = new HashMap<>();
            if (success) {
                response.put("code", "SUCCESS");
                response.put("message", "成功");
                log.info("微信支付回调处理成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("code", "FAIL");
                response.put("message", "处理失败");
                log.error("微信支付回调处理失败");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } catch (Exception e) {
            log.error("处理微信支付回调异常", e);
            Map<String, String> response = new HashMap<>();
            response.put("code", "FAIL");
            response.put("message", "系统异常");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 查询订单状态
     */
    @GetMapping("/order/{outTradeNo}/status")
    public ResponseEntity<Map<String, Object>> queryOrderStatus(@PathVariable String outTradeNo) {
        try {
            WeChatPayOrder order = weChatPayService.queryOrderStatus(outTradeNo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "查询成功");
            response.put("data", buildOrderResponse(order));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("查询订单状态失败，订单号：{}", outTradeNo, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 关闭订单
     */
    @PostMapping("/order/{outTradeNo}/close")
    public ResponseEntity<Map<String, Object>> closeOrder(@PathVariable String outTradeNo) {
        try {
            boolean success = weChatPayService.closeOrder(outTradeNo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "订单关闭成功" : "订单关闭失败");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("关闭订单失败，订单号：{}", outTradeNo, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "关闭失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 获取订单统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getOrderStatistics() {
        try {
            Map<String, Object> statistics = weChatPayService.getOrderStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "获取统计信息成功");
            response.put("data", statistics);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取订单统计信息失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取统计信息失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 模拟支付成功回调（仅用于测试）
     */
    @PostMapping("/mock/success/{outTradeNo}")
    public ResponseEntity<Map<String, String>> mockPaymentSuccess(@PathVariable String outTradeNo) {
        try {
            // 构建模拟回调数据
            Map<String, Object> mockData = new HashMap<>();
            mockData.put("out_trade_no", outTradeNo);
            mockData.put("transaction_id", "4200" + System.currentTimeMillis());
            mockData.put("trade_state", "SUCCESS");
            mockData.put("trade_state_desc", "支付成功");
            mockData.put("bank_type", "OTHERS");
            mockData.put("attach", "test");
            mockData.put("success_time", java.time.LocalDateTime.now().toString());

            // 转换为JSON字符串
            String requestBody = new ObjectMapper().writeValueAsString(mockData);
            
            // 处理模拟回调
            boolean success = weChatPayService.handlePaymentNotify(requestBody, new HashMap<>());

            Map<String, String> response = new HashMap<>();
            if (success) {
                response.put("code", "SUCCESS");
                response.put("message", "模拟支付成功");
                log.info("模拟支付成功，订单号：{}", outTradeNo);
            } else {
                response.put("code", "FAIL");
                response.put("message", "模拟支付失败");
                log.error("模拟支付失败，订单号：{}", outTradeNo);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("模拟支付回调异常，订单号：{}", outTradeNo, e);
            Map<String, String> response = new HashMap<>();
            response.put("code", "FAIL");
            response.put("message", "模拟支付异常：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 清理过期订单
     */
    @PostMapping("/cleanup/expired")
    public ResponseEntity<Map<String, Object>> cleanupExpiredOrders() {
        try {
            weChatPayService.cleanExpiredOrders().thenAccept(count -> {
                log.info("异步清理过期订单完成，处理数量：{}", count);
            });

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "清理任务已启动");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("清理过期订单失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "清理失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 读取请求体内容
     */
    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 获取请求头信息
     */
    private Map<String, String> getRequestHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        
        // 微信支付相关的重要请求头
        String[] importantHeaders = {
            "Wechatpay-Signature",
            "Wechatpay-Timestamp",
            "Wechatpay-Nonce",
            "Wechatpay-Serial",
            "Content-Type",
            "User-Agent"
        };
        
        for (String headerName : importantHeaders) {
            String headerValue = request.getHeader(headerName);
            if (headerValue != null) {
                headers.put(headerName, headerValue);
            }
        }
        
        return headers;
    }

    /**
     * 构建订单响应数据
     */
    private Map<String, Object> buildOrderResponse(WeChatPayOrder order) {
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("outTradeNo", order.getOutTradeNo());
        orderData.put("transactionId", order.getTransactionId());
        orderData.put("status", order.getStatus().name());
        orderData.put("statusDesc", order.getStatus().getDescription());
        orderData.put("amount", order.getAmountDecimal());
        orderData.put("currency", order.getCurrency());
        orderData.put("description", order.getDescription());
        orderData.put("qrCodeImage", order.getQrCodeImage());
        orderData.put("codeUrl", order.getCodeUrl());
        orderData.put("expireTime", order.getExpireTime());
        orderData.put("paidTime", order.getPaidTime());
        orderData.put("createdTime", order.getCreatedTime());
        orderData.put("isMock", order.getIsMock());
        orderData.put("canPay", order.canPay());
        orderData.put("isPaid", order.isPaid());
        orderData.put("isExpired", order.isExpired());
        
        return orderData;
    }
} 