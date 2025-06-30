package com.courtlink.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple Rate Limiter
 * Prevents system overload and malicious attacks
 */
@Component
public class RateLimiter {

    // Store request records for each user
    private final ConcurrentHashMap<String, UserRequestRecord> userRequests = new ConcurrentHashMap<>();
    
    // Default settings
    private static final int DEFAULT_MAX_REQUESTS = 100; // Max requests per time window
    private static final int DEFAULT_TIME_WINDOW_MINUTES = 1; // Time window in minutes
    
    /**
     * Check if request is allowed
     * @param userId User ID or IP address
     * @return true if allowed, false if should be blocked
     */
    public boolean isAllowed(String userId) {
        return isAllowed(userId, DEFAULT_MAX_REQUESTS, DEFAULT_TIME_WINDOW_MINUTES);
    }
    
    /**
     * Check if request is allowed with custom parameters
     * @param userId User ID or IP address
     * @param maxRequests Maximum requests allowed
     * @param timeWindowMinutes Time window in minutes
     * @return true if allowed, false if should be blocked
     */
    public boolean isAllowed(String userId, int maxRequests, int timeWindowMinutes) {
        LocalDateTime now = LocalDateTime.now();
        
        UserRequestRecord record = userRequests.computeIfAbsent(userId, 
            k -> new UserRequestRecord(now, new AtomicInteger(0)));
        
        // Check if time window has expired
        if (ChronoUnit.MINUTES.between(record.getWindowStart(), now) >= timeWindowMinutes) {
            // Reset counter and time window
            record.setWindowStart(now);
            record.getRequestCount().set(0);
        }
        
        // Check if limit exceeded
        int currentCount = record.getRequestCount().incrementAndGet();
        
        if (currentCount > maxRequests) {
            return false; // Rate limit exceeded
        }
        
        return true; // Request allowed
    }
    
    /**
     * Get current request statistics for user
     * @param userId User ID
     * @return Request statistics
     */
    public RequestStats getRequestStats(String userId) {
        UserRequestRecord record = userRequests.get(userId);
        if (record == null) {
            return new RequestStats(0, LocalDateTime.now(), true);
        }
        
        LocalDateTime now = LocalDateTime.now();
        boolean isInCurrentWindow = ChronoUnit.MINUTES.between(record.getWindowStart(), now) < DEFAULT_TIME_WINDOW_MINUTES;
        
        return new RequestStats(
            isInCurrentWindow ? record.getRequestCount().get() : 0,
            record.getWindowStart(),
            isInCurrentWindow
        );
    }
    
    /**
     * Clean up expired records (can be called periodically)
     */
    public void cleanupExpiredRecords() {
        LocalDateTime cutoff = LocalDateTime.now().minus(DEFAULT_TIME_WINDOW_MINUTES * 2, ChronoUnit.MINUTES);
        
        userRequests.entrySet().removeIf(entry -> 
            entry.getValue().getWindowStart().isBefore(cutoff)
        );
    }
    
    /**
     * Get current rate limiter statistics
     */
    public RateLimiterStats getStats() {
        return new RateLimiterStats(
            userRequests.size(),
            DEFAULT_MAX_REQUESTS,
            DEFAULT_TIME_WINDOW_MINUTES
        );
    }
    
    /**
     * User request record internal class
     */
    private static class UserRequestRecord {
        private LocalDateTime windowStart;
        private AtomicInteger requestCount;
        
        public UserRequestRecord(LocalDateTime windowStart, AtomicInteger requestCount) {
            this.windowStart = windowStart;
            this.requestCount = requestCount;
        }
        
        public LocalDateTime getWindowStart() {
            return windowStart;
        }
        
        public void setWindowStart(LocalDateTime windowStart) {
            this.windowStart = windowStart;
        }
        
        public AtomicInteger getRequestCount() {
            return requestCount;
        }
    }
    
    /**
     * Request statistics
     */
    public static class RequestStats {
        private final int requestCount;
        private final LocalDateTime windowStart;
        private final boolean isInCurrentWindow;
        
        public RequestStats(int requestCount, LocalDateTime windowStart, boolean isInCurrentWindow) {
            this.requestCount = requestCount;
            this.windowStart = windowStart;
            this.isInCurrentWindow = isInCurrentWindow;
        }
        
        public int getRequestCount() {
            return requestCount;
        }
        
        public LocalDateTime getWindowStart() {
            return windowStart;
        }
        
        public boolean isInCurrentWindow() {
            return isInCurrentWindow;
        }
    }
    
    /**
     * Rate limiter statistics
     */
    public static class RateLimiterStats {
        private final int activeUsers;
        private final int maxRequestsPerWindow;
        private final int timeWindowMinutes;
        
        public RateLimiterStats(int activeUsers, int maxRequestsPerWindow, int timeWindowMinutes) {
            this.activeUsers = activeUsers;
            this.maxRequestsPerWindow = maxRequestsPerWindow;
            this.timeWindowMinutes = timeWindowMinutes;
        }
        
        public int getActiveUsers() {
            return activeUsers;
        }
        
        public int getMaxRequestsPerWindow() {
            return maxRequestsPerWindow;
        }
        
        public int getTimeWindowMinutes() {
            return timeWindowMinutes;
        }
    }
} 