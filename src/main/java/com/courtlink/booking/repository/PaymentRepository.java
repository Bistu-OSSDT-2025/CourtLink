package com.courtlink.booking.repository;

import com.courtlink.booking.entity.Payment;
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
 * ֧�����ݷ��ʲ�ӿ�?
 * 
 * @author Your Name
 * @version 1.0.0
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * ����֧�������Ų�ѯ֧����Ϣ
     * 
     * @param paymentNo ֧��������
     * @return ֧����Ϣ
     */
    Optional<Payment> findByPaymentNo(String paymentNo);

    /**
     * �����û�ID��ѯ֧���б�
     * 
     * @param userId �û�ID
     * @param pageable ��ҳ����
     * @return ֧����ҳ�б�
     */
    Page<Payment> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    /**
     * ����ԤԼID��ѯ֧����Ϣ
     * 
     * @param appointmentId ԤԼID
     * @return ֧����Ϣ
     */
    Optional<Payment> findByAppointmentId(String appointmentId);

    /**
     * ����״̬��ѯ֧���б�
     * 
     * @param status ֧��״̬
     * @param pageable ��ҳ����
     * @return ֧����ҳ�б�
     */
    Page<Payment> findByStatusOrderByCreatedAtDesc(Payment.PaymentStatus status, Pageable pageable);

    /**
     * ����֧����ʽ��ѯ֧���б�
     * 
     * @param paymentMethod ֧����ʽ
     * @param pageable ��ҳ����
     * @return ֧����ҳ�б�
     */
    Page<Payment> findByPaymentMethodOrderByCreatedAtDesc(Payment.PaymentMethod paymentMethod, Pageable pageable);

    /**
     * ��ѯָ��ʱ�䷶Χ�ڵ�֧����¼
     * 
     * @param startTime ��ʼʱ��
     * @param endTime ����ʱ��
     * @return ֧���б�
     */
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startTime AND :endTime")
    List<Payment> findByTimeRange(@Param("startTime") LocalDateTime startTime,
                                 @Param("endTime") LocalDateTime endTime);

    /**
     * ��ѯ��������֧�������ڶ�ʱ����
     * 
     * @param status ֧��״̬
     * @param timeoutMinutes ��ʱ������
     * @return ��������֧���б�
     */
    @Query("SELECT p FROM Payment p WHERE p.status = :status " +
           "AND p.createdAt < :timeoutTime")
    List<Payment> findPendingPayments(@Param("status") Payment.PaymentStatus status,
                                     @Param("timeoutTime") LocalDateTime timeoutTime);

    /**
     * ��ѯ��Ҫ�˿��֧��?
     * 
     * @param status ֧��״̬
     * @return ��Ҫ�˿��֧���б�?
     */
    @Query("SELECT p FROM Payment p WHERE p.status = :status AND p.refundAmount IS NULL")
    List<Payment> findPaymentsForRefund(@Param("status") Payment.PaymentStatus status);

    /**
     * ͳ���û�֧�����?
     * 
     * @param userId �û�ID
     * @param status ֧��״̬
     * @return ֧���ܽ��?
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.userId = :userId AND p.status = :status")
    Double sumAmountByUserIdAndStatus(@Param("userId") String userId,
                                     @Param("status") Payment.PaymentStatus status);

    /**
     * ͳ��֧���ɹ���
     * 
     * @param startTime ��ʼʱ��
     * @param endTime ����ʱ��
     * @return ֧���ɹ���
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN p.status = 'SUCCESS' THEN 1 END) * 100.0 / COUNT(p) " +
           "FROM Payment p WHERE p.createdAt BETWEEN :startTime AND :endTime")
    Double calculateSuccessRate(@Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime);

    /**
     * ���ݵ���������ID��ѯ֧����Ϣ
     * 
     * @param transactionId ����������ID
     * @return ֧����Ϣ
     */
    Optional<Payment> findByTransactionId(String transactionId);

    /**
     * ��ѯʧ�ܵ�֧����¼���������ԣ�
     * 
     * @param status ֧��״̬
     * @param retryCount ���Դ�������
     * @return ʧ�ܵ�֧���б�
     */
    @Query("SELECT p FROM Payment p WHERE p.status = :status " +
           "AND p.retryCount < :maxRetryCount " +
           "ORDER BY p.createdAt ASC")
    List<Payment> findFailedPaymentsForRetry(@Param("status") Payment.PaymentStatus status,
                                            @Param("maxRetryCount") int maxRetryCount);
} 
