package com.courtlink.booking.service;

import com.courtlink.booking.entity.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ԤԼ��������ӿ�?
 * 
 * @author Your Name
 * @version 1.0.0
 */
public interface AppointmentService {

    /**
     * ����ԤԼ
     * 
     * @param appointment ԤԼ��Ϣ
     * @return ������ԤԼ
     */
    Appointment createAppointment(Appointment appointment);

    /**
     * ����ԤԼ
     * 
     * @param id ԤԼID
     * @param appointment ԤԼ��Ϣ
     * @return ���º��Ԥ�?
     */
    Appointment updateAppointment(Long id, Appointment appointment);

    /**
     * ȡ��ԤԼ
     * 
     * @param id ԤԼID
     * @param reason ȡ��ԭ��
     * @return ȡ�����Ԥ�?
     */
    Appointment cancelAppointment(Long id, String reason);

    /**
     * ȷ��ԤԼ
     * 
     * @param id ԤԼID
     * @return ȷ�Ϻ��Ԥ�?
     */
    Appointment confirmAppointment(Long id);

    /**
     * ���Ԥ�?
     * 
     * @param id ԤԼID
     * @return ��ɺ��ԤԼ
     */
    Appointment completeAppointment(Long id);

    /**
     * ����ID��ѯԤԼ
     * 
     * @param id ԤԼID
     * @return ԤԼ��Ϣ
     */
    Appointment getAppointmentById(Long id);

    /**
     * �����û�ID��ѯԤԼ�б�
     * 
     * @param userId �û�ID
     * @param pageable ��ҳ����
     * @return ԤԼ��ҳ�б�
     */
    Page<Appointment> getAppointmentsByUserId(String userId, Pageable pageable);

    /**
     * ���ݷ����ṩ��ID��ѯԤԼ�б�
     * 
     * @param providerId �����ṩ��ID
     * @param pageable ��ҳ����
     * @return ԤԼ��ҳ�б�
     */
    Page<Appointment> getAppointmentsByProviderId(String providerId, Pageable pageable);

    /**
     * ����״̬��ѯԤԼ�б�
     * 
     * @param status ԤԼ״̬
     * @param pageable ��ҳ����
     * @return ԤԼ��ҳ�б�
     */
    Page<Appointment> getAppointmentsByStatus(Appointment.AppointmentStatus status, Pageable pageable);

    /**
     * ���ʱ���ͻ
     * 
     * @param providerId �����ṩ��ID
     * @param startTime ��ʼʱ��
     * @param endTime ����ʱ��
     * @param excludeId �ų���ԤԼID
     * @return �Ƿ���ڳ��?
     */
    boolean hasTimeConflict(String providerId, LocalDateTime startTime, LocalDateTime endTime, Long excludeId);

    /**
     * ��ѯָ��ʱ�䷶Χ�ڵ�ԤԼ
     * 
     * @param startTime ��ʼʱ��
     * @param endTime ����ʱ��
     * @param status ԤԼ״̬
     * @return ԤԼ�б�
     */
    List<Appointment> getAppointmentsByTimeRange(LocalDateTime startTime, LocalDateTime endTime, 
                                                Appointment.AppointmentStatus status);

    /**
     * ��ѯ�������ڵ�ԤԼ�����ڷ������ѣ�
     * 
     * @param startTime ��ʼʱ��
     * @param endTime ����ʱ��
     * @param status ԤԼ״̬
     * @return �������ڵ�ԤԼ�б�
     */
    List<Appointment> getUpcomingAppointments(LocalDateTime startTime, LocalDateTime endTime, 
                                             Appointment.AppointmentStatus status);

    /**
     * ��������ԤԼ
     * 
     * @return ������ԤԼ����
     */
    int cleanupExpiredAppointments();

    /**
     * ����ԤԼ����
     * 
     * @param appointment ԤԼ��Ϣ
     */
    void sendAppointmentReminder(Appointment appointment);

    /**
     * ����ԤԼ֪ͨ
     * 
     * @param appointment ԤԼ��Ϣ
     * @param notificationType ֪ͨ����
     */
    void sendAppointmentNotification(Appointment appointment, String notificationType);

    /**
     * ͳ���û�ԤԼ����
     * 
     * @param userId �û�ID
     * @param status ԤԼ״̬
     * @return ԤԼ����
     */
    long countAppointmentsByUserIdAndStatus(String userId, Appointment.AppointmentStatus status);

    /**
     * ͳ�Ʒ����ṩ��ԤԼ����
     * 
     * @param providerId �����ṩ��ID
     * @param status ԤԼ״̬
     * @return ԤԼ����
     */
    long countAppointmentsByProviderIdAndStatus(String providerId, Appointment.AppointmentStatus status);
} 
