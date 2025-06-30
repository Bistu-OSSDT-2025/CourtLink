package com.example.appointment.repository;

import com.example.appointment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 支付数据访问层接口
 * 
 * @author Your Name
 * @version 1.0.0
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * 根据支付订单号查询支付信息
     * 
     * @param paymentNo 支付订单号
     * @return 支付信息
     */
    Optional<Payment> findByPaymentNo(String paymentNo);

    /**
     * 根据用户ID查询支付列表
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 支付分页列表
     */
    Page<Payment> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    /**
     * 根据预约ID查询支付信息
     * 
     * @param appointmentId 预约ID
     * @return 支付信息
     */
    Optional<Payment> findByAppointmentId(String appointmentId);

    /**
     * 根据状态查询支付列表
     * 
     * @param status 支付状态
     * @param pageable 分页参数
     * @return 支付分页列表
     */
    Page<Payment> findByStatusOrderByCreatedAtDesc(Payment.PaymentStatus status, Pageable pageable);

    /**
     * 根据支付方式查询支付列表
     * 
     * @param paymentMethod 支付方式
     * @param pageable 分页参数
     * @return 支付分页列表
     */
    Page<Payment> findByPaymentMethodOrderByCreatedAtDesc(Payment.PaymentMethod paymentMethod, Pageable pageable);

    /**
     * 查询指定时间范围内的支付记录
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 支付列表
     */
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startTime AND :endTime")
    List<Payment> findByTimeRange(@Param("startTime") LocalDateTime startTime,
                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 查询待处理的支付（用于定时任务）
     * 
     * @param status 支付状态
     * @param timeoutMinutes 超时分钟数
     * @return 待处理的支付列表
     */
    @Query("SELECT p FROM Payment p WHERE p.status = :status " +
           "AND p.createdAt < :timeoutTime")
    List<Payment> findPendingPayments(@Param("status") Payment.PaymentStatus status,
                                     @Param("timeoutTime") LocalDateTime timeoutTime);

    /**
     * 查询需要退款的支付
     * 
     * @param status 支付状态
     * @return 需要退款的支付列表
     */
    @Query("SELECT p FROM Payment p WHERE p.status = :status AND p.refundAmount IS NULL")
    List<Payment> findPaymentsForRefund(@Param("status") Payment.PaymentStatus status);

    /**
     * 统计用户支付金额
     * 
     * @param userId 用户ID
     * @param status 支付状态
     * @return 支付总金额
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.userId = :userId AND p.status = :status")
    Double sumAmountByUserIdAndStatus(@Param("userId") String userId,
                                     @Param("status") Payment.PaymentStatus status);

    /**
     * 统计支付成功率
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 支付成功率
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN p.status = 'SUCCESS' THEN 1 END) * 100.0 / COUNT(p) " +
           "FROM Payment p WHERE p.createdAt BETWEEN :startTime AND :endTime")
    Double calculateSuccessRate(@Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime);

    /**
     * 根据第三方交易ID查询支付信息
     * 
     * @param transactionId 第三方交易ID
     * @return 支付信息
     */
    Optional<Payment> findByTransactionId(String transactionId);

    /**
     * 查询失败的支付记录（用于重试）
     * 
     * @param status 支付状态
     * @param retryCount 重试次数限制
     * @return 失败的支付列表
     */
    @Query("SELECT p FROM Payment p WHERE p.status = :status " +
           "AND p.retryCount < :maxRetryCount " +
           "ORDER BY p.createdAt ASC")
    List<Payment> findFailedPaymentsForRetry(@Param("status") Payment.PaymentStatus status,
                                            @Param("maxRetryCount") int maxRetryCount);
} 