package com.courtlink.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * �򵥵�����������
 * ���ڷ�ֹϵͳ���غͶ�������
 */
@Component
public class RateLimiter {

    // �洢ÿ���û��������¼
    private final ConcurrentHashMap<String, UserRequestRecord> userRequests = new ConcurrentHashMap<>();
    
    // Ĭ������
    private static final int DEFAULT_MAX_REQUESTS = 100; // ÿ�������������
    private static final int DEFAULT_TIME_WINDOW_MINUTES = 1; // ʱ�䴰�ڣ����ӣ�
    
    /**
     * ����Ƿ���������
     * @param userId �û�ID��IP��ַ
     * @return true��ʾ��������false��ʾӦ�ñ�����
     */
    public boolean isAllowed(String userId) {
        return isAllowed(userId, DEFAULT_MAX_REQUESTS, DEFAULT_TIME_WINDOW_MINUTES);
    }
    
    /**
     * ����Ƿ����������Զ��������
     * @param userId �û�ID��IP��ַ
     * @param maxRequests ���������
     * @param timeWindowMinutes ʱ�䴰�ڣ����ӣ�
     * @return true��ʾ��������false��ʾӦ�ñ�����
     */
    public boolean isAllowed(String userId, int maxRequests, int timeWindowMinutes) {
        LocalDateTime now = LocalDateTime.now();
        
        UserRequestRecord record = userRequests.computeIfAbsent(userId, 
            k -> new UserRequestRecord(now, new AtomicInteger(0)));
        
        // ���ʱ�䴰���Ƿ��ѹ���
        if (ChronoUnit.MINUTES.between(record.getWindowStart(), now) >= timeWindowMinutes) {
            // ���ü�������ʱ�䴰��
            record.setWindowStart(now);
            record.getRequestCount().set(0);
        }
        
        // ����Ƿ񳬹�����
        int currentCount = record.getRequestCount().incrementAndGet();
        
        if (currentCount > maxRequests) {
            return false; // ��������
        }
        
        return true; // ��������
    }
    
    /**
     * ��ȡ�û���ǰ����ͳ��
     * @param userId �û�ID
     * @return ����ͳ����Ϣ
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
     * ������ڵļ�¼����������������Ե��ã�
     */
    public void cleanupExpiredRecords() {
        LocalDateTime cutoff = LocalDateTime.now().minus(DEFAULT_TIME_WINDOW_MINUTES * 2, ChronoUnit.MINUTES);
        
        userRequests.entrySet().removeIf(entry -> 
            entry.getValue().getWindowStart().isBefore(cutoff)
        );
    }
    
    /**
     * ��ȡ��ǰ������״̬
     */
    public RateLimiterStats getStats() {
        return new RateLimiterStats(
            userRequests.size(),
            DEFAULT_MAX_REQUESTS,
            DEFAULT_TIME_WINDOW_MINUTES
        );
    }
    
    /**
     * �û������¼�ڲ���
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
     * ����ͳ����Ϣ
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
     * ������ͳ����Ϣ
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