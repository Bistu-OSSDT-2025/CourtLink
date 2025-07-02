package com.courtlink.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentStatistics {
    private long totalCount;
    private long completedCount;
    private long cancelledCount;
    private long pendingCount;
} 