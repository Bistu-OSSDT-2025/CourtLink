package com.bistu.ossdt.courtlink.admin.integration;

import com.bistu.ossdt.courtlink.admin.dto.CreateUserRequest;
import com.bistu.ossdt.courtlink.admin.dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    @Sql("/sql/init-test-users.sql")
    void userManagementFlow() throws Exception {
        // 1. 创建用户
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setUsername("integrationtest");
        createRequest.setPassword("password123");
        createRequest.setEmail("integration@test.com");
        createRequest.setPhone("13900139000");
        createRequest.setRealName("Integration Test");
        createRequest.setRole("USER");

        MvcResult createResult = mockMvc.perform(post("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(createRequest.getUsername()))
                .andReturn();

        String response = createResult.getResponse().getContentAsString();
        UserDTO createdUser = objectMapper.readValue(response, UserDTO.class);

        // 2. 获取用户详情
        mockMvc.perform(get("/api/admin/users/" + createdUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(createRequest.getUsername()));

        // 3. 更新用户
        createRequest.setRealName("Updated Name");
        mockMvc.perform(put("/api/admin/users/" + createdUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.realName").value("Updated Name"));

        // 4. 切换用户状态
        mockMvc.perform(put("/api/admin/users/" + createdUser.getId() + "/toggle-status"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // 5. 获取用户列表
        mockMvc.perform(get("/api/admin/users")
                .param("page", "1")
                .param("size", "10")
                .param("search", "integration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records[0].username").value(createRequest.getUsername()));

        // 6. 删除用户
        mockMvc.perform(delete("/api/admin/users/" + createdUser.getId()))
                .andExpect(status().isOk());

        // 7. 验证用户已被删除
        mockMvc.perform(get("/api/admin/users/" + createdUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Sql("/sql/init-test-users.sql")
    void concurrentUserOperations() throws Exception {
        // 模拟并发操作
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("concurrent");
        request.setPassword("password123");
        request.setEmail("concurrent@test.com");
        request.setPhone("13800138000");
        request.setRealName("Concurrent Test");
        request.setRole("USER");

        // 创建用户
        MvcResult createResult = mockMvc.perform(post("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        UserDTO createdUser = objectMapper.readValue(
            createResult.getResponse().getContentAsString(), 
            UserDTO.class
        );

        // 并发更新测试
        Runnable updateTask = () -> {
            try {
                request.setRealName("Updated " + Thread.currentThread().getId());
                mockMvc.perform(put("/api/admin/users/" + createdUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk());
            } catch (Exception e) {
                fail("Concurrent update failed: " + e.getMessage());
            }
        };

        // 创建多个线程同时更新
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(updateTask);
            threads[i].start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }

        // 验证最终状态
        mockMvc.perform(get("/api/admin/users/" + createdUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdUser.getId()));
    }
} 