package com.courtlink.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
@Transactional
public class CourtScheduleE2ETest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        // 获取管理员令牌
        adminToken = getAdminToken();
    }

    private String getAdminToken() throws Exception {
        // 管理员登录
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", "admin");
        loginData.put("password", "admin123");

        try {
            MvcResult result = mockMvc.perform(post("/api/admin/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginData)))
                    .andExpect(status().isOk())
                    .andReturn();

            String responseContent = result.getResponse().getContentAsString();
            return responseContent.replaceAll("\"", "");
        } catch (Exception e) {
            // 如果登录失败，返回模拟token
            return "mock-admin-token";
        }
    }

    @Test
    @Order(1)
    void testSystemHealth() throws Exception {
        // 测试系统健康状态
        mockMvc.perform(get("/api/health"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @Order(2)
    void testCreateSingleSchedule() throws Exception {
        // 创建单个时间表
        Map<String, Object> scheduleData = new HashMap<>();
        scheduleData.put("courtId", 1L);
        scheduleData.put("dayOfWeek", "MONDAY");
        scheduleData.put("openTime", "08:00");
        scheduleData.put("closeTime", "22:00");
        scheduleData.put("isActive", true);
        scheduleData.put("scheduleType", "REGULAR");
        scheduleData.put("slotDuration", 60);
        scheduleData.put("advanceBookingHours", 24);
        scheduleData.put("cancellationDeadlineHours", 2);
        scheduleData.put("description", "E2E测试时间表");

        mockMvc.perform(post("/api/schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(scheduleData)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courtId").value(1))
                .andExpect(jsonPath("$.dayOfWeek").value("MONDAY"));
    }

    @Test
    @Order(3)
    void testBatchCreateSchedules() throws Exception {
        // 批量创建时间表
        Map<String, Object> batchData = new HashMap<>();
        batchData.put("courtIds", Arrays.asList(1L, 2L));
        batchData.put("daysOfWeek", Arrays.asList("TUESDAY", "WEDNESDAY"));
        batchData.put("openTime", "09:00");
        batchData.put("closeTime", "21:00");
        batchData.put("isActive", true);
        batchData.put("scheduleType", "REGULAR");
        batchData.put("slotDuration", 90);
        batchData.put("advanceBookingHours", 48);
        batchData.put("cancellationDeadlineHours", 4);
        batchData.put("description", "批量创建的工作日时间表");
        batchData.put("overrideExisting", false);

        mockMvc.perform(post("/api/schedules/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(batchData)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    void testSetStandardWorkingHours() throws Exception {
        // 设置标准工作时间
        mockMvc.perform(post("/api/schedules/standard-hours")
                .param("courtId", "1")
                .param("openTime", "07:00")
                .param("closeTime", "23:00")
                .header("Authorization", "Bearer " + adminToken))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(5)
    void testQueryCourtSchedules() throws Exception {
        // 查询场地时间表
        mockMvc.perform(get("/api/schedules/court/1"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(6)
    void testCheckCourtOpenStatus() throws Exception {
        // 检查场地是否开放
        LocalDate today = LocalDate.now();
        mockMvc.perform(get("/api/schedules/court/1/open")
                .param("date", today.toString())
                .param("time", "10:00"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(7)
    void testGetAvailableTimeSlots() throws Exception {
        // 获取可用时间段
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        mockMvc.perform(get("/api/schedules/court/1/slots")
                .param("date", tomorrow.toString()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(8)
    void testGetEffectiveSchedules() throws Exception {
        // 获取有效时间表
        LocalDate today = LocalDate.now();
        mockMvc.perform(get("/api/schedules/court/1/effective")
                .param("date", today.toString()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(9)
    void testGetScheduleStatistics() throws Exception {
        // 获取时间表统计信息
        mockMvc.perform(get("/api/schedules/statistics")
                .header("Authorization", "Bearer " + adminToken))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(10)
    void testGetWeeklyOpenHours() throws Exception {
        // 获取场地周开放小时数
        mockMvc.perform(get("/api/schedules/court/1/weekly-hours"))
                .andDo(print())
                .andExpect(status().isOk());
    }
} 