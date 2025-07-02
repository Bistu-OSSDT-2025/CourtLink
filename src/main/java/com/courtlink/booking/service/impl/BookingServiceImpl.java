package com.courtlink.booking.service.impl;

import com.courtlink.booking.dto.BookingDTO;
import com.courtlink.booking.dto.BookingRequest;
import com.courtlink.booking.entity.Booking;
import com.courtlink.booking.entity.BookingStatus;
import com.courtlink.booking.repository.BookingRepository;
import com.courtlink.booking.service.BookingService;
import com.courtlink.court.entity.Court;
import com.courtlink.court.repository.CourtRepository;
import com.courtlink.court.entity.CourtStatus;
import com.courtlink.user.entity.User;
import com.courtlink.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;
    private final UserService userService;

    @Override
    @Transactional
    public BookingDTO createBooking(BookingRequest request) {
        // 验证时间段是否可用
        if (!isTimeSlotAvailable(request.getCourtId(), request.getStartTime(), request.getEndTime())) {
            throw new IllegalStateException("所选时间段已被预约");
        }

        Court court = courtRepository.findById(request.getCourtId())
                .orElseThrow(() -> new EntityNotFoundException("场地不存在"));

        if (court.getStatus() != CourtStatus.AVAILABLE) {
            throw new IllegalStateException("场地当前不可用");
        }

        User currentUser = userService.getCurrentUser();

        Booking booking = new Booking();
        booking.setCourt(court);
        booking.setUser(currentUser);
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setRemarks(request.getRemarks());

        // 计算总价
        long hours = Duration.between(request.getStartTime(), request.getEndTime()).toHours();
        booking.setTotalPrice(court.getPricePerHour().multiply(BigDecimal.valueOf(hours)));

        booking.setStatus(BookingStatus.PENDING);
        booking.setLastModifiedBy(currentUser.getUsername());

        return BookingDTO.fromEntity(bookingRepository.save(booking));
    }

    @Override
    public BookingDTO getBookingById(Long id) {
        return bookingRepository.findById(id)
                .map(BookingDTO::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("预约不存在"));
    }

    @Override
    public List<BookingDTO> getBookingsByUser(Long userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(BookingDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDTO> getBookingsByCourt(Long courtId) {
        return bookingRepository.findByCourtId(courtId).stream()
                .map(BookingDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDTO> getBookingsByStatus(BookingStatus status) {
        return bookingRepository.findByStatus(status).stream()
                .map(BookingDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDTO> getBookingsByUserAndDateRange(Long userId, LocalDateTime start, LocalDateTime end) {
        return bookingRepository.findByUserIdAndStartTimeBetween(userId, start, end).stream()
                .map(BookingDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDTO> getBookingsByCourtAndDateRange(Long courtId, LocalDateTime start, LocalDateTime end) {
        return bookingRepository.findByCourtIdAndStartTimeBetween(courtId, start, end).stream()
                .map(BookingDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingDTO cancelBooking(Long id, String reason) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("预约不存在"));

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException("已完成的预约不能取消");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("预约已经被取消");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelReason(reason);
        booking.setLastModifiedBy(userService.getCurrentUser().getUsername());

        return BookingDTO.fromEntity(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDTO updateBookingStatus(Long id, BookingStatus status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("预约不存在"));

        booking.setStatus(status);
        booking.setLastModifiedBy(userService.getCurrentUser().getUsername());

        return BookingDTO.fromEntity(bookingRepository.save(booking));
    }

    @Override
    public boolean isTimeSlotAvailable(Long courtId, LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("开始时间不能晚于结束时间");
        }

        List<Booking> conflictingBookings = bookingRepository.findConflictingBookings(courtId, startTime, endTime);
        return conflictingBookings.isEmpty();
    }
} 