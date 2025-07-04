package com.courtlink.court.dto;

import com.courtlink.court.entity.CourtSchedule;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourtScheduleBatchRequest {
    
    @NotEmpty(message = "场地ID列表不能为空")
    private List<Long> courtIds;
    
    @NotEmpty(message = "工作日列表不能为空")
    private List<DayOfWeek> daysOfWeek;
    
    @NotNull(message = "开放时间不能为空")
    private LocalTime openTime;
    
    @NotNull(message = "关闭时间不能为空")
    private LocalTime closeTime;
    
    private Boolean isActive = true;
    
    // 特殊日期列表（用于批量设置节假日时间表）
    private List<LocalDate> specialDates;
    
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
    
    // 是否覆盖已存在的时间表
    private Boolean overrideExisting = false;
    
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
    public boolean isSpecialDatesValid() {
        if (scheduleType == CourtSchedule.ScheduleType.SPECIAL) {
            if (specialDates == null || specialDates.isEmpty()) {
                return false;
            }
            return specialDates.stream().allMatch(date -> !date.isBefore(LocalDate.now()));
        }
        return true;
    }
    
    /**
     * 计算总共会创建多少个时间表
     */
    public int getTotalSchedulesCount() {
        int baseCount = courtIds.size() * daysOfWeek.size();
        if (scheduleType == CourtSchedule.ScheduleType.SPECIAL && specialDates != null) {
            baseCount *= specialDates.size();
        }
        return baseCount;
    }
} 