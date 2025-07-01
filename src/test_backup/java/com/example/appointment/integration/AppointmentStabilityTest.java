package com.example.appointment.integration;

import com.example.appointment.AppointmentApplication;
import com.example.appointment.dto.AppointmentRequest;
import com.example.appointment.entity.Appointment;
import com.example.appointment.repository.AppointmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = AppointmentApplication.class)
@AutoConfigureMockMvc
public class AppointmentStabilityTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testConcurrentCreateAppointments() throws Exception {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Long> createdIds = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            int idx = i;
            executor.submit(() -> {
                try {
                    AppointmentRequest req = new AppointmentRequest();
                    req.setUserId("user" + idx);
                    req.setProviderId("provider1");
                    req.setServiceType("羽毛球场");
                    req.setStartTime(LocalDateTime.now().plusDays(1));
                    req.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
                    req.setAmount(new java.math.BigDecimal("100.00"));
                    req.setNotes("并发测试");
                    String json = objectMapper.writeValueAsString(req);
                    MvcResult result = mockMvc.perform(post("/api/appointments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                            .andExpect(status().isOk())
                            .andReturn();
                    String response = result.getResponse().getContentAsString();
                    Appointment appointment = objectMapper.readValue(response, Appointment.class);
                    synchronized (createdIds) {
                        createdIds.add(appointment.getId());
                    }
                } catch (Exception e) {
                    // 允许部分失败（如时间冲突），但不能全部失败
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();
        // 至少有一个预约成功
        Assertions.assertTrue(createdIds.size() > 0);
    }
} 