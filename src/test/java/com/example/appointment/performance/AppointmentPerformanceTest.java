package com.example.appointment.performance;

import com.example.appointment.dto.AppointmentRequest;
import com.example.appointment.dto.AppointmentResponse;
import com.example.appointment.service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AppointmentPerformanceTest {

    @Autowired
    private AppointmentService appointmentService;

    private List<AppointmentRequest> testRequests;

    @BeforeEach
    void setUp() {
        testRequests = new ArrayList<>();
        
        // 准备测试数据
        for (int i = 0; i < 100; i++) {
            AppointmentRequest request = new AppointmentRequest();
            request.setUserId((long) (i + 1));
            request.setVenueId((long) ((i % 5) + 1)); // 5个不同的场地
            request.setStartTime(LocalDateTime.now().plusHours(i + 1));
            request.setEndTime(LocalDateTime.now().plusHours(i + 2));
            request.setSportType("篮球");
            request.setNotes("性能测试预约 " + i);
            testRequests.add(request);
        }
    }

    @Test
    void testCreateAppointment_Performance() {
        // 测试创建预约的性能
        long startTime = System.currentTimeMillis();
        
        List<AppointmentResponse> responses = new ArrayList<>();
        for (AppointmentRequest request : testRequests) {
            try {
                AppointmentResponse response = appointmentService.createAppointment(request);
                responses.add(response);
            } catch (Exception e) {
                // 忽略时间冲突的异常
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("创建 " + responses.size() + " 个预约耗时: " + duration + "ms");
        System.out.println("平均每个预约耗时: " + (duration / (double) responses.size()) + "ms");
        
        // 性能断言：每个预约创建应该在100ms以内
        assertTrue(duration / (double) responses.size() < 100, 
                "平均创建时间超过100ms: " + (duration / (double) responses.size()) + "ms");
    }

    @Test
    void testConcurrentAppointmentCreation() throws InterruptedException {
        // 测试并发创建预约
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        long startTime = System.currentTimeMillis();
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < 10; j++) {
                    try {
                        AppointmentRequest request = new AppointmentRequest();
                        request.setUserId((long) (threadIndex * 10 + j + 1));
                        request.setVenueId((long) ((threadIndex % 5) + 1));
                        request.setStartTime(LocalDateTime.now().plusHours(threadIndex * 10 + j + 1));
                        request.setEndTime(LocalDateTime.now().plusHours(threadIndex * 10 + j + 2));
                        request.setSportType("篮球");
                        request.setNotes("并发测试预约 " + threadIndex + "-" + j);
                        
                        appointmentService.createAppointment(request);
                    } catch (Exception e) {
                        // 忽略时间冲突的异常
                    }
                }
            }, executor);
            
            futures.add(future);
        }
        
        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        
        System.out.println("并发创建预约耗时: " + duration + "ms");
        System.out.println("平均每个线程耗时: " + (duration / (double) threadCount) + "ms");
        
        // 性能断言：并发创建应该在5秒内完成
        assertTrue(duration < 5000, "并发创建时间超过5秒: " + duration + "ms");
    }

    @Test
    void testGetAppointment_Performance() {
        // 先创建一些预约
        List<Long> appointmentIds = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            try {
                AppointmentResponse response = appointmentService.createAppointment(testRequests.get(i));
                appointmentIds.add(response.getAppointmentId());
            } catch (Exception e) {
                // 忽略时间冲突的异常
            }
        }
        
        // 测试获取预约的性能
        long startTime = System.currentTimeMillis();
        
        for (Long appointmentId : appointmentIds) {
            appointmentService.getAppointment(appointmentId);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("获取 " + appointmentIds.size() + " 个预约耗时: " + duration + "ms");
        System.out.println("平均每个获取耗时: " + (duration / (double) appointmentIds.size()) + "ms");
        
        // 性能断言：每个预约获取应该在50ms以内
        assertTrue(duration / (double) appointmentIds.size() < 50, 
                "平均获取时间超过50ms: " + (duration / (double) appointmentIds.size()) + "ms");
    }

    @Test
    void testUpdateAppointment_Performance() {
        // 先创建一些预约
        List<Long> appointmentIds = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            try {
                AppointmentResponse response = appointmentService.createAppointment(testRequests.get(i));
                appointmentIds.add(response.getAppointmentId());
            } catch (Exception e) {
                // 忽略时间冲突的异常
            }
        }
        
        // 测试更新预约的性能
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < appointmentIds.size(); i++) {
            try {
                AppointmentRequest updateRequest = new AppointmentRequest();
                updateRequest.setUserId(1L);
                updateRequest.setVenueId(1L);
                updateRequest.setStartTime(LocalDateTime.now().plusHours(i + 10));
                updateRequest.setEndTime(LocalDateTime.now().plusHours(i + 11));
                updateRequest.setSportType("足球");
                updateRequest.setNotes("性能测试更新 " + i);
                
                appointmentService.updateAppointment(appointmentIds.get(i), updateRequest);
            } catch (Exception e) {
                // 忽略更新失败的异常
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("更新 " + appointmentIds.size() + " 个预约耗时: " + duration + "ms");
        System.out.println("平均每个更新耗时: " + (duration / (double) appointmentIds.size()) + "ms");
        
        // 性能断言：每个预约更新应该在100ms以内
        assertTrue(duration / (double) appointmentIds.size() < 100, 
                "平均更新时间超过100ms: " + (duration / (double) appointmentIds.size()) + "ms");
    }

    @Test
    void testGetAppointmentStatistics_Performance() {
        // 先创建一些不同状态的预约
        createAppointmentsForStatistics();
        
        // 测试获取统计信息的性能
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 100; i++) {
            appointmentService.getAppointmentStatistics();
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("获取100次统计信息耗时: " + duration + "ms");
        System.out.println("平均每次获取统计耗时: " + (duration / 100.0) + "ms");
        
        // 性能断言：每次统计获取应该在20ms以内
        assertTrue(duration / 100.0 < 20, 
                "平均统计获取时间超过20ms: " + (duration / 100.0) + "ms");
    }

    private void createAppointmentsForStatistics() {
        // 创建不同状态的预约用于统计测试
        for (int i = 0; i < 20; i++) {
            try {
                AppointmentRequest request = new AppointmentRequest();
                request.setUserId((long) (i + 1));
                request.setVenueId((long) ((i % 3) + 1));
                request.setStartTime(LocalDateTime.now().plusHours(i + 1));
                request.setEndTime(LocalDateTime.now().plusHours(i + 2));
                request.setSportType("篮球");
                request.setNotes("统计性能测试 " + i);
                
                AppointmentResponse response = appointmentService.createAppointment(request);
                
                // 根据索引设置不同状态
                switch (i % 4) {
                    case 0:
                        // PENDING - 保持默认状态
                        break;
                    case 1:
                        appointmentService.confirmAppointment(response.getAppointmentId());
                        break;
                    case 2:
                        appointmentService.confirmAppointment(response.getAppointmentId());
                        appointmentService.completeAppointment(response.getAppointmentId());
                        break;
                    case 3:
                        appointmentService.cancelAppointment(response.getAppointmentId());
                        break;
                }
            } catch (Exception e) {
                // 忽略创建失败的异常
            }
        }
    }
} 