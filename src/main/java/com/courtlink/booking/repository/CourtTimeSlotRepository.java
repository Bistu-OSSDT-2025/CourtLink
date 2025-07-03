package com.courtlink.booking.repository;

import com.courtlink.booking.entity.CourtTimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CourtTimeSlotRepository extends JpaRepository<CourtTimeSlot, Long> {
    
    List<CourtTimeSlot> findByCourtIdAndDate(Long courtId, LocalDate date);
    
    List<CourtTimeSlot> findByCourtIdAndDateBetween(Long courtId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT c FROM CourtTimeSlot c WHERE c.court.id = :courtId AND c.date = :date ORDER BY c.startTime")
    List<CourtTimeSlot> findByCourtIdAndDateOrderByStartTime(@Param("courtId") Long courtId, @Param("date") LocalDate date);
    
    @Query("SELECT c FROM CourtTimeSlot c WHERE c.court.id IN :courtIds AND c.date = :date ORDER BY c.court.id, c.startTime")
    List<CourtTimeSlot> findByCourtIdsAndDate(@Param("courtIds") List<Long> courtIds, @Param("date") LocalDate date);
    
    boolean existsByCourtIdAndDateAndStartTime(Long courtId, LocalDate date, java.time.LocalTime startTime);
} 