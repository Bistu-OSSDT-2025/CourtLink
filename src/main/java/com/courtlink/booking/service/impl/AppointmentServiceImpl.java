package com.courtlink.booking.service.impl;

import com.courtlink.booking.entity.Appointment;
import com.courtlink.booking.repository.AppointmentRepository;
import com.courtlink.booking.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ԤԼ����ʵ����
 * 
 * @author Your Name
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;

    @Override
    @Transactional
    public Appointment createAppointment(Appointment appointment) {
        log.info("����ԤԼ: userId={}, providerId={}, startTime={}, endTime={}", 
                appointment.getUserId(), appointment.getProviderId(), 
                appointment.getStartTime(), appointment.getEndTime());

        // ���ʱ���ͻ
        if (hasTimeConflict(appointment.getProviderId(), appointment.getStartTime(), 
                appointment.getEndTime(), null)) {
            log.warn("ԤԼʱ����? providerId={}, startTime={}, endTime={}", 
                    appointment.getProviderId(), appointment.getStartTime(), appointment.getEndTime());
            throw new RuntimeException("ԤԼʱ���ͻ����ѡ������ʱ��?);
        }

        // ���ó�ʼ״̬
        appointment.setStatus(Appointment.AppointmentStatus.PENDING);
        
        Appointment savedAppointment = appointmentRepository.save(appointment);
        log.info("ԤԼ�����ɹ�: appointmentId={}", savedAppointment.getId());
        
        return savedAppointment;
    }

    @Override
    @Transactional
    public Appointment updateAppointment(Long id, Appointment appointment) {
        log.info("����ԤԼ: appointmentId={}", id);
        
        Optional<Appointment> existingAppointment = appointmentRepository.findById(id);
        if (existingAppointment.isEmpty()) {
            log.warn("ԤԼ������: appointmentId={}", id);
            throw new RuntimeException("ԤԼ������");
        }

        Appointment existing = existingAppointment.get();
        
        // ���ʱ���ͻ���ų���ǰԤԼ��
        if (hasTimeConflict(appointment.getProviderId(), appointment.getStartTime(), 
                appointment.getEndTime(), id)) {
            log.warn("ԤԼʱ����? appointmentId={}, providerId={}, startTime={}, endTime={}", 
                    id, appointment.getProviderId(), appointment.getStartTime(), appointment.getEndTime());
            throw new RuntimeException("ԤԼʱ���ͻ����ѡ������ʱ��?);
        }

        // �����ֶ�
        existing.setProviderId(appointment.getProviderId());
        existing.setServiceType(appointment.getServiceType());
        existing.setStartTime(appointment.getStartTime());
        existing.setEndTime(appointment.getEndTime());
        existing.setAmount(appointment.getAmount());
        existing.setNotes(appointment.getNotes());

        Appointment updatedAppointment = appointmentRepository.save(existing);
        log.info("ԤԼ���³ɹ�: appointmentId={}", updatedAppointment.getId());
        
        return updatedAppointment;
    }

    @Override
    @Transactional
    public Appointment cancelAppointment(Long id, String reason) {
        log.info("ȡ��ԤԼ: appointmentId={}, reason={}", id, reason);
        
        Optional<Appointment> appointment = appointmentRepository.findById(id);
        if (appointment.isEmpty()) {
            log.warn("ԤԼ������: appointmentId={}", id);
            throw new RuntimeException("ԤԼ������");
        }

        Appointment existing = appointment.get();
        
        // ����Ƿ����ȡ��
        if (existing.getStatus() == Appointment.AppointmentStatus.CANCELLED) {
            log.warn("ԤԼ��ȡ��: appointmentId={}", id);
            throw new RuntimeException("ԤԼ��ȡ��");
        }
        
        if (existing.getStatus() == Appointment.AppointmentStatus.COMPLETED) {
            log.warn("ԤԼ����ɣ��޷�ȡ��? appointmentId={}", id);
            throw new RuntimeException("ԤԼ����ɣ��޷�ȡ��?);
        }

        existing.setStatus(Appointment.AppointmentStatus.CANCELLED);
        existing.setNotes(existing.getNotes() + " [ȡ��ԭ��: " + reason + "]");

        Appointment cancelledAppointment = appointmentRepository.save(existing);
        log.info("ԤԼȡ���ɹ�: appointmentId={}", cancelledAppointment.getId());
        
        return cancelledAppointment;
    }

    @Override
    @Transactional
    public Appointment confirmAppointment(Long id) {
        log.info("ȷ��ԤԼ: appointmentId={}", id);
        
        Optional<Appointment> appointment = appointmentRepository.findById(id);
        if (appointment.isEmpty()) {
            log.warn("ԤԼ������: appointmentId={}", id);
            throw new RuntimeException("ԤԼ������");
        }

        Appointment existing = appointment.get();
        
        if (existing.getStatus() != Appointment.AppointmentStatus.PENDING) {
            log.warn("ԤԼ״̬����ȷ���޷�ȷ��: appointmentId={}, status={}", id, existing.getStatus());
            throw new RuntimeException("ԤԼ״̬����ȷ���޷�ȷ��");
        }

        existing.setStatus(Appointment.AppointmentStatus.CONFIRMED);

        Appointment confirmedAppointment = appointmentRepository.save(existing);
        log.info("ԤԼȷ�ϳɹ�: appointmentId={}", confirmedAppointment.getId());
        
        return confirmedAppointment;
    }

    @Override
    @Transactional
    public Appointment completeAppointment(Long id) {
        log.info("���Ԥ�? appointmentId={}", id);
        
        Optional<Appointment> appointment = appointmentRepository.findById(id);
        if (appointment.isEmpty()) {
            log.warn("ԤԼ������: appointmentId={}", id);
            throw new RuntimeException("ԤԼ������");
        }

        Appointment existing = appointment.get();
        
        if (existing.getStatus() != Appointment.AppointmentStatus.CONFIRMED) {
            log.warn("ԤԼ״̬����ȷ���޷����? appointmentId={}, status={}", id, existing.getStatus());
            throw new RuntimeException("ԤԼ״̬����ȷ���޷����?);
        }

        existing.setStatus(Appointment.AppointmentStatus.COMPLETED);

        Appointment completedAppointment = appointmentRepository.save(existing);
        log.info("ԤԼ��ɳɹ�? appointmentId={}", completedAppointment.getId());
        
        return completedAppointment;
    }

    @Override
    public Appointment getAppointmentById(Long id) {
        log.debug("��ѯԤԼ����: appointmentId={}", id);
        
        Optional<Appointment> appointment = appointmentRepository.findById(id);
        if (appointment.isEmpty()) {
            log.warn("ԤԼ������: appointmentId={}", id);
            throw new RuntimeException("ԤԼ������");
        }
        
        return appointment.get();
    }

    @Override
    public Page<Appointment> getAppointmentsByUserId(String userId, Pageable pageable) {
        log.debug("��ѯ�û�ԤԼ�б�: userId={}, page={}, size={}", 
                userId, pageable.getPageNumber(), pageable.getPageSize());
        
        return appointmentRepository.findByUserIdOrderByStartTimeDesc(userId, pageable);
    }

    @Override
    public Page<Appointment> getAppointmentsByProviderId(String providerId, Pageable pageable) {
        log.debug("��ѯ�����ṩ��ԤԼ�б�: providerId={}, page={}, size={}", 
                providerId, pageable.getPageNumber(), pageable.getPageSize());
        
        return appointmentRepository.findByProviderIdOrderByStartTimeDesc(providerId, pageable);
    }

    @Override
    public Page<Appointment> getAppointmentsByStatus(Appointment.AppointmentStatus status, Pageable pageable) {
        log.debug("��ѯԤԼ�б�: status={}, page={}, size={}", 
                status, pageable.getPageNumber(), pageable.getPageSize());
        
        return appointmentRepository.findByStatusOrderByStartTimeDesc(status, pageable);
    }

    @Override
    public boolean hasTimeConflict(String providerId, LocalDateTime startTime, LocalDateTime endTime, Long excludeId) {
        log.debug("���ʱ���ͻ: providerId={}, startTime={}, endTime={}, excludeId={}", 
                providerId, startTime, endTime, excludeId);
        
        List<Appointment> conflictingAppointments = appointmentRepository.findConflictingAppointments(
                providerId, startTime, endTime, excludeId);
        
        boolean hasConflict = !conflictingAppointments.isEmpty();
        log.debug("ʱ���ͻ�����: hasConflict={}, conflictCount={}", hasConflict, conflictingAppointments.size());
        
        return hasConflict;
    }

    @Override
    public List<Appointment> getAppointmentsByTimeRange(LocalDateTime startTime, LocalDateTime endTime, 
                                                       Appointment.AppointmentStatus status) {
        log.debug("��ѯʱ�䷶Χ��ԤԼ: startTime={}, endTime={}, status={}", startTime, endTime, status);
        
        return appointmentRepository.findByTimeRangeAndStatus(startTime, endTime, status);
    }

    @Override
    public List<Appointment> getUpcomingAppointments(LocalDateTime startTime, LocalDateTime endTime, 
                                                    Appointment.AppointmentStatus status) {
        log.debug("��ѯ�������ڵ�ԤԼ: startTime={}, endTime={}, status={}", startTime, endTime, status);
        
        return appointmentRepository.findUpcomingAppointments(startTime, endTime, status);
    }

    @Override
    @Transactional
    public int cleanupExpiredAppointments() {
        log.info("��ʼ��������ԤԼ");
        
        LocalDateTime currentTime = LocalDateTime.now();
        List<Appointment> expiredAppointments = appointmentRepository.findExpiredAppointments(
                currentTime, Appointment.AppointmentStatus.PENDING);
        
        for (Appointment appointment : expiredAppointments) {
            appointment.setStatus(Appointment.AppointmentStatus.EXPIRED);
            appointmentRepository.save(appointment);
            log.debug("��ǹ���Ԥ�? appointmentId={}", appointment.getId());
        }
        
        log.info("����ԤԼ�������? count={}", expiredAppointments.size());
        return expiredAppointments.size();
    }

    @Override
    public void sendAppointmentReminder(Appointment appointment) {
        log.info("����ԤԼ����: appointmentId={}, userId={}, startTime={}", 
                appointment.getId(), appointment.getUserId(), appointment.getStartTime());
        
        // TODO: ʵ�־����֪ͨ�߼�?
        // 1. �����ʼ�֪ͨ
        // 2. ���Ͷ���֪ͨ
        // 3. ��������֪ͨ
        
        log.info("ԤԼ���ѷ������? appointmentId={}", appointment.getId());
    }

    @Override
    public void sendAppointmentNotification(Appointment appointment, String notificationType) {
        log.info("����ԤԼ֪ͨ: appointmentId={}, notificationType={}", appointment.getId(), notificationType);
        
        // TODO: ʵ�־����֪ͨ�߼�?
        // ����notificationType���Ͳ�ͬ���͵�֪ͨ
        
        log.info("ԤԼ֪ͨ�������? appointmentId={}, notificationType={}", appointment.getId(), notificationType);
    }

    @Override
    public long countAppointmentsByUserIdAndStatus(String userId, Appointment.AppointmentStatus status) {
        log.debug("ͳ���û�ԤԼ����: userId={}, status={}", userId, status);
        
        return appointmentRepository.countByUserIdAndStatus(userId, status);
    }

    @Override
    public long countAppointmentsByProviderIdAndStatus(String providerId, Appointment.AppointmentStatus status) {
        log.debug("ͳ�Ʒ����ṩ��ԤԼ����: providerId={}, status={}", providerId, status);
        
        return appointmentRepository.countByProviderIdAndStatus(providerId, status);
    }
} 
