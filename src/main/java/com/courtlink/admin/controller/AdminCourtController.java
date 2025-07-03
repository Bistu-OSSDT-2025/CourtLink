package com.courtlink.admin.controller;

import com.courtlink.booking.dto.*;
import com.courtlink.booking.entity.Court;
import com.courtlink.booking.service.CourtService;
import com.courtlink.admin.service.AdminCourtService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/courts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminCourtController {

    private final CourtService courtService;
    private final AdminCourtService adminCourtService;

    // 获取所有场地的管理视图
    @GetMapping("/management")
    public ResponseEntity<List<CourtManagementDto>> getCourtsForManagement(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(adminCourtService.getCourtsWithTimeSlots(targetDate));
    }

    // 创建新场地
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<Court> createCourt(@RequestBody CreateCourtRequest request) {
        Court court = adminCourtService.createCourt(request);
        return ResponseEntity.ok(court);
    }

    // 更新场地信息
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<Court> updateCourt(@PathVariable Long id, @RequestBody UpdateCourtRequest request) {
        Court court = adminCourtService.updateCourt(id, request);
        return ResponseEntity.ok(court);
    }

    // 删除场地
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteCourt(@PathVariable Long id) {
        adminCourtService.deleteCourt(id);
        return ResponseEntity.ok().build();
    }

    // 批量更新时间段开放状态
    @PutMapping("/time-slots/batch-update")
    public ResponseEntity<Void> batchUpdateTimeSlots(@RequestBody List<TimeSlotUpdateRequest> requests) {
        adminCourtService.batchUpdateTimeSlots(requests);
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