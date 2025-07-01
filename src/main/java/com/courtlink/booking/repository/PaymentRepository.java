package com.courtlink.booking.repository;

import com.courtlink.booking.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Payment Data Access Interface
 * 
 * @author CourtLink Team
 * @version 1.0.0
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find payment by payment number
     * 
     * @param paymentNo Payment number
     * @return Payment if found
     */
    Optional<Payment> findByPaymentNo(String paymentNo);

    /**
     * Find payment by appointment ID
     * 
     * @param appointmentId Appointment ID
     * @return Payment if found
     */
    Optional<Payment> findByAppointmentId(String appointmentId);

    /**
     * Find payments by user ID
     * 
     * @param userId User ID
     * @param pageable Pagination
     * @return Page of payments
     */
    Page<Payment> findByUserId(String userId, Pageable pageable);

    /**
     * Find payments by status
     * 
     * @param status Payment status
     * @param pageable Pagination
     * @return Page of payments
     */
    Page<Payment> findByStatus(Payment.PaymentStatus status, Pageable pageable);

    /**
     * Find payments by payment method
     * 
     * @param paymentMethod Payment method
     * @param pageable Pagination
     * @return Page of payments
     */
    Page<Payment> findByPaymentMethod(Payment.PaymentMethod paymentMethod, Pageable pageable);

    /**
     * Find payments by time range
     * 
     * @param startTime Start time
     * @param endTime End time
     * @return List of payments
     */
    @Query("SELECT p FROM Payment p WHERE p.createdAt >= :startTime AND p.createdAt <= :endTime")
    List<Payment> findByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * Find pending payments
     * 
     * @return List of pending payments
     */
    List<Payment> findByStatus(Payment.PaymentStatus status);

    /**
     * Find payments requiring refund
     * 
     * @param status Payment status
     * @return List of payments requiring refund
     */
    @Query("SELECT p FROM Payment p WHERE p.status = :status AND p.refundAmount IS NULL")
    List<Payment> findPaymentsForRefund(@Param("status") Payment.PaymentStatus status);

    /**
     * Sum payment amounts by user and status
     * 
     * @param userId User ID
     * @param status Payment status
     * @return Total amount
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.userId = :userId AND p.status = :status")
    BigDecimal sumAmountByUserIdAndStatus(@Param("userId") String userId, @Param("status") Payment.PaymentStatus status);

    /**
     * Count payments by status
     * 
     * @param status Payment status
     * @return Count of payments
     */
    long countByStatus(Payment.PaymentStatus status);

    /**
     * Find failed payments for retry
     * 
     * @param retryBefore Retry before time
     * @return List of failed payments
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED' AND p.updatedAt < :retryBefore")
    List<Payment> findFailedPaymentsForRetry(@Param("retryBefore") LocalDateTime retryBefore);

    /**
     * Find successful payments by date range
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return List of successful payments
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'SUCCESS' AND p.paidAt >= :startDate AND p.paidAt <= :endDate")
    List<Payment> findSuccessfulPaymentsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
} 
