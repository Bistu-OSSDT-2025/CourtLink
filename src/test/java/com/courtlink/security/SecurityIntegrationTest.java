package com.courtlink.security;

import com.courtlink.admin.dto.AdminLoginRequest;
import com.courtlink.admin.entity.Admin;
import com.courtlink.admin.repository.AdminRepository;
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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private Admin testAdmin;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        adminRepository.deleteAll();
        testAdmin = createTestAdmin();
        adminRepository.save(testAdmin);
    }

    @Test
    void testPublicEndpointAccess() throws Exception {
        mockMvc.perform(get("/api/v1/public/health"))
                .andExpect(status().isOk());
    }

    @Test
    void testProtectedEndpointWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/admin/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLoginSuccess() throws Exception {
        AdminLoginRequest loginRequest = new AdminLoginRequest(testAdmin.getUsername(), "password");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        jwtToken = result.getResponse().getContentAsString();
        assert !jwtToken.isEmpty();
    }

    @Test
    void testLoginFailure() throws Exception {
        AdminLoginRequest loginRequest = new AdminLoginRequest(testAdmin.getUsername(), "wrongPassword");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testProtectedEndpointWithAuth() throws Exception {
        // First login to get the token
        AdminLoginRequest loginRequest = new AdminLoginRequest(testAdmin.getUsername(), "password");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String token = result.getResponse().getContentAsString();

        // Then use the token to access protected endpoint
        mockMvc.perform(get("/api/v1/admin/profile")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
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

    private Set<String> createTestRoles() {
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_ADMIN");
        return roles;
    }
} 