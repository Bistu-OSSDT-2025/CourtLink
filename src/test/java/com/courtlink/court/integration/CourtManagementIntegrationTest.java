package com.courtlink.court.integration;

import com.courtlink.court.dto.CourtBatchRequest;
import com.courtlink.court.entity.Court;
import com.courtlink.court.entity.CourtStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(locations = "classpath:application-test.yml")
@Transactional
@DisplayName("场地管理集成测试")
class CourtManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String getAdminToken() throws Exception {
        // 使用测试中的管理员登录
        String loginJson = "{\"username\":\"admin\",\"password\":\"admin123\"}";
        
        return mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .trim();
    }

    @Test
    @DisplayName("场地管理统计信息API测试")
    void testCourtManagementInfoAPI() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get("/api/v1/courts/management/info")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCourts").exists())
                .andExpect(jsonPath("$.enabledCourts").exists())
                .andExpect(jsonPath("$.systemHealth").exists())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("批量创建场地API测试")
    void testBatchCreateCourtsAPI() throws Exception {
        String token = getAdminToken();

        CourtBatchRequest request = new CourtBatchRequest();
        request.setNamePrefix("测试场地");
        request.setCount(2);
        request.setCourtType(Court.CourtType.BADMINTON);
        request.setCourtLocation(Court.CourtLocation.INDOOR);
        request.setPricePerHour(new BigDecimal("45.00"));
        request.setLocation("测试区域");
        request.setStartNumber(1);
        request.setNumberFormat("T{number}");

        mockMvc.perform(post("/api/v1/courts/batch")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("测试场地T1"))
                .andExpect(jsonPath("$[1].name").value("测试场地T2"));
    }

    @Test
    @DisplayName("获取可用场地API测试")
    void testGetAvailableCourtsAPI() throws Exception {
        mockMvc.perform(get("/api/v1/courts/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("按位置查询场地API测试")
    void testGetCourtsByLocationAPI() throws Exception {
        mockMvc.perform(get("/api/v1/courts/location/INDOOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("维护模式API测试 - 需要管理员权限")
    void testMaintenanceModeAPI_RequiresAuth() throws Exception {
        // 测试未认证访问
        mockMvc.perform(put("/api/v1/courts/1/maintenance/start")
                        .param("reason", "测试维护"))
                .andExpect(status().isForbidden());

        // 测试认证访问
        String token = getAdminToken();
        
        // 由于没有实际的场地ID=1，这里会返回404，但说明权限验证通过了
        mockMvc.perform(put("/api/v1/courts/1/maintenance/start")
                        .header("Authorization", "Bearer " + token)
                        .param("reason", "测试维护"))
                .andExpect(status().isNotFound()); // EntityNotFoundException
    }

    @Test
    @DisplayName("批量操作API权限测试")
    void testBatchOperationsPermissions() throws Exception {
        String token = getAdminToken();

        // 测试批量更新状态
        mockMvc.perform(put("/api/v1/courts/batch/status")
                        .header("Authorization", "Bearer " + token)
                        .param("status", "MAINTENANCE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1,2,3]"))
                .andExpect(status().isNotFound()); // 场地不存在，但权限验证通过

        // 测试未认证访问
        mockMvc.perform(put("/api/v1/courts/batch/status")
                        .param("status", "MAINTENANCE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1,2,3]"))
                .andExpect(status().isForbidden());
    }
} 