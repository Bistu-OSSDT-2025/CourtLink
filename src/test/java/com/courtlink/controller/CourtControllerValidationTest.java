package com.courtlink.controller;

import com.courtlink.dto.CourtRequest;
import com.courtlink.service.CourtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 控制器数据验证测试
 * 测试各种无效数据和边界条件
 */
@WebMvcTest(CourtController.class)
@ExtendWith(MockitoExtension.class)
public class CourtControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourtService courtService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // 测试初始化
    }

    @Test
    void createCourt_WithNullName_ShouldReturnBadRequest() throws Exception {
        CourtRequest request = new CourtRequest();
        request.setName(null);  // 空名称
        request.setDescription("测试描述");
        request.setCapacity(20);

        mockMvc.perform(post("/api/courts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCourt_WithEmptyName_ShouldReturnBadRequest() throws Exception {
        CourtRequest request = new CourtRequest();
        request.setName("");  // 空字符串名称
        request.setDescription("测试描述");
        request.setCapacity(20);

        mockMvc.perform(post("/api/courts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCourt_WithNegativeCapacity_ShouldReturnBadRequest() throws Exception {
        CourtRequest request = new CourtRequest();
        request.setName("测试场地");
        request.setDescription("测试描述");
        request.setCapacity(-5);  // 负数容量

        mockMvc.perform(post("/api/courts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCourt_WithZeroCapacity_ShouldReturnBadRequest() throws Exception {
        CourtRequest request = new CourtRequest();
        request.setName("测试场地");
        request.setDescription("测试描述");
        request.setCapacity(0);  // 零容量

        mockMvc.perform(post("/api/courts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCourt_WithTooLongName_ShouldReturnBadRequest() throws Exception {
        CourtRequest request = new CourtRequest();
        request.setName("这是一个非常非常非常非常非常非常非常非常非常长的场地名称，应该超过了系统限制的最大长度，用来测试长度验证功能是否正常工作");
        request.setDescription("测试描述");
        request.setCapacity(20);

        mockMvc.perform(post("/api/courts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCourt_WithSpecialCharactersName_ShouldReturnBadRequest() throws Exception {
        CourtRequest request = new CourtRequest();
        request.setName("<script>alert('xss')</script>");  // XSS攻击测试
        request.setDescription("测试描述");
        request.setCapacity(20);

        mockMvc.perform(post("/api/courts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCourt_WithSQLInjection_ShouldReturnBadRequest() throws Exception {
        CourtRequest request = new CourtRequest();
        request.setName("'; DROP TABLE courts; --");  // SQL注入测试
        request.setDescription("测试描述");
        request.setCapacity(20);

        mockMvc.perform(post("/api/courts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCourt_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        CourtRequest request = new CourtRequest();
        request.setName("更新的场地");
        request.setDescription("更新的描述");
        request.setCapacity(25);

        mockMvc.perform(put("/api/courts/invalid-id")  // 无效ID
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCourt_WithMissingFields_ShouldReturnBadRequest() throws Exception {
        String incompleteJson = "{\"name\":\"测试场地\"}";  // 缺少必填字段

        mockMvc.perform(post("/api/courts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(incompleteJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCourt_WithInvalidJSON_ShouldReturnBadRequest() throws Exception {
        String invalidJson = "{\"name\":\"测试场地\",\"capacity\":}";  // 无效JSON格式

        mockMvc.perform(post("/api/courts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCourt_WithExtremelyLargeCapacity_ShouldReturnBadRequest() throws Exception {
        CourtRequest request = new CourtRequest();
        request.setName("测试场地");
        request.setDescription("测试描述");
        request.setCapacity(999999999);  // 极大容量值

        mockMvc.perform(post("/api/courts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCourt_WithNullDescription_ShouldHandleGracefully() throws Exception {
        CourtRequest request = new CourtRequest();
        request.setName("测试场地");
        request.setDescription(null);  // 空描述
        request.setCapacity(20);

        // 空描述应该被允许，或返回适当的错误
        mockMvc.perform(post("/api/courts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk().or(status().isBadRequest()));
    }
} 