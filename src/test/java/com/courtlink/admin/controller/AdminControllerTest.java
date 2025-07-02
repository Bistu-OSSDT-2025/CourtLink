package com.courtlink.admin.controller;

import com.courtlink.admin.dto.AdminLoginRequest;
import com.courtlink.admin.entity.Admin;
import com.courtlink.admin.service.AdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    private ObjectMapper objectMapper;
    private Admin testAdmin;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
        objectMapper = new ObjectMapper();

        testAdmin = new Admin();
        testAdmin.setId(1L);
        testAdmin.setUsername("testAdmin");
        testAdmin.setPassword("password");
        testAdmin.setEmail("test@example.com");
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_ADMIN");
        testAdmin.setRoles(roles);
        testAdmin.setEnabled(true);
    }

    @Test
    void login_Success() throws Exception {
        AdminLoginRequest loginRequest = new AdminLoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("password");

        when(adminService.login(any(AdminLoginRequest.class))).thenReturn("test.jwt.token");

        mockMvc.perform(post("/api/v1/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test.jwt.token"));
    }

    @Test
    void login_InvalidRequest() throws Exception {
        AdminLoginRequest loginRequest = new AdminLoginRequest();
        // 不设置用户名和密码，触发验证失败

        mockMvc.perform(post("/api/v1/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAdminProfile_Success() throws Exception {
        when(adminService.getCurrentAdmin()).thenReturn(testAdmin);

        mockMvc.perform(get("/api/admin/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testAdmin"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateAdminProfile_Success() throws Exception {
        when(adminService.updateAdmin(any(Long.class), any(Admin.class))).thenReturn(testAdmin);

        mockMvc.perform(put("/api/admin/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"updated@example.com\",\"roles\":[\"ROLE_ADMIN\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"));
    }

    @Test
    void getCurrentAdmin_Unauthorized() throws Exception {
        when(adminService.getCurrentAdmin()).thenReturn(null);

        mockMvc.perform(get("/api/v1/admin/profile"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void checkAuth_Success() throws Exception {
        when(adminService.isAdmin()).thenReturn(true);
        when(adminService.isSuperAdmin()).thenReturn(false);

        mockMvc.perform(get("/api/v1/admin/check-auth"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAdmin").value(true))
                .andExpect(jsonPath("$.isSuperAdmin").value(false));
    }

    @Test
    void checkAuth_SuperAdmin() throws Exception {
        when(adminService.isAdmin()).thenReturn(false);
        when(adminService.isSuperAdmin()).thenReturn(true);

        mockMvc.perform(get("/api/v1/admin/check-auth"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAdmin").value(false))
                .andExpect(jsonPath("$.isSuperAdmin").value(true));
    }
} 