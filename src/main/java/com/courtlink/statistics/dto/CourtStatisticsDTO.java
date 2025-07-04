package com.courtlink.statistics.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class CourtStatisticsDTO {
    private Long courtId;
    private String courtName;
    private Integer totalBookings;
    private Integer completedBookings;
    private Integer cancelledBookings;
    private BigDecimal totalRevenue;
    private Double occupancyRate; // 使用率（0-1）
    private Integer peakHourBookings;
    private Map<String, Integer> bookingsByStatus; // 各状态预约数量
    private Map<String, Integer> bookingsByHour; // 按小时统计的预约数量
    private LocalDateTime startTime;
    private LocalDateTime endTime;
} 