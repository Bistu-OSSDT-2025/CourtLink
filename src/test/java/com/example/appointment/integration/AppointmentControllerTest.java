package com.example.appointment.integration;

import com.example.appointment.AppointmentApplication;
import com.example.appointment.dto.AppointmentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = AppointmentApplication.class)
@AutoConfigureMockMvc
public class AppointmentControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateAppointment_missingUserId() throws Exception {
        AppointmentRequest req = new AppointmentRequest();
        req.setProviderId("provider1");
        req.setServiceType("羽毛球场");
        req.setStartTime(LocalDateTime.now().plusDays(1));
        req.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        req.setAmount(new java.math.BigDecimal("100.00"));
        req.setNotes("缺少用户ID");
        mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateAppointment_invalidTime() throws Exception {
        AppointmentRequest req = new AppointmentRequest();
        req.setUserId("user1");
        req.setProviderId("provider1");
        req.setServiceType("羽毛球场");
        req.setStartTime(LocalDateTime.now().minusDays(1)); // 过去时间
        req.setEndTime(LocalDateTime.now().plusDays(1));
        req.setAmount(new java.math.BigDecimal("100.00"));
        req.setNotes("预约开始时间无效");
        mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCancelAppointment_notFound() throws Exception {
        mockMvc.perform(post("/api/appointments/99999/cancel?reason=测试不存在预约"))
                .andExpect(status().is4xxClientError());
    }
} 