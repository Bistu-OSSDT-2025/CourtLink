package com.courtlink.booking.enums;

/**
 * Appointment status enumeration
 */
public enum AppointmentStatus {
    /**
     * Pending confirmation
     */
    PENDING,
    
    /**
     * Confirmed and ready
     */
    CONFIRMED,
    
    /**
     * Completed successfully
     */
    COMPLETED,
    
    /**
     * Cancelled by user or provider
     */
    CANCELLED,
    
    /**
     * Expired due to time
     */
    EXPIRED
} 