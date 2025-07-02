package com.courtlink.statistics.service;

import com.courtlink.statistics.dto.CourtStatisticsDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface CourtStatisticsService {
    CourtStatisticsDTO getCourtStatistics(Long courtId, LocalDateTime startTime, LocalDateTime endTime);
    List<CourtStatisticsDTO> getAllCourtsStatistics(LocalDateTime startTime, LocalDateTime endTime);
    CourtStatisticsDTO getCourtDailyStatistics(Long courtId, LocalDateTime date);
    CourtStatisticsDTO getCourtMonthlyStatistics(Long courtId, int year, int month);
    List<CourtStatisticsDTO> getTopPerformingCourts(LocalDateTime startTime, LocalDateTime endTime, int limit);
    Double getOverallOccupancyRate(LocalDateTime startTime, LocalDateTime endTime);
} 