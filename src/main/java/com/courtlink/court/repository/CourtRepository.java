package com.courtlink.court.repository;

import com.courtlink.court.entity.Court;
import com.courtlink.court.entity.CourtStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CourtRepository extends JpaRepository<Court, Long> {
    List<Court> findByEnabled(boolean enabled);
    List<Court> findByStatus(CourtStatus status);
    List<Court> findByCourtType(Court.CourtType courtType);
    boolean existsByName(String name);
    
    List<Court> findByCourtLocation(Court.CourtLocation courtLocation);
    
    List<Court> findByEnabledTrueAndStatus(CourtStatus status);
    
    List<Court> findByStatusIn(List<CourtStatus> statuses);
    
    List<Court> findByPricePerHourBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    List<Court> findByHasLighting(boolean hasLighting);
    
    @Query("SELECT c FROM Court c WHERE c.status = 'MAINTENANCE' AND c.maintenanceStartTime IS NOT NULL")
    List<Court> findCourtsInMaintenance();
    
    @Query("SELECT c FROM Court c WHERE c.status = 'MAINTENANCE' AND c.maintenanceEndTime < :currentTime")
    List<Court> findOverdueMaintenanceCourts(@Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT COUNT(c) FROM Court c WHERE c.enabled = true")
    Long countEnabledCourts();
    
    @Query("SELECT COUNT(c) FROM Court c WHERE c.enabled = false")
    Long countDisabledCourts();
    
    @Query("SELECT COUNT(c) FROM Court c WHERE c.status = :status")
    Long countByStatus(@Param("status") CourtStatus status);
    
    @Query("SELECT COUNT(c) FROM Court c WHERE c.courtType = :courtType")
    Long countByCourtType(@Param("courtType") Court.CourtType courtType);
    
    @Query("SELECT COUNT(c) FROM Court c WHERE c.courtLocation = :location")
    Long countByCourtLocation(@Param("location") Court.CourtLocation location);
    
    @Query("SELECT AVG(c.pricePerHour) FROM Court c WHERE c.enabled = true")
    BigDecimal findAveragePrice();
    
    @Query("SELECT MIN(c.pricePerHour) FROM Court c WHERE c.enabled = true")
    BigDecimal findMinPrice();
    
    @Query("SELECT MAX(c.pricePerHour) FROM Court c WHERE c.enabled = true")
    BigDecimal findMaxPrice();
    
    @Query("SELECT SUM(c.maxPlayers) FROM Court c WHERE c.enabled = true AND c.maxPlayers IS NOT NULL")
    Long sumTotalCapacity();
    
    @Query("SELECT AVG(c.maxPlayers) FROM Court c WHERE c.enabled = true AND c.maxPlayers IS NOT NULL")
    Double findAverageCapacity();
    
    @Query("SELECT AVG(c.operatingHours) FROM Court c WHERE c.enabled = true")
    Double findAverageOperatingHours();
    
    @Query("SELECT SUM(c.operatingHours) FROM Court c WHERE c.enabled = true")
    Long sumTotalOperatingHours();
    
    @Query("SELECT COUNT(c) FROM Court c WHERE c.enabled = true AND c.hasLighting = true")
    Long countCourtsWithLighting();
    
    @Query("SELECT COUNT(c) FROM Court c WHERE c.enabled = true AND c.hasLighting = false")
    Long countCourtsWithoutLighting();
    
    @Query("SELECT MIN(c.createdAt) FROM Court c")
    LocalDateTime findOldestCourtCreatedTime();
    
    @Query("SELECT MAX(c.createdAt) FROM Court c")
    LocalDateTime findNewestCourtCreatedTime();
    
    List<Court> findByNameStartingWith(String namePrefix);
    
    List<Court> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);
} 