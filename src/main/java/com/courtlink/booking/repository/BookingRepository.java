package com.courtlink.booking.repository;

import com.courtlink.booking.entity.Booking;
import com.courtlink.booking.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
    List<Booking> findByCourtId(Long courtId);
    List<Booking> findByStatus(BookingStatus status);
    
    @Query("SELECT b FROM Booking b WHERE b.court.id = :courtId " +
           "AND b.status NOT IN ('CANCELLED', 'REFUNDED') " +
           "AND ((b.startTime <= :endTime AND b.endTime >= :startTime) " +
           "OR (b.startTime >= :startTime AND b.startTime < :endTime))")
    List<Booking> findConflictingBookings(Long courtId, LocalDateTime startTime, LocalDateTime endTime);

    List<Booking> findByUserIdAndStartTimeBetween(Long userId, LocalDateTime start, LocalDateTime end);
    List<Booking> findByCourtIdAndStartTimeBetween(Long courtId, LocalDateTime start, LocalDateTime end);
} 