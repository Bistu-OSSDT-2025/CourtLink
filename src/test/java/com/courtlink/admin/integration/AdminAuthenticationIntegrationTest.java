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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

        testAdmin = new Admin();
        testAdmin.setUsername("testAdmin");
        testAdmin.setPassword(passwordEncoder.encode("password"));
        testAdmin.setEmail("test@example.com");
        Set<String> adminRoles = new HashSet<>();
        adminRoles.add("ROLE_ADMIN");
        testAdmin.setRoles(adminRoles);
        testAdmin.setEnabled(true);
        testAdmin.setCreatedAt(LocalDateTime.now());
        adminRepository.save(testAdmin);

        testSuperAdmin = new Admin();
        testSuperAdmin.setUsername("superAdmin");
        testSuperAdmin.setPassword(passwordEncoder.encode("password"));
        testSuperAdmin.setEmail("super@example.com");
        Set<String> superAdminRoles = new HashSet<>();
        superAdminRoles.add("ROLE_SUPER_ADMIN");
        testSuperAdmin.setRoles(superAdminRoles);
        testSuperAdmin.setEnabled(true);
        testSuperAdmin.setCreatedAt(LocalDateTime.now());
        adminRepository.save(testSuperAdmin);
    }

    @Test
    void loginSuccess() throws Exception {
        AdminLoginRequest request = new AdminLoginRequest();
        request.setUsername("testAdmin");
        request.setPassword(TEST_PASSWORD);

        mockMvc.perform(post("/api/v1/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
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
} 