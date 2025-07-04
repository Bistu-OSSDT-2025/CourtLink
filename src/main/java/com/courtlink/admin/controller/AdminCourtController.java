package com.courtlink.admin.controller;

import com.courtlink.booking.dto.*;
import com.courtlink.booking.entity.Court;
import com.courtlink.booking.service.CourtService;
import com.courtlink.admin.service.AdminCourtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/courts")
@RequiredArgsConstructor
@Slf4j
public class AdminCourtController {

    private final CourtService courtService;
    private final AdminCourtService adminCourtService;

    // 测试权限的简单端点
    @GetMapping("/test-auth")
    public ResponseEntity<Map<String, Object>> testAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> response = new HashMap<>();
        
        if (auth != null) {
            response.put("authenticated", true);
            response.put("username", auth.getName());
            response.put("authorities", auth.getAuthorities());
            response.put("principal", auth.getPrincipal().getClass().getSimpleName());
        } else {
            response.put("authenticated", false);
        }
        
        log.info("管理员权限测试 - 认证状态: {}", response);
        return ResponseEntity.ok(response);
    }

    // 获取所有场地的管理视图
    @GetMapping("/management")
    public ResponseEntity<List<CourtManagementDto>> getCourtsForManagement(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("获取场地管理数据，日期: {}", date);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("当前认证状态: {}, 权限: {}", auth != null ? auth.getName() : "未认证", 
                auth != null ? auth.getAuthorities() : "无权限");
                
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(adminCourtService.getCourtsWithTimeSlots(targetDate));
    }

    // 创建新场地
    @PostMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<Court> createCourt(@RequestBody CreateCourtRequest request) {
        Court court = adminCourtService.createCourt(request);
        return ResponseEntity.ok(court);
    }

    // 更新场地信息
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<Court> updateCourt(@PathVariable Long id, @RequestBody UpdateCourtRequest request) {
        Court court = adminCourtService.updateCourt(id, request);
        return ResponseEntity.ok(court);
    }

    // 删除场地
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<Void> deleteCourt(@PathVariable Long id) {
        adminCourtService.deleteCourt(id);
        return ResponseEntity.ok().build();
    }

    // 批量更新时间段开放状态
    @PutMapping("/time-slots/batch-update")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_MODERATOR')")
    public ResponseEntity<Void> batchUpdateTimeSlots(@RequestBody List<TimeSlotUpdateRequest> requests) {
        log.info("收到批量更新请求，请求数量: {}", requests != null ? requests.size() : 0);
        if (requests != null) {
            for (TimeSlotUpdateRequest request : requests) {
                log.info("更新请求详情: timeSlotId={}, open={}, note={}", 
                        request.getTimeSlotId(), request.isOpen(), request.getNote());
            }
        }
        
        adminCourtService.batchUpdateTimeSlots(requests);
        log.info("批量更新请求处理完成");
        return ResponseEntity.ok().build();
    }

    // 生成指定日期的时间段
    @PostMapping("/{courtId}/generate-time-slots")
    public ResponseEntity<Void> generateTimeSlots(
            @PathVariable Long courtId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        adminCourtService.generateTimeSlotsForDate(courtId, date);
        return ResponseEntity.ok().build();
    }

    // 获取场地统计信息
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getCourtStatistics() {
        return ResponseEntity.ok(adminCourtService.getCourtStatistics());
    }

    // 设置场地开放状态
    @PutMapping("/{courtId}/availability")
    public ResponseEntity<Void> setCourtAvailability(
            @PathVariable Long courtId, 
            @RequestParam boolean available) {
        adminCourtService.setCourtAvailability(courtId, available);
        return ResponseEntity.ok().build();
    }
} 