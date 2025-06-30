package com.bistu.ossdt.courtlink.admin.service;

import com.bistu.ossdt.courtlink.admin.dto.StatisticsDTO;

import java.time.LocalDate;
import java.util.Map;

public interface StatisticsService {
    /**
     * 获取系统总体统计信息
     */
    StatisticsDTO getOverallStatistics();
    
    /**
     * 获取用户统计信息
     */
    Map<String, Object> getUserStatistics();
    
    /**
     * 获取场地使用统计
     */
    Map<String, Object> getCourtUsageStatistics();
    
    /**
     * 获取审核统计信息
     */
    Map<String, Object> getAuditStatistics();
    
    /**
     * 获取日期范围内的活动统计
     */
    Map<String, Long> getDailyActivityStatistics(LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取系统访问统计
     */
    Map<String, Object> getSystemAccessStatistics();
} 