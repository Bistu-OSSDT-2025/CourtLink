package com.courtlink.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 简单的限流工具类
 * 用于防止系统过载和恶意请求
 */
@Component
public class RateLimiter {

    // 存储每个用户的请求记录
    private final ConcurrentHashMap<String, UserRequestRecord> userRequests = new ConcurrentHashMap<>();
    
    // 默认配置
    private static final int DEFAULT_MAX_REQUESTS = 100; // 每分钟最大请求数
    private static final int DEFAULT_TIME_WINDOW_MINUTES = 1; // 时间窗口（分钟）
    
    /**
     * 检查是否允许请求
     * @param userId 用户ID或IP地址
     * @return true表示允许请求，false表示应该被限制
     */
    public boolean isAllowed(String userId) {
        return isAllowed(userId, DEFAULT_MAX_REQUESTS, DEFAULT_TIME_WINDOW_MINUTES);
    }
    
    /**
     * 检查是否允许请求（自定义参数）
     * @param userId 用户ID或IP地址
     * @param maxRequests 最大请求数
     * @param timeWindowMinutes 时间窗口（分钟）
     * @return true表示允许请求，false表示应该被限制
     */
    public boolean isAllowed(String userId, int maxRequests, int timeWindowMinutes) {
        LocalDateTime now = LocalDateTime.now();
        
        UserRequestRecord record = userRequests.computeIfAbsent(userId, 
            k -> new UserRequestRecord(now, new AtomicInteger(0)));
        
        // 检查时间窗口是否已过期
        if (ChronoUnit.MINUTES.between(record.getWindowStart(), now) >= timeWindowMinutes) {
            // 重置计数器和时间窗口
            record.setWindowStart(now);
            record.getRequestCount().set(0);
        }
        
        // 检查是否超过限制
        int currentCount = record.getRequestCount().incrementAndGet();
        
        if (currentCount > maxRequests) {
            return false; // 超过限制
        }
        
        return true; // 允许请求
    }
    
    /**
     * 获取用户当前请求统计
     * @param userId 用户ID
     * @return 请求统计信息
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
     * 清理过期的记录（定期清理任务可以调用）
     */
    public void cleanupExpiredRecords() {
        LocalDateTime cutoff = LocalDateTime.now().minus(DEFAULT_TIME_WINDOW_MINUTES * 2, ChronoUnit.MINUTES);
        
        userRequests.entrySet().removeIf(entry -> 
            entry.getValue().getWindowStart().isBefore(cutoff)
        );
    }
    
    /**
     * 获取当前限流器状态
     */
    public RateLimiterStats getStats() {
        return new RateLimiterStats(
            userRequests.size(),
            DEFAULT_MAX_REQUESTS,
            DEFAULT_TIME_WINDOW_MINUTES
        );
    }
    
    /**
     * 用户请求记录内部类
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
     * 请求统计信息
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
     * 限流器统计信息
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