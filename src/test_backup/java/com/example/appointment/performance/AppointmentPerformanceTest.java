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
        
        // ׼����������
        for (int i = 0; i < 100; i++) {
            AppointmentRequest request = new AppointmentRequest();
            request.setUserId((long) (i + 1));
            request.setVenueId((long) ((i % 5) + 1)); // 5����ͬ�ĳ���
            request.setStartTime(LocalDateTime.now().plusHours(i + 1));
            request.setEndTime(LocalDateTime.now().plusHours(i + 2));
            request.setSportType("����");
            request.setNotes("���ܲ���ԤԼ " + i);
            testRequests.add(request);
        }
    }

    @Test
    void testCreateAppointment_Performance() {
        // ���Դ���ԤԼ������
        long startTime = System.currentTimeMillis();
        
        List<AppointmentResponse> responses = new ArrayList<>();
        for (AppointmentRequest request : testRequests) {
            try {
                AppointmentResponse response = appointmentService.createAppointment(request);
                responses.add(response);
            } catch (Exception e) {
                // ����ʱ���ͻ���쳣
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("���� " + responses.size() + " ��ԤԼ��ʱ: " + duration + "ms");
        System.out.println("ƽ��ÿ��ԤԼ��ʱ: " + (duration / (double) responses.size()) + "ms");
        
        // ���ܶ��ԣ�ÿ��ԤԼ����Ӧ����100ms����
        assertTrue(duration / (double) responses.size() < 100, 
                "ƽ������ʱ�䳬��100ms: " + (duration / (double) responses.size()) + "ms");
    }

    @Test
    void testConcurrentAppointmentCreation() throws InterruptedException {
        // ���Բ�������ԤԼ
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
                        request.setSportType("����");
                        request.setNotes("��������ԤԼ " + threadIndex + "-" + j);
                        
                        appointmentService.createAppointment(request);
                    } catch (Exception e) {
                        // ����ʱ���ͻ���쳣
                    }
                }
            }, executor);
            
            futures.add(future);
        }
        
        // �ȴ������������
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        
        System.out.println("��������ԤԼ��ʱ: " + duration + "ms");
        System.out.println("ƽ��ÿ���̺߳�ʱ: " + (duration / (double) threadCount) + "ms");
        
        // ���ܶ��ԣ���������Ӧ����5�������
        assertTrue(duration < 5000, "��������ʱ�䳬��5��: " + duration + "ms");
    }

    @Test
    void testGetAppointment_Performance() {
        // �ȴ���һЩԤԼ
        List<Long> appointmentIds = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            try {
                AppointmentResponse response = appointmentService.createAppointment(testRequests.get(i));
                appointmentIds.add(response.getAppointmentId());
            } catch (Exception e) {
                // ����ʱ���ͻ���쳣
            }
        }
        
        // ���Ի�ȡԤԼ������
        long startTime = System.currentTimeMillis();
        
        for (Long appointmentId : appointmentIds) {
            appointmentService.getAppointment(appointmentId);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("��ȡ " + appointmentIds.size() + " ��ԤԼ��ʱ: " + duration + "ms");
        System.out.println("ƽ��ÿ����ȡ��ʱ: " + (duration / (double) appointmentIds.size()) + "ms");
        
        // ���ܶ��ԣ�ÿ��ԤԼ��ȡӦ����50ms����
        assertTrue(duration / (double) appointmentIds.size() < 50, 
                "ƽ����ȡʱ�䳬��50ms: " + (duration / (double) appointmentIds.size()) + "ms");
    }

    @Test
    void testUpdateAppointment_Performance() {
        // �ȴ���һЩԤԼ
        List<Long> appointmentIds = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            try {
                AppointmentResponse response = appointmentService.createAppointment(testRequests.get(i));
                appointmentIds.add(response.getAppointmentId());
            } catch (Exception e) {
                // ����ʱ���ͻ���쳣
            }
        }
        
        // ���Ը���ԤԼ������
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < appointmentIds.size(); i++) {
            try {
                AppointmentRequest updateRequest = new AppointmentRequest();
                updateRequest.setUserId(1L);
                updateRequest.setVenueId(1L);
                updateRequest.setStartTime(LocalDateTime.now().plusHours(i + 10));
                updateRequest.setEndTime(LocalDateTime.now().plusHours(i + 11));
                updateRequest.setSportType("����");
                updateRequest.setNotes("���ܲ��Ը��� " + i);
                
                appointmentService.updateAppointment(appointmentIds.get(i), updateRequest);
            } catch (Exception e) {
                // ���Ը���ʧ�ܵ��쳣
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("���� " + appointmentIds.size() + " ��ԤԼ��ʱ: " + duration + "ms");
        System.out.println("ƽ��ÿ�����º�ʱ: " + (duration / (double) appointmentIds.size()) + "ms");
        
        // ���ܶ��ԣ�ÿ��ԤԼ����Ӧ����100ms����
        assertTrue(duration / (double) appointmentIds.size() < 100, 
                "ƽ������ʱ�䳬��100ms: " + (duration / (double) appointmentIds.size()) + "ms");
    }

    @Test
    void testGetAppointmentStatistics_Performance() {
        // �ȴ���һЩ��ͬ״̬��ԤԼ
        createAppointmentsForStatistics();
        
        // ���Ի�ȡͳ����Ϣ������
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 100; i++) {
            appointmentService.getAppointmentStatistics();
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("��ȡ100��ͳ����Ϣ��ʱ: " + duration + "ms");
        System.out.println("ƽ��ÿ�λ�ȡͳ�ƺ�ʱ: " + (duration / 100.0) + "ms");
        
        // ���ܶ��ԣ�ÿ��ͳ�ƻ�ȡӦ����20ms����
        assertTrue(duration / 100.0 < 20, 
                "ƽ��ͳ�ƻ�ȡʱ�䳬��20ms: " + (duration / 100.0) + "ms");
    }

    private void createAppointmentsForStatistics() {
        // ������ͬ״̬��ԤԼ����ͳ�Ʋ���
        for (int i = 0; i < 20; i++) {
            try {
                AppointmentRequest request = new AppointmentRequest();
                request.setUserId((long) (i + 1));
                request.setVenueId((long) ((i % 3) + 1));
                request.setStartTime(LocalDateTime.now().plusHours(i + 1));
                request.setEndTime(LocalDateTime.now().plusHours(i + 2));
                request.setSportType("����");
                request.setNotes("ͳ�����ܲ��� " + i);
                
                AppointmentResponse response = appointmentService.createAppointment(request);
                
                // �����������ò�ͬ״̬
                switch (i % 4) {
                    case 0:
                        // PENDING - ����Ĭ��״̬
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
                // ���Դ���ʧ�ܵ��쳣
            }
        }
    }
} 