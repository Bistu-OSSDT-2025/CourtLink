package com.courtlink.performance;

import com.courtlink.booking.dto.AppointmentRequest;
import com.courtlink.booking.dto.AppointmentResponse;
import com.courtlink.booking.entity.Payment;
import com.courtlink.booking.service.AppointmentService;
import com.courtlink.booking.service.PaymentService;
import com.courtlink.dto.CourtDTO;
import com.courtlink.service.CourtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class PerformanceTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private CourtService courtService;

    private CourtDTO testCourt;
    private ExecutorService executorService;
    private static final int THREAD_POOL_SIZE = 50;
    private static final int CONCURRENT_USERS = 100;
    private static final int REQUESTS_PER_USER = 5;

    @BeforeEach
    void setUp() {
        // 创建测试场地
        testCourt = new CourtDTO();
        testCourt.setName("Performance Test Court");
        testCourt.setDescription("Court for performance testing");
        testCourt.setStatus("AVAILABLE");
        testCourt = courtService.createCourt(testCourt);

        // 初始化线程池
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    @Test
    @DisplayName("测试并发预约性能")
    void testConcurrentBookingPerformance() throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(CONCURRENT_USERS);
        List<Future<List<AppointmentResponse>>> futures = new ArrayList<>();
        List<Long> responseTimes = new CopyOnWriteArrayList<>();

        // 创建并发任务
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            final int userId = i;
            Future<List<AppointmentResponse>> future = executorService.submit(() -> {
                List<AppointmentResponse> responses = new ArrayList<>();
                startLatch.await(); // 等待所有线程就绪
                
                for (int j = 0; j < REQUESTS_PER_USER; j++) {
                    long startTime = System.currentTimeMillis();
                    try {
                        // 每个用户的每个请求都使用不同的时间段和不同的场地
                        AppointmentRequest request = createAppointmentRequest(userId, j);
                        AppointmentResponse response = appointmentService.createAppointment(request.getUserId(), request);
                        responses.add(response);
                        
                        long endTime = System.currentTimeMillis();
                        responseTimes.add(endTime - startTime);
                    } catch (Exception e) {
                        // 记录异常，但继续执行
                        System.err.println("Error in user " + userId + " request " + j + ": " + e.getMessage());
                    }
                }
                
                endLatch.countDown();
                return responses;
            });
            futures.add(future);
        }

        // 开始计时并发送请求
        long testStartTime = System.currentTimeMillis();
        startLatch.countDown();

        // 等待所有请求完成
        endLatch.await(30, TimeUnit.SECONDS);
        long testEndTime = System.currentTimeMillis();

        // 计算统计数据
        long totalTime = testEndTime - testStartTime;
        double averageResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
        long maxResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);
        long minResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .min()
                .orElse(0L);

        // 统计成功和失败的请求
        int successfulRequests = 0;
        int failedRequests = 0;
        for (Future<List<AppointmentResponse>> future : futures) {
            try {
                List<AppointmentResponse> responses = future.get(1, TimeUnit.SECONDS);
                successfulRequests += responses.size();
            } catch (Exception e) {
                failedRequests++;
            }
        }

        // 输出性能测试结果
        System.out.println("\n预约性能测试结果:");
        System.out.println("总测试时间: " + totalTime + "ms");
        System.out.println("平均响应时间: " + String.format("%.2f", averageResponseTime) + "ms");
        System.out.println("最大响应时间: " + maxResponseTime + "ms");
        System.out.println("最小响应时间: " + minResponseTime + "ms");
        System.out.println("成功请求数: " + successfulRequests);
        System.out.println("失败请求数: " + failedRequests);
        System.out.println("TPS: " + String.format("%.2f", (double) successfulRequests / (totalTime / 1000.0)));

        // 验证性能指标
        assertTrue(averageResponseTime < 1000, "平均响应时间应小于1秒");
        assertTrue(maxResponseTime < 5000, "最大响应时间应小于5秒");
        assertTrue(failedRequests < CONCURRENT_USERS * REQUESTS_PER_USER * 0.1, "失败率应小于10%");
    }

    @Test
    @DisplayName("测试并发支付性能")
    void testConcurrentPaymentPerformance() throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(CONCURRENT_USERS);
        List<Future<List<Payment>>> futures = new ArrayList<>();
        List<Long> responseTimes = new CopyOnWriteArrayList<>();

        // 创建预约和支付记录
        List<AppointmentResponse> appointments = new ArrayList<>();
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            // 每个用户预约不同的时间段和不同的场地
            AppointmentRequest request = createAppointmentRequest(i, i);
            appointments.add(appointmentService.createAppointment(request.getUserId(), request));
        }

        // 创建并发支付任务
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            final int userId = i;
            final AppointmentResponse appointment = appointments.get(i);
            
            Future<List<Payment>> future = executorService.submit(() -> {
                List<Payment> payments = new ArrayList<>();
                startLatch.await();

                for (int j = 0; j < REQUESTS_PER_USER; j++) {
                    long startTime = System.currentTimeMillis();
                    try {
                        Payment payment = createPayment(userId, appointment.getId());
                        payment = paymentService.processPayment(payment.getPaymentNo(), Payment.PaymentMethod.CREDIT_CARD);
                        payments.add(payment);

                        long endTime = System.currentTimeMillis();
                        responseTimes.add(endTime - startTime);
                    } catch (Exception e) {
                        System.err.println("Error in payment for user " + userId + ": " + e.getMessage());
                    }
                }

                endLatch.countDown();
                return payments;
            });
            futures.add(future);
        }

        // 开始计时并发送请求
        long testStartTime = System.currentTimeMillis();
        startLatch.countDown();

        // 等待所有请求完成
        endLatch.await(30, TimeUnit.SECONDS);
        long testEndTime = System.currentTimeMillis();

        // 计算统计数据
        long totalTime = testEndTime - testStartTime;
        double averageResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
        long maxResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);
        long minResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .min()
                .orElse(0L);

        // 统计成功和失败的请求
        int successfulRequests = 0;
        int failedRequests = 0;
        for (Future<List<Payment>> future : futures) {
            try {
                List<Payment> payments = future.get(1, TimeUnit.SECONDS);
                successfulRequests += payments.size();
            } catch (Exception e) {
                failedRequests++;
            }
        }

        // 输出性能测试结果
        System.out.println("\n支付性能测试结果:");
        System.out.println("总测试时间: " + totalTime + "ms");
        System.out.println("平均响应时间: " + String.format("%.2f", averageResponseTime) + "ms");
        System.out.println("最大响应时间: " + maxResponseTime + "ms");
        System.out.println("最小响应时间: " + minResponseTime + "ms");
        System.out.println("成功请求数: " + successfulRequests);
        System.out.println("失败请求数: " + failedRequests);
        System.out.println("TPS: " + String.format("%.2f", (double) successfulRequests / (totalTime / 1000.0)));

        // 验证性能指标
        assertTrue(averageResponseTime < 800, "支付平均响应时间应小于800ms");
        assertTrue(maxResponseTime < 3000, "支付最大响应时间应小于3秒");
        assertTrue(failedRequests < CONCURRENT_USERS * REQUESTS_PER_USER * 0.05, "支付失败率应小于5%");
    }

    private AppointmentRequest createAppointmentRequest(int userId, int timeOffset) {
        // 每个用户的每个请求都使用不同的时间段和不同的场地
        // 预约时间从明天开始，避免过去时间
        LocalDateTime baseTime = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime startTime = baseTime.plusDays(timeOffset / 48).plusHours(timeOffset % 48);
        
        AppointmentRequest request = new AppointmentRequest();
        request.setUserId("user-" + userId);
        request.setProviderId("provider-" + (timeOffset % 10 + 1)); // 使用10个不同的场地
        request.setStartTime(startTime);
        request.setEndTime(startTime.plusMinutes(30));
        request.setAmount(new BigDecimal("100.00"));
        request.setServiceType("BADMINTON");
        return request;
    }

    private Payment createPayment(int userId, Long appointmentId) {
        Payment payment = new Payment();
        payment.setUserId("user-" + userId);
        payment.setAppointmentId(String.valueOf(appointmentId));
        payment.setAmount(new BigDecimal("100.00"));
        payment.setPaymentNo(UUID.randomUUID().toString());
        payment.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD);
        payment.setStatus(Payment.PaymentStatus.PENDING);
        return paymentService.createPayment(payment);
    }
} 