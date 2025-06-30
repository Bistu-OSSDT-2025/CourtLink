package com.example.appointment.service.impl;

import com.example.appointment.entity.Appointment;
import com.example.appointment.repository.AppointmentRepository;
import com.example.appointment.service.AppointmentService;
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
 * 预约服务实现类
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
        log.info("创建预约: userId={}, providerId={}, startTime={}, endTime={}", 
                appointment.getUserId(), appointment.getProviderId(), 
                appointment.getStartTime(), appointment.getEndTime());

        // 检查时间冲突
        if (hasTimeConflict(appointment.getProviderId(), appointment.getStartTime(), 
                appointment.getEndTime(), null)) {
            log.warn("预约时间冲突: providerId={}, startTime={}, endTime={}", 
                    appointment.getProviderId(), appointment.getStartTime(), appointment.getEndTime());
            throw new RuntimeException("预约时间冲突，请选择其他时间");
        }

        // 设置初始状态
        appointment.setStatus(Appointment.AppointmentStatus.PENDING);
        
        Appointment savedAppointment = appointmentRepository.save(appointment);
        log.info("预约创建成功: appointmentId={}", savedAppointment.getId());
        
        return savedAppointment;
    }

    @Override
    @Transactional
    public Appointment updateAppointment(Long id, Appointment appointment) {
        log.info("更新预约: appointmentId={}", id);
        
        Optional<Appointment> existingAppointment = appointmentRepository.findById(id);
        if (existingAppointment.isEmpty()) {
            log.warn("预约不存在: appointmentId={}", id);
            throw new RuntimeException("预约不存在");
        }

        Appointment existing = existingAppointment.get();
        
        // 检查时间冲突（排除当前预约）
        if (hasTimeConflict(appointment.getProviderId(), appointment.getStartTime(), 
                appointment.getEndTime(), id)) {
            log.warn("预约时间冲突: appointmentId={}, providerId={}, startTime={}, endTime={}", 
                    id, appointment.getProviderId(), appointment.getStartTime(), appointment.getEndTime());
            throw new RuntimeException("预约时间冲突，请选择其他时间");
        }

        // 更新字段
        existing.setProviderId(appointment.getProviderId());
        existing.setServiceType(appointment.getServiceType());
        existing.setStartTime(appointment.getStartTime());
        existing.setEndTime(appointment.getEndTime());
        existing.setAmount(appointment.getAmount());
        existing.setNotes(appointment.getNotes());

        Appointment updatedAppointment = appointmentRepository.save(existing);
        log.info("预约更新成功: appointmentId={}", updatedAppointment.getId());
        
        return updatedAppointment;
    }

    @Override
    @Transactional
    public Appointment cancelAppointment(Long id, String reason) {
        log.info("取消预约: appointmentId={}, reason={}", id, reason);
        
        Optional<Appointment> appointment = appointmentRepository.findById(id);
        if (appointment.isEmpty()) {
            log.warn("预约不存在: appointmentId={}", id);
            throw new RuntimeException("预约不存在");
        }

        Appointment existing = appointment.get();
        
        // 检查是否可以取消
        if (existing.getStatus() == Appointment.AppointmentStatus.CANCELLED) {
            log.warn("预约已取消: appointmentId={}", id);
            throw new RuntimeException("预约已取消");
        }
        
        if (existing.getStatus() == Appointment.AppointmentStatus.COMPLETED) {
            log.warn("预约已完成，无法取消: appointmentId={}", id);
            throw new RuntimeException("预约已完成，无法取消");
        }

        existing.setStatus(Appointment.AppointmentStatus.CANCELLED);
        existing.setNotes(existing.getNotes() + " [取消原因: " + reason + "]");

        Appointment cancelledAppointment = appointmentRepository.save(existing);
        log.info("预约取消成功: appointmentId={}", cancelledAppointment.getId());
        
        return cancelledAppointment;
    }

    @Override
    @Transactional
    public Appointment confirmAppointment(Long id) {
        log.info("确认预约: appointmentId={}", id);
        
        Optional<Appointment> appointment = appointmentRepository.findById(id);
        if (appointment.isEmpty()) {
            log.warn("预约不存在: appointmentId={}", id);
            throw new RuntimeException("预约不存在");
        }

        Appointment existing = appointment.get();
        
        if (existing.getStatus() != Appointment.AppointmentStatus.PENDING) {
            log.warn("预约状态不正确，无法确认: appointmentId={}, status={}", id, existing.getStatus());
            throw new RuntimeException("预约状态不正确，无法确认");
        }

        existing.setStatus(Appointment.AppointmentStatus.CONFIRMED);

        Appointment confirmedAppointment = appointmentRepository.save(existing);
        log.info("预约确认成功: appointmentId={}", confirmedAppointment.getId());
        
        return confirmedAppointment;
    }

    @Override
    @Transactional
    public Appointment completeAppointment(Long id) {
        log.info("完成预约: appointmentId={}", id);
        
        Optional<Appointment> appointment = appointmentRepository.findById(id);
        if (appointment.isEmpty()) {
            log.warn("预约不存在: appointmentId={}", id);
            throw new RuntimeException("预约不存在");
        }

        Appointment existing = appointment.get();
        
        if (existing.getStatus() != Appointment.AppointmentStatus.CONFIRMED) {
            log.warn("预约状态不正确，无法完成: appointmentId={}, status={}", id, existing.getStatus());
            throw new RuntimeException("预约状态不正确，无法完成");
        }

        existing.setStatus(Appointment.AppointmentStatus.COMPLETED);

        Appointment completedAppointment = appointmentRepository.save(existing);
        log.info("预约完成成功: appointmentId={}", completedAppointment.getId());
        
        return completedAppointment;
    }

    @Override
    public Appointment getAppointmentById(Long id) {
        log.debug("查询预约详情: appointmentId={}", id);
        
        Optional<Appointment> appointment = appointmentRepository.findById(id);
        if (appointment.isEmpty()) {
            log.warn("预约不存在: appointmentId={}", id);
            throw new RuntimeException("预约不存在");
        }
        
        return appointment.get();
    }

    @Override
    public Page<Appointment> getAppointmentsByUserId(String userId, Pageable pageable) {
        log.debug("查询用户预约列表: userId={}, page={}, size={}", 
                userId, pageable.getPageNumber(), pageable.getPageSize());
        
        return appointmentRepository.findByUserIdOrderByStartTimeDesc(userId, pageable);
    }

    @Override
    public Page<Appointment> getAppointmentsByProviderId(String providerId, Pageable pageable) {
        log.debug("查询服务提供者预约列表: providerId={}, page={}, size={}", 
                providerId, pageable.getPageNumber(), pageable.getPageSize());
        
        return appointmentRepository.findByProviderIdOrderByStartTimeDesc(providerId, pageable);
    }

    @Override
    public Page<Appointment> getAppointmentsByStatus(Appointment.AppointmentStatus status, Pageable pageable) {
        log.debug("查询预约列表: status={}, page={}, size={}", 
                status, pageable.getPageNumber(), pageable.getPageSize());
        
        return appointmentRepository.findByStatusOrderByStartTimeDesc(status, pageable);
    }

    @Override
    public boolean hasTimeConflict(String providerId, LocalDateTime startTime, LocalDateTime endTime, Long excludeId) {
        log.debug("检查时间冲突: providerId={}, startTime={}, endTime={}, excludeId={}", 
                providerId, startTime, endTime, excludeId);
        
        List<Appointment> conflictingAppointments = appointmentRepository.findConflictingAppointments(
                providerId, startTime, endTime, excludeId);
        
        boolean hasConflict = !conflictingAppointments.isEmpty();
        log.debug("时间冲突检查结果: hasConflict={}, conflictCount={}", hasConflict, conflictingAppointments.size());
        
        return hasConflict;
    }

    @Override
    public List<Appointment> getAppointmentsByTimeRange(LocalDateTime startTime, LocalDateTime endTime, 
                                                       Appointment.AppointmentStatus status) {
        log.debug("查询时间范围内预约: startTime={}, endTime={}, status={}", startTime, endTime, status);
        
        return appointmentRepository.findByTimeRangeAndStatus(startTime, endTime, status);
    }

    @Override
    public List<Appointment> getUpcomingAppointments(LocalDateTime startTime, LocalDateTime endTime, 
                                                    Appointment.AppointmentStatus status) {
        log.debug("查询即将到期的预约: startTime={}, endTime={}, status={}", startTime, endTime, status);
        
        return appointmentRepository.findUpcomingAppointments(startTime, endTime, status);
    }

    @Override
    @Transactional
    public int cleanupExpiredAppointments() {
        log.info("开始清理过期预约");
        
        LocalDateTime currentTime = LocalDateTime.now();
        List<Appointment> expiredAppointments = appointmentRepository.findExpiredAppointments(
                currentTime, Appointment.AppointmentStatus.PENDING);
        
        for (Appointment appointment : expiredAppointments) {
            appointment.setStatus(Appointment.AppointmentStatus.EXPIRED);
            appointmentRepository.save(appointment);
            log.debug("标记过期预约: appointmentId={}", appointment.getId());
        }
        
        log.info("过期预约清理完成: count={}", expiredAppointments.size());
        return expiredAppointments.size();
    }

    @Override
    public void sendAppointmentReminder(Appointment appointment) {
        log.info("发送预约提醒: appointmentId={}, userId={}, startTime={}", 
                appointment.getId(), appointment.getUserId(), appointment.getStartTime());
        
        // TODO: 实现具体的通知逻辑
        // 1. 发送邮件通知
        // 2. 发送短信通知
        // 3. 发送推送通知
        
        log.info("预约提醒发送完成: appointmentId={}", appointment.getId());
    }

    @Override
    public void sendAppointmentNotification(Appointment appointment, String notificationType) {
        log.info("发送预约通知: appointmentId={}, notificationType={}", appointment.getId(), notificationType);
        
        // TODO: 实现具体的通知逻辑
        // 根据notificationType发送不同类型的通知
        
        log.info("预约通知发送完成: appointmentId={}, notificationType={}", appointment.getId(), notificationType);
    }

    @Override
    public long countAppointmentsByUserIdAndStatus(String userId, Appointment.AppointmentStatus status) {
        log.debug("统计用户预约数量: userId={}, status={}", userId, status);
        
        return appointmentRepository.countByUserIdAndStatus(userId, status);
    }

    @Override
    public long countAppointmentsByProviderIdAndStatus(String providerId, Appointment.AppointmentStatus status) {
        log.debug("统计服务提供者预约数量: providerId={}, status={}", providerId, status);
        
        return appointmentRepository.countByProviderIdAndStatus(providerId, status);
    }
} 