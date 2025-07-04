package com.courtlink.booking.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class UpdateCourtRequest {
    private String name;
    private String description;
    private BigDecimal pricePerHour;
    private boolean available;
} 