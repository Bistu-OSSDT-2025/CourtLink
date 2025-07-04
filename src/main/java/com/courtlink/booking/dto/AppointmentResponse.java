package com.courtlink.booking.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentResponse {

    private Long id;
    private Long userId;
    private String userName;
    private Long courtId;
    private String courtName;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Double totalPrice;
    private String status;
    private String note;
    private List<TimeSlotInfo> timeSlots;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TimeSlotInfo {
        private Long id;
        private LocalTime startTime;
        private LocalTime endTime;
        private String note;
    }

    // 计算预约时长
    public int getDurationHours() {
        if (startTime != null && endTime != null) {
            return endTime.getHour() - startTime.getHour();
        }
        return 0;
    }

    // 格式化时间范围
    public String getTimeRange() {
        if (startTime != null && endTime != null) {
            return String.format("%s - %s", startTime.toString(), endTime.toString());
        }
        return "";
    }
} 