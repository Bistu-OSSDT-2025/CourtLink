package com.courtlink.court.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "court_schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourtSchedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "court_id", nullable = false)
    private Long courtId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;
    
    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;
    
    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    // 特殊日期设置（可选，用于节假日等特殊情况）
    @Column(name = "special_date")
    private java.time.LocalDate specialDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", nullable = false)
    private ScheduleType scheduleType = ScheduleType.REGULAR;
    
    // 时间分片设置（分钟为单位，用于预订时段划分）
    @Column(name = "slot_duration", nullable = false)
    private Integer slotDuration = 60; // 默认60分钟一个时段
    
    // 提前预订时间限制（小时）
    @Column(name = "advance_booking_hours")
    private Integer advanceBookingHours = 24; // 默认提前24小时可预订
    
    // 最晚取消时间（小时）
    @Column(name = "cancellation_deadline_hours")
    private Integer cancellationDeadlineHours = 2; // 默认提前2小时可取消
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "last_modified_at")
    private LocalDateTime lastModifiedAt;
    
    @Column(name = "last_modified_by")
    private String lastModifiedBy;
    
    @Column(name = "description")
    private String description; // 时间安排描述
    
    public enum ScheduleType {
        REGULAR,     // 常规时间表
        SPECIAL,     // 特殊日期时间表（节假日等）
        MAINTENANCE, // 维护时间表
        EVENT        // 活动时间表
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastModifiedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastModifiedAt = LocalDateTime.now();
    }
    
    /**
     * 检查指定时间是否在开放时间内
     */
    public boolean isTimeInRange(LocalTime time) {
        return !time.isBefore(openTime) && !time.isAfter(closeTime);
    }
    
    /**
     * 计算当天的总开放小时数
     */
    public double getTotalOpenHours() {
        if (openTime == null || closeTime == null) {
            return 0;
        }
        
        long minutes = java.time.Duration.between(openTime, closeTime).toMinutes();
        return minutes / 60.0;
    }
    
    /**
     * 获取可用的时间段数量
     */
    public int getAvailableSlots() {
        if (slotDuration == null || slotDuration <= 0) {
            return 0;
        }
        
        long totalMinutes = java.time.Duration.between(openTime, closeTime).toMinutes();
        return (int) (totalMinutes / slotDuration);
    }
    
    /**
     * 检查时间表是否有效
     */
    public boolean isValid() {
        return openTime != null && 
               closeTime != null && 
               openTime.isBefore(closeTime) &&
               slotDuration != null && 
               slotDuration > 0;
    }
} 