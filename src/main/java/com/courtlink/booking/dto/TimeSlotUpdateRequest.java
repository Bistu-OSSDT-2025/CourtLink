package com.courtlink.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimeSlotUpdateRequest {
    private Long timeSlotId;
    
<<<<<<< HEAD
    @JsonProperty("open")
=======
    @JsonProperty("isOpen")
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
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