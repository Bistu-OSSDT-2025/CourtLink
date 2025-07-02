package com.courtlink.booking.service.impl;

import com.courtlink.booking.dto.AppointmentQuery;
import com.courtlink.booking.dto.AppointmentRequest;
import com.courtlink.booking.dto.AppointmentResponse;
import com.courtlink.booking.dto.AppointmentStatistics;
import com.courtlink.booking.entity.Appointment;
import com.courtlink.booking.exception.ResourceNotFoundException;
import com.courtlink.booking.repository.AppointmentRepository;
import com.courtlink.booking.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Appointment Service Implementation
 * 
 * @author CourtLink Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;

    @Override
    @Transactional
    public AppointmentResponse createAppointment(String userId, AppointmentRequest request) {
        log.info("Creating appointment: userId={}, providerId={}, startTime={}, endTime={}",
                userId, request.getProviderId(), request.getStartTime(), request.getEndTime());

        // 验证预约时间
        validateAppointmentTime(request.getStartTime(), request.getEndTime());

        // 检查时间冲突
        List<Appointment> conflictingAppointments = appointmentRepository.findConflictingAppointments(
            request.getProviderId(), request.getStartTime(), request.getEndTime(), null);

        if (!conflictingAppointments.isEmpty()) {
            log.warn("Appointment time conflict: providerId={}, startTime={}, endTime={}, conflictCount={}", 
                request.getProviderId(), request.getStartTime(), request.getEndTime(), conflictingAppointments.size());
            throw new RuntimeException("预约时间冲突，请选择其他时间段");
        }

        // Create appointment
        Appointment appointment = new Appointment();
        appointment.setUserId(userId);
        appointment.setProviderId(request.getProviderId());
        appointment.setServiceType(request.getServiceType());
        appointment.setStartTime(request.getStartTime());
        appointment.setEndTime(request.getEndTime());
        appointment.setAmount(request.getAmount());
        appointment.setNotes(request.getNotes());
        appointment.setStatus(Appointment.AppointmentStatus.PENDING);
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setUpdatedAt(LocalDateTime.now());
        
        Appointment savedAppointment = appointmentRepository.save(appointment);
        log.info("Appointment created successfully: appointmentId={}", savedAppointment.getId());
        
        return AppointmentResponse.fromEntity(savedAppointment);
    }

    @Override
    @Transactional
    public AppointmentResponse updateAppointment(Long id, AppointmentRequest request) {
        log.info("Updating appointment: appointmentId={}", id);
        
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Appointment not found: appointmentId={}", id);
                    return new RuntimeException("Appointment not found");
                });

        // Check for time conflicts excluding current appointment
        if (hasConflict(request.getProviderId(), request.getStartTime(), request.getEndTime(), id)) {
            log.warn("Appointment time conflict during update: providerId={}, startTime={}, endTime={}, excludeId={}",
                    request.getProviderId(), request.getStartTime(), request.getEndTime(), id);
            throw new RuntimeException("Appointment time conflict, please choose a different time");
        }

        // Update appointment
        appointment.setProviderId(request.getProviderId());
        appointment.setServiceType(request.getServiceType());
        appointment.setStartTime(request.getStartTime());
        appointment.setEndTime(request.getEndTime());
        appointment.setAmount(request.getAmount());
        appointment.setNotes(request.getNotes());

        Appointment savedAppointment = appointmentRepository.save(appointment);
        log.info("Appointment updated successfully: appointmentId={}", savedAppointment.getId());
        
        return AppointmentResponse.fromEntity(savedAppointment);
    }

    @Override
    @Transactional
    public AppointmentResponse cancelAppointment(Long id) {
        log.info("Cancelling appointment: appointmentId={}", id);
        
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Appointment not found: appointmentId={}", id);
                    return new RuntimeException("Appointment not found");
                });

        if (appointment.getStatus() == Appointment.AppointmentStatus.COMPLETED) {
            throw new RuntimeException("Cannot cancel a completed appointment");
        }

        if (appointment.getStatus() != Appointment.AppointmentStatus.PENDING 
            && appointment.getStatus() != Appointment.AppointmentStatus.CONFIRMED) {
            throw new RuntimeException("Appointment cannot be cancelled in current status: " + appointment.getStatus());
        }

        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        log.info("Appointment cancelled successfully: appointmentId={}", savedAppointment.getId());
        return AppointmentResponse.fromEntity(savedAppointment);
    }

    @Override
    @Transactional
    public AppointmentResponse completeAppointment(Long id) {
        log.info("Completing appointment: appointmentId={}", id);
        
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Appointment not found: appointmentId={}", id);
                    return new RuntimeException("Appointment not found");
                });

        appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        log.info("Appointment completed successfully: appointmentId={}", savedAppointment.getId());
        return AppointmentResponse.fromEntity(savedAppointment);
    }

    @Override
    public AppointmentResponse getAppointmentById(Long id) {
        log.info("Getting appointment by ID: appointmentId={}", id);
        
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Appointment not found: appointmentId={}", id);
                    return new RuntimeException("Appointment not found");
                });

        return AppointmentResponse.fromEntity(appointment);
    }

    @Override
    public Page<AppointmentResponse> getAppointmentsByUserId(String userId, Pageable pageable) {
        log.info("Getting appointments by user ID: userId={}", userId);
        
        Page<Appointment> appointments = appointmentRepository.findByUserId(userId, pageable);
        return appointments.map(AppointmentResponse::fromEntity);
    }

    @Override
    public Page<AppointmentResponse> getAppointmentsByProviderId(String providerId, Pageable pageable) {
        log.info("Getting appointments by provider ID: providerId={}", providerId);
        
        Page<Appointment> appointments = appointmentRepository.findByProviderId(providerId, pageable);
        return appointments.map(AppointmentResponse::fromEntity);
    }

    @Override
    public Page<AppointmentResponse> getAppointmentsByStatus(Appointment.AppointmentStatus status, Pageable pageable) {
        log.info("Getting appointments by status: status={}", status);
        
        Page<Appointment> appointments = appointmentRepository.findByStatus(status, pageable);
        return appointments.map(AppointmentResponse::fromEntity);
    }

    @Override
    public Page<AppointmentResponse> searchAppointments(AppointmentQuery query, Pageable pageable) {
        log.info("Searching appointments with query: {}", query);
        
        // This is a simplified implementation. In a real application,
        // you might use JPA Specifications or other query mechanisms
        Page<Appointment> appointments = appointmentRepository.findAll(pageable);
        return appointments.map(AppointmentResponse::fromEntity);
    }

    @Override
    public List<AppointmentResponse> getAppointmentsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Getting appointments by time range: startTime={}, endTime={}", startTime, endTime);
        
        List<Appointment> appointments = appointmentRepository.findByTimeRange(startTime, endTime);
        return appointments.stream()
                .map(AppointmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasConflict(String providerId, LocalDateTime startTime, LocalDateTime endTime, Long excludeId) {
        log.debug("Checking for appointment conflicts: providerId={}, startTime={}, endTime={}, excludeId={}",
                providerId, startTime, endTime, excludeId);
        
        long conflictCount = appointmentRepository.countConflictingAppointments(
                providerId, startTime, endTime, excludeId);
        
        boolean hasConflict = conflictCount > 0;
        log.debug("Conflict check result: hasConflict={}, conflictCount={}", hasConflict, conflictCount);
        
        return hasConflict;
    }

    @Override
    @Transactional
    public int processExpiredAppointments() {
        log.info("Processing expired appointments");
        
        LocalDateTime cutoffTime = LocalDateTime.now();
        List<Appointment> expiredAppointments = appointmentRepository.findExpiredAppointments(cutoffTime);
        
        for (Appointment appointment : expiredAppointments) {
            appointment.setStatus(Appointment.AppointmentStatus.EXPIRED);
        }
        
        appointmentRepository.saveAll(expiredAppointments);
        
        int processedCount = expiredAppointments.size();
        log.info("Processed {} expired appointments", processedCount);
        
        return processedCount;
    }

    @Override
    public AppointmentStatistics getStatisticsByUserId(String userId) {
        log.info("Getting appointment statistics for user: userId={}", userId);
        
        long completedCount = appointmentRepository.countByUserIdAndStatus(userId, Appointment.AppointmentStatus.COMPLETED);
        long cancelledCount = appointmentRepository.countByUserIdAndStatus(userId, Appointment.AppointmentStatus.CANCELLED);
        long pendingCount = appointmentRepository.countByUserIdAndStatus(userId, Appointment.AppointmentStatus.PENDING);
        long confirmedCount = appointmentRepository.countByUserIdAndStatus(userId, Appointment.AppointmentStatus.CONFIRMED);
        long totalCount = completedCount + cancelledCount + pendingCount + confirmedCount;
        
        return new AppointmentStatistics(totalCount, completedCount, cancelledCount, pendingCount);
    }

    @Override
    @Transactional
    public AppointmentResponse confirmAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        if (appointment.getStatus() != Appointment.AppointmentStatus.PENDING) {
            throw new IllegalStateException("Appointment cannot be confirmed because it is not in PENDING status");
        }

        // 设置支付ID
        appointment.setPaymentId(appointment.getPaymentId());
        appointment.setStatus(Appointment.AppointmentStatus.CONFIRMED);
        appointment = appointmentRepository.save(appointment);
        return AppointmentResponse.fromEntity(appointment);
    }

    private void validateAppointmentTime(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();
        
        // 验证开始时间不能是过去时间
        if (startTime.isBefore(now)) {
            throw new RuntimeException("预约开始时间不能是过去时间");
        }

        // 验证结束时间必须在开始时间之后
        if (!endTime.isAfter(startTime)) {
            throw new RuntimeException("预约结束时间必须在开始时间之后");
        }

        // 验证预约时长不能超过4小时
        Duration duration = Duration.between(startTime, endTime);
        if (duration.toHours() > 4) {
            throw new RuntimeException("预约时长不能超过4小时");
        }

        // 验证预约时长不能少于30分钟
        if (duration.toMinutes() < 30) {
            throw new RuntimeException("预约时长不能少于30分钟");
        }
    }

    private void checkTimeConflict(String providerId, LocalDateTime startTime, LocalDateTime endTime, Long excludeId) {
        // Implementation of checkTimeConflict method
    }
} 
