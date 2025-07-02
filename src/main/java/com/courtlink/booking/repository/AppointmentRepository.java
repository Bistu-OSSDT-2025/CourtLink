package com.courtlink.booking.repository;

import com.courtlink.booking.entity.Appointment;
import com.courtlink.booking.entity.Appointment.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Appointment Data Access Interface
 * 
 * @author CourtLink Team
 * @version 1.0.0
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Find appointments by user ID
     * 
     * @param userId User ID
     * @param pageable Pagination
     * @return Page of appointments
     */
    Page<Appointment> findByUserId(String userId, Pageable pageable);

    /**
     * Find appointments by provider ID
     * 
     * @param providerId Provider ID
     * @param pageable Pagination
     * @return Page of appointments
     */
    Page<Appointment> findByProviderId(String providerId, Pageable pageable);

    /**
     * Find appointments by status
     * 
     * @param status Appointment status
     * @param pageable Pagination
     * @return Page of appointments
     */
    Page<Appointment> findByStatus(AppointmentStatus status, Pageable pageable);

    /**
     * Find appointments by time range
     * 
     * @param startTime Start time
     * @param endTime End time
     * @return List of appointments
     */
    @Query("SELECT a FROM Appointment a WHERE a.startTime >= :startTime AND a.endTime <= :endTime")
    List<Appointment> findByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * Check for conflicting appointments
     * 
     * @param providerId Provider ID
     * @param startTime Start time
     * @param endTime End time
     * @param excludeId Appointment ID to exclude
     * @return Count of conflicting appointments
     */
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.providerId = :providerId " +
           "AND ((a.startTime < :endTime AND a.endTime > :startTime)) " +
           "AND a.status IN ('PENDING', 'CONFIRMED') " +
           "AND (:excludeId IS NULL OR a.id != :excludeId)")
    long countConflictingAppointments(@Param("providerId") String providerId,
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime,
                                                 @Param("excludeId") Long excludeId);

    /**
     * Find expired appointments
     * 
     * @param cutoffTime Cutoff time
     * @return List of expired appointments
     */
    @Query("SELECT a FROM Appointment a WHERE a.endTime < :cutoffTime AND a.status = 'PENDING'")
    List<Appointment> findExpiredAppointments(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Count appointments by user ID and status
     * 
     * @param userId User ID
     * @param status Appointment status
     * @return Count of appointments
     */
    long countByUserIdAndStatus(String userId, AppointmentStatus status);

    /**
     * Find appointments by user ID and status
     * 
     * @param userId User ID
     * @param status Appointment status
     * @return List of appointments
     */
    List<Appointment> findByUserIdAndStatus(String userId, AppointmentStatus status);

    /**
     * Find recent appointments
     * 
     * @param userId User ID
     * @param days Number of days
     * @return List of recent appointments
     */
    @Query("SELECT a FROM Appointment a WHERE a.userId = :userId " +
           "AND a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<Appointment> findRecentAppointments(@Param("userId") String userId, @Param("since") LocalDateTime since);

    /**
     * Find appointment by payment ID
     * 
     * @param paymentId Payment ID
     * @return Appointment if found
     */
    Optional<Appointment> findByPaymentId(String paymentId);

    /**
     * Count appointments by provider ID and status
     * 
     * @param providerId Provider ID
     * @param status Appointment status
     * @return Count of appointments
     */
    long countByProviderIdAndStatus(String providerId, AppointmentStatus status);

    @Query("SELECT a FROM Appointment a " +
           "WHERE a.providerId = :providerId " +
           "AND ((a.startTime < :endTime AND a.endTime > :startTime)) " +
           "AND a.status IN ('PENDING', 'CONFIRMED') " +
           "AND (:excludeId IS NULL OR a.id <> :excludeId)")
    List<Appointment> findConflictingAppointments(
        @Param("providerId") String providerId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("excludeId") Long excludeId
    );

    List<Appointment> findByCourtId(Long courtId);
    
    @Query("SELECT a FROM Appointment a WHERE a.courtId = :courtId AND " +
           "((a.startTime <= :endTime AND a.endTime >= :startTime) OR " +
           "(a.startTime >= :startTime AND a.startTime < :endTime))")
    List<Appointment> findOverlappingAppointments(
            @Param("courtId") Long courtId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
    

    
    Page<Appointment> findByCourtId(Long courtId, Pageable pageable);
    
    @Query("SELECT a FROM Appointment a WHERE a.startTime >= :startTime AND a.startTime < :endTime")
    List<Appointment> findByDateRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.courtId = :courtId AND " +
           "a.startTime >= :startTime AND a.startTime < :endTime")
    long countByCourtIdAndDateRange(
            @Param("courtId") Long courtId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT a FROM Appointment a WHERE a.userId = :userId AND " +
           "a.startTime >= :startTime AND a.startTime < :endTime")
    List<Appointment> findByUserIdAndDateRange(
            @Param("userId") String userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT a FROM Appointment a WHERE a.status = :status AND " +
           "a.startTime >= :startTime AND a.startTime < :endTime")
    List<Appointment> findByStatusAndDateRange(
            @Param("status") AppointmentStatus status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
} 
