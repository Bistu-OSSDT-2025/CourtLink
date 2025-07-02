package com.courtlink.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourtDTO {
    private Long id;
    private String name;
    private String location;
    private String description;
    private String status;
    private Double pricePerHour;
    private String facilities;
    private String openingHours;
    private String maintenanceSchedule;
    private Boolean isActive;
} 