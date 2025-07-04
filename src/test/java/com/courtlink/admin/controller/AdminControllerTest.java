package com.courtlink.admin.controller;

import com.courtlink.admin.dto.AdminLoginRequest;
import com.courtlink.admin.entity.Admin;
import com.courtlink.admin.service.AdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @Autowired
    private ObjectMapper objectMapper;

    private Admin testAdmin;
    private Admin testSuperAdmin;
    private AdminLoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testAdmin = new Admin();
        testAdmin.setId(1L);
        testAdmin.setUsername("testAdmin");
        testAdmin.setPassword("password");
        testAdmin.setEmail("admin@test.com");
        Set<String> adminRoles = new HashSet<>();
        adminRoles.add("ROLE_ADMIN");
        testAdmin.setRoles(adminRoles);
        testAdmin.setEnabled(true);

        testSuperAdmin = new Admin();
        testSuperAdmin.setId(2L);
        testSuperAdmin.setUsername("testSuperAdmin");
        testSuperAdmin.setPassword("password");
        testSuperAdmin.setEmail("superadmin@test.com");
        Set<String> superAdminRoles = new HashSet<>();
        superAdminRoles.add("ROLE_SUPER_ADMIN");
        testSuperAdmin.setRoles(superAdminRoles);
        testSuperAdmin.setEnabled(true);

        loginRequest = new AdminLoginRequest();
        loginRequest.setUsername("testAdmin");
        loginRequest.setPassword("password");
    }

    @Test
    void login_Success() throws Exception {
        when(adminService.login(any(AdminLoginRequest.class))).thenReturn("test.jwt.token");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("test.jwt.token"));
    }

    @Test
    void login_InvalidRequest() throws Exception {
        loginRequest.setPassword("");
        loginRequest.setUsername("");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testAdmin", roles = {"ADMIN"})
    void getCurrentAdmin_Success() throws Exception {
        when(adminService.getCurrentAdmin()).thenReturn(testAdmin);

        mockMvc.perform(get("/api/v1/admin/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testAdmin"))
                .andExpect(jsonPath("$.email").value("admin@test.com"));
    }

    @Test
    void getCurrentAdmin_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/profile"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testAdmin", roles = {"ADMIN"})
    void updateAdminProfile_Success() throws Exception {
        Admin updateRequest = new Admin();
        updateRequest.setEmail("updated@test.com");

        Admin updatedAdmin = new Admin();
        updatedAdmin.setId(1L);
        updatedAdmin.setUsername("testAdmin");
        updatedAdmin.setEmail("updated@test.com");

        when(adminService.getCurrentAdmin()).thenReturn(testAdmin);
        when(adminService.updateAdmin(anyLong(), any(Admin.class))).thenReturn(updatedAdmin);

        mockMvc.perform(put("/api/v1/admin/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@test.com"));
    }

    @Test
    @WithMockUser(username = "testAdmin", roles = {"ADMIN"})
    void checkAuth_Success() throws Exception {
        when(adminService.isAdmin()).thenReturn(true);
        when(adminService.isSuperAdmin()).thenReturn(false);

        mockMvc.perform(get("/api/v1/check-auth"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAdmin").value(true))
                .andExpect(jsonPath("$.isSuperAdmin").value(false));
    }

    @Test
    @WithMockUser(username = "testSuperAdmin", roles = {"SUPER_ADMIN"})
    void checkAuth_SuperAdmin() throws Exception {
        when(adminService.isAdmin()).thenReturn(false);
        when(adminService.isSuperAdmin()).thenReturn(true);

        mockMvc.perform(get("/api/v1/check-auth"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAdmin").value(false))
                .andExpect(jsonPath("$.isSuperAdmin").value(true));
    }

    @Test
    @WithMockUser(username = "testAdmin", roles = {"ADMIN"})
    void getAdminProfile_Success() throws Exception {
        when(adminService.getCurrentAdmin()).thenReturn(testAdmin);

        mockMvc.perform(get("/api/v1/admin/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testAdmin"))
                .andExpect(jsonPath("$.email").value("admin@test.com"));
    }
} 