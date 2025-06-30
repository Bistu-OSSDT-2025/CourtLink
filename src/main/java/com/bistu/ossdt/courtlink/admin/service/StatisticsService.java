package com.bistu.ossdt.courtlink.admin.service;

import com.bistu.ossdt.courtlink.admin.dto.StatisticsDTO;

import java.time.LocalDate;
import java.util.Map;

public interface StatisticsService {
    /**
     * ��ȡϵͳ����ͳ����Ϣ
     */
    StatisticsDTO getOverallStatistics();
    
    /**
     * ��ȡ�û�ͳ����Ϣ
     */
    Map<String, Object> getUserStatistics();
    
    /**
     * ��ȡ����ʹ��ͳ��
     */
    Map<String, Object> getCourtUsageStatistics();
    
    /**
     * ��ȡ���ͳ����Ϣ
     */
    Map<String, Object> getAuditStatistics();
    
    /**
     * ��ȡ���ڷ�Χ�ڵĻͳ��
     */
    Map<String, Long> getDailyActivityStatistics(LocalDate startDate, LocalDate endDate);
    
    /**
     * ��ȡϵͳ����ͳ��
     */
    Map<String, Object> getSystemAccessStatistics();
} 