package com.courtlink.payment.service.impl;

import com.courtlink.payment.config.WeChatPayConfig;
import com.courtlink.payment.entity.Payment;
import com.courtlink.payment.entity.WeChatPayOrder;
import com.courtlink.payment.repository.PaymentRepository;
import com.courtlink.payment.repository.WeChatPayOrderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeChatPayServiceImpl {

    private final WeChatPayConfig weChatPayConfig;
    private final WeChatPayOrderRepository weChatPayOrderRepository;
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;

    /**
     * 创建微信支付订单
     */
    @Transactional
    public WeChatPayOrder createPayOrder(Payment payment, String description) {
        try {
            // 生成商户订单号
            String outTradeNo = generateOutTradeNo();
            
            // 创建微信支付订单
            WeChatPayOrder weChatOrder = new WeChatPayOrder();
            weChatOrder.setOutTradeNo(outTradeNo);
            weChatOrder.setPaymentId(payment.getId());
            weChatOrder.setDescription(description);
            weChatOrder.setAmountDecimal(payment.getAmount());
            weChatOrder.setTotalAmount(payment.getAmount().multiply(new BigDecimal("100")).longValue());
            weChatOrder.setCurrency("CNY");
            weChatOrder.setNotifyUrl(weChatPayConfig.getNotifyUrl());
            weChatOrder.setIsMock(weChatPayConfig.isMockMode());
            weChatOrder.setExpireTime(LocalDateTime.now().plusMinutes(weChatPayConfig.getOrderExpireMinutes()));

            // 保存订单
            weChatOrder = weChatPayOrderRepository.save(weChatOrder);

            // 创建支付二维码
            if (weChatPayConfig.isMockMode()) {
                createMockPayOrder(weChatOrder);
            } else {
                createRealPayOrder(weChatOrder);
            }

            // 更新订单信息
            weChatOrder = weChatPayOrderRepository.save(weChatOrder);
            
            log.info("微信支付订单创建成功，订单号：{}", outTradeNo);
            return weChatOrder;

        } catch (Exception e) {
            log.error("创建微信支付订单失败", e);
            throw new RuntimeException("创建微信支付订单失败: " + e.getMessage());
        }
    }

    /**
     * 创建模拟支付订单
     */
    private void createMockPayOrder(WeChatPayOrder order) {
        try {
            // 生成模拟的支付URL
            String mockPayUrl = String.format("weixin://wxpay/bizpayurl?pr=%s", UUID.randomUUID().toString());
            order.setCodeUrl(mockPayUrl);
            
            // 生成二维码图片 - 暂时禁用
            // String qrCodeImage = QrCodeUtil.generateQrCodeBase64(mockPayUrl);
            // order.setQrCodeImage(qrCodeImage);
            order.setQrCodeImage("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+ip1sAAAAASUVORK5CYII=");
            
            // 设置模拟的prepay_id
            order.setPrepayId("wx" + System.currentTimeMillis());
            
            log.info("模拟微信支付订单创建成功，订单号：{}", order.getOutTradeNo());
            
        } catch (Exception e) {
            log.error("创建模拟支付订单失败", e);
            throw new RuntimeException("创建模拟支付订单失败: " + e.getMessage());
        }
    }

    /**
     * 创建真实的微信支付订单
     */
    private void createRealPayOrder(WeChatPayOrder order) {
        try {
            // TODO: 集成真实的微信支付API
            // 这里需要使用微信支付SDK调用统一下单接口
            
            // 构建支付参数
            Map<String, Object> paymentParams = buildPaymentParams(order);
            
            // 调用微信支付API（这里暂时使用模拟实现）
            log.warn("真实微信支付功能需要配置商户证书和密钥");
            
            // 暂时使用模拟实现
            createMockPayOrder(order);
            
        } catch (Exception e) {
            log.error("创建真实微信支付订单失败", e);
            throw new RuntimeException("创建真实微信支付订单失败: " + e.getMessage());
        }
    }

    /**
     * 构建支付参数
     */
    private Map<String, Object> buildPaymentParams(WeChatPayOrder order) {
        Map<String, Object> params = new HashMap<>();
        params.put("appid", weChatPayConfig.getAppId());
        params.put("mchid", weChatPayConfig.getMerchantId());
        params.put("description", order.getDescription());
        params.put("out_trade_no", order.getOutTradeNo());
        params.put("notify_url", order.getNotifyUrl());
        
        // 金额信息
        Map<String, Object> amount = new HashMap<>();
        amount.put("total", order.getTotalAmount());
        amount.put("currency", order.getCurrency());
        params.put("amount", amount);
        
        return params;
    }

    /**
     * 查询订单状态
     */
    public WeChatPayOrder queryOrderStatus(String outTradeNo) {
        try {
            Optional<WeChatPayOrder> orderOpt = weChatPayOrderRepository.findByOutTradeNo(outTradeNo);
            if (orderOpt.isEmpty()) {
                throw new RuntimeException("订单不存在: " + outTradeNo);
            }

            WeChatPayOrder order = orderOpt.get();
            
            // 检查订单是否过期
            if (order.isExpired() && order.getStatus() == WeChatPayOrder.OrderStatus.PENDING) {
                order.setStatus(WeChatPayOrder.OrderStatus.EXPIRED);
                order = weChatPayOrderRepository.save(order);
                log.info("订单已过期，订单号：{}", outTradeNo);
            }

            // 如果是模拟模式，随机模拟支付结果
            if (weChatPayConfig.isMockMode() && order.getStatus() == WeChatPayOrder.OrderStatus.PENDING) {
                simulatePaymentResult(order);
            } else if (!weChatPayConfig.isMockMode()) {
                // 调用微信支付查询接口
                queryRealOrderStatus(order);
            }

            return order;

        } catch (Exception e) {
            log.error("查询订单状态失败，订单号：{}", outTradeNo, e);
            throw new RuntimeException("查询订单状态失败: " + e.getMessage());
        }
    }

    /**
     * 模拟支付结果
     */
    private void simulatePaymentResult(WeChatPayOrder order) {
        try {
            // 模拟支付有30%的概率在30秒内完成
            long orderAge = System.currentTimeMillis() - 
                order.getCreatedTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
            
            if (orderAge > 30000) { // 30秒后
                Random random = new Random();
                if (random.nextInt(100) < 30) { // 30%概率支付成功
                    String mockTransactionId = "4200" + System.currentTimeMillis();
                    order.markAsPaid(mockTransactionId);
                    weChatPayOrderRepository.save(order);
                    
                    // 更新关联的Payment状态
                    updatePaymentStatus(order.getPaymentId(), Payment.PaymentStatus.COMPLETED);
                    
                    log.info("模拟支付成功，订单号：{}，交易号：{}", order.getOutTradeNo(), mockTransactionId);
                }
            }
        } catch (Exception e) {
            log.error("模拟支付结果失败", e);
        }
    }

    /**
     * 查询真实订单状态
     */
    private void queryRealOrderStatus(WeChatPayOrder order) {
        try {
            // TODO: 调用微信支付查询订单接口
            log.info("查询真实订单状态，订单号：{}", order.getOutTradeNo());
            
        } catch (Exception e) {
            log.error("查询真实订单状态失败", e);
        }
    }

    /**
     * 处理支付回调
     */
    @Transactional
    public boolean handlePaymentNotify(String requestBody, Map<String, String> headers) {
        try {
            log.info("收到微信支付回调，内容：{}", requestBody);
            
            if (weChatPayConfig.isMockMode()) {
                return handleMockNotify(requestBody);
            } else {
                return handleRealNotify(requestBody, headers);
            }
            
        } catch (Exception e) {
            log.error("处理支付回调失败", e);
            return false;
        }
    }

    /**
     * 处理模拟回调
     */
    private boolean handleMockNotify(String requestBody) {
        try {
            JsonNode jsonNode = objectMapper.readTree(requestBody);
            String outTradeNo = jsonNode.get("out_trade_no").asText();
            String transactionId = jsonNode.get("transaction_id").asText();
            String tradeState = jsonNode.get("trade_state").asText();
            
            Optional<WeChatPayOrder> orderOpt = weChatPayOrderRepository.findByOutTradeNo(outTradeNo);
            if (orderOpt.isEmpty()) {
                log.warn("回调订单不存在：{}", outTradeNo);
                return false;
            }
            
            WeChatPayOrder order = orderOpt.get();
            order.setCallbackInfo(requestBody);
            
            if ("SUCCESS".equals(tradeState)) {
                order.markAsPaid(transactionId);
                updatePaymentStatus(order.getPaymentId(), Payment.PaymentStatus.COMPLETED);
                log.info("模拟回调支付成功，订单号：{}", outTradeNo);
            } else {
                order.markAsFailed("支付失败：" + tradeState);
                updatePaymentStatus(order.getPaymentId(), Payment.PaymentStatus.FAILED);
                log.info("模拟回调支付失败，订单号：{}，状态：{}", outTradeNo, tradeState);
            }
            
            weChatPayOrderRepository.save(order);
            return true;
            
        } catch (Exception e) {
            log.error("处理模拟回调失败", e);
            return false;
        }
    }

    /**
     * 处理真实回调
     */
    private boolean handleRealNotify(String requestBody, Map<String, String> headers) {
        try {
            // TODO: 验证微信支付回调签名
            // TODO: 解密回调数据
            // TODO: 处理支付结果
            
            log.info("处理真实微信支付回调");
            return true;
            
        } catch (Exception e) {
            log.error("处理真实回调失败", e);
            return false;
        }
    }

    /**
     * 关闭订单
     */
    @Transactional
    public boolean closeOrder(String outTradeNo) {
        try {
            Optional<WeChatPayOrder> orderOpt = weChatPayOrderRepository.findByOutTradeNo(outTradeNo);
            if (orderOpt.isEmpty()) {
                throw new RuntimeException("订单不存在: " + outTradeNo);
            }

            WeChatPayOrder order = orderOpt.get();
            if (order.isPaid()) {
                throw new RuntimeException("订单已支付，无法关闭: " + outTradeNo);
            }

            order.markAsClosed();
            weChatPayOrderRepository.save(order);
            
            // 更新关联的Payment状态
            updatePaymentStatus(order.getPaymentId(), Payment.PaymentStatus.CANCELLED);
            
            log.info("订单已关闭，订单号：{}", outTradeNo);
            return true;

        } catch (Exception e) {
            log.error("关闭订单失败，订单号：{}", outTradeNo, e);
            return false;
        }
    }

    /**
     * 更新支付状态
     */
    private void updatePaymentStatus(Long paymentId, Payment.PaymentStatus status) {
        try {
            Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                payment.setStatus(status);
                payment.setUpdatedAt(LocalDateTime.now());
                paymentRepository.save(payment);
                log.info("更新支付状态成功，支付ID：{}，状态：{}", paymentId, status);
            }
        } catch (Exception e) {
            log.error("更新支付状态失败，支付ID：{}", paymentId, e);
        }
    }

    /**
     * 生成商户订单号
     */
    private String generateOutTradeNo() {
        String prefix = "WX";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return prefix + timestamp + random;
    }

    /**
     * 清理过期订单
     */
    @Async
    @Transactional
    public CompletableFuture<Integer> cleanExpiredOrders() {
        try {
            LocalDateTime currentTime = LocalDateTime.now();
            int updatedCount = weChatPayOrderRepository.updateExpiredOrders(currentTime);
            log.info("清理过期订单完成，更新数量：{}", updatedCount);
            return CompletableFuture.completedFuture(updatedCount);
        } catch (Exception e) {
            log.error("清理过期订单失败", e);
            return CompletableFuture.completedFuture(0);
        }
    }

    /**
     * 获取订单统计信息
     */
    public Map<String, Object> getOrderStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // 统计各状态订单数量
            for (WeChatPayOrder.OrderStatus status : WeChatPayOrder.OrderStatus.values()) {
                long count = weChatPayOrderRepository.countByStatus(status);
                statistics.put(status.name().toLowerCase() + "_count", count);
            }
            
            // 统计今日订单金额
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
            Long todayAmount = weChatPayOrderRepository.sumTotalAmountByPaidTimeBetween(startOfDay, endOfDay);
            statistics.put("today_amount", todayAmount != null ? todayAmount : 0L);
            
            return statistics;
            
        } catch (Exception e) {
            log.error("获取订单统计信息失败", e);
            return new HashMap<>();
        }
    }
} 