package com.bistu.ossdt.courtlink.admin.controller;

import com.bistu.ossdt.courtlink.admin.dto.StatisticsDTO;
import com.bistu.ossdt.courtlink.admin.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/statistics")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "System statistics API")
@PreAuthorize("hasRole('ADMIN')")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/overview")
    @Operation(summary = "Get system overview statistics", description = "Get overall statistics for all system modules")
    public StatisticsDTO getOverviewStatistics() {
        return statisticsService.getOverallStatistics();
    }

    @GetMapping("/users")
    @Operation(summary = "Get user statistics", description = "Get detailed user statistics")
    public Map<String, Object> getUserStatistics() {
        return statisticsService.getUserStatistics();
    }

    @GetMapping("/courts")
    @Operation(summary = "Get court statistics", description = "Get court usage statistics")
    public Map<String, Object> getCourtStatistics() {
        return statisticsService.getCourtUsageStatistics();
    }

    @GetMapping("/audits")
    @Operation(summary = "Get audit statistics", description = "Get audit related statistics")
    public Map<String, Object> getAuditStatistics() {
        return statisticsService.getAuditStatistics();
    }

    @GetMapping("/activity")
    @Operation(summary = "Get activity statistics", description = "Get activity statistics for specified date range")
    public Map<String, Long> getDailyActivityStatistics(
            @Parameter(description = "Start date", example = "2023-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date", example = "2023-01-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return statisticsService.getDailyActivityStatistics(startDate, endDate);
    }

    @GetMapping("/access")
    @Operation(summary = "Get system access statistics", description = "Get system access related statistics")
    public Map<String, Object> getSystemAccessStatistics() {
        return statisticsService.getSystemAccessStatistics();
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard data", description = "Get aggregated data for admin dashboard")
    public Map<String, Object> getDashboardData() {
        Map<String, Object> dashboard = Map.of(
            "overview", statisticsService.getOverallStatistics(),
            "userStats", statisticsService.getUserStatistics(),
            "auditStats", statisticsService.getAuditStatistics(),
            "accessStats", statisticsService.getSystemAccessStatistics()
        );
        return dashboard;
    }
} 