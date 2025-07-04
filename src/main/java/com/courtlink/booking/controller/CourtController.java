package com.courtlink.booking.controller;

import com.courtlink.booking.entity.Court;
import com.courtlink.booking.service.CourtService;
import lombok.RequiredArgsConstructor;
<<<<<<< HEAD
import lombok.extern.slf4j.Slf4j;
=======
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
<<<<<<< HEAD
@RequestMapping("/api/v1/courts")
@RequiredArgsConstructor
@Slf4j
=======
@RequestMapping("/api/courts")
@RequiredArgsConstructor
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
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

<<<<<<< HEAD
    // 测试端点
    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Test endpoint works!");
    }

    // 用户预约信息API - 包含时间段信息
    @GetMapping("/booking")
    public ResponseEntity<?> getCourtsForBooking(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        try {
            LocalDate targetDate = date != null ? date : LocalDate.now();
            log.info("获取预约场地信息，日期: {}", targetDate);

            // 获取包含时间段的场地信息
            List<Court> courts = courtService.getCourtsWithTimeSlots(targetDate);
            log.info("成功获取场地信息，数量: {}", courts.size());

            return ResponseEntity.ok(courts);
        } catch (Exception e) {
            log.error("获取预约场地信息失败", e);
            
            // 返回详细的错误信息
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取场地信息失败: " + e.getMessage());
            errorResponse.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.internalServerError().body(errorResponse);
=======
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
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
        }
    }
} 