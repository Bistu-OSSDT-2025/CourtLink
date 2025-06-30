package com.example.appointment.service;

import com.example.appointment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 支付管理服务接口
 * 
 * @author Your Name
 * @version 1.0.0
 */
public interface PaymentService {

    /**
     * 创建支付订单
     * 
     * @param payment 支付信息
     * @return 创建的支付订单
     */
    Payment createPayment(Payment payment);

    /**
     * 处理支付
     * 
     * @param paymentNo 支付订单号
     * @param paymentMethod 支付方式
     * @return 处理后的支付订单
     */
    Payment processPayment(String paymentNo, Payment.PaymentMethod paymentMethod);

    /**
     * 模拟支付处理
     * 
     * @param paymentNo 支付订单号
     * @return 处理后的支付订单
     */
    Payment processMockPayment(String paymentNo);

    /**
     * 处理支付回调
     * 
     * @param paymentNo 支付订单号
     * @param transactionId 第三方交易ID
     * @param success 是否成功
     * @return 处理后的支付订单
     */
    Payment handlePaymentCallback(String paymentNo, String transactionId, boolean success);

    /**
     * 退款处理
     * 
     * @param paymentNo 支付订单号
     * @param refundAmount 退款金额
     * @param reason 退款原因
     * @return 处理后的支付订单
     */
    Payment processRefund(String paymentNo, BigDecimal refundAmount, String reason);

    /**
     * 取消支付
     * 
     * @param paymentNo 支付订单号
     * @return 取消后的支付订单
     */
    Payment cancelPayment(String paymentNo);

    /**
     * 根据支付订单号查询支付信息
     * 
     * @param paymentNo 支付订单号
     * @return 支付信息
     */
    Payment getPaymentByPaymentNo(String paymentNo);

    /**
     * 根据用户ID查询支付列表
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 支付分页列表
     */
    Page<Payment> getPaymentsByUserId(String userId, Pageable pageable);

    /**
     * 根据状态查询支付列表
     * 
     * @param status 支付状态
     * @param pageable 分页参数
     * @return 支付分页列表
     */
    Page<Payment> getPaymentsByStatus(Payment.PaymentStatus status, Pageable pageable);

    /**
     * 根据支付方式查询支付列表
     * 
     * @param paymentMethod 支付方式
     * @param pageable 分页参数
     * @return 支付分页列表
     */
    Page<Payment> getPaymentsByPaymentMethod(Payment.PaymentMethod paymentMethod, Pageable pageable);

    /**
     * 查询指定时间范围内的支付记录
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 支付列表
     */
    List<Payment> getPaymentsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 处理超时支付
     * 
     * @return 处理的支付数量
     */
    int processTimeoutPayments();

    /**
     * 重试失败的支付
     * 
     * @param paymentNo 支付订单号
     * @return 重试后的支付订单
     */
    Payment retryFailedPayment(String paymentNo);

    /**
     * 统计用户支付金额
     * 
     * @param userId 用户ID
     * @param status 支付状态
     * @return 支付总金额
     */
    BigDecimal sumAmountByUserIdAndStatus(String userId, Payment.PaymentStatus status);

    /**
     * 统计支付成功率
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 支付成功率
     */
    Double calculateSuccessRate(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 生成支付订单号
     * 
     * @return 支付订单号
     */
    String generatePaymentNo();

    /**
     * 验证支付订单
     * 
     * @param payment 支付订单
     * @return 是否有效
     */
    boolean validatePayment(Payment payment);

    /**
     * 发送支付通知
     * 
     * @param payment 支付订单
     * @param notificationType 通知类型
     */
    void sendPaymentNotification(Payment payment, String notificationType);
} 