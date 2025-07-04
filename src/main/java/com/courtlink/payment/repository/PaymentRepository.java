package com.courtlink.payment.repository;

import com.courtlink.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    /**
     * 根据支付ID查找支付记录
     */
    Optional<Payment> findByPaymentId(String paymentId);
    
    /**
     * 根据用户ID查找所有支付记录
     */
    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * 根据用户ID和支付状态查找支付记录
     */
    List<Payment> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, Payment.PaymentStatus status);
    
    /**
     * 根据支付状态查找支付记录
     */
    List<Payment> findByStatusOrderByCreatedAtDesc(Payment.PaymentStatus status);
    
    /**
     * 根据第三方交易ID查找支付记录
     */
    Optional<Payment> findByExternalTransactionId(String externalTransactionId);
    
    /**
     * 检查用户是否有待支付的订单
     */
    @Query("SELECT COUNT(p) > 0 FROM Payment p WHERE p.userId = :userId AND p.status = 'PENDING'")
    boolean existsPendingPaymentByUserId(@Param("userId") Long userId);

    /**
     * 根据预定ID查找支付记录（使用自定义查询）
     */
    @Query("SELECT p FROM Payment p WHERE p.appointmentIds LIKE CONCAT('%', :appointmentId, '%')")
    List<Payment> findByAppointmentIdContaining(@Param("appointmentId") String appointmentId);

    /**
     * 根据预定ID和支付状态查找支付记录（使用自定义查询）
     */
    @Query("SELECT p FROM Payment p WHERE p.appointmentIds LIKE CONCAT('%', :appointmentId, '%') AND p.status = :status")
    List<Payment> findByAppointmentIdContainingAndStatus(@Param("appointmentId") String appointmentId, @Param("status") Payment.PaymentStatus status);
} 