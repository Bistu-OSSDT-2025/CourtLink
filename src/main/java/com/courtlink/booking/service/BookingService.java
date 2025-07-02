package com.courtlink.booking.service;

import com.courtlink.booking.dto.BookingDTO;
import com.courtlink.booking.dto.BookingRequest;
import com.courtlink.booking.entity.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {
    BookingDTO createBooking(BookingRequest request);
    BookingDTO getBookingById(Long id);
    List<BookingDTO> getBookingsByUser(Long userId);
    List<BookingDTO> getBookingsByCourt(Long courtId);
    List<BookingDTO> getBookingsByStatus(BookingStatus status);
    List<BookingDTO> getBookingsByUserAndDateRange(Long userId, LocalDateTime start, LocalDateTime end);
    List<BookingDTO> getBookingsByCourtAndDateRange(Long courtId, LocalDateTime start, LocalDateTime end);
    BookingDTO cancelBooking(Long id, String reason);
    BookingDTO updateBookingStatus(Long id, BookingStatus status);
    boolean isTimeSlotAvailable(Long courtId, LocalDateTime startTime, LocalDateTime endTime);
} 