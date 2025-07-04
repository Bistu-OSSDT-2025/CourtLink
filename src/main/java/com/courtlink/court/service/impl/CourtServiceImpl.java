package com.courtlink.court.service.impl;

import com.courtlink.admin.service.AdminService;
import com.courtlink.court.dto.CourtDTO;
import com.courtlink.court.dto.CourtRequest;
import com.courtlink.court.dto.CourtBatchRequest;
import com.courtlink.court.dto.CourtManagementDTO;
import com.courtlink.court.entity.Court;
import com.courtlink.court.entity.CourtStatus;
import com.courtlink.court.repository.CourtRepository;
import com.courtlink.court.service.CourtService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourtServiceImpl implements CourtService {

    private final CourtRepository courtRepository;
    private final AdminService adminService;

    @Override
    @Transactional
    public CourtDTO createCourt(CourtRequest request) {
        Court court = new Court();
        updateCourtFromRequest(court, request);
        court.setCreatedAt(LocalDateTime.now());
        court.setLastModifiedBy(adminService.getCurrentAdmin().getUsername());
        return CourtDTO.fromEntity(courtRepository.save(court));
    }

    @Override
    @Transactional
    public CourtDTO updateCourt(Long id, CourtRequest request) {
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("场地不存在: " + id));
        
        updateCourtFromRequest(court, request);
        court.setLastModifiedBy(adminService.getCurrentAdmin().getUsername());
        return CourtDTO.fromEntity(courtRepository.save(court));
    }

    @Override
    public CourtDTO getCourtById(Long id) {
        return courtRepository.findById(id)
                .map(CourtDTO::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("场地不存在: " + id));
    }

    @Override
    public List<CourtDTO> getAllCourts() {
        return courtRepository.findAll().stream()
                .map(CourtDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourtDTO> getCourtsByStatus(CourtStatus status) {
        return courtRepository.findByStatus(status).stream()
                .map(CourtDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourtDTO> getCourtsByType(Court.CourtType courtType) {
        return courtRepository.findByCourtType(courtType).stream()
                .map(CourtDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteCourt(Long id) {
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("场地不存在: " + id));
        courtRepository.delete(court);
    }

    @Override
    @Transactional
    public CourtDTO updateCourtStatus(Long id, CourtStatus status) {
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("场地不存在: " + id));
        
        court.setStatus(status);
        court.setLastModifiedBy(adminService.getCurrentAdmin().getUsername());
        return CourtDTO.fromEntity(courtRepository.save(court));
    }

    @Override
    @Transactional
    public CourtDTO toggleCourtEnabled(Long id) {
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("场地不存在: " + id));
        
        court.setEnabled(!court.isEnabled());
        court.setLastModifiedBy(adminService.getCurrentAdmin().getUsername());
        return CourtDTO.fromEntity(courtRepository.save(court));
    }

    // 新增：批量创建场地
    @Override
    @Transactional
    public List<CourtDTO> createMultipleCourts(CourtBatchRequest request) {
        String currentAdmin = adminService.getCurrentAdmin().getUsername();
        List<Court> courts = new ArrayList<>();
        
        for (int i = 0; i < request.getCount(); i++) {
            Court court = new Court();
            
            // 生成场地名称
            String courtNumber = generateCourtNumber(request.getStartNumber() + i, request.getNumberFormat());
            String fullName = request.getNamePrefix() + courtNumber;
            
            // 检查名称是否已存在
            if (courtRepository.existsByName(fullName)) {
                log.warn("场地名称已存在，跳过创建: {}", fullName);
                continue;
            }
            
            // 设置基本信息
            court.setName(fullName);
            court.setCourtType(request.getCourtType());
            court.setCourtLocation(request.getCourtLocation());
            court.setPricePerHour(request.getPricePerHour());
            court.setLocation(request.getLocation());
            court.setDescription(request.getDescription());
            court.setFacilities(request.getFacilities());
            court.setRules(request.getRules());
            court.setMaxPlayers(request.getMaxPlayers());
            court.setHasLighting(request.getHasLighting());
            court.setOperatingHours(request.getOperatingHours());
            court.setStatus(request.getInitialStatus());
            court.setEnabled(request.getEnabled());
            
            // 设置时间戳和操作人
            court.setCreatedAt(LocalDateTime.now());
            court.setLastModifiedBy(currentAdmin);
            
            courts.add(court);
        }
        
        List<Court> savedCourts = courtRepository.saveAll(courts);
        log.info("批量创建场地成功，共创建{}个场地", savedCourts.size());
        
        return savedCourts.stream()
                .map(CourtDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 新增：批量删除场地
    @Override
    @Transactional
    public void deleteMultipleCourts(List<Long> courtIds) {
        List<Court> courts = courtRepository.findAllById(courtIds);
        if (courts.size() != courtIds.size()) {
            throw new EntityNotFoundException("部分场地不存在");
        }
        
        // 检查是否有活跃的预订
        for (Court court : courts) {
            if (court.getStatus() == CourtStatus.OCCUPIED || court.getStatus() == CourtStatus.RESERVED) {
                throw new IllegalStateException("场地 " + court.getName() + " 当前有活跃预订，无法删除");
            }
        }
        
        courtRepository.deleteAll(courts);
        log.info("批量删除场地成功，共删除{}个场地", courts.size());
    }

    // 新增：批量更新状态
    @Override
    @Transactional
    public List<CourtDTO> batchUpdateStatus(List<Long> courtIds, CourtStatus status) {
        String currentAdmin = adminService.getCurrentAdmin().getUsername();
        List<Court> courts = courtRepository.findAllById(courtIds);
        
        if (courts.size() != courtIds.size()) {
            throw new EntityNotFoundException("部分场地不存在");
        }
        
        courts.forEach(court -> {
            court.setStatus(status);
            court.setLastModifiedBy(currentAdmin);
        });
        
        List<Court> savedCourts = courtRepository.saveAll(courts);
        log.info("批量更新场地状态成功，共更新{}个场地为{}", courts.size(), status);
        
        return savedCourts.stream()
                .map(CourtDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 新增：批量切换启用状态
    @Override
    @Transactional
    public List<CourtDTO> batchToggleEnabled(List<Long> courtIds) {
        String currentAdmin = adminService.getCurrentAdmin().getUsername();
        List<Court> courts = courtRepository.findAllById(courtIds);
        
        if (courts.size() != courtIds.size()) {
            throw new EntityNotFoundException("部分场地不存在");
        }
        
        courts.forEach(court -> {
            court.setEnabled(!court.isEnabled());
            court.setLastModifiedBy(currentAdmin);
        });
        
        List<Court> savedCourts = courtRepository.saveAll(courts);
        log.info("批量切换场地启用状态成功，共更新{}个场地", courts.size());
        
        return savedCourts.stream()
                .map(CourtDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 新增：获取场地管理统计信息
    @Override
    public CourtManagementDTO getCourtManagementInfo() {
        CourtManagementDTO.CourtManagementDTOBuilder builder = CourtManagementDTO.builder();
        
        // 基础统计
        long totalCourts = courtRepository.count();
        Long enabledCourts = courtRepository.countEnabledCourts();
        Long disabledCourts = courtRepository.countDisabledCourts();
        
        builder.totalCourts((int) totalCourts)
               .enabledCourts(enabledCourts != null ? enabledCourts.intValue() : 0)
               .disabledCourts(disabledCourts != null ? disabledCourts.intValue() : 0);
        
        // 按状态统计
        Map<CourtStatus, Integer> statusMap = new HashMap<>();
        for (CourtStatus status : CourtStatus.values()) {
            Long count = courtRepository.countByStatus(status);
            statusMap.put(status, count != null ? count.intValue() : 0);
        }
        builder.courtsByStatus(statusMap);
        
        // 设置具体状态计数
        builder.availableCourts(statusMap.get(CourtStatus.AVAILABLE))
               .occupiedCourts(statusMap.get(CourtStatus.OCCUPIED))
               .maintenanceCourts(statusMap.get(CourtStatus.MAINTENANCE))
               .closedCourts(statusMap.get(CourtStatus.CLOSED))
               .reservedCourts(statusMap.get(CourtStatus.RESERVED));
        
        // 按类型统计
        Map<Court.CourtType, Integer> typeMap = new HashMap<>();
        for (Court.CourtType type : Court.CourtType.values()) {
            Long count = courtRepository.countByCourtType(type);
            typeMap.put(type, count != null ? count.intValue() : 0);
        }
        builder.courtsByType(typeMap);
        
        // 按位置统计
        Map<Court.CourtLocation, Integer> locationMap = new HashMap<>();
        for (Court.CourtLocation location : Court.CourtLocation.values()) {
            Long count = courtRepository.countByCourtLocation(location);
            locationMap.put(location, count != null ? count.intValue() : 0);
        }
        builder.courtsByLocation(locationMap);
        
        // 价格统计
        BigDecimal avgPrice = courtRepository.findAveragePrice();
        BigDecimal minPrice = courtRepository.findMinPrice();
        BigDecimal maxPrice = courtRepository.findMaxPrice();
        
        builder.averagePrice(avgPrice)
               .minPrice(minPrice)
               .maxPrice(maxPrice);
        
        // 容量统计
        Long totalCapacity = courtRepository.sumTotalCapacity();
        Double avgCapacity = courtRepository.findAverageCapacity();
        
        builder.totalCapacity(totalCapacity != null ? totalCapacity.intValue() : 0)
               .averageCapacity(avgCapacity);
        
        // 照明统计
        Long withLighting = courtRepository.countCourtsWithLighting();
        Long withoutLighting = courtRepository.countCourtsWithoutLighting();
        
        builder.courtsWithLighting(withLighting != null ? withLighting.intValue() : 0)
               .courtsWithoutLighting(withoutLighting != null ? withoutLighting.intValue() : 0);
        
        // 营业时间统计
        Double avgOperatingHours = courtRepository.findAverageOperatingHours();
        Long totalOperatingHours = courtRepository.sumTotalOperatingHours();
        
        builder.averageOperatingHours(avgOperatingHours)
               .totalOperatingHours(totalOperatingHours != null ? totalOperatingHours.intValue() : 0);
        
        // 时间统计
        LocalDateTime oldestCreated = courtRepository.findOldestCourtCreatedTime();
        LocalDateTime newestCreated = courtRepository.findNewestCourtCreatedTime();
        
        builder.lastUpdateTime(LocalDateTime.now())
               .oldestCourtCreated(oldestCreated)
               .newestCourtCreated(newestCreated);
        
        // 计算系统健康状态
        String healthStatus = calculateSystemHealth(statusMap, totalCourts);
        String[] healthIssues = calculateHealthIssues(statusMap, totalCourts);
        
        builder.systemHealth(healthStatus)
               .healthIssues(healthIssues);
        
        return builder.build();
    }

    // 新增：按位置查询场地
    @Override
    public List<CourtDTO> getCourtsByLocation(Court.CourtLocation location) {
        return courtRepository.findByCourtLocation(location).stream()
                .map(CourtDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 新增：获取可用场地
    @Override
    public List<CourtDTO> getAvailableCourts() {
        return courtRepository.findByEnabledTrueAndStatus(CourtStatus.AVAILABLE).stream()
                .map(CourtDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 新增：设置维护模式
    @Override
    @Transactional
    public CourtDTO setMaintenanceMode(Long id, String reason) {
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("场地不存在: " + id));
        
        if (court.getStatus() == CourtStatus.OCCUPIED || court.getStatus() == CourtStatus.RESERVED) {
            throw new IllegalStateException("场地当前有活跃预订，无法进入维护模式");
        }
        
        String currentAdmin = adminService.getCurrentAdmin().getUsername();
        
        court.setStatus(CourtStatus.MAINTENANCE);
        court.setMaintenanceReason(reason);
        court.setMaintenanceStartTime(LocalDateTime.now());
        court.setMaintenanceBy(currentAdmin);
        court.setLastModifiedBy(currentAdmin);
        
        log.info("场地{}进入维护模式，原因：{}", court.getName(), reason);
        
        return CourtDTO.fromEntity(courtRepository.save(court));
    }

    // 新增：结束维护模式
    @Override
    @Transactional
    public CourtDTO endMaintenanceMode(Long id) {
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("场地不存在: " + id));
        
        if (court.getStatus() != CourtStatus.MAINTENANCE) {
            throw new IllegalStateException("场地当前不在维护模式");
        }
        
        String currentAdmin = adminService.getCurrentAdmin().getUsername();
        
        court.setStatus(CourtStatus.AVAILABLE);
        court.setMaintenanceEndTime(LocalDateTime.now());
        court.setLastModifiedBy(currentAdmin);
        
        log.info("场地{}结束维护模式", court.getName());
        
        return CourtDTO.fromEntity(courtRepository.save(court));
    }

    // 新增：获取维护中的场地
    @Override
    public List<CourtDTO> getCourtsInMaintenance() {
        return courtRepository.findCourtsInMaintenance().stream()
                .map(CourtDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 辅助方法：生成场地编号
    private String generateCourtNumber(int number, String format) {
        return format.replace("{number}", String.valueOf(number));
    }

    // 辅助方法：计算系统健康状态
    private String calculateSystemHealth(Map<CourtStatus, Integer> statusMap, long totalCourts) {
        if (totalCourts == 0) {
            return "CRITICAL";
        }
        
        int available = statusMap.get(CourtStatus.AVAILABLE);
        int maintenance = statusMap.get(CourtStatus.MAINTENANCE);
        int closed = statusMap.get(CourtStatus.CLOSED);
        
        double availabilityRate = (double) available / totalCourts;
        double maintenanceRate = (double) maintenance / totalCourts;
        double closedRate = (double) closed / totalCourts;
        
        if (availabilityRate >= 0.8 && maintenanceRate <= 0.1) {
            return "EXCELLENT";
        } else if (availabilityRate >= 0.6 && maintenanceRate <= 0.2) {
            return "GOOD";
        } else if (availabilityRate >= 0.4 && closedRate <= 0.3) {
            return "WARNING";
        } else {
            return "CRITICAL";
        }
    }

    // 辅助方法：计算健康问题
    private String[] calculateHealthIssues(Map<CourtStatus, Integer> statusMap, long totalCourts) {
        List<String> issues = new ArrayList<>();
        
        if (totalCourts == 0) {
            issues.add("系统中没有任何场地");
            return issues.toArray(new String[0]);
        }
        
        int maintenance = statusMap.get(CourtStatus.MAINTENANCE);
        int closed = statusMap.get(CourtStatus.CLOSED);
        int available = statusMap.get(CourtStatus.AVAILABLE);
        
        double maintenanceRate = (double) maintenance / totalCourts;
        double closedRate = (double) closed / totalCourts;
        double availabilityRate = (double) available / totalCourts;
        
        if (maintenanceRate > 0.2) {
            issues.add("维护中场地比例过高 (" + 
                String.format("%.1f", maintenanceRate * 100) + "%)");
        }
        
        if (closedRate > 0.3) {
            issues.add("关闭场地比例过高 (" + 
                String.format("%.1f", closedRate * 100) + "%)");
        }
        
        if (availabilityRate < 0.4) {
            issues.add("可用场地比例过低 (" + 
                String.format("%.1f", availabilityRate * 100) + "%)");
        }
        
        if (issues.isEmpty()) {
            issues.add("系统运行正常");
        }
        
        return issues.toArray(new String[0]);
    }

    private void updateCourtFromRequest(Court court, CourtRequest request) {
        court.setName(request.getName());
        court.setCourtType(request.getCourtType());
        court.setDescription(request.getDescription());
        court.setPricePerHour(request.getPricePerHour());
        court.setLocation(request.getLocation());
        court.setFacilities(request.getFacilities());
        court.setRules(request.getRules());
    }
} 