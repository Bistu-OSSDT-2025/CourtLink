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
        request.setSportType("����");
        request.setNotes("���ɲ���ԤԼ");

        // When
        AppointmentResponse response = appointmentService.createAppointment(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getAppointmentId());
        assertEquals("PENDING", response.getStatus());
        assertEquals("����", response.getSportType());

        // ��֤���ݿ��е�����
        Appointment savedAppointment = appointmentRepository.findById(response.getAppointmentId()).orElse(null);
        assertNotNull(savedAppointment);
        assertEquals(1L, savedAppointment.getUserId());
        assertEquals(1L, savedAppointment.getVenueId());
        assertEquals("����", savedAppointment.getSportType());
    }

    @Test
    void testGetAppointment_Integration() {
        // Given - �ȴ���һ��ԤԼ
        AppointmentRequest request = new AppointmentRequest();
        request.setUserId(1L);
        request.setVenueId(1L);
        request.setStartTime(LocalDateTime.now().plusHours(1));
        request.setEndTime(LocalDateTime.now().plusHours(2));
        request.setSportType("����");
        request.setNotes("��ȡԤԼ����");

        AppointmentResponse createdResponse = appointmentService.createAppointment(request);
        Long appointmentId = createdResponse.getAppointmentId();

        // When
        AppointmentResponse response = appointmentService.getAppointment(appointmentId);

        // Then
        assertNotNull(response);
        assertEquals(appointmentId, response.getAppointmentId());
        assertEquals(1L, response.getUserId());
        assertEquals(1L, response.getVenueId());
        assertEquals("����", response.getSportType());
        assertEquals("PENDING", response.getStatus());
    }

    @Test
    void testUpdateAppointment_Integration() {
        // Given - �ȴ���һ��ԤԼ
        AppointmentRequest createRequest = new AppointmentRequest();
        createRequest.setUserId(1L);
        createRequest.setVenueId(1L);
        createRequest.setStartTime(LocalDateTime.now().plusHours(1));
        createRequest.setEndTime(LocalDateTime.now().plusHours(2));
        createRequest.setSportType("����");
        createRequest.setNotes("ԭʼԤԼ");

        AppointmentResponse createdResponse = appointmentService.createAppointment(createRequest);
        Long appointmentId = createdResponse.getAppointmentId();

        // ������������
        AppointmentRequest updateRequest = new AppointmentRequest();
        updateRequest.setUserId(1L);
        updateRequest.setVenueId(1L);
        updateRequest.setStartTime(LocalDateTime.now().plusHours(2));
        updateRequest.setEndTime(LocalDateTime.now().plusHours(3));
        updateRequest.setSportType("����");
        updateRequest.setNotes("���º��ԤԼ");

        // When
        AppointmentResponse response = appointmentService.updateAppointment(appointmentId, updateRequest);

        // Then
        assertNotNull(response);
        assertEquals(appointmentId, response.getAppointmentId());
        assertEquals("����", response.getSportType());
        assertEquals("���º��ԤԼ", response.getNotes());

        // ��֤���ݿ��е������Ѹ���
        Appointment updatedAppointment = appointmentRepository.findById(appointmentId).orElse(null);
        assertNotNull(updatedAppointment);
        assertEquals("����", updatedAppointment.getSportType());
        assertEquals("���º��ԤԼ", updatedAppointment.getNotes());
    }

    @Test
    void testCancelAppointment_Integration() {
        // Given - �ȴ���һ��ԤԼ
        AppointmentRequest request = new AppointmentRequest();
        request.setUserId(1L);
        request.setVenueId(1L);
        request.setStartTime(LocalDateTime.now().plusHours(1));
        request.setEndTime(LocalDateTime.now().plusHours(2));
        request.setSportType("����");
        request.setNotes("ȡ��ԤԼ����");

        AppointmentResponse createdResponse = appointmentService.createAppointment(request);
        Long appointmentId = createdResponse.getAppointmentId();

        // When
        AppointmentResponse response = appointmentService.cancelAppointment(appointmentId);

        // Then
        assertNotNull(response);
        assertEquals(appointmentId, response.getAppointmentId());
        assertEquals("CANCELLED", response.getStatus());

        // ��֤���ݿ��е�״̬�Ѹ���
        Appointment cancelledAppointment = appointmentRepository.findById(appointmentId).orElse(null);
        assertNotNull(cancelledAppointment);
        assertEquals("CANCELLED", cancelledAppointment.getStatus());
    }

    @Test
    void testConfirmAppointment_Integration() {
        // Given - �ȴ���һ��ԤԼ
        AppointmentRequest request = new AppointmentRequest();
        request.setUserId(1L);
        request.setVenueId(1L);
        request.setStartTime(LocalDateTime.now().plusHours(1));
        request.setEndTime(LocalDateTime.now().plusHours(2));
        request.setSportType("����");
        request.setNotes("ȷ��ԤԼ����");

        AppointmentResponse createdResponse = appointmentService.createAppointment(request);
        Long appointmentId = createdResponse.getAppointmentId();

        // When
        AppointmentResponse response = appointmentService.confirmAppointment(appointmentId);

        // Then
        assertNotNull(response);
        assertEquals(appointmentId, response.getAppointmentId());
        assertEquals("CONFIRMED", response.getStatus());

        // ��֤���ݿ��е�״̬�Ѹ���
        Appointment confirmedAppointment = appointmentRepository.findById(appointmentId).orElse(null);
        assertNotNull(confirmedAppointment);
        assertEquals("CONFIRMED", confirmedAppointment.getStatus());
    }

    @Test
    void testCompleteAppointment_Integration() {
        // Given - �ȴ���һ��ԤԼ��ȷ��
        AppointmentRequest request = new AppointmentRequest();
        request.setUserId(1L);
        request.setVenueId(1L);
        request.setStartTime(LocalDateTime.now().plusHours(1));
        request.setEndTime(LocalDateTime.now().plusHours(2));
        request.setSportType("����");
        request.setNotes("���ԤԼ����");

        AppointmentResponse createdResponse = appointmentService.createAppointment(request);
        Long appointmentId = createdResponse.getAppointmentId();

        // ��ȷ��ԤԼ
        appointmentService.confirmAppointment(appointmentId);

        // When - ���ԤԼ
        AppointmentResponse response = appointmentService.completeAppointment(appointmentId);

        // Then
        assertNotNull(response);
        assertEquals(appointmentId, response.getAppointmentId());
        assertEquals("COMPLETED", response.getStatus());

        // ��֤���ݿ��е�״̬�Ѹ���
        Appointment completedAppointment = appointmentRepository.findById(appointmentId).orElse(null);
        assertNotNull(completedAppointment);
        assertEquals("COMPLETED", completedAppointment.getStatus());
    }

    @Test
    void testTimeConflict_Integration() {
        // Given - �ȴ���һ��ԤԼ
        AppointmentRequest firstRequest = new AppointmentRequest();
        firstRequest.setUserId(1L);
        firstRequest.setVenueId(1L);
        firstRequest.setStartTime(LocalDateTime.now().plusHours(1));
        firstRequest.setEndTime(LocalDateTime.now().plusHours(2));
        firstRequest.setSportType("����");
        firstRequest.setNotes("��һ��ԤԼ");

        appointmentService.createAppointment(firstRequest);

        // ����ʱ���ͻ�ĵڶ���ԤԼ
        AppointmentRequest secondRequest = new AppointmentRequest();
        secondRequest.setUserId(2L);
        secondRequest.setVenueId(1L); // ͬһ������
        secondRequest.setStartTime(LocalDateTime.now().plusHours(1).plusMinutes(30)); // ʱ���ͻ
        secondRequest.setEndTime(LocalDateTime.now().plusHours(2).plusMinutes(30));
        secondRequest.setSportType("����");
        secondRequest.setNotes("��ͻ��ԤԼ");

        // When & Then - Ӧ���׳��쳣
        assertThrows(RuntimeException.class, () -> {
            appointmentService.createAppointment(secondRequest);
        });
    }

    @Test
    void testGetAppointmentStatistics_Integration() {
        // Given - ���������ͬ״̬��ԤԼ
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
        request.setSportType("����");
        request.setNotes("ͳ�Ʋ���ԤԼ");

        AppointmentResponse response = appointmentService.createAppointment(request);
        Long appointmentId = response.getAppointmentId();

        // ����״̬����ԤԼ
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
                // PENDING״̬����Ҫ�������
                break;
        }
    }
} 