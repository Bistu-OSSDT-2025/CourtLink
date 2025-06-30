package com.bistu.ossdt.courtlink.admin.dto;

import lombok.Data;

import java.util.Map;

@Data
public class StatisticsDTO {
    // 用户统计
    private Long totalUsers;
    private Long activeUsers;
    private Long inactiveUsers;
    private Map<String, Long> userRoleDistribution;
    
    // 场地统计
    private Long totalCourts;
    private Long availableCourts;
    private Long unavailableCourts;
    private Double averageUtilizationRate;
    
    // 审核统计
    private Long totalAudits;
    private Long pendingAudits;
    private Long approvedAudits;
    private Long rejectedAudits;
    private Double approvalRate;
    
    // 系统统计
    private Long totalLogins;
    private Long todayLogins;
    private Long systemConfigs;
    private Map<String, Long> dailyActivityData;
} 