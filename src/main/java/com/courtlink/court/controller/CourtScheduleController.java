package com.courtlink.court.controller;

import com.courtlink.court.dto.*;
import com.courtlink.court.service.CourtScheduleService;
import com.courtlink.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class CourtScheduleController {
    
    private final CourtScheduleService scheduleService;
    private final AdminService adminService;
    
    /**
     * 创建单个时间表
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourtScheduleDTO> createSchedule(
            @Valid @RequestBody CourtScheduleRequest request,
            @RequestHeader("Authorization") String token) {
        
        String operatorName = adminService.getCurrentAdmin().getUsername();
        CourtScheduleDTO created = scheduleService.createSchedule(request, operatorName);
        
        log.info("Created schedule {} for court {} by {}", created.getId(), created.getCourtId(), operatorName);
        return ResponseEntity.ok(created);
    }
    
    /**
     * 批量创建时间表
     */
    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CourtScheduleDTO>> createSchedulesBatch(
            @Valid @RequestBody CourtScheduleBatchRequest request,
            @RequestHeader("Authorization") String token) {
        
        String operatorName = adminService.getCurrentAdmin().getUsername();
        List<CourtScheduleDTO> created = scheduleService.createSchedulesBatch(request, operatorName);
        
        log.info("Created {} schedules in batch by {}", created.size(), operatorName);
        return ResponseEntity.ok(created);
    }
    
    /**
     * 更新时间表
     */
    @PutMapping("/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourtScheduleDTO> updateSchedule(
            @PathVariable Long scheduleId,
            @Valid @RequestBody CourtScheduleRequest request,
            @RequestHeader("Authorization") String token) {
        
        String operatorName = adminService.getCurrentAdmin().getUsername();
        CourtScheduleDTO updated = scheduleService.updateSchedule(scheduleId, request, operatorName);
        
        log.info("Updated schedule {} by {}", scheduleId, operatorName);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * 删除时间表
     */
    @DeleteMapping("/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSchedule(
            @PathVariable Long scheduleId,
            @RequestHeader("Authorization") String token) {
        
        String operatorName = adminService.getCurrentAdmin().getUsername();
        scheduleService.deleteSchedule(scheduleId, operatorName);
        
        log.info("Deleted schedule {} by {}", scheduleId, operatorName);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 获取场地的所有时间表
     */
    @GetMapping("/court/{courtId}")
    public ResponseEntity<List<CourtScheduleDTO>> getSchedulesByCourtId(@PathVariable Long courtId) {
        List<CourtScheduleDTO> schedules = scheduleService.getSchedulesByCourtId(courtId);
        return ResponseEntity.ok(schedules);
    }
    
    /**
     * 获取场地在指定日期的有效时间表
     */
    @GetMapping("/court/{courtId}/effective")
    public ResponseEntity<List<CourtScheduleDTO>> getEffectiveSchedules(
            @PathVariable Long courtId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        List<CourtScheduleDTO> schedules = scheduleService.getEffectiveSchedules(courtId, date);
        return ResponseEntity.ok(schedules);
    }
    
    /**
     * 检查场地在指定时间是否开放
     */
    @GetMapping("/court/{courtId}/open")
    public ResponseEntity<Boolean> isCourtOpenAt(
            @PathVariable Long courtId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time) {
        
        boolean isOpen = scheduleService.isCourtOpenAt(courtId, date, time);
        return ResponseEntity.ok(isOpen);
    }
    
    /**
     * 获取场地在指定日期的可用时间段
     */
    @GetMapping("/court/{courtId}/slots")
    public ResponseEntity<List<Map<String, LocalTime>>> getAvailableTimeSlots(
            @PathVariable Long courtId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        List<Map<String, LocalTime>> slots = scheduleService.getAvailableTimeSlots(courtId, date);
        return ResponseEntity.ok(slots);
    }
    
    /**
     * 设置标准工作时间（周一到周日）
     */
    @PostMapping("/standard-hours")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CourtScheduleDTO>> setStandardWorkingHours(
            @RequestParam Long courtId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime openTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime closeTime,
            @RequestHeader("Authorization") String token) {
        
        String operatorName = adminService.getCurrentAdmin().getUsername();
        List<CourtScheduleDTO> schedules = scheduleService.setStandardWorkingHours(courtId, openTime, closeTime, operatorName);
        
        log.info("Set standard working hours for court {} by {}", courtId, operatorName);
        return ResponseEntity.ok(schedules);
    }
    
    /**
     * 批量设置标准工作时间
     */
    @PostMapping("/standard-hours/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CourtScheduleDTO>> setStandardWorkingHoursBatch(
            @RequestParam List<Long> courtIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime openTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime closeTime,
            @RequestHeader("Authorization") String token) {
        
        String operatorName = adminService.getCurrentAdmin().getUsername();
        List<CourtScheduleDTO> schedules = scheduleService.setStandardWorkingHoursBatch(courtIds, openTime, closeTime, operatorName);
        
        log.info("Set standard working hours for {} courts by {}", courtIds.size(), operatorName);
        return ResponseEntity.ok(schedules);
    }
    
    /**
     * 获取时间表统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getScheduleStatistics() {
        Map<String, Object> statistics = scheduleService.getScheduleStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * 获取场地的周总开放小时数
     */
    @GetMapping("/court/{courtId}/weekly-hours")
    public ResponseEntity<Double> getWeeklyOpenHours(@PathVariable Long courtId) {
        double hours = scheduleService.getWeeklyOpenHours(courtId);
        return ResponseEntity.ok(hours);
    }
} 