package com.bistu.ossdt.courtlink.admin.performance;

import com.bistu.ossdt.courtlink.admin.dto.CreateUserRequest;
import com.bistu.ossdt.courtlink.admin.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserManagementPerformanceTest {

    @Autowired
    private UserService userService;

    private static final int CONCURRENT_USERS = 100;
    private static final int OPERATIONS_PER_USER = 10;
    private static final long ACCEPTABLE_RESPONSE_TIME = 500; // 毫秒

    @Test
    @Sql("/sql/init-test-users.sql")
    void bulkOperationsPerformance() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<CompletableFuture<Duration>> futures = new ArrayList<>();
        List<Long> createdUserIds = new ArrayList<>();

        // 批量创建用户
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            final int userId = i;
            CompletableFuture<Duration> future = CompletableFuture.supplyAsync(() -> {
                Instant start = Instant.now();
                try {
                    for (int j = 0; j < OPERATIONS_PER_USER; j++) {
                        CreateUserRequest request = new CreateUserRequest();
                        request.setUsername("perftest" + userId + "_" + j);
                        request.setPassword("password123");
                        request.setEmail("perf" + userId + "_" + j + "@test.com");
                        request.setPhone("138" + String.format("%08d", userId));
                        request.setRealName("Performance Test " + userId);
                        request.setRole("USER");

                        var user = userService.createUser(request);
                        synchronized (createdUserIds) {
                            createdUserIds.add(user.getId());
                        }
                    }
                } catch (Exception e) {
                    fail("Performance test failed: " + e.getMessage());
                }
                return Duration.between(start, Instant.now());
            }, executor);
            futures.add(future);
        }

        // 等待所有操作完成并收集执行时间
        List<Duration> executionTimes = new ArrayList<>();
        for (CompletableFuture<Duration> future : futures) {
            executionTimes.add(future.get(30, TimeUnit.SECONDS));
        }

        // 计算平均响应时间
        double averageResponseTime = executionTimes.stream()
                .mapToLong(Duration::toMillis)
                .average()
                .orElse(0.0);

        // 验证性能指标
        assertTrue(averageResponseTime <= ACCEPTABLE_RESPONSE_TIME,
                "Average response time (" + averageResponseTime + "ms) exceeds acceptable threshold (" + ACCEPTABLE_RESPONSE_TIME + "ms)");

        // 清理测试数据
        executor.shutdown();
        assertTrue(executor.awaitTermination(1, TimeUnit.MINUTES));
    }

    @Test
    @Sql("/sql/init-test-users.sql")
    void searchPerformance() throws Exception {
        // 首先创建足够多的测试数据
        for (int i = 0; i < 1000; i++) {
            CreateUserRequest request = new CreateUserRequest();
            request.setUsername("searchtest" + i);
            request.setPassword("password123");
            request.setEmail("search" + i + "@test.com");
            request.setPhone("138" + String.format("%08d", i));
            request.setRealName("Search Test " + i);
            request.setRole("USER");
            userService.createUser(request);
        }

        // 测试搜索性能
        Instant start = Instant.now();
        var result = userService.getUsers(1, 10, "searchtest");
        Duration searchTime = Duration.between(start, Instant.now());

        assertTrue(searchTime.toMillis() <= ACCEPTABLE_RESPONSE_TIME,
                "Search operation time (" + searchTime.toMillis() + "ms) exceeds acceptable threshold (" + ACCEPTABLE_RESPONSE_TIME + "ms)");
        assertTrue(result.getTotal() > 0, "Search should return results");
    }
} 