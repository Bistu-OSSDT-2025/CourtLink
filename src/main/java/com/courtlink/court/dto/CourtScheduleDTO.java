package com.courtlink.court.dto;

import com.courtlink.court.entity.CourtSchedule;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourtScheduleDTO {
    
    private Long id;
    private Long courtId;
    private String courtName; // 场地名称，用于显示
    private DayOfWeek dayOfWeek;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Boolean isActive;
    private LocalDate specialDate;
    private CourtSchedule.ScheduleType scheduleType;
    private Integer slotDuration;
    private Integer advanceBookingHours;
    private Integer cancellationDeadlineHours;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
    private String lastModifiedBy;
    
    // 计算字段
    private Double totalOpenHours;
    private Integer availableSlots;
    private Boolean isValid;
    
    public static CourtScheduleDTO fromEntity(CourtSchedule schedule) {
        CourtScheduleDTO dto = new CourtScheduleDTO();
        dto.setId(schedule.getId());
        dto.setCourtId(schedule.getCourtId());
        dto.setDayOfWeek(schedule.getDayOfWeek());
        dto.setOpenTime(schedule.getOpenTime());
        dto.setCloseTime(schedule.getCloseTime());
        dto.setIsActive(schedule.getIsActive());
        dto.setSpecialDate(schedule.getSpecialDate());
        dto.setScheduleType(schedule.getScheduleType());
        dto.setSlotDuration(schedule.getSlotDuration());
        dto.setAdvanceBookingHours(schedule.getAdvanceBookingHours());
        dto.setCancellationDeadlineHours(schedule.getCancellationDeadlineHours());
        dto.setDescription(schedule.getDescription());
        dto.setCreatedAt(schedule.getCreatedAt());
        dto.setLastModifiedAt(schedule.getLastModifiedAt());
        dto.setLastModifiedBy(schedule.getLastModifiedBy());
        
        // 计算字段
        dto.setTotalOpenHours(schedule.getTotalOpenHours());
        dto.setAvailableSlots(schedule.getAvailableSlots());
        dto.setIsValid(schedule.isValid());
        
        return dto;
    }
    
    public CourtSchedule toEntity() {
        CourtSchedule schedule = new CourtSchedule();
        schedule.setId(this.id);
        schedule.setCourtId(this.courtId);
        schedule.setDayOfWeek(this.dayOfWeek);
        schedule.setOpenTime(this.openTime);
        schedule.setCloseTime(this.closeTime);
        schedule.setIsActive(this.isActive);
        schedule.setSpecialDate(this.specialDate);
        schedule.setScheduleType(this.scheduleType);
        schedule.setSlotDuration(this.slotDuration);
        schedule.setAdvanceBookingHours(this.advanceBookingHours);
        schedule.setCancellationDeadlineHours(this.cancellationDeadlineHours);
        schedule.setDescription(this.description);
        return schedule;
    }
} 