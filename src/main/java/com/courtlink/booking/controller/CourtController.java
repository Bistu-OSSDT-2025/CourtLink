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

    // 用户预约信息API - 包含时间段信息
    @GetMapping("/booking")
    public ResponseEntity<List<Court>> getCourtsForBooking(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            LocalDate targetDate = date != null ? date : LocalDate.now();
            
            // 获取包含时间段的场地信息
            List<Court> courts = courtService.getCourtsWithTimeSlots(targetDate);
            
            return ResponseEntity.ok(courts);
        } catch (Exception e) {
            // 返回错误响应
            return ResponseEntity.internalServerError().build();
        }
    }
} 