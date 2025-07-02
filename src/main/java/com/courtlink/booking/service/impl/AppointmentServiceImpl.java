package com.courtlink.booking.service.impl;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.Duration;

@Service
public class AppointmentServiceImpl {
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

        // 计算预约时长（小时）
        long hours = Duration.between(startTime, endTime).toHours();

        // 验证预约时长必须在1-2小时之间
        if (hours < 1 || hours > 2) {
            throw new RuntimeException("预约时长必须在1-2小时之间");
        }

        // 验证开始时间必须是整点
        if (startTime.getMinute() != 0 || startTime.getSecond() != 0) {
            throw new RuntimeException("预约开始时间必须是整点（如：14:00）");
        }

        // 验证结束时间必须是整点
        if (endTime.getMinute() != 0 || endTime.getSecond() != 0) {
            throw new RuntimeException("预约结束时间必须是整点（如：15:00）");
        }

        // 获取开始和结束的小时
        int startHour = startTime.getHour();
        int endHour = endTime.getHour();

        // 验证时间段是否在允许的范围内
        boolean isValidTimeSlot = 
            // 上午时段：9:00-11:00
            (startHour >= 9 && startHour <= 10 && endHour <= 11) ||
            // 下午时段：14:00-20:00
            (startHour >= 14 && startHour <= 19 && endHour <= 20);

        if (!isValidTimeSlot) {
            throw new RuntimeException("预约时间必须在允许的时间段内（9:00-11:00或14:00-20:00）");
        }
    }
} 