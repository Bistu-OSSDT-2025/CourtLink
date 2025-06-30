package com.bistu.ossdt.courtlink.admin.service;

import com.bistu.ossdt.courtlink.admin.dto.CreateUserRequest;
import com.bistu.ossdt.courtlink.admin.mapper.UserMapper;
import com.bistu.ossdt.courtlink.admin.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceBoundaryTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private CreateUserRequest request;

    @BeforeEach
    void setUp() {
        request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setEmail("test@example.com");
        request.setPhone("13800138000");
        request.setRealName("Test User");
        request.setRole("USER");
    }

    @Test
    void createUser_WithEmptyUsername() {
        request.setUsername("");
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(request));
    }

    @Test
    void createUser_WithNullUsername() {
        request.setUsername(null);
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(request));
    }

    @ParameterizedTest
    @ValueSource(strings = {"user@", "@domain.com", "user@domain", "user.com", ""})
    void createUser_WithInvalidEmail(String email) {
        request.setEmail(email);
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(request));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345", "123456789012", "abcd12345678"})
    void createUser_WithInvalidPhone(String phone) {
        request.setPhone(phone);
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(request));
    }

    @Test
    void createUser_WithVeryLongUsername() {
        request.setUsername("a".repeat(101)); // 假设最大长度为100
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(request));
    }

    @Test
    void createUser_WithInvalidRole() {
        request.setRole("INVALID_ROLE");
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(request));
    }

    @Test
    void createUser_WithWeakPassword() {
        request.setPassword("123");
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(request));
    }

    @Test
    void updateUser_WithExistingEmail() {
        when(userMapper.countByEmail(anyString())).thenReturn(1);
        assertThrows(IllegalArgumentException.class, () -> 
            userService.updateUser(1L, request));
    }

    @Test
    void toggleUserStatus_WithSystemAdmin() {
        // 系统管理员状态不能被禁用
        request.setRole("SYSTEM_ADMIN");
        assertThrows(IllegalArgumentException.class, () -> 
            userService.toggleUserStatus(1L));
    }
} 