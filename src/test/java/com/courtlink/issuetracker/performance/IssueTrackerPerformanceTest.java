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
            "���ܲ��Ա���",
            "���ܲ�������",
            "http://example.com/screenshot.png",
            IssuePriority.HIGH,
            IssueModule.FRONTEND
        );

        // Ԥ��
        issueService.createIssue(request);

        // ���Ե��δ�������
        stopWatch.start("���δ���");
        var response = issueService.createIssue(request);
        stopWatch.stop();
        assertThat(stopWatch.getLastTaskTimeMillis()).isLessThan(200); // ȷ�����δ�����200ms�����

        // ����������������
        int batchSize = 100;
        List<IssueRequest> requests = new ArrayList<>();
        for (int i = 0; i < batchSize; i++) {
            requests.add(new IssueRequest(
                "���ܲ��Ա���" + i,
                "���ܲ�������" + i,
                "http://example.com/screenshot" + i + ".png",
                IssuePriority.HIGH,
                IssueModule.FRONTEND
            ));
        }

        stopWatch.start("��������");
        requests.forEach(issueService::createIssue);
        stopWatch.stop();
        
        // ȷ��ƽ��ÿ������������100ms�����
        assertThat(stopWatch.getLastTaskTimeMillis() / batchSize).isLessThan(100);
    }

    @Test
    void findIssues_Performance() {
        var stopWatch = new StopWatch();
        var pageable = PageRequest.of(0, 20);

        // Ԥ��
        issueService.findIssues(null, null, null, pageable);

        // �����޹���������ѯ����
        stopWatch.start("�޹���������ѯ");
        var result = issueService.findIssues(null, null, null, pageable);
        stopWatch.stop();
        assertThat(stopWatch.getLastTaskTimeMillis()).isLessThan(100); // ȷ����ѯ��100ms�����

        // ���Դ�����������ѯ����
        stopWatch.start("������������ѯ");
        result = issueService.findIssues(null, IssuePriority.HIGH, IssueModule.FRONTEND, pageable);
        stopWatch.stop();
        assertThat(stopWatch.getLastTaskTimeMillis()).isLessThan(150); // ȷ�������˵Ĳ�ѯ��150ms�����
    }

    @Test
    void concurrentAccess_StabilityTest() throws Exception {
        int threadCount = 10;
        int requestsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        var stopWatch = new StopWatch();
        stopWatch.start("��������");

        // ������������
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            var future = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    try {
                        // ����ȱ��
                        var request = new IssueRequest(
                            "��������-" + threadId + "-" + j,
                            "������������",
                            "http://example.com/screenshot.png",
                            IssuePriority.HIGH,
                            IssueModule.FRONTEND
                        );
                        var response = issueService.createIssue(request);
                        assertThat(response.id()).isNotNull();

                        // ��ѯȱ��
                        var searchResult = issueService.getIssue(response.id());
                        assertThat(searchResult.title()).isEqualTo(request.title());
                    } catch (Exception e) {
                        throw new RuntimeException("��������ʧ��", e);
                    }
                }
            }, executor);
            futures.add(future);
        }

        // �ȴ������������
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        stopWatch.stop();

        executor.shutdown();
        
        // ��֤����ָ��
        long totalRequests = threadCount * requestsPerThread * 2; // ÿ��ѭ�����������Ͳ�ѯ��������
        long averageTimePerRequest = stopWatch.getTotalTimeMillis() / totalRequests;
        assertThat(averageTimePerRequest).isLessThan(50); // ȷ��ƽ��ÿ��������50ms�����
    }
} 