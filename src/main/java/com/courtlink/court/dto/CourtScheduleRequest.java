package com.courtlink.court.dto;

import com.courtlink.court.entity.CourtSchedule;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourtScheduleRequest {
    
    @NotNull(message = "场地ID不能为空")
    private Long courtId;
    
    @NotNull(message = "星期几不能为空")
    private DayOfWeek dayOfWeek;
    
    @NotNull(message = "开放时间不能为空")
    private LocalTime openTime;
    
    @NotNull(message = "关闭时间不能为空")
    private LocalTime closeTime;
    
    private Boolean isActive = true;
    
    // 特殊日期（可选）
    private LocalDate specialDate;
    
    @NotNull(message = "时间表类型不能为空")
    private CourtSchedule.ScheduleType scheduleType = CourtSchedule.ScheduleType.REGULAR;
    
    @Min(value = 15, message = "时间段不能少于15分钟")
    @Max(value = 240, message = "时间段不能超过240分钟")
    private Integer slotDuration = 60;
    
    @Min(value = 1, message = "提前预订时间不能少于1小时")
    @Max(value = 168, message = "提前预订时间不能超过168小时(7天)")
    private Integer advanceBookingHours = 24;
    
    @Min(value = 0, message = "取消时间限制不能少于0小时")
    @Max(value = 48, message = "取消时间限制不能超过48小时")
    private Integer cancellationDeadlineHours = 2;
    
    @Size(max = 500, message = "描述不能超过500个字符")
    private String description;
    
    /**
     * 验证时间设置是否合理
     */
    public boolean isTimeValid() {
        if (openTime == null || closeTime == null) {
            return false;
        }
        return openTime.isBefore(closeTime);
    }
    
    /**
     * 验证特殊日期设置
     */
    public boolean isSpecialDateValid() {
        if (scheduleType == CourtSchedule.ScheduleType.SPECIAL) {
            return specialDate != null && !specialDate.isBefore(LocalDate.now());
        }
        return true;
    }
} 