package com.courtlink.admin.integration;

import com.courtlink.admin.dto.AdminLoginRequest;
import com.courtlink.admin.entity.Admin;
import com.courtlink.admin.repository.AdminRepository;
import com.courtlink.admin.service.impl.AdminServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private AdminServiceImpl adminService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private Admin testAdmin;
    private Admin testSuperAdmin;
    private final String TEST_PASSWORD = "Test@123";

    @BeforeEach
    void setUp() {
        adminRepository.deleteAll();

        testAdmin = createTestAdmin();
        testSuperAdmin = createTestSuperAdmin();
        adminRepository.save(testAdmin);
        adminRepository.save(testSuperAdmin);
    }

    @Test
    void testSuccessfulLogin() throws Exception {
        AdminLoginRequest loginRequest = new AdminLoginRequest(testAdmin.getUsername(), "password");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String token = result.getResponse().getContentAsString();
        assert !token.isEmpty();
    }

    @Test
    void testFailedLogin() throws Exception {
        AdminLoginRequest loginRequest = new AdminLoginRequest(testAdmin.getUsername(), "wrongPassword");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginFailure_WrongPassword() throws Exception {
        AdminLoginRequest request = new AdminLoginRequest();
        request.setUsername("testAdmin");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/v1/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginFailure_NonexistentUser() throws Exception {
        AdminLoginRequest request = new AdminLoginRequest();
        request.setUsername("nonexistent");
        request.setPassword(TEST_PASSWORD);

        mockMvc.perform(post("/api/v1/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginFailure_DisabledUser() throws Exception {
        testAdmin.setEnabled(false);
        adminRepository.save(testAdmin);

        AdminLoginRequest request = new AdminLoginRequest();
        request.setUsername("testAdmin");
        request.setPassword(TEST_PASSWORD);

        mockMvc.perform(post("/api/v1/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessProtectedEndpoint_WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/admin/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessProtectedEndpoint_WithAuth() throws Exception {
        // 先登录获取token
        AdminLoginRequest request = new AdminLoginRequest();
        request.setUsername("testAdmin");
        request.setPassword(TEST_PASSWORD);

        String response = mockMvc.perform(post("/api/v1/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(response).get("token").asText();

        // 使用token访问受保护的端点
        mockMvc.perform(get("/api/v1/admin/profile")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testAdmin"))
                .andExpect(jsonPath("$.roles").value(java.util.Arrays.asList("ROLE_ADMIN")));
    }

    @Test
    void superAdminAccess_Success() throws Exception {
        // 超级管理员登录
        AdminLoginRequest request = new AdminLoginRequest();
        request.setUsername("superAdmin");
        request.setPassword(TEST_PASSWORD);

        String response = mockMvc.perform(post("/api/v1/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(response).get("token").asText();

        // 检查权限
        mockMvc.perform(get("/api/v1/admin/check-auth")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAdmin").value(false))
                .andExpect(jsonPath("$.isSuperAdmin").value(true));
    }

    @Test
    void normalAdminAccess_Success() throws Exception {
        // 普通管理员登录
        AdminLoginRequest request = new AdminLoginRequest();
        request.setUsername("testAdmin");
        request.setPassword(TEST_PASSWORD);

        String response = mockMvc.perform(post("/api/v1/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(response).get("token").asText();

        // 检查权限
        mockMvc.perform(get("/api/v1/admin/check-auth")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAdmin").value(true))
                .andExpect(jsonPath("$.isSuperAdmin").value(false));
    }

    @Test
    void loadUserByUsername_Success() {
        Admin result = (Admin) adminService.loadUserByUsername("testAdmin");

        assertNotNull(result);
        assertEquals("testAdmin", result.getUsername());
        assertTrue(result.getRoles().contains("ROLE_ADMIN"));
    }

    @Test
    void validateAdminRole_Success() {
        Admin admin = adminRepository.findByUsername("testAdmin").orElseThrow();
        assertTrue(admin.getRoles().contains("ROLE_ADMIN"));
        assertFalse(admin.getRoles().contains("ROLE_SUPER_ADMIN"));

        Admin superAdmin = adminRepository.findByUsername("superAdmin").orElseThrow();
        assertTrue(superAdmin.getRoles().contains("ROLE_SUPER_ADMIN"));
        assertFalse(superAdmin.getRoles().contains("ROLE_ADMIN"));
    }

    private Admin createTestAdmin() {
        Admin admin = new Admin();
        admin.setUsername("testAdmin");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setEmail("test@example.com");
        admin.setRoles(createTestRoles());
        admin.setEnabled(true);
        admin.setCreatedAt(new Date());
        admin.setUpdatedAt(new Date());
        admin.setPhone("1234567890");
        admin.setRealName("Test Admin");
        admin.setLastLoginAt(new Date());
        return admin;
    }

    private Admin createTestSuperAdmin() {
        Admin admin = new Admin();
        admin.setUsername("superAdmin");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setEmail("super@example.com");
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_SUPER_ADMIN");
        admin.setRoles(roles);
        admin.setEnabled(true);
        admin.setCreatedAt(new Date());
        admin.setUpdatedAt(new Date());
        admin.setPhone("1234567890");
        admin.setRealName("Super Admin");
        admin.setLastLoginAt(new Date());
        return admin;
    }

    private Set<String> createTestRoles() {
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_ADMIN");
        return roles;
    }
} 