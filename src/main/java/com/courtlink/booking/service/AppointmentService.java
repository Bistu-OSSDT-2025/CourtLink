package com.courtlink.booking.service;

import com.courtlink.booking.dto.AppointmentQuery;
import com.courtlink.booking.dto.AppointmentRequest;
import com.courtlink.booking.dto.AppointmentResponse;
import com.courtlink.booking.entity.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Appointment Service Interface
 * 
 * @author CourtLink Team
 * @version 1.0.0
 */
public interface AppointmentService {

    /**
     * Create appointment
     * 
     * @param userId User ID
     * @param request Appointment request
     * @return Created appointment
     */
    AppointmentResponse createAppointment(String userId, AppointmentRequest request);

    /**
     * Update appointment
     * 
     * @param id Appointment ID
     * @param request Appointment request
     * @return Updated appointment
     */
    AppointmentResponse updateAppointment(Long id, AppointmentRequest request);

    /**
     * Cancel appointment
     * 
     * @param id Appointment ID
     * @return Cancelled appointment
     */
    AppointmentResponse cancelAppointment(Long id);

    /**
     * Complete appointment
     * 
     * @param id Appointment ID
     * @return Completed appointment
     */
    AppointmentResponse completeAppointment(Long id);

    /**
     * Confirm appointment
     * 
     * @param id Appointment ID
     * @return Confirmed appointment
     */
    AppointmentResponse confirmAppointment(Long id);

    /**
     * Get appointment by ID
     * 
     * @param id Appointment ID
     * @return Appointment if found
     */
    AppointmentResponse getAppointmentById(Long id);

    /**
     * Get appointments by user ID
     * 
     * @param userId User ID
     * @param pageable Pagination
     * @return Page of appointments
     */
    Page<AppointmentResponse> getAppointmentsByUserId(String userId, Pageable pageable);

    /**
     * Get appointments by provider ID
     * 
     * @param providerId Provider ID
     * @param pageable Pagination
     * @return Page of appointments
     */
    Page<AppointmentResponse> getAppointmentsByProviderId(String providerId, Pageable pageable);

    /**
     * Get appointments by status
     * 
     * @param status Appointment status
     * @param pageable Pagination
     * @return Page of appointments
     */
    Page<AppointmentResponse> getAppointmentsByStatus(Appointment.AppointmentStatus status, Pageable pageable);

    /**
     * Search appointments with query
     * 
     * @param query Search query
     * @param pageable Pagination
     * @return Page of appointments
     */
    Page<AppointmentResponse> searchAppointments(AppointmentQuery query, Pageable pageable);

    /**
     * Get appointments by time range
     * 
     * @param startTime Start time
     * @param endTime End time
     * @return List of appointments
     */
    List<AppointmentResponse> getAppointmentsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Check appointment conflicts
     * 
     * @param providerId Provider ID
     * @param startTime Start time
     * @param endTime End time
     * @param excludeId Appointment ID to exclude
     * @return True if conflict exists
     */
    boolean hasConflict(String providerId, LocalDateTime startTime, LocalDateTime endTime, Long excludeId);

    /**
     * Process expired appointments
     * 
     * @return Number of processed appointments
     */
    int processExpiredAppointments();

    /**
     * Get statistics by user ID
     * 
     * @param userId User ID
     * @return Appointment statistics
     */
    AppointmentStatistics getStatisticsByUserId(String userId);

    /**
     * Appointment Statistics
     */
    class AppointmentStatistics {
        private long totalCount;
        private long completedCount;
        private long cancelledCount;
        private long pendingCount;

        // Constructors, getters and setters
        public AppointmentStatistics() {}

        public AppointmentStatistics(long totalCount, long completedCount, long cancelledCount, long pendingCount) {
            this.totalCount = totalCount;
            this.completedCount = completedCount;
            this.cancelledCount = cancelledCount;
            this.pendingCount = pendingCount;
        }

        public long getTotalCount() { return totalCount; }
        public void setTotalCount(long totalCount) { this.totalCount = totalCount; }

        public long getCompletedCount() { return completedCount; }
        public void setCompletedCount(long completedCount) { this.completedCount = completedCount; }

        public long getCancelledCount() { return cancelledCount; }
        public void setCancelledCount(long cancelledCount) { this.cancelledCount = cancelledCount; }

        public long getPendingCount() { return pendingCount; }
        public void setPendingCount(long pendingCount) { this.pendingCount = pendingCount; }
    }
} 
