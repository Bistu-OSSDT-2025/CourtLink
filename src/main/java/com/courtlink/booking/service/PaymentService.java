package com.courtlink.booking.service;

import com.courtlink.booking.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ֧����������ӿ�?
 * 
 * @author Your Name
 * @version 1.0.0
 */
public interface PaymentService {

    /**
     * ����֧������
     * 
     * @param payment ֧����Ϣ
     * @return ������֧������
     */
    Payment createPayment(Payment payment);

    /**
     * ����֧��
     * 
     * @param paymentNo ֧��������
     * @param paymentMethod ֧����ʽ
     * @return �������֧������?
     */
    Payment processPayment(String paymentNo, Payment.PaymentMethod paymentMethod);

    /**
     * ģ��֧������
     * 
     * @param paymentNo ֧��������
     * @return �������֧������?
     */
    Payment processMockPayment(String paymentNo);

    /**
     * ����֧���ص�
     * 
     * @param paymentNo ֧��������
     * @param transactionId ����������ID
     * @param success �Ƿ�ɹ�?
     * @return �������֧������?
     */
    Payment handlePaymentCallback(String paymentNo, String transactionId, boolean success);

    /**
     * �˿��
     * 
     * @param paymentNo ֧��������
     * @param refundAmount �˿���
     * @param reason �˿�ԭ��
     * @return �������֧������?
     */
    Payment processRefund(String paymentNo, BigDecimal refundAmount, String reason);

    /**
     * ȡ��֧��
     * 
     * @param paymentNo ֧��������
     * @return ȡ�����֧������?
     */
    Payment cancelPayment(String paymentNo);

    /**
     * ����֧�������Ų�ѯ֧����Ϣ
     * 
     * @param paymentNo ֧��������
     * @return ֧����Ϣ
     */
    Payment getPaymentByPaymentNo(String paymentNo);

    /**
     * �����û�ID��ѯ֧���б�
     * 
     * @param userId �û�ID
     * @param pageable ��ҳ����
     * @return ֧����ҳ�б�
     */
    Page<Payment> getPaymentsByUserId(String userId, Pageable pageable);

    /**
     * ����״̬��ѯ֧���б�
     * 
     * @param status ֧��״̬
     * @param pageable ��ҳ����
     * @return ֧����ҳ�б�
     */
    Page<Payment> getPaymentsByStatus(Payment.PaymentStatus status, Pageable pageable);

    /**
     * ����֧����ʽ��ѯ֧���б�
     * 
     * @param paymentMethod ֧����ʽ
     * @param pageable ��ҳ����
     * @return ֧����ҳ�б�
     */
    Page<Payment> getPaymentsByPaymentMethod(Payment.PaymentMethod paymentMethod, Pageable pageable);

    /**
     * ��ѯָ��ʱ�䷶Χ�ڵ�֧����¼
     * 
     * @param startTime ��ʼʱ��
     * @param endTime ����ʱ��
     * @return ֧���б�
     */
    List<Payment> getPaymentsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * ������ʱ֧��
     * 
     * @return ������֧������
     */
    int processTimeoutPayments();

    /**
     * ����ʧ�ܵ�֧��
     * 
     * @param paymentNo ֧��������
     * @return ���Ժ��֧������?
     */
    Payment retryFailedPayment(String paymentNo);

    /**
     * ͳ���û�֧�����?
     * 
     * @param userId �û�ID
     * @param status ֧��״̬
     * @return ֧���ܽ��?
     */
    BigDecimal sumAmountByUserIdAndStatus(String userId, Payment.PaymentStatus status);

    /**
     * ͳ��֧���ɹ���
     * 
     * @param startTime ��ʼʱ��
     * @param endTime ����ʱ��
     * @return ֧���ɹ���
     */
    Double calculateSuccessRate(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * ����֧��������
     * 
     * @return ֧��������
     */
    String generatePaymentNo();

    /**
     * ��֤֧������
     * 
     * @param payment ֧������
     * @return �Ƿ���Ч
     */
    boolean validatePayment(Payment payment);

    /**
     * ����֧��֪ͨ
     * 
     * @param payment ֧������
     * @param notificationType ֪ͨ����
     */
    void sendPaymentNotification(Payment payment, String notificationType);
} 
