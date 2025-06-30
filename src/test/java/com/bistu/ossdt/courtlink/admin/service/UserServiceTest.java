package com.bistu.ossdt.courtlink.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bistu.ossdt.courtlink.admin.dto.CreateUserRequest;
import com.bistu.ossdt.courtlink.admin.dto.UserDTO;
import com.bistu.ossdt.courtlink.admin.mapper.UserMapper;
import com.bistu.ossdt.courtlink.admin.model.User;
import com.bistu.ossdt.courtlink.admin.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private CreateUserRequest createUserRequest;
    private User user;

    @BeforeEach
    void setUp() {
        createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("testuser");
        createUserRequest.setPassword("password123");
        createUserRequest.setEmail("test@example.com");
        createUserRequest.setPhone("13800138000");
        createUserRequest.setRealName("Test User");
        createUserRequest.setRole("USER");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setEmail("test@example.com");
        user.setPhone("13800138000");
        user.setRealName("Test User");
        user.setRole("USER");
        user.setEnabled(true);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
    }

    @Test
    void createUser_Success() {
        // Arrange
        when(userMapper.countByUsername(anyString())).thenReturn(0);
        when(userMapper.countByEmail(anyString())).thenReturn(0);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userMapper.insert(any(User.class))).thenReturn(1);

        // Act
        UserDTO result = userService.createUser(createUserRequest);

        // Assert
        assertNotNull(result);
        assertEquals(createUserRequest.getUsername(), result.getUsername());
        assertEquals(createUserRequest.getEmail(), result.getEmail());
        verify(userMapper).insert(any(User.class));
    }

    @Test
    void createUser_DuplicateUsername() {
        // Arrange
        when(userMapper.countByUsername(anyString())).thenReturn(1);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            userService.createUser(createUserRequest)
        );
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void updateUser_Success() {
        // Arrange
        when(userMapper.selectById(anyLong())).thenReturn(user);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // Act
        UserDTO result = userService.updateUser(1L, createUserRequest);

        // Assert
        assertNotNull(result);
        assertEquals(createUserRequest.getUsername(), result.getUsername());
        assertEquals(createUserRequest.getEmail(), result.getEmail());
        verify(userMapper).updateById(any(User.class));
    }

    @Test
    void updateUser_UserNotFound() {
        // Arrange
        when(userMapper.selectById(anyLong())).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            userService.updateUser(1L, createUserRequest)
        );
        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    void deleteUser_Success() {
        // Arrange
        when(userMapper.deleteById(anyLong())).thenReturn(1);

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userMapper).deleteById(1L);
    }

    @Test
    void getUserById_Success() {
        // Arrange
        when(userMapper.selectById(anyLong())).thenReturn(user);

        // Act
        UserDTO result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    void getUserById_NotFound() {
        // Arrange
        when(userMapper.selectById(anyLong())).thenReturn(null);

        // Act
        UserDTO result = userService.getUserById(1L);

        // Assert
        assertNull(result);
    }

    @Test
    void toggleUserStatus_Success() {
        // Arrange
        when(userMapper.selectById(anyLong())).thenReturn(user);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // Act
        boolean result = userService.toggleUserStatus(1L);

        // Assert
        assertTrue(result);
        verify(userMapper).updateById(any(User.class));
    }

    @Test
    void toggleUserStatus_UserNotFound() {
        // Arrange
        when(userMapper.selectById(anyLong())).thenReturn(null);

        // Act
        boolean result = userService.toggleUserStatus(1L);

        // Assert
        assertFalse(result);
        verify(userMapper, never()).updateById(any(User.class));
    }
} 