package com.courtlink.booking.service.impl;

import com.courtlink.booking.dto.AppointmentRequest;
import com.courtlink.booking.dto.AppointmentResponse;
import com.courtlink.booking.entity.Appointment;
import com.courtlink.booking.entity.Court;
import com.courtlink.booking.entity.CourtTimeSlot;
import com.courtlink.booking.repository.AppointmentRepository;
import com.courtlink.booking.repository.CourtRepository;
import com.courtlink.booking.repository.CourtTimeSlotRepository;
import com.courtlink.booking.service.AppointmentService;
import com.courtlink.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final CourtRepository courtRepository;
    private final CourtTimeSlotRepository timeSlotRepository;

    @Override
    @Transactional
    public AppointmentResponse createAppointment(AppointmentRequest request, User user) {
        log.info("创建预约：用户ID={}, 场地ID={}, 日期={}", user.getId(), request.getCourtId(), request.getAppointmentDate());

        // 1. 验证预约请求
        validateAppointmentRequest(request, user);

        // 2. 获取场地信息
        Court court = courtRepository.findById(request.getCourtId())
                .orElseThrow(() -> new IllegalArgumentException("场地不存在"));

        if (!court.isAvailable()) {
            throw new IllegalArgumentException("场地当前不可用");
        }

        // 3. 获取并验证时间段
        List<CourtTimeSlot> timeSlots = timeSlotRepository.findAllById(request.getTimeSlotIds());
        if (timeSlots.size() != request.getTimeSlotIds().size()) {
            throw new IllegalArgumentException("部分时间段不存在");
        }

        // 验证时间段属于同一场地
        boolean allSameCourt = timeSlots.stream()
                .allMatch(slot -> slot.getCourt().getId().equals(request.getCourtId()));
        if (!allSameCourt) {
            throw new IllegalArgumentException("所有时间段必须属于同一场地");
        }

        // 验证时间段是否可用
        timeSlots.forEach(slot -> {
            if (!slot.isAvailable() || !slot.isOpen() || slot.getAppointment() != null) {
                throw new IllegalArgumentException("时间段 " + slot.getStartTime() + "-" + slot.getEndTime() + " 不可用");
            }
        });

        // 验证时间段是否相邻（如果选择了2个时间段）
        if (timeSlots.size() == 2) {
            validateAdjacentTimeSlots(timeSlots);
        }

        // 4. 计算总价
        double totalPrice = timeSlots.size() * court.getPricePerHour().doubleValue();

        // 5. 创建预约
        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setCourt(court);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setStartTime(request.getStartTime());
        appointment.setEndTime(request.getEndTime());
        appointment.setTotalPrice(totalPrice);
        appointment.setNote(request.getNote());
        appointment.setStatus(Appointment.AppointmentStatus.PENDING);

        // 6. 保存预约
        appointment = appointmentRepository.save(appointment);

        // 7. 更新时间段状态
        final Appointment savedAppointment = appointment;
        timeSlots.forEach(slot -> {
            slot.setAvailable(false);
            slot.setAppointment(savedAppointment);
        });
        timeSlotRepository.saveAll(timeSlots);

        log.info("预约创建成功：预约ID={}", appointment.getId());
        return convertToResponse(appointment);
    }

    @Override
    public List<AppointmentResponse> getUserAppointments(User user) {
        List<Appointment> appointments = appointmentRepository.findByUserOrderByAppointmentDateDescCreatedAtDesc(user);
        return appointments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AppointmentResponse getAppointmentById(Long id, User user) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("预约不存在"));

        if (!appointment.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("无权访问此预约");
        }

        return convertToResponse(appointment);
    }

    @Override
    @Transactional
    public void cancelAppointment(Long id, User user) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("预约不存在"));

        if (!appointment.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("无权取消此预约");
        }

        if (appointment.getStatus() == Appointment.AppointmentStatus.CANCELLED) {
            throw new IllegalArgumentException("预约已经被取消");
        }

        if (appointment.getStatus() == Appointment.AppointmentStatus.COMPLETED) {
            throw new IllegalArgumentException("已完成的预约无法取消");
        }

        // 更新预约状态
        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);

        // 释放时间段
        List<CourtTimeSlot> timeSlots = timeSlotRepository.findByAppointmentId(id);
        timeSlots.forEach(slot -> {
            slot.setAvailable(true);
            slot.setAppointment(null);
        });
        timeSlotRepository.saveAll(timeSlots);

        log.info("预约已取消：预约ID={}", id);
    }

    @Override
    public void validateAppointmentRequest(AppointmentRequest request, User user) {
        // 限制1：检查用户在该日期该场地是否已有预约
        long existingCount = appointmentRepository.countUserAppointmentsOnCourtAndDate(
                user, request.getCourtId(), request.getAppointmentDate());
        if (existingCount > 0) {
            throw new IllegalArgumentException("您在该日期已有该场地的预约");
        }

        // 限制2：验证时间段数量（最多2个）
        if (request.getTimeSlotIds().size() > 2) {
            throw new IllegalArgumentException("最多只能选择2个时间段");
        }

        // 限制3：如果选择2个时间段，验证它们是否相邻
        if (request.getTimeSlotIds().size() == 2) {
            List<CourtTimeSlot> timeSlots = timeSlotRepository.findAllById(request.getTimeSlotIds());
            if (timeSlots.size() != 2) {
                throw new IllegalArgumentException("时间段信息不完整");
            }
            validateAdjacentTimeSlots(timeSlots);
        }

        // 检查时间冲突
        List<Appointment> conflictingAppointments = appointmentRepository.findConflictingAppointments(
                request.getCourtId(), request.getAppointmentDate(), 
                request.getStartTime(), request.getEndTime());
        if (!conflictingAppointments.isEmpty()) {
            throw new IllegalArgumentException("选择的时间段与现有预约冲突");
        }
    }

    /**
     * 验证时间段是否相邻
     */
    private void validateAdjacentTimeSlots(List<CourtTimeSlot> timeSlots) {
        if (timeSlots.size() != 2) {
            return;
        }

        CourtTimeSlot slot1 = timeSlots.get(0);
        CourtTimeSlot slot2 = timeSlots.get(1);

        // 按开始时间排序
        if (slot1.getStartTime().isAfter(slot2.getStartTime())) {
            CourtTimeSlot temp = slot1;
            slot1 = slot2;
            slot2 = temp;
        }

        // 检查是否相邻：第一个时间段的结束时间应该等于第二个时间段的开始时间
        if (!slot1.getEndTime().equals(slot2.getStartTime())) {
            throw new IllegalArgumentException("选择的两个时间段必须相邻");
        }

        // 进一步验证：时间差应该正好是1小时
        long hoursDiff = slot2.getStartTime().getHour() - slot1.getStartTime().getHour();
        if (hoursDiff != 1) {
            throw new IllegalArgumentException("选择的两个时间段必须相邻");
        }
    }

    @Override
    public AppointmentResponse convertToResponse(Appointment appointment) {
        List<AppointmentResponse.TimeSlotInfo> timeSlotInfos = null;
        if (appointment.getTimeSlots() != null) {
            timeSlotInfos = appointment.getTimeSlots().stream()
                    .map(slot -> AppointmentResponse.TimeSlotInfo.builder()
                            .id(slot.getId())
                            .startTime(slot.getStartTime())
                            .endTime(slot.getEndTime())
                            .note(slot.getNote())
                            .build())
                    .collect(Collectors.toList());
        }

        return AppointmentResponse.builder()
                .id(appointment.getId())
                .userId(appointment.getUser().getId())
                .userName(appointment.getUser().getUsername())
                .courtId(appointment.getCourt().getId())
                .courtName(appointment.getCourt().getName())
                .appointmentDate(appointment.getAppointmentDate())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .totalPrice(appointment.getTotalPrice())
                .status(appointment.getStatus().name())
                .note(appointment.getNote())
                .timeSlots(timeSlotInfos)
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }
} 