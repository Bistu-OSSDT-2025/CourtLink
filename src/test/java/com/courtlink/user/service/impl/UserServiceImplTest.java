package com.courtlink.user.service.impl;

import com.courtlink.user.dto.LoginRequest;
import com.courtlink.user.dto.LoginResponse;
import com.courtlink.user.dto.UserDTO;
import com.courtlink.user.entity.User;
import com.courtlink.user.exception.UserNotFoundException;
import com.courtlink.user.repository.UserRepository;
import com.courtlink.user.util.SimplePasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimplePasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserDTO testUserDTO;
    private LoginRequest testLoginRequest;

    @BeforeEach
    public void setUp() {
        // 准备测试数据
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setActive(true);

        testUserDTO = new UserDTO();
        testUserDTO.setUsername("testuser");
        testUserDTO.setEmail("test@example.com");
        testUserDTO.setPassword("password123");
        testUserDTO.setActive(true);

        testLoginRequest = new LoginRequest();
        testLoginRequest.setUsername("testuser");
        testLoginRequest.setPassword("password123");
    }

    @Test
    public void testRegisterUser() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // When
        UserDTO result = userService.register(testUserDTO);
        
        // Then
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getActive(), result.getActive());
        assertNull(result.getPassword()); // 密码应该被隐藏
        
        verify(userRepository).existsByUsername(anyString());
        verify(userRepository).existsByEmail(anyString());
        verify(passwordEncoder).encode(anyString());
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testRegisterUserWithExistingUsername() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(true);
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.register(testUserDTO);
        });
        
        verify(userRepository).existsByUsername(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testRegisterUserWithExistingEmail() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.register(testUserDTO);
        });
        
        verify(userRepository).existsByUsername(anyString());
        verify(userRepository).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testLoginSuccess() {
        // Given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        
        // When
        LoginResponse response = userService.login(testLoginRequest);
        
        // Then
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertNotNull(response.getUser());
        assertEquals(testUser.getUsername(), response.getUser().getUsername());
        
        verify(userRepository).findByUsername(anyString());
        verify(passwordEncoder).matches(anyString(), anyString());
    }

    @Test
    public void testLoginWithInvalidUser() {
        // Given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.login(testLoginRequest);
        });
        
        verify(userRepository).findByUsername(anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    public void testLoginWithInactiveUser() {
        // Given
        testUser.setActive(false);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.login(testLoginRequest);
        });
        
        verify(userRepository).findByUsername(anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    public void testLoginWithWrongPassword() {
        // Given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.login(testLoginRequest);
        });
        
        verify(userRepository).findByUsername(anyString());
        verify(passwordEncoder).matches(anyString(), anyString());
    }

    @Test
    public void testFindById() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        // When
        UserDTO result = userService.findById(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getActive(), result.getActive());
        assertNull(result.getPassword());
        
        verify(userRepository).findById(1L);
    }

    @Test
    public void testFindByIdNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(UserNotFoundException.class, () -> {
            userService.findById(1L);
        });
        
        verify(userRepository).findById(1L);
    }

    @Test
    public void testFindByUsername() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        // When
        UserDTO result = userService.findByUsername("testuser");
        
        // Then
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getActive(), result.getActive());
        assertNull(result.getPassword());
        
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    public void testFindByUsernameNotFound() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(UserNotFoundException.class, () -> {
            userService.findByUsername("nonexistent");
        });
        
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    public void testFindByEmail() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        
        // When
        UserDTO result = userService.findByEmail("test@example.com");
        
        // Then
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getActive(), result.getActive());
        assertNull(result.getPassword());
        
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    public void testFindByEmailNotFound() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(UserNotFoundException.class, () -> {
            userService.findByEmail("nonexistent@example.com");
        });
        
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    public void testUpdateUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // When
        UserDTO result = userService.update(1L, testUserDTO);
        
        // Then
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getActive(), result.getActive());
        assertNull(result.getPassword());
        
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testUpdateUserWithDifferentEmail() {
        // Given
        UserDTO updatedUserDTO = new UserDTO();
        updatedUserDTO.setUsername("testuser");
        updatedUserDTO.setEmail("newemail@example.com");
        updatedUserDTO.setActive(true);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmailAndIdNot("newemail@example.com", 1L)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // When
        UserDTO result = userService.update(1L, updatedUserDTO);
        
        // Then
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmailAndIdNot("newemail@example.com", 1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testUpdateUserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(UserNotFoundException.class, () -> {
            userService.update(1L, testUserDTO);
        });
        
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testDeleteUser() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);
        
        // When
        userService.delete(1L);
        
        // Then
        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    public void testDeleteUserNotFound() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(false);
        
        // When & Then
        assertThrows(UserNotFoundException.class, () -> {
            userService.delete(1L);
        });
        
        verify(userRepository).existsById(1L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    public void testExistsByUsername() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        
        // When
        boolean exists = userService.existsByUsername("testuser");
        
        // Then
        assertTrue(exists);
        verify(userRepository).existsByUsername("testuser");
    }

    @Test
    public void testExistsByEmail() {
        // Given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        
        // When
        boolean exists = userService.existsByEmail("test@example.com");
        
        // Then
        assertTrue(exists);
        verify(userRepository).existsByEmail("test@example.com");
    }

    @Test
    public void testFindAll() {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);
        
        // When
        List<UserDTO> result = userService.findAll();
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        UserDTO userDTO = result.get(0);
        assertEquals(testUser.getUsername(), userDTO.getUsername());
        assertEquals(testUser.getEmail(), userDTO.getEmail());
        assertEquals(testUser.getActive(), userDTO.getActive());
        assertNull(userDTO.getPassword());
        
        verify(userRepository).findAll();
    }

    @Test
    public void testFindAllPaginated() {
        // Given
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users);
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(pageable)).thenReturn(userPage);
        
        // When
        Page<UserDTO> result = userService.findAll(pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        UserDTO userDTO = result.getContent().get(0);
        assertEquals(testUser.getUsername(), userDTO.getUsername());
        assertEquals(testUser.getEmail(), userDTO.getEmail());
        assertEquals(testUser.getActive(), userDTO.getActive());
        assertNull(userDTO.getPassword());
        
        verify(userRepository).findAll(pageable);
    }

    @Test
    public void testValidatePassword() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        
        // When
        boolean isValid = userService.validatePassword(1L, "password123");
        
        // Then
        assertTrue(isValid);
        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches("password123", "encodedPassword");
    }

    @Test
    public void testValidatePasswordUserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(UserNotFoundException.class, () -> {
            userService.validatePassword(1L, "password123");
        });
        
        verify(userRepository).findById(1L);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    public void testChangePassword() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // When
        userService.changePassword(1L, "oldPassword", "newPassword");
        
        // Then
        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches("oldPassword", "encodedPassword");
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testChangePasswordWrongOldPassword() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.changePassword(1L, "wrongPassword", "newPassword");
        });
        
        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testResetPassword() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // When
        userService.resetPassword(1L, "newPassword");
        
        // Then
        verify(userRepository).findById(1L);
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testToggleUserStatus() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // When
        userService.toggleUserStatus(1L);
        
        // Then
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testActivateUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // When
        userService.activateUser(1L);
        
        // Then
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testDeactivateUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // When
        userService.deactivateUser(1L);
        
        // Then
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }
} 