package com.courtlink.admin.service;

import com.courtlink.booking.dto.*;
import com.courtlink.booking.entity.Court;
import com.courtlink.booking.entity.CourtTimeSlot;
import com.courtlink.booking.repository.CourtRepository;
import com.courtlink.booking.repository.CourtTimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCourtService {

    private final CourtRepository courtRepository;
    private final CourtTimeSlotRepository courtTimeSlotRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    // 获取带时间段的场地管理数据
    @Transactional
    public List<CourtManagementDto> getCourtsWithTimeSlots(LocalDate date) {
        List<Court> courts = courtRepository.findAll();
        List<Long> courtIds = courts.stream().map(Court::getId).collect(Collectors.toList());
        
        // 获取指定日期的时间段
        List<CourtTimeSlot> timeSlots = (courtIds.isEmpty()) ? 
            new ArrayList<>() : courtTimeSlotRepository.findByCourtIdsAndDate(courtIds, date);
        
        Map<Long, List<CourtTimeSlot>> timeSlotsMap = timeSlots.stream()
            .collect(Collectors.groupingBy(slot -> slot.getCourt().getId()));

        return courts.stream().map(court -> {
            CourtManagementDto dto = new CourtManagementDto();
            dto.setId(court.getId());
            dto.setName(court.getName());
            dto.setDescription(court.getDescription());
            dto.setPricePerHour(court.getPricePerHour());
            dto.setAvailable(court.isAvailable());
            
            // 获取时间段，如果没有则自动生成并保存
            List<CourtTimeSlot> courtTimeSlots = timeSlotsMap.getOrDefault(court.getId(), new ArrayList<>());
            
            List<CourtManagementDto.TimeSlotDto> timeSlotDtos;
            if (courtTimeSlots.isEmpty()) {
                // 如果没有时间段，自动生成并保存到数据库
                generateTimeSlotsForDate(court.getId(), date);
                // 重新获取生成的时间段
                courtTimeSlots = courtTimeSlotRepository.findByCourtIdAndDate(court.getId(), date);
                timeSlotDtos = courtTimeSlots.stream()
                    .map(this::convertToTimeSlotDto)
                    .sorted(Comparator.comparing(CourtManagementDto.TimeSlotDto::getStartTime))
                    .collect(Collectors.toList());
            } else {
                timeSlotDtos = courtTimeSlots.stream()
                    .map(this::convertToTimeSlotDto)
                    .sorted(Comparator.comparing(CourtManagementDto.TimeSlotDto::getStartTime))
                    .collect(Collectors.toList());
            }
                
            dto.setTimeSlots(timeSlotDtos);
            return dto;
        }).collect(Collectors.toList());
    }

    // 创建新场地
    @Transactional
    public Court createCourt(CreateCourtRequest request) {
        Court court = new Court();
        court.setName(request.getName());
        court.setDescription(request.getDescription());
        court.setPricePerHour(request.getPricePerHour());
        court.setAvailable(request.isAvailable());
        
        Court savedCourt = courtRepository.save(court);
        
        // 为今天和未来7天生成时间段
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            generateTimeSlotsForDate(savedCourt.getId(), today.plusDays(i));
        }
        
        return savedCourt;
    }

    // 更新场地信息
    @Transactional
    public Court updateCourt(Long id, UpdateCourtRequest request) {
        Court court = courtRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("场地不存在"));
            
        court.setName(request.getName());
        court.setDescription(request.getDescription());
        court.setPricePerHour(request.getPricePerHour());
        court.setAvailable(request.isAvailable());
        
        return courtRepository.save(court);
    }

    // 删除场地
    @Transactional
    public void deleteCourt(Long id) {
        if (!courtRepository.existsById(id)) {
            throw new RuntimeException("场地不存在");
        }
        
        // 删除相关的时间段
        courtTimeSlotRepository.deleteAll(
            courtTimeSlotRepository.findByCourtIdAndDateBetween(id, LocalDate.now(), LocalDate.now().plusDays(365))
        );
        
        courtRepository.deleteById(id);
    }

    // 批量更新时间段
    @Transactional
    public void batchUpdateTimeSlots(List<TimeSlotUpdateRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            System.out.println("批量更新请求为空");
            return;
        }

        System.out.println("批量更新时间段，请求数量: " + requests.size());
        
        // 直接更新每个时间段
        for (TimeSlotUpdateRequest request : requests) {
            System.out.println("处理请求: timeSlotId=" + request.getTimeSlotId() + ", open=" + request.isOpen() + ", note=" + request.getNote());
            
            if (request.getTimeSlotId() != null) {
                CourtTimeSlot slot = courtTimeSlotRepository.findById(request.getTimeSlotId())
                    .orElse(null);
                if (slot != null) {
                    System.out.println("更新前: timeSlotId=" + slot.getId() + ", open=" + slot.isOpen());
                    slot.setOpen(request.isOpen());
                    if (request.getNote() != null) {
                        slot.setNote(request.getNote());
                    }
                    courtTimeSlotRepository.save(slot);
                    System.out.println("更新后: timeSlotId=" + slot.getId() + ", open=" + slot.isOpen());
                } else {
                    System.out.println("未找到时间段: " + request.getTimeSlotId());
                }
            }
        }
        
        // 强制刷新到数据库
        entityManager.flush();
        System.out.println("批量更新完成，已刷新到数据库");
    }

    // 为指定日期生成时间段
    @Transactional
    public void generateTimeSlotsForDate(Long courtId, LocalDate date) {
        Court court = courtRepository.findById(courtId)
            .orElseThrow(() -> new RuntimeException("场地不存在"));
            
        // 检查是否已存在时间段
        List<CourtTimeSlot> existing = courtTimeSlotRepository.findByCourtIdAndDate(courtId, date);
        if (!existing.isEmpty()) {
            return; // 已存在，跳过
        }
        
        // 生成8:00-23:00的15个时间段（每小时一段）
        List<CourtTimeSlot> timeSlots = new ArrayList<>();
        for (int hour = 8; hour < 23; hour++) {
            CourtTimeSlot slot = new CourtTimeSlot();
            slot.setCourt(court);
            slot.setDate(date);
            slot.setStartTime(LocalTime.of(hour, 0));
            slot.setEndTime(LocalTime.of(hour + 1, 0));
            slot.setAvailable(true);
            slot.setOpen(true);
            slot.setNote("");
            timeSlots.add(slot);
        }
        
        courtTimeSlotRepository.saveAll(timeSlots);
        entityManager.flush();
    }

    // 获取场地统计信息
    @Transactional(readOnly = true)
    public Map<String, Object> getCourtStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalCourts = courtRepository.count();
        long availableCourts = courtRepository.countByAvailable(true);
        
        LocalDate today = LocalDate.now();
        List<Court> courts = courtRepository.findAll();
        List<Long> courtIds = courts.stream().map(Court::getId).collect(Collectors.toList());
        
        long totalSlots = 0;
        long openSlots = 0;
        long availableSlots = 0;
        
        if (!courtIds.isEmpty()) {
            List<CourtTimeSlot> todaySlots = courtTimeSlotRepository.findByCourtIdsAndDate(courtIds, today);
            totalSlots = todaySlots.size();
            openSlots = todaySlots.stream().mapToLong(slot -> slot.isOpen() ? 1 : 0).sum();
            availableSlots = todaySlots.stream().mapToLong(slot -> slot.isAvailable() && slot.isOpen() ? 1 : 0).sum();
        }
        
        stats.put("totalCourts", totalCourts);
        stats.put("availableCourts", availableCourts);
        stats.put("totalSlots", totalSlots);
        stats.put("openSlots", openSlots);
        stats.put("availableSlots", availableSlots);
        stats.put("occupancyRate", totalSlots > 0 ? (double)(totalSlots - availableSlots) / totalSlots * 100 : 0);
        
        return stats;
    }

    // 设置场地可用性
    @Transactional
    public void setCourtAvailability(Long courtId, boolean available) {
        Court court = courtRepository.findById(courtId)
            .orElseThrow(() -> new RuntimeException("场地不存在"));
        court.setAvailable(available);
        courtRepository.save(court);
    }

    // 转换为DTO
    private CourtManagementDto.TimeSlotDto convertToTimeSlotDto(CourtTimeSlot slot) {
        CourtManagementDto.TimeSlotDto dto = new CourtManagementDto.TimeSlotDto();
        dto.setId(slot.getId());
        dto.setDate(slot.getDate());
        dto.setStartTime(slot.getStartTime());
        dto.setEndTime(slot.getEndTime());
        dto.setAvailable(slot.isAvailable());
        dto.setOpen(slot.isOpen());
        dto.setNote(slot.getNote());
        return dto;
    }
} 