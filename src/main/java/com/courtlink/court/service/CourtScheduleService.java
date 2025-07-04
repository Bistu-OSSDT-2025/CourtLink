package com.courtlink.court.service;

import com.courtlink.court.dto.*;
import com.courtlink.court.entity.CourtSchedule;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public interface CourtScheduleService {
    
    /**
     * 创建单个时间表
     */
    CourtScheduleDTO createSchedule(CourtScheduleRequest request, String operatorName);
    
    /**
     * 批量创建时间表
     */
    List<CourtScheduleDTO> createSchedulesBatch(CourtScheduleBatchRequest request, String operatorName);
    
    /**
     * 更新时间表
     */
    CourtScheduleDTO updateSchedule(Long scheduleId, CourtScheduleRequest request, String operatorName);
    
    /**
     * 删除时间表
     */
    void deleteSchedule(Long scheduleId, String operatorName);
    
    /**
     * 批量删除时间表
     */
    void deleteSchedulesBatch(List<Long> scheduleIds, String operatorName);
    
    /**
     * 根据ID获取时间表
     */
    CourtScheduleDTO getScheduleById(Long scheduleId);
    
    /**
     * 获取场地的所有时间表
     */
    List<CourtScheduleDTO> getSchedulesByCourtId(Long courtId);
    
    /**
     * 获取场地在指定日期的有效时间表
     */
    List<CourtScheduleDTO> getEffectiveSchedules(Long courtId, LocalDate date);
    
    /**
     * 检查场地在指定时间是否开放
     */
    boolean isCourtOpenAt(Long courtId, LocalDate date, LocalTime time);
    
    /**
     * 获取场地在指定日期的可用时间段
     */
    List<Map<String, LocalTime>> getAvailableTimeSlots(Long courtId, LocalDate date);
    
    /**
     * 复制时间表到其他场地
     */
    List<CourtScheduleDTO> copySchedulesToCourts(Long sourceCourtId, List<Long> targetCourtIds, String operatorName);
    
    /**
     * 为场地设置标准工作时间（周一到周日）
     */
    List<CourtScheduleDTO> setStandardWorkingHours(Long courtId, LocalTime openTime, LocalTime closeTime, String operatorName);
    
    /**
     * 批量设置多个场地的标准工作时间
     */
    List<CourtScheduleDTO> setStandardWorkingHoursBatch(List<Long> courtIds, LocalTime openTime, LocalTime closeTime, String operatorName);
    
    /**
     * 设置特殊日期时间表（节假日等）
     */
    CourtScheduleDTO setSpecialDateSchedule(Long courtId, LocalDate specialDate, LocalTime openTime, LocalTime closeTime, String description, String operatorName);
    
    /**
     * 批量设置特殊日期时间表
     */
    List<CourtScheduleDTO> setSpecialDateSchedulesBatch(List<Long> courtIds, List<LocalDate> specialDates, LocalTime openTime, LocalTime closeTime, String description, String operatorName);
    
    /**
     * 启用/禁用时间表
     */
    CourtScheduleDTO toggleScheduleStatus(Long scheduleId, boolean isActive, String operatorName);
    
    /**
     * 批量启用/禁用时间表
     */
    List<CourtScheduleDTO> toggleScheduleStatusBatch(List<Long> scheduleIds, boolean isActive, String operatorName);
    
    /**
     * 获取时间表统计信息
     */
    Map<String, Object> getScheduleStatistics();
    
    /**
     * 获取即将过期的特殊时间表
     */
    List<CourtScheduleDTO> getExpiringSpecialSchedules(int daysAhead);
    
    /**
     * 验证时间表设置是否合理
     */
    boolean validateScheduleRequest(CourtScheduleRequest request);
    
    /**
     * 检查时间表冲突
     */
    List<CourtScheduleDTO> checkScheduleConflicts(Long courtId, DayOfWeek dayOfWeek, CourtSchedule.ScheduleType scheduleType, LocalDate specialDate);
    
    /**
     * 获取场地的周总开放小时数
     */
    double getWeeklyOpenHours(Long courtId);
    
    /**
     * 调整时间段长度
     */
    CourtScheduleDTO adjustSlotDuration(Long scheduleId, int newSlotDuration, String operatorName);
    
    /**
     * 批量调整时间段长度
     */
    List<CourtScheduleDTO> adjustSlotDurationBatch(List<Long> scheduleIds, int newSlotDuration, String operatorName);
    
    /**
     * 设置预订和取消规则
     */
    CourtScheduleDTO setBookingRules(Long scheduleId, int advanceBookingHours, int cancellationDeadlineHours, String operatorName);
    
    /**
     * 根据时间表类型获取时间表
     */
    List<CourtScheduleDTO> getSchedulesByType(CourtSchedule.ScheduleType scheduleType);
    
    /**
     * 清理过期的特殊时间表
     */
    int cleanupExpiredSpecialSchedules(String operatorName);
    
    /**
     * 导入时间表模板
     */
    List<CourtScheduleDTO> importScheduleTemplate(List<CourtScheduleRequest> scheduleRequests, String operatorName);
    
    /**
     * 导出场地时间表
     */
    List<CourtScheduleDTO> exportCourtSchedules(Long courtId);
} 