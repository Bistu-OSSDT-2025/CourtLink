package com.courtlink.booking.repository;

import com.courtlink.booking.entity.Appointment;
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
 * ԤԼ���ݷ��ʲ�ӿ�?
 * 
 * @author Your Name
 * @version 1.0.0
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * �����û�ID��ѯԤԼ�б�
     * 
     * @param userId �û�ID
     * @param pageable ��ҳ����
     * @return ԤԼ��ҳ�б�
     */
    Page<Appointment> findByUserIdOrderByStartTimeDesc(String userId, Pageable pageable);

    /**
     * ���ݷ����ṩ��ID��ѯԤԼ�б�
     * 
     * @param providerId �����ṩ��ID
     * @param pageable ��ҳ����
     * @return ԤԼ��ҳ�б�
     */
    Page<Appointment> findByProviderIdOrderByStartTimeDesc(String providerId, Pageable pageable);

    /**
     * ����״̬��ѯԤԼ�б�
     * 
     * @param status ԤԼ״̬
     * @param pageable ��ҳ����
     * @return ԤԼ��ҳ�б�
     */
    Page<Appointment> findByStatusOrderByStartTimeDesc(Appointment.AppointmentStatus status, Pageable pageable);

    /**
     * ��ѯָ��ʱ�䷶Χ�ڵ�ԤԼ
     * 
     * @param startTime ��ʼʱ��
     * @param endTime ����ʱ��
     * @param status ԤԼ״̬
     * @return ԤԼ�б�
     */
    @Query("SELECT a FROM Appointment a WHERE a.startTime >= :startTime AND a.endTime <= :endTime AND a.status = :status")
    List<Appointment> findByTimeRangeAndStatus(@Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime,
                                              @Param("status") Appointment.AppointmentStatus status);

    /**
     * ���ʱ���ͻ
     * 
     * @param providerId �����ṩ��ID
     * @param startTime ��ʼʱ��
     * @param endTime ����ʱ��
     * @param excludeId �ų���ԤԼID�����ڸ���ʱ��
     * @return ��ͻ��ԤԼ�б�
     */
    @Query("SELECT a FROM Appointment a WHERE a.providerId = :providerId " +
           "AND a.status NOT IN ('CANCELLED', 'EXPIRED') " +
           "AND ((a.startTime < :endTime AND a.endTime > :startTime) " +
           "OR (a.startTime = :startTime AND a.endTime = :endTime)) " +
           "AND (:excludeId IS NULL OR a.id != :excludeId)")
    List<Appointment> findConflictingAppointments(@Param("providerId") String providerId,
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime,
                                                 @Param("excludeId") Long excludeId);

    /**
     * ��ѯ�������ڵ�ԤԼ�����ڷ������ѣ�
     * 
     * @param startTime ��ʼʱ��
     * @param endTime ����ʱ��
     * @param status ԤԼ״̬
     * @return �������ڵ�ԤԼ�б�
     */
    @Query("SELECT a FROM Appointment a WHERE a.startTime BETWEEN :startTime AND :endTime " +
           "AND a.status = :status")
    List<Appointment> findUpcomingAppointments(@Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime,
                                              @Param("status") Appointment.AppointmentStatus status);

    /**
     * ��ѯ���ڵ�ԤԼ
     * 
     * @param currentTime ��ǰʱ��
     * @param status ԤԼ״̬
     * @return ���ڵ�ԤԼ�б�
     */
    @Query("SELECT a FROM Appointment a WHERE a.endTime < :currentTime AND a.status = :status")
    List<Appointment> findExpiredAppointments(@Param("currentTime") LocalDateTime currentTime,
                                             @Param("status") Appointment.AppointmentStatus status);

    /**
     * ����֧��ID��ѯԤԼ
     * 
     * @param paymentId ֧��ID
     * @return ԤԼ��Ϣ
     */
    Optional<Appointment> findByPaymentId(String paymentId);

    /**
     * ͳ���û�ԤԼ����
     * 
     * @param userId �û�ID
     * @param status ԤԼ״̬
     * @return ԤԼ����
     */
    long countByUserIdAndStatus(String userId, Appointment.AppointmentStatus status);

    /**
     * ͳ�Ʒ����ṩ��ԤԼ����
     * 
     * @param providerId �����ṩ��ID
     * @param status ԤԼ״̬
     * @return ԤԼ����
     */
    long countByProviderIdAndStatus(String providerId, Appointment.AppointmentStatus status);
} 
