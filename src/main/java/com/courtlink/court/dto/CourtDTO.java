package com.courtlink.court.dto;

import com.courtlink.court.entity.Court;
import com.courtlink.court.entity.CourtStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CourtDTO {
    private Long id;
    private String name;
    private Court.CourtType courtType;
    private Court.CourtLocation courtLocation;
    private String description;
    private BigDecimal pricePerHour;
    private CourtStatus status;
    private String location;
    private String facilities;
    private String rules;
    private boolean enabled;
    private Integer maxPlayers;
    private Boolean hasLighting;
    private Integer operatingHours;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
    private String lastModifiedBy;
    
    // 维护相关字段
    private String maintenanceReason;
    private LocalDateTime maintenanceStartTime;
    private LocalDateTime maintenanceEndTime;
    private String maintenanceBy;

    public static CourtDTO fromEntity(Court court) {
        CourtDTO dto = new CourtDTO();
        dto.setId(court.getId());
        dto.setName(court.getName());
        dto.setCourtType(court.getCourtType());
        dto.setCourtLocation(court.getCourtLocation());
        dto.setDescription(court.getDescription());
        dto.setPricePerHour(court.getPricePerHour());
        dto.setStatus(court.getStatus());
        dto.setLocation(court.getLocation());
        dto.setFacilities(court.getFacilities());
        dto.setRules(court.getRules());
        dto.setEnabled(court.isEnabled());
        dto.setMaxPlayers(court.getMaxPlayers());
        dto.setHasLighting(court.getHasLighting());
        dto.setOperatingHours(court.getOperatingHours());
        dto.setCreatedAt(court.getCreatedAt());
        dto.setLastModifiedAt(court.getLastModifiedAt());
        dto.setLastModifiedBy(court.getLastModifiedBy());
        
        // 维护相关字段
        dto.setMaintenanceReason(court.getMaintenanceReason());
        dto.setMaintenanceStartTime(court.getMaintenanceStartTime());
        dto.setMaintenanceEndTime(court.getMaintenanceEndTime());
        dto.setMaintenanceBy(court.getMaintenanceBy());
        
        return dto;
    }
} 