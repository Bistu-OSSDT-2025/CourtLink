package com.courtlink.statistics.service.impl;

import com.courtlink.booking.entity.Booking;
import com.courtlink.booking.entity.BookingStatus;
import com.courtlink.booking.repository.BookingRepository;
import com.courtlink.court.entity.Court;
import com.courtlink.court.repository.CourtRepository;
import com.courtlink.statistics.dto.CourtStatisticsDTO;
import com.courtlink.statistics.service.CourtStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourtStatisticsServiceImpl implements CourtStatisticsService {

    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;

    @Override
    public CourtStatisticsDTO getCourtStatistics(Long courtId, LocalDateTime startTime, LocalDateTime endTime) {
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new RuntimeException("Court not found"));

        List<Booking> bookings = bookingRepository.findByCourtIdAndStartTimeBetween(courtId, startTime, endTime);

        return calculateStatistics(court, bookings, startTime, endTime);
    }

    @Override
    public List<CourtStatisticsDTO> getAllCourtsStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        List<Court> courts = courtRepository.findAll();
        return courts.stream()
                .map(court -> {
                    List<Booking> bookings = bookingRepository.findByCourtIdAndStartTimeBetween(
                            court.getId(), startTime, endTime);
                    return calculateStatistics(court, bookings, startTime, endTime);
                })
                .collect(Collectors.toList());
    }

    @Override
    public CourtStatisticsDTO getCourtDailyStatistics(Long courtId, LocalDateTime date) {
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return getCourtStatistics(courtId, startOfDay, endOfDay);
    }

    @Override
    public CourtStatisticsDTO getCourtMonthlyStatistics(Long courtId, int year, int month) {
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);
        return getCourtStatistics(courtId, startOfMonth, endOfMonth);
    }

    @Override
    public List<CourtStatisticsDTO> getTopPerformingCourts(LocalDateTime startTime, LocalDateTime endTime, int limit) {
        List<CourtStatisticsDTO> allStats = getAllCourtsStatistics(startTime, endTime);
        return allStats.stream()
                .sorted(Comparator.comparing(CourtStatisticsDTO::getTotalRevenue).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public Double getOverallOccupancyRate(LocalDateTime startTime, LocalDateTime endTime) {
        List<CourtStatisticsDTO> allStats = getAllCourtsStatistics(startTime, endTime);
        return allStats.stream()
                .mapToDouble(CourtStatisticsDTO::getOccupancyRate)
                .average()
                .orElse(0.0);
    }

    private CourtStatisticsDTO calculateStatistics(Court court, List<Booking> bookings, 
            LocalDateTime startTime, LocalDateTime endTime) {
        CourtStatisticsDTO stats = new CourtStatisticsDTO();
        stats.setCourtId(court.getId());
        stats.setCourtName(court.getName());
        stats.setStartTime(startTime);
        stats.setEndTime(endTime);

        // 基本统计
        stats.setTotalBookings(bookings.size());
        stats.setCompletedBookings((int) bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .count());
        stats.setCancelledBookings((int) bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CANCELLED)
                .count());

        // 收入统计
        BigDecimal totalRevenue = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .map(Booking::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalRevenue(totalRevenue);

        // 使用率统计
        long totalHours = startTime.until(endTime, java.time.temporal.ChronoUnit.HOURS);
        if (totalHours > 0) {
            double occupiedHours = bookings.stream()
                    .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                    .mapToDouble(b -> b.getStartTime().until(b.getEndTime(), 
                            java.time.temporal.ChronoUnit.MINUTES) / 60.0)
                    .sum();
            stats.setOccupancyRate(occupiedHours / (totalHours * court.getOperatingHours()));
        }

        // 按状态统计
        Map<String, Integer> bookingsByStatus = bookings.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getStatus().toString(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
        stats.setBookingsByStatus(bookingsByStatus);

        // 按小时统计
        Map<String, Integer> bookingsByHour = bookings.stream()
                .collect(Collectors.groupingBy(
                        b -> String.format("%02d:00", b.getStartTime().getHour()),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
        stats.setBookingsByHour(bookingsByHour);

        // 高峰时段预约数
        stats.setPeakHourBookings(bookingsByHour.values().stream()
                .max(Integer::compareTo)
                .orElse(0));

        return stats;
    }
} 