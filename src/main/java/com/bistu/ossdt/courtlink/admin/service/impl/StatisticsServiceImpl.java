package com.bistu.ossdt.courtlink.admin.service.impl;

import com.bistu.ossdt.courtlink.admin.dto.StatisticsDTO;
import com.bistu.ossdt.courtlink.admin.mapper.AuditLogMapper;
import com.bistu.ossdt.courtlink.admin.mapper.SystemConfigMapper;
import com.bistu.ossdt.courtlink.admin.mapper.UserMapper;
import com.bistu.ossdt.courtlink.admin.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final UserMapper userMapper;
    private final AuditLogMapper auditLogMapper;
    private final SystemConfigMapper systemConfigMapper;

    @Override
    public StatisticsDTO getOverallStatistics() {
        StatisticsDTO statistics = new StatisticsDTO();
        
        // User statistics
        statistics.setTotalUsers(userMapper.selectCount(null));
        statistics.setActiveUsers(userMapper.countByEnabled(true));
        statistics.setInactiveUsers(userMapper.countByEnabled(false));
        
        // Role distribution
        Map<String, Long> roleDistribution = new HashMap<>();
        roleDistribution.put("ADMIN", userMapper.countByRole("ADMIN"));
        roleDistribution.put("USER", userMapper.countByRole("USER"));
        roleDistribution.put("STAFF", userMapper.countByRole("STAFF"));
        statistics.setUserRoleDistribution(roleDistribution);
        
        // System configuration statistics
        statistics.setSystemConfigs(systemConfigMapper.selectCount(null));
        
        // Audit statistics (based on audit logs)
        statistics.setTotalAudits(auditLogMapper.selectCount(null));
        
        // Today's login statistics
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        statistics.setTodayLogins(auditLogMapper.countByOperationTimeRange(todayStart, todayEnd));
        
        return statistics;
    }

    @Override
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalUsers", userMapper.selectCount(null));
        stats.put("activeUsers", userMapper.countByEnabled(true));
        stats.put("inactiveUsers", userMapper.countByEnabled(false));
        
        // Role distribution
        Map<String, Long> roleStats = new HashMap<>();
        roleStats.put("ADMIN", userMapper.countByRole("ADMIN"));
        roleStats.put("USER", userMapper.countByRole("USER"));
        roleStats.put("STAFF", userMapper.countByRole("STAFF"));
        stats.put("roleDistribution", roleStats);
        
        // Registration trend (last 7 days)
        Map<String, Long> registrationTrend = new HashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = start.plusDays(1);
            Long count = userMapper.countByCreateTimeRange(start, end);
            registrationTrend.put(date.toString(), count);
        }
        stats.put("registrationTrend", registrationTrend);
        
        return stats;
    }

    @Override
    public Map<String, Object> getCourtUsageStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Mock data as court functionality is not yet implemented
        stats.put("totalCourts", 20L);
        stats.put("availableCourts", 15L);
        stats.put("unavailableCourts", 5L);
        stats.put("averageUtilizationRate", 75.5);
        
        // Usage trend
        Map<String, Double> usageTrend = new HashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            usageTrend.put(date.toString(), 60.0 + Math.random() * 30);
        }
        stats.put("usageTrend", usageTrend);
        
        return stats;
    }

    @Override
    public Map<String, Object> getAuditStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalAudits", auditLogMapper.selectCount(null));
        
        // Operation type distribution
        Map<String, Long> operationStats = new HashMap<>();
        operationStats.put("CREATE", auditLogMapper.countByOperationType("createUser"));
        operationStats.put("UPDATE", auditLogMapper.countByOperationType("updateUser"));
        operationStats.put("DELETE", auditLogMapper.countByOperationType("deleteUser"));
        stats.put("operationDistribution", operationStats);
        
        // Audit activity for last 7 days
        Map<String, Long> auditTrend = new HashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = start.plusDays(1);
            Long count = auditLogMapper.countByOperationTimeRange(start, end);
            auditTrend.put(date.toString(), count);
        }
        stats.put("auditTrend", auditTrend);
        
        return stats;
    }

    @Override
    public Map<String, Long> getDailyActivityStatistics(LocalDate startDate, LocalDate endDate) {
        Map<String, Long> dailyStats = new HashMap<>();
        
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            LocalDateTime start = current.atStartOfDay();
            LocalDateTime end = start.plusDays(1);
            Long count = auditLogMapper.countByOperationTimeRange(start, end);
            dailyStats.put(current.toString(), count);
            current = current.plusDays(1);
        }
        
        return dailyStats;
    }

    @Override
    public Map<String, Object> getSystemAccessStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Today's access statistics
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        stats.put("todayAccess", auditLogMapper.countByOperationTimeRange(todayStart, todayEnd));
        
        // Total access statistics
        stats.put("totalAccess", auditLogMapper.selectCount(null));
        
        // Access trend (last 30 days)
        Map<String, Long> accessTrend = new HashMap<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = start.plusDays(1);
            Long count = auditLogMapper.countByOperationTimeRange(start, end);
            accessTrend.put(date.toString(), count);
        }
        stats.put("accessTrend", accessTrend);
        
        return stats;
    }
} 