package com.bistu.ossdt.courtlink.admin.dto;

import lombok.Data;

import java.util.Map;

@Data
public class StatisticsDTO {
    // �û�ͳ��
    private Long totalUsers;
    private Long activeUsers;
    private Long inactiveUsers;
    private Map<String, Long> userRoleDistribution;
    
    // ����ͳ��
    private Long totalCourts;
    private Long availableCourts;
    private Long unavailableCourts;
    private Double averageUtilizationRate;
    
    // ���ͳ��
    private Long totalAudits;
    private Long pendingAudits;
    private Long approvedAudits;
    private Long rejectedAudits;
    private Double approvalRate;
    
    // ϵͳͳ��
    private Long totalLogins;
    private Long todayLogins;
    private Long systemConfigs;
    private Map<String, Long> dailyActivityData;
} 