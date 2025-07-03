package com.courtlink.booking.controller;

import com.courtlink.booking.entity.Court;
import com.courtlink.booking.service.CourtService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/courts")
@RequiredArgsConstructor
public class CourtController {

    private final CourtService courtService;

    @GetMapping
    public ResponseEntity<List<Court>> getAllCourts() {
        return ResponseEntity.ok(courtService.getAllCourts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Court> getCourtById(@PathVariable Long id) {
        return ResponseEntity.ok(courtService.getCourtById(id));
    }

    @GetMapping("/available")
    public ResponseEntity<List<Court>> getAvailableCourts() {
        return ResponseEntity.ok(courtService.getAvailableCourts());
    }

    // 简化版的用户预约信息API
    @GetMapping("/booking")
    public ResponseEntity<Map<String, Object>> getCourtsForBooking(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            LocalDate targetDate = date != null ? date : LocalDate.now();
            
            // 返回基本的场地信息，用户可以基于此进行预约
            List<Court> courts = courtService.getAvailableCourts();
            
            Map<String, Object> response = new HashMap<>();
            response.put("courts", courts);
            response.put("date", targetDate.toString());
            response.put("success", true);
            response.put("message", "请选择场地后查看具体时间段");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取场地信息失败: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }
} 