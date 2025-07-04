package com.courtlink.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimeSlotUpdateRequest {
    private Long timeSlotId;
    
    @JsonProperty("open")
    private boolean open;
    
    private String note;
    
    // 为了兼容性，提供isOpen的getter方法
    public boolean isOpen() {
        return open;
    }
    
    public void setOpen(boolean open) {
        this.open = open;
    }
} 