package com.courtlink.court.dto;

import com.courtlink.court.entity.Court;
import com.courtlink.court.entity.CourtStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourtManagementDTO {
    
    // 总体统计
    private Integer totalCourts;           // 总场地数
    private Integer enabledCourts;         // 已启用场地数
    private Integer disabledCourts;        // 已禁用场地数
    
    // 按状态统计
    private Map<CourtStatus, Integer> courtsByStatus;
    
    // 按类型统计
    private Map<Court.CourtType, Integer> courtsByType;
    
    // 按位置统计
    private Map<Court.CourtLocation, Integer> courtsByLocation;
    
    // 可用性统计
    private Integer availableCourts;       // 当前可用场地数
    private Integer occupiedCourts;        // 当前占用场地数
    private Integer maintenanceCourts;     // 维护中场地数
    private Integer closedCourts;          // 关闭场地数
    private Integer reservedCourts;        // 已预订场地数
    
    // 价格统计
    private BigDecimal averagePrice;       // 平均价格
    private BigDecimal minPrice;           // 最低价格
    private BigDecimal maxPrice;           // 最高价格
    
    // 容量统计
    private Integer totalCapacity;         // 总容量（所有场地最大人数之和）
    private Double averageCapacity;        // 平均容量
    
    // 设施统计
    private Integer courtsWithLighting;    // 有照明的场地数
    private Integer courtsWithoutLighting; // 无照明的场地数
    
    // 时间统计
    private LocalDateTime lastUpdateTime;  // 最后更新时间
    private LocalDateTime oldestCourtCreated; // 最早创建的场地时间
    private LocalDateTime newestCourtCreated; // 最新创建的场地时间
    
    // 营业时间统计
    private Double averageOperatingHours;  // 平均营业时间
    private Integer totalOperatingHours;   // 总营业时间
    
    // 使用率统计（如果有预订数据）
    private Double overallUtilizationRate; // 整体使用率
    
    // 健康状态
    private String systemHealth;           // 系统健康状态："EXCELLENT", "GOOD", "WARNING", "CRITICAL"
    private String[] healthIssues;         // 健康问题列表
    
    /**
     * 计算可用率
     */
    public Double getAvailabilityRate() {
        if (totalCourts == null || totalCourts == 0) {
            return 0.0;
        }
        return (double) availableCourts / totalCourts * 100;
    }
    
    /**
     * 计算启用率
     */
    public Double getEnabledRate() {
        if (totalCourts == null || totalCourts == 0) {
            return 0.0;
        }
        return (double) enabledCourts / totalCourts * 100;
    }
    
    /**
     * 计算维护率
     */
    public Double getMaintenanceRate() {
        if (totalCourts == null || totalCourts == 0) {
            return 0.0;
        }
        return (double) maintenanceCourts / totalCourts * 100;
    }
} 