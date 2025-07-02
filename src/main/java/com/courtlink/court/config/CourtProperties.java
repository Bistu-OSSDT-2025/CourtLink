package com.courtlink.court.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "court")
public class CourtProperties {
    private List<String> types;
    private Price price;
    private Operation operation;

    @Data
    public static class Price {
        private BigDecimal min;
        private BigDecimal max;
    }

    @Data
    public static class Operation {
        private LocalTime startTime;
        private LocalTime endTime;
    }
} 