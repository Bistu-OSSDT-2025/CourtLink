package com.courtlink.e2e;

import com.courtlink.admin.dto.AdminLoginRequest;
import com.courtlink.admin.entity.Admin;
import com.courtlink.admin.repository.AdminRepository;
import com.courtlink.court.dto.CourtDTO;
import com.courtlink.court.entity.Court;
import com.courtlink.court.entity.CourtStatus;
import com.courtlink.court.repository.CourtRepository;
import com.courtlink.user.entity.User;
import com.courtlink.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 端到端测试 - CourtLink 羽毛球场预订系统
 * 测试完整的业务流程和用户场景
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class CourtLinkE2ETest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private CourtRepository courtRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String superAdminToken;
    private Admin testAdmin;
    private Admin testSuperAdmin;
    private User testUser;
    private Court testCourt;

    @BeforeEach
    void setUp() {
        // 手动创建MockMvc实例
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
                
        // 清理数据库
        cleanupDatabase();
        
        // 创建测试数据
        createTestData();
    }

    @AfterEach
    void tearDown() {
        cleanupDatabase();
    }

    /**
     * 测试1: 系统健康检查
     * 验证系统基本可用性
     */
    @Test
    @Order(1)
    @DisplayName("E2E-01: 系统健康检查")
    void testSystemHealth() throws Exception {
        // 测试公共健康检查端点（无需认证）
        mockMvc.perform(get("/api/v1/public/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("CourtLink Booking System"));

        // 测试受保护的健康检查端点（需要认证，应该返回403）
        mockMvc.perform(get("/api/health/simple"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/health/live"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/health/ready"))
                .andExpect(status().isForbidden());
    }

    /**
     * 测试2: 管理员认证流程
     * 验证完整的登录认证流程
     */
    @Test
    @Order(2)
    @DisplayName("E2E-02: 管理员认证流程")
    void testAdminAuthenticationFlow() throws Exception {
        // 1. 测试管理员登录成功
        AdminLoginRequest loginRequest = new AdminLoginRequest();
        loginRequest.setUsername("testAdmin");
        loginRequest.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // 提取JWT令牌（直接返回token字符串）
        String response = loginResult.getResponse().getContentAsString();
        adminToken = response.trim();
        assertNotNull(adminToken);
        assertTrue(adminToken.length() > 0);

        // 2. 测试使用令牌访问受保护端点
        mockMvc.perform(get("/api/v1/admin/profile")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testAdmin"));

        // 3. 测试权限验证
        mockMvc.perform(get("/api/v1/check-auth")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAdmin").value(true));
    }

    /**
     * 测试3: 超级管理员权限流程
     * 验证超级管理员特殊权限
     */
    @Test
    @Order(3)
    @DisplayName("E2E-03: 超级管理员权限流程")
    void testSuperAdminAuthFlow() throws Exception {
        // 1. 超级管理员登录
        AdminLoginRequest superAdminLogin = new AdminLoginRequest();
        superAdminLogin.setUsername("superAdmin");
        superAdminLogin.setPassword("superpass123");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(superAdminLogin)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        superAdminToken = response.trim();

        // 2. 测试超级管理员专用端点
        mockMvc.perform(get("/api/v1/super-admin/dashboard")
                        .header("Authorization", "Bearer " + superAdminToken))
                .andExpect(status().isOk());

        // 3. 验证普通管理员无法访问超级管理员端点
        mockMvc.perform(get("/api/v1/super-admin/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    /**
     * 测试4: 场地管理完整流程
     * 验证场地的CRUD操作
     */
    @Test
    @Order(4)
    @DisplayName("E2E-04: 场地管理完整流程")
    void testCourtManagementFlow() throws Exception {
        // 需要先获取管理员令牌
        setupAdminToken();
        
        // 1. 创建新场地
        CourtDTO newCourt = new CourtDTO();
        newCourt.setName("测试羽毛球场A1");
        newCourt.setCourtType(Court.CourtType.BADMINTON);
        newCourt.setDescription("专业羽毛球场地，设施完善");
        newCourt.setPricePerHour(new BigDecimal("50.00"));
        newCourt.setLocation("体育馆一楼");
        newCourt.setFacilities("专业地胶、标准网架、LED照明");

        MvcResult createResult = mockMvc.perform(post("/api/v1/courts")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCourt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("测试羽毛球场A1"))
                .andReturn();

        // 提取创建的场地ID
        String createResponse = createResult.getResponse().getContentAsString();
        JsonNode createJsonResponse = objectMapper.readTree(createResponse);
        Long courtId = createJsonResponse.get("id").asLong();

        // 2. 查询单个场地
        mockMvc.perform(get("/api/v1/courts/" + courtId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("测试羽毛球场A1"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));

        // 3. 查询所有场地
        mockMvc.perform(get("/api/v1/courts")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // 4. 更新场地信息
        newCourt.setName("更新后的羽毛球场A1");
        newCourt.setPricePerHour(new BigDecimal("55.00"));

        mockMvc.perform(put("/api/v1/courts/" + courtId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCourt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("更新后的羽毛球场A1"))
                .andExpect(jsonPath("$.pricePerHour").value(55.00));

        // 5. 切换场地状态
        mockMvc.perform(put("/api/v1/courts/" + courtId + "/toggle")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    /**
     * 测试5: 用户管理流程
     * 验证用户注册、查询、管理功能
     */
    @Test
    @Order(5)
    @DisplayName("E2E-05: 用户管理流程")
    void testUserManagementFlow() throws Exception {
        setupAdminToken();
        
        // 1. 测试用户名检查
        mockMvc.perform(get("/api/users/check-username")
                        .param("username", "newuser"))
                .andExpect(status().isOk());

        // 2. 测试邮箱检查
        mockMvc.perform(get("/api/users/check-email")
                        .param("email", "newuser@example.com"))
                .andExpect(status().isOk());

        // 3. 查询用户列表（需要管理员权限）
        mockMvc.perform(get("/api/users/list")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // 4. 查询特定用户
        if (testUser != null) {
            mockMvc.perform(get("/api/users/" + testUser.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(testUser.getUsername()));
        }
    }

    /**
     * 测试6: 统计信息查询流程
     * 验证各种统计API
     */
    @Test
    @Order(6)
    @DisplayName("E2E-06: 统计信息查询流程")
    void testStatisticsFlow() throws Exception {
        setupAdminToken();
        
        // 1. 查询所有场地统计
        mockMvc.perform(get("/api/v1/statistics/courts")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // 2. 查询场地占用率
        mockMvc.perform(get("/api/v1/statistics/courts/occupancy-rate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // 3. 查询热门场地
        mockMvc.perform(get("/api/v1/statistics/courts/top-performing")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    /**
     * 测试7: 错误处理和边界情况
     * 验证系统的健壮性
     */
    @Test
    @Order(7)
    @DisplayName("E2E-07: 错误处理和边界情况")
    void testErrorHandlingAndEdgeCases() throws Exception {
        setupAdminToken();
        
        // 1. 测试无效令牌
        mockMvc.perform(get("/api/v1/admin/profile")
                        .header("Authorization", "Bearer invalid_token"))
                .andExpect(status().isForbidden());

        // 2. 测试不存在的资源
        mockMvc.perform(get("/api/v1/courts/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());

        // 3. 测试无权限访问
        mockMvc.perform(get("/api/v1/super-admin/dashboard"))
                .andExpect(status().isForbidden());

        // 4. 测试无效的登录凭据
        AdminLoginRequest invalidLogin = new AdminLoginRequest();
        invalidLogin.setUsername("invalid");
        invalidLogin.setPassword("invalid");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLogin)))
                .andExpect(status().isBadRequest());
    }

    /**
     * 测试8: 完整业务场景
     * 模拟完整的用户使用场景
     */
    @Test
    @Order(8)
    @DisplayName("E2E-08: 完整业务场景流程")
    void testCompleteBusinessScenario() throws Exception {
        // 1. 管理员登录
        AdminLoginRequest loginRequest = new AdminLoginRequest();
        loginRequest.setUsername("testAdmin");
        loginRequest.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String token = extractTokenFromResponse(loginResult);

        // 2. 查看系统状态
        mockMvc.perform(get("/api/v1/admin/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // 3. 查看场地列表
        mockMvc.perform(get("/api/v1/courts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // 4. 查看统计信息
        mockMvc.perform(get("/api/v1/statistics/courts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // 5. 系统健康检查
        mockMvc.perform(get("/api/v1/public/health"))
                .andExpect(status().isOk());
    }

    // 辅助方法

    private void setupAdminToken() throws Exception {
        if (adminToken == null) {
            AdminLoginRequest loginRequest = new AdminLoginRequest();
            loginRequest.setUsername("testAdmin");
            loginRequest.setPassword("password123");

            MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            adminToken = extractTokenFromResponse(loginResult);
        }
    }

    private void createTestData() {
        // 创建测试管理员
        testAdmin = new Admin();
        testAdmin.setUsername("testAdmin");
        testAdmin.setPassword(passwordEncoder.encode("password123"));
        testAdmin.setEmail("admin@test.com");
        testAdmin.setEnabled(true);
        Set<String> adminRoles = new HashSet<>();
        adminRoles.add("ROLE_ADMIN");
        testAdmin.setRoles(adminRoles);
        testAdmin = adminRepository.save(testAdmin);

        // 创建测试超级管理员
        testSuperAdmin = new Admin();
        testSuperAdmin.setUsername("superAdmin");
        testSuperAdmin.setPassword(passwordEncoder.encode("superpass123"));
        testSuperAdmin.setEmail("superadmin@test.com");
        testSuperAdmin.setEnabled(true);
        Set<String> superAdminRoles = new HashSet<>();
        superAdminRoles.add("ROLE_SUPER_ADMIN");
        testSuperAdmin.setRoles(superAdminRoles);
        testSuperAdmin = adminRepository.save(testSuperAdmin);

        // 创建测试用户
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("userpass123"));
        testUser.setEmail("user@test.com");
        testUser.setActive(true);
        testUser = userRepository.save(testUser);

        // 创建测试场地
        testCourt = new Court();
        testCourt.setName("测试场地");
        testCourt.setCourtType(Court.CourtType.BADMINTON);
        testCourt.setDescription("用于测试的羽毛球场地");
        testCourt.setPricePerHour(new BigDecimal("45.00"));
        testCourt.setLocation("测试区域");
        testCourt.setStatus(CourtStatus.AVAILABLE);
        testCourt.setEnabled(true);
        testCourt.setCourtLocation(Court.CourtLocation.INDOOR);
        testCourt.setCreatedAt(LocalDateTime.now());
        testCourt.setLastModifiedAt(LocalDateTime.now());
        testCourt = courtRepository.save(testCourt);
    }

    private void cleanupDatabase() {
        // 清理测试数据
        if (adminRepository != null) {
            adminRepository.deleteAll();
        }
        if (userRepository != null) {
            userRepository.deleteAll();
        }
        if (courtRepository != null) {
            courtRepository.deleteAll();
        }
    }

    private String extractTokenFromResponse(MvcResult result) throws Exception {
        String response = result.getResponse().getContentAsString();
        return response.trim();
    }
} 