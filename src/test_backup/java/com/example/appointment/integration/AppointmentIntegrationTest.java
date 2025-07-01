package com.example.appointment.integration;

import com.example.appointment.dto.AppointmentRequest;
import com.example.appointment.dto.AppointmentResponse;
import com.example.appointment.entity.Appointment;
import com.example.appointment.repository.AppointmentRepository;
import com.example.appointment.service.AppointmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class AppointmentIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testCreateAppointment_Integration() {
        // Given
        AppointmentRequest request = new AppointmentRequest();
        request.setUserId(1L);
        request.setVenueId(1L);
        request.setStartTime(LocalDateTime.now().plusHours(1));
        request.setEndTime(LocalDateTime.now().plusHours(2));
        request.setSportType("篮球");
        request.setNotes("集成测试预约");

        // When
        AppointmentResponse response = appointmentService.createAppointment(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getAppointmentId());
        assertEquals("PENDING", response.getStatus());
        assertEquals("篮球", response.getSportType());

        // 验证数据库中的数据
        Appointment savedAppointment = appointmentRepository.findById(response.getAppointmentId()).orElse(null);
        assertNotNull(savedAppointment);
        assertEquals(1L, savedAppointment.getUserId());
        assertEquals(1L, savedAppointment.getVenueId());
        assertEquals("篮球", savedAppointment.getSportType());
    }

    @Test
    void testGetAppointment_Integration() {
        // Given - 先创建一个预约
        AppointmentRequest request = new AppointmentRequest();
        request.setUserId(1L);
        request.setVenueId(1L);
        request.setStartTime(LocalDateTime.now().plusHours(1));
        request.setEndTime(LocalDateTime.now().plusHours(2));
        request.setSportType("足球");
        request.setNotes("获取预约测试");

        AppointmentResponse createdResponse = appointmentService.createAppointment(request);
        Long appointmentId = createdResponse.getAppointmentId();

        // When
        AppointmentResponse response = appointmentService.getAppointment(appointmentId);

        // Then
        assertNotNull(response);
        assertEquals(appointmentId, response.getAppointmentId());
        assertEquals(1L, response.getUserId());
        assertEquals(1L, response.getVenueId());
        assertEquals("足球", response.getSportType());
        assertEquals("PENDING", response.getStatus());
    }

    @Test
    void testUpdateAppointment_Integration() {
        // Given - 先创建一个预约
        AppointmentRequest createRequest = new AppointmentRequest();
        createRequest.setUserId(1L);
        createRequest.setVenueId(1L);
        createRequest.setStartTime(LocalDateTime.now().plusHours(1));
        createRequest.setEndTime(LocalDateTime.now().plusHours(2));
        createRequest.setSportType("篮球");
        createRequest.setNotes("原始预约");

        AppointmentResponse createdResponse = appointmentService.createAppointment(createRequest);
        Long appointmentId = createdResponse.getAppointmentId();

        // 创建更新请求
        AppointmentRequest updateRequest = new AppointmentRequest();
        updateRequest.setUserId(1L);
        updateRequest.setVenueId(1L);
        updateRequest.setStartTime(LocalDateTime.now().plusHours(2));
        updateRequest.setEndTime(LocalDateTime.now().plusHours(3));
        updateRequest.setSportType("足球");
        updateRequest.setNotes("更新后的预约");

        // When
        AppointmentResponse response = appointmentService.updateAppointment(appointmentId, updateRequest);

        // Then
        assertNotNull(response);
        assertEquals(appointmentId, response.getAppointmentId());
        assertEquals("足球", response.getSportType());
        assertEquals("更新后的预约", response.getNotes());

        // 验证数据库中的数据已更新
        Appointment updatedAppointment = appointmentRepository.findById(appointmentId).orElse(null);
        assertNotNull(updatedAppointment);
        assertEquals("足球", updatedAppointment.getSportType());
        assertEquals("更新后的预约", updatedAppointment.getNotes());
    }

    @Test
    void testCancelAppointment_Integration() {
        // Given - 先创建一个预约
        AppointmentRequest request = new AppointmentRequest();
        request.setUserId(1L);
        request.setVenueId(1L);
        request.setStartTime(LocalDateTime.now().plusHours(1));
        request.setEndTime(LocalDateTime.now().plusHours(2));
        request.setSportType("篮球");
        request.setNotes("取消预约测试");

        AppointmentResponse createdResponse = appointmentService.createAppointment(request);
        Long appointmentId = createdResponse.getAppointmentId();

        // When
        AppointmentResponse response = appointmentService.cancelAppointment(appointmentId);

        // Then
        assertNotNull(response);
        assertEquals(appointmentId, response.getAppointmentId());
        assertEquals("CANCELLED", response.getStatus());

        // 验证数据库中的状态已更新
        Appointment cancelledAppointment = appointmentRepository.findById(appointmentId).orElse(null);
        assertNotNull(cancelledAppointment);
        assertEquals("CANCELLED", cancelledAppointment.getStatus());
    }

    @Test
    void testConfirmAppointment_Integration() {
        // Given - 先创建一个预约
        AppointmentRequest request = new AppointmentRequest();
        request.setUserId(1L);
        request.setVenueId(1L);
        request.setStartTime(LocalDateTime.now().plusHours(1));
        request.setEndTime(LocalDateTime.now().plusHours(2));
        request.setSportType("篮球");
        request.setNotes("确认预约测试");

        AppointmentResponse createdResponse = appointmentService.createAppointment(request);
        Long appointmentId = createdResponse.getAppointmentId();

        // When
        AppointmentResponse response = appointmentService.confirmAppointment(appointmentId);

        // Then
        assertNotNull(response);
        assertEquals(appointmentId, response.getAppointmentId());
        assertEquals("CONFIRMED", response.getStatus());

        // 验证数据库中的状态已更新
        Appointment confirmedAppointment = appointmentRepository.findById(appointmentId).orElse(null);
        assertNotNull(confirmedAppointment);
        assertEquals("CONFIRMED", confirmedAppointment.getStatus());
    }

    @Test
    void testCompleteAppointment_Integration() {
        // Given - 先创建一个预约并确认
        AppointmentRequest request = new AppointmentRequest();
        request.setUserId(1L);
        request.setVenueId(1L);
        request.setStartTime(LocalDateTime.now().plusHours(1));
        request.setEndTime(LocalDateTime.now().plusHours(2));
        request.setSportType("篮球");
        request.setNotes("完成预约测试");

        AppointmentResponse createdResponse = appointmentService.createAppointment(request);
        Long appointmentId = createdResponse.getAppointmentId();

        // 先确认预约
        appointmentService.confirmAppointment(appointmentId);

        // When - 完成预约
        AppointmentResponse response = appointmentService.completeAppointment(appointmentId);

        // Then
        assertNotNull(response);
        assertEquals(appointmentId, response.getAppointmentId());
        assertEquals("COMPLETED", response.getStatus());

        // 验证数据库中的状态已更新
        Appointment completedAppointment = appointmentRepository.findById(appointmentId).orElse(null);
        assertNotNull(completedAppointment);
        assertEquals("COMPLETED", completedAppointment.getStatus());
    }

    @Test
    void testTimeConflict_Integration() {
        // Given - 先创建一个预约
        AppointmentRequest firstRequest = new AppointmentRequest();
        firstRequest.setUserId(1L);
        firstRequest.setVenueId(1L);
        firstRequest.setStartTime(LocalDateTime.now().plusHours(1));
        firstRequest.setEndTime(LocalDateTime.now().plusHours(2));
        firstRequest.setSportType("篮球");
        firstRequest.setNotes("第一个预约");

        appointmentService.createAppointment(firstRequest);

        // 创建时间冲突的第二个预约
        AppointmentRequest secondRequest = new AppointmentRequest();
        secondRequest.setUserId(2L);
        secondRequest.setVenueId(1L); // 同一个场地
        secondRequest.setStartTime(LocalDateTime.now().plusHours(1).plusMinutes(30)); // 时间冲突
        secondRequest.setEndTime(LocalDateTime.now().plusHours(2).plusMinutes(30));
        secondRequest.setSportType("足球");
        secondRequest.setNotes("冲突的预约");

        // When & Then - 应该抛出异常
        assertThrows(RuntimeException.class, () -> {
            appointmentService.createAppointment(secondRequest);
        });
    }

    @Test
    void testGetAppointmentStatistics_Integration() {
        // Given - 创建多个不同状态的预约
        createAppointmentWithStatus("PENDING");
        createAppointmentWithStatus("PENDING");
        createAppointmentWithStatus("CONFIRMED");
        createAppointmentWithStatus("CONFIRMED");
        createAppointmentWithStatus("CONFIRMED");
        createAppointmentWithStatus("COMPLETED");
        createAppointmentWithStatus("CANCELLED");

        // When
        var statistics = appointmentService.getAppointmentStatistics();

        // Then
        assertNotNull(statistics);
        assertTrue(statistics.get("PENDING") >= 2L);
        assertTrue(statistics.get("CONFIRMED") >= 3L);
        assertTrue(statistics.get("COMPLETED") >= 1L);
        assertTrue(statistics.get("CANCELLED") >= 1L);
    }

    private void createAppointmentWithStatus(String status) {
        AppointmentRequest request = new AppointmentRequest();
        request.setUserId(1L);
        request.setVenueId(1L);
        request.setStartTime(LocalDateTime.now().plusHours(1));
        request.setEndTime(LocalDateTime.now().plusHours(2));
        request.setSportType("篮球");
        request.setNotes("统计测试预约");

        AppointmentResponse response = appointmentService.createAppointment(request);
        Long appointmentId = response.getAppointmentId();

        // 根据状态更新预约
        switch (status) {
            case "CONFIRMED":
                appointmentService.confirmAppointment(appointmentId);
                break;
            case "COMPLETED":
                appointmentService.confirmAppointment(appointmentId);
                appointmentService.completeAppointment(appointmentId);
                break;
            case "CANCELLED":
                appointmentService.cancelAppointment(appointmentId);
                break;
            default:
                // PENDING状态不需要额外操作
                break;
        }
    }
} 