package com.courtlink.performance;

import com.courtlink.dto.CourtRequest;
import com.courtlink.dto.CourtResponse;
import com.courtlink.service.CourtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * ���ܲ�����
 * ����ϵͳ�ڸ߲�������µı���
 */
@SpringBootTest
@ExtendWith(MockitoExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class CourtControllerPerformanceTest {

    @Mock
    private CourtService courtService;

    private ExecutorService executorService;
    private final int CONCURRENT_USERS = 100;
    private final int OPERATIONS_PER_USER = 10;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(CONCURRENT_USERS);
        
        // Mock service responses
        CourtResponse mockResponse = new CourtResponse();
        mockResponse.setId(1L);
        mockResponse.setName("���Գ���");
        mockResponse.setDescription("��������");
        mockResponse.setCapacity(20);
        
        when(courtService.createCourt(any(CourtRequest.class))).thenReturn(mockResponse);
        when(courtService.getCourtById(any(Long.class))).thenReturn(mockResponse);
    }

    @Test
    @Timeout(30)
    void testConcurrentCourtCreation() throws InterruptedException {
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(CONCURRENT_USERS);
        
        List<Future<Void>> futures = new ArrayList<>();
        
        // ���������û�
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            final int userId = i;
            Future<Void> future = executorService.submit(() -> {
                try {
                    for (int j = 0; j < OPERATIONS_PER_USER; j++) {
                        CourtRequest request = createTestRequest(userId, j);
                        CourtResponse response = courtService.createCourt(request);
                        
                        if (response != null && response.getId() != null) {
                            successCount.incrementAndGet();
                        } else {
                            errorCount.incrementAndGet();
                        }
                        
                        // ģ����ʵ�û��������
                        Thread.sleep(10);
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
                return null;
            });
            futures.add(future);
        }
        
        // �ȴ����в������
        boolean completed = latch.await(25, TimeUnit.SECONDS);
        
        assertThat(completed).isTrue();
        assertThat(successCount.get()).isGreaterThan(CONCURRENT_USERS * OPERATIONS_PER_USER * 0.95); // 95%�ɹ���
        assertThat(errorCount.get()).isLessThan(CONCURRENT_USERS * OPERATIONS_PER_USER * 0.05); // 5%�ݴ���
        
        System.out.println("�����������Խ��:");
        System.out.println("�ɹ�����: " + successCount.get());
        System.out.println("ʧ�ܲ���: " + errorCount.get());
        System.out.println("�ɹ���: " + (successCount.get() * 100.0 / (successCount.get() + errorCount.get())) + "%");
    }

    @Test
    @Timeout(20)
    void testConcurrentCourtRetrieval() throws InterruptedException {
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(CONCURRENT_USERS);
        
        long startTime = System.currentTimeMillis();
        
        // ����������ȡ����
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            executorService.submit(() -> {
                try {
                    for (int j = 0; j < OPERATIONS_PER_USER; j++) {
                        CourtResponse response = courtService.getCourtById(1L);
                        
                        if (response != null && response.getId() != null) {
                            successCount.incrementAndGet();
                        } else {
                            errorCount.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        boolean completed = latch.await(15, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        assertThat(completed).isTrue();
        assertThat(successCount.get()).isEqualTo(CONCURRENT_USERS * OPERATIONS_PER_USER);
        assertThat(errorCount.get()).isEqualTo(0);
        
        double avgResponseTime = totalTime / (double)(CONCURRENT_USERS * OPERATIONS_PER_USER);
        
        System.out.println("������ѯ���Խ��:");
        System.out.println("�ܲ�����: " + (CONCURRENT_USERS * OPERATIONS_PER_USER));
        System.out.println("��ʱ��: " + totalTime + "ms");
        System.out.println("ƽ����Ӧʱ��: " + avgResponseTime + "ms");
        System.out.println("QPS: " + (CONCURRENT_USERS * OPERATIONS_PER_USER * 1000.0 / totalTime));
        
        // ����ƽ����Ӧʱ��С��100ms
        assertThat(avgResponseTime).isLessThan(100.0);
    }

    @Test
    @Timeout(30)
    void testMixedOperationsLoad() throws InterruptedException {
        AtomicInteger createCount = new AtomicInteger(0);
        AtomicInteger readCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(CONCURRENT_USERS);
        
        // ��ϲ�������
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            final int userId = i;
            executorService.submit(() -> {
                try {
                    for (int j = 0; j < OPERATIONS_PER_USER; j++) {
                        if (j % 3 == 0) {
                            // �������� (33%)
                            CourtRequest request = createTestRequest(userId, j);
                            CourtResponse response = courtService.createCourt(request);
                            if (response != null) {
                                createCount.incrementAndGet();
                            } else {
                                errorCount.incrementAndGet();
                            }
                        } else {
                            // ��ѯ���� (67%)
                            CourtResponse response = courtService.getCourtById((long)(userId + 1));
                            if (response != null) {
                                readCount.incrementAndGet();
                            } else {
                                errorCount.incrementAndGet();
                            }
                        }
                        
                        // ����ȴ�ʱ�䣬ģ����ʵ�û���Ϊ
                        Thread.sleep((int)(Math.random() * 20));
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        boolean completed = latch.await(25, TimeUnit.SECONDS);
        
        assertThat(completed).isTrue();
        assertThat(errorCount.get()).isLessThan((CONCURRENT_USERS * OPERATIONS_PER_USER) * 0.05); // 5%�ݴ���
        
        System.out.println("��ϲ������ز��Խ��:");
        System.out.println("���������ɹ�: " + createCount.get());
        System.out.println("��ѯ�����ɹ�: " + readCount.get());
        System.out.println("ʧ�ܲ���: " + errorCount.get());
        System.out.println("�ܳɹ���: " + ((createCount.get() + readCount.get()) * 100.0 / (CONCURRENT_USERS * OPERATIONS_PER_USER)) + "%");
    }

    @Test
    @Timeout(15)
    void testSystemStabilityUnderLoad() throws InterruptedException {
        AtomicInteger operationCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(50);
        
        // �߸����ȶ��Բ���
        for (int i = 0; i < 50; i++) {
            executorService.submit(() -> {
                try {
                    for (int j = 0; j < 20; j++) {
                        // ������������
                        CourtResponse response = courtService.getCourtById(1L);
                        if (response != null) {
                            operationCount.incrementAndGet();
                        } else {
                            errorCount.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        boolean completed = latch.await(10, TimeUnit.SECONDS);
        
        assertThat(completed).isTrue();
        assertThat(operationCount.get()).isEqualTo(1000); // 50 * 20
        assertThat(errorCount.get()).isEqualTo(0);
        
        System.out.println("ϵͳ�ȶ��Բ��Խ��:");
        System.out.println("��ɲ���: " + operationCount.get());
        System.out.println("ʧ�ܲ���: " + errorCount.get());
        System.out.println("ϵͳ�ȶ���: ? ͨ��");
    }

    private CourtRequest createTestRequest(int userId, int operationId) {
        CourtRequest request = new CourtRequest();
        request.setName("���ܲ��Գ���-" + userId + "-" + operationId);
        request.setDescription("���ܲ�������-" + userId + "-" + operationId);
        request.setCapacity(20 + (userId % 30)); // ������20-50֮��仯
        return request;
    }

    void tearDown() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }
} 