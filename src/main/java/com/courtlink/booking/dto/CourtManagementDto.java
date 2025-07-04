<<<<<<< HEAD
package com.courtlink.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
public class CourtManagementDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal pricePerHour;
    private boolean available;
    
    private List<TimeSlotDto> timeSlots;
    
    @Getter
    @Setter
    public static class TimeSlotDto {
        private Long id;
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
        private boolean available;
        
        @JsonProperty("isOpen")
        private boolean open;
        private String note;
        
        // 为了兼容性，提供isOpen的getter和setter方法
        public boolean isOpen() {
            return open;
        }
        
        public void setOpen(boolean open) {
            this.open = open;
        }
    }
=======
package com.courtlink.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
public class CourtManagementDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal pricePerHour;
    private boolean available;
    
    private List<TimeSlotDto> timeSlots;
    
    @Getter
    @Setter
    public static class TimeSlotDto {
        private Long id;
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
        private boolean available;
        
        @JsonProperty("isOpen")
        private boolean open;
        private String note;
        
        // 为了兼容性，提供isOpen的getter和setter方法
        public boolean isOpen() {
            return open;
        }
        
        public void setOpen(boolean open) {
            this.open = open;
        }
    }
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
} 