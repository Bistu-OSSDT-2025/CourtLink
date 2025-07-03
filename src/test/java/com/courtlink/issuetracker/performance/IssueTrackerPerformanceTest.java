package com.courtlink.issuetracker.performance;

import com.courtlink.issuetracker.dto.IssueRequest;
import com.courtlink.issuetracker.enums.IssueModule;
import com.courtlink.issuetracker.enums.IssuePriority;
import com.courtlink.issuetracker.service.IssueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class IssueTrackerPerformanceTest {

    @Autowired
    private IssueService issueService;

    @Test
    void createIssue_Performance() {
        var stopWatch = new StopWatch();
        var request = new IssueRequest(
            "性能测试标题",
            "性能测试描述",
            "http://example.com/screenshot.png",
            IssuePriority.HIGH,
            IssueModule.FRONTEND
        );

        // 预热
        issueService.createIssue(request);

        // 测试单次创建性能
        stopWatch.start("单次创建");
        var response = issueService.createIssue(request);
        stopWatch.stop();
        assertThat(stopWatch.getLastTaskTimeMillis()).isLessThan(200); // 确保单次创建在200ms内完成

        // 测试批量创建性能
        int batchSize = 100;
        List<IssueRequest> requests = new ArrayList<>();
        for (int i = 0; i < batchSize; i++) {
            requests.add(new IssueRequest(
                "性能测试标题" + i,
                "性能测试描述" + i,
                "http://example.com/screenshot" + i + ".png",
                IssuePriority.HIGH,
                IssueModule.FRONTEND
            ));
        }

        stopWatch.start("批量创建");
        requests.forEach(issueService::createIssue);
        stopWatch.stop();
        
        // 确保平均每个创建操作在100ms内完成
        assertThat(stopWatch.getLastTaskTimeMillis() / batchSize).isLessThan(100);
    }

    @Test
    void findIssues_Performance() {
        var stopWatch = new StopWatch();
        var pageable = PageRequest.of(0, 20);

        // 预热
        issueService.findIssues(null, null, null, pageable);

        // 测试无过滤条件查询性能
        stopWatch.start("无过滤条件查询");
        var result = issueService.findIssues(null, null, null, pageable);
        stopWatch.stop();
        assertThat(stopWatch.getLastTaskTimeMillis()).isLessThan(100); // 确保查询在100ms内完成

        // 测试带过滤条件查询性能
        stopWatch.start("带过滤条件查询");
        result = issueService.findIssues(null, IssuePriority.HIGH, IssueModule.FRONTEND, pageable);
        stopWatch.stop();
        assertThat(stopWatch.getLastTaskTimeMillis()).isLessThan(150); // 确保带过滤的查询在150ms内完成
    }

    @Test
    void concurrentAccess_StabilityTest() throws Exception {
        int threadCount = 10;
        int requestsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        var stopWatch = new StopWatch();
        stopWatch.start("并发测试");

        // 创建并发任务
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            var future = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    try {
                        // 创建缺陷
                        var request = new IssueRequest(
                            "并发测试-" + threadId + "-" + j,
                            "并发测试描述",
                            "http://example.com/screenshot.png",
                            IssuePriority.HIGH,
                            IssueModule.FRONTEND
                        );
                        var response = issueService.createIssue(request);
                        assertThat(response.id()).isNotNull();

                        // 查询缺陷
                        var searchResult = issueService.getIssue(response.id());
                        assertThat(searchResult.title()).isEqualTo(request.title());
                    } catch (Exception e) {
                        throw new RuntimeException("并发测试失败", e);
                    }
                }
            }, executor);
            futures.add(future);
        }

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        stopWatch.stop();

        executor.shutdown();
        
        // 验证性能指标
        long totalRequests = threadCount * requestsPerThread * 2; // 每次循环包含创建和查询两个操作
        long averageTimePerRequest = stopWatch.getTotalTimeMillis() / totalRequests;
        assertThat(averageTimePerRequest).isLessThan(50); // 确保平均每个请求在50ms内完成
    }
} 