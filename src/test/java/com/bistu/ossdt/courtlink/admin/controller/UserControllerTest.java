package com.bistu.ossdt.courtlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bistu.ossdt.courtlink.admin.dto.CreateUserRequest;
import com.bistu.ossdt.courtlink.admin.dto.UserDTO;
import com.bistu.ossdt.courtlink.admin.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateUserRequest createUserRequest;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("testuser");
        createUserRequest.setPassword("password123");
        createUserRequest.setEmail("test@example.com");
        createUserRequest.setPhone("13800138000");
        createUserRequest.setRealName("Test User");
        createUserRequest.setRole("USER");

        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("testuser");
        userDTO.setEmail("test@example.com");
        userDTO.setPhone("13800138000");
        userDTO.setRealName("Test User");
        userDTO.setRole("USER");
        userDTO.setEnabled(true);
        userDTO.setCreateTime(LocalDateTime.now());
        userDTO.setLastLoginTime(LocalDateTime.now());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_Success() throws Exception {
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(userDTO);

        mockMvc.perform(post("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(userDTO.getUsername()))
                .andExpect(jsonPath("$.email").value(userDTO.getEmail()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_Success() throws Exception {
        when(userService.updateUser(anyLong(), any(CreateUserRequest.class))).thenReturn(userDTO);

        mockMvc.perform(put("/api/admin/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(userDTO.getUsername()))
                .andExpect(jsonPath("$.email").value(userDTO.getEmail()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_Success() throws Exception {
        mockMvc.perform(delete("/api/admin/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUser_Success() throws Exception {
        when(userService.getUserById(anyLong())).thenReturn(userDTO);

        mockMvc.perform(get("/api/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(userDTO.getUsername()))
                .andExpect(jsonPath("$.email").value(userDTO.getEmail()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUsers_Success() throws Exception {
        Page<UserDTO> page = new Page<>();
        page.setRecords(Arrays.asList(userDTO));
        page.setTotal(1);
        page.setCurrent(1);
        page.setSize(10);

        when(userService.getUsers(anyInt(), anyInt(), anyString())).thenReturn(page);

        mockMvc.perform(get("/api/admin/users")
                .param("page", "1")
                .param("size", "10")
                .param("search", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records[0].username").value(userDTO.getUsername()))
                .andExpect(jsonPath("$.records[0].email").value(userDTO.getEmail()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void toggleUserStatus_Success() throws Exception {
        when(userService.toggleUserStatus(anyLong())).thenReturn(true);

        mockMvc.perform(put("/api/admin/users/1/toggle-status"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void checkUsername_Success() throws Exception {
        when(userService.isUsernameExists(anyString())).thenReturn(true);

        mockMvc.perform(get("/api/admin/users/check-username")
                .param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void checkEmail_Success() throws Exception {
        when(userService.isEmailExists(anyString())).thenReturn(true);

        mockMvc.perform(get("/api/admin/users/check-email")
                .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void unauthorized_Access() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void forbidden_Access() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }
} 