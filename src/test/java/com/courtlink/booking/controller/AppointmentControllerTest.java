package com.courtlink.booking.controller;

import com.courtlink.booking.ApiResponse;
import com.courtlink.booking.BookingApplication;
import com.courtlink.booking.dto.AppointmentRequest;
import com.courtlink.booking.dto.AppointmentResponse;
import com.courtlink.booking.entity.Appointment;
import com.courtlink.booking.service.AppointmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AppointmentController Integration Tests
 * 
 * @author CourtLink Team
 * @version 1.0.0
 */
@SpringBootTest(classes = BookingApplication.class)
@AutoConfigureMockMvc
@DisplayName("Appointment Controller Tests")
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentService appointmentService;

    @Autowired
    private ObjectMapper objectMapper;

    private AppointmentRequest appointmentRequest;
    private AppointmentResponse appointmentResponse;

    @BeforeEach
    void setUp() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(1);

        appointmentRequest = new AppointmentRequest();
        appointmentRequest.setUserId("test123");
        appointmentRequest.setProviderId("provider123");
        appointmentRequest.setServiceType("BADMINTON");
        appointmentRequest.setStartTime(startTime);
        appointmentRequest.setEndTime(endTime);
        appointmentRequest.setAmount(new BigDecimal("100.00"));
        appointmentRequest.setNotes("Test appointment");

        appointmentResponse = new AppointmentResponse();
        appointmentResponse.setId(1L);
        appointmentResponse.setUserId("test123");
        appointmentResponse.setProviderId("provider123");
        appointmentResponse.setServiceType("BADMINTON");
        appointmentResponse.setStartTime(startTime);
        appointmentResponse.setEndTime(endTime);
        appointmentResponse.setAmount(new BigDecimal("100.00"));
        appointmentResponse.setStatus(Appointment.AppointmentStatus.PENDING.name());
        appointmentResponse.setNotes("Test appointment");
    }

    @Test
    @DisplayName("Should create appointment successfully")
    void shouldCreateAppointmentSuccessfully() throws Exception {
        // Given
        when(appointmentService.createAppointment(eq("test123"), any(AppointmentRequest.class)))
                .thenReturn(appointmentResponse);

        // When & Then
        ResultActions result = mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appointmentRequest)));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Appointment created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userId").value("test123"))
                .andExpect(jsonPath("$.data.providerId").value("provider123"));
    }

    @Test
    @DisplayName("Should get appointment by ID successfully")
    void shouldGetAppointmentByIdSuccessfully() throws Exception {
        // Given
        when(appointmentService.getAppointmentById(1L))
                .thenReturn(appointmentResponse);

        // When & Then
        ResultActions result = mockMvc.perform(get("/api/appointments/1")
                        .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Query successful"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userId").value("test123"));
    }

    @Test
    @DisplayName("Should get appointments by user ID")
    void shouldGetAppointmentsByUserId() throws Exception {
        // Given
        Page<AppointmentResponse> page = new PageImpl<>(Arrays.asList(appointmentResponse));
        when(appointmentService.getAppointmentsByUserId(eq("test123"), any(PageRequest.class)))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/appointments/user/test123")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].userId").value("test123"));
    }

    @Test
    @DisplayName("Should cancel appointment successfully")
    void shouldCancelAppointmentSuccessfully() throws Exception {
        // Given
        AppointmentResponse cancelledResponse = new AppointmentResponse();
        BeanUtils.copyProperties(appointmentResponse, cancelledResponse);
        cancelledResponse.setStatus(Appointment.AppointmentStatus.CANCELLED.name());
        
        when(appointmentService.cancelAppointment(1L))
                .thenReturn(cancelledResponse);

        // When & Then
        ResultActions result = mockMvc.perform(post("/api/appointments/1/cancel")
                        .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cancellation successful"))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("Should update appointment successfully")
    void shouldUpdateAppointmentSuccessfully() throws Exception {
        // Given
        when(appointmentService.updateAppointment(eq(1L), any(AppointmentRequest.class)))
                .thenReturn(appointmentResponse);

        // When & Then
        mockMvc.perform(put("/api/appointments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appointmentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("Should get appointments by provider ID")
    void shouldGetAppointmentsByProviderId() throws Exception {
        // Given
        Page<AppointmentResponse> page = new PageImpl<>(Arrays.asList(appointmentResponse));
        when(appointmentService.getAppointmentsByProviderId(eq("provider123"), any(PageRequest.class)))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/appointments/provider/provider123")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].providerId").value("provider123"));
    }

    @Test
    @DisplayName("Should handle validation errors")
    void shouldHandleValidationErrors() throws Exception {
        // Given - invalid appointment request
        AppointmentRequest invalidRequest = new AppointmentRequest();
        // Missing required fields

        // When & Then
        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
} 