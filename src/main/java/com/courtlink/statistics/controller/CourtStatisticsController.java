package com.courtlink.statistics.controller;

import com.courtlink.statistics.dto.CourtStatisticsDTO;
import com.courtlink.statistics.service.CourtStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
@Tag(name = "场地统计", description = "场地使用统计相关接口")
public class CourtStatisticsController {

    private final CourtStatisticsService courtStatisticsService;

    @GetMapping("/courts/{courtId}")
    @Operation(summary = "获取指定场地的统计数据")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourtStatisticsDTO> getCourtStatistics(
            @PathVariable Long courtId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(courtStatisticsService.getCourtStatistics(courtId, startTime, endTime));
    }

    @GetMapping("/courts")
    @Operation(summary = "获取所有场地的统计数据")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CourtStatisticsDTO>> getAllCourtsStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(courtStatisticsService.getAllCourtsStatistics(startTime, endTime));
    }

    @GetMapping("/courts/{courtId}/daily")
    @Operation(summary = "获取指定场地的每日统计数据")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourtStatisticsDTO> getCourtDailyStatistics(
            @PathVariable Long courtId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        return ResponseEntity.ok(courtStatisticsService.getCourtDailyStatistics(courtId, date));
    }

    @GetMapping("/courts/{courtId}/monthly")
    @Operation(summary = "获取指定场地的月度统计数据")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourtStatisticsDTO> getCourtMonthlyStatistics(
            @PathVariable Long courtId,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(courtStatisticsService.getCourtMonthlyStatistics(courtId, year, month));
    }

    @GetMapping("/courts/top-performing")
    @Operation(summary = "获取表现最好的场地")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CourtStatisticsDTO>> getTopPerformingCourts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(courtStatisticsService.getTopPerformingCourts(startTime, endTime, limit));
    }

    @GetMapping("/courts/occupancy-rate")
    @Operation(summary = "获取场地整体使用率")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Double> getOverallOccupancyRate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(courtStatisticsService.getOverallOccupancyRate(startTime, endTime));
    }
} 