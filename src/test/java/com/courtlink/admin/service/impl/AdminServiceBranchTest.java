package com.courtlink.admin.service.impl;

import com.courtlink.admin.dto.AdminLoginRequest;
import com.courtlink.admin.entity.Admin;
import com.courtlink.admin.exception.UserNotFoundException;
import com.courtlink.admin.repository.AdminRepository;
import com.courtlink.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceBranchTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminServiceImpl adminService;

    private Admin testAdmin;
    private AdminLoginRequest loginRequest;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        // 设置测试数据
        testAdmin = new Admin();
        testAdmin.setId(1L);
        testAdmin.setUsername("testAdmin");
        testAdmin.setPassword("encodedPassword");
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_ADMIN");
        testAdmin.setRoles(roles);
        testAdmin.setEnabled(true);
        testAdmin.setLastLoginAt(LocalDateTime.now());

        loginRequest = new AdminLoginRequest();
        loginRequest.setUsername("testAdmin");
        loginRequest.setPassword("password");

        authentication = new UsernamePasswordAuthenticationToken(
            testAdmin.getUsername(),
            testAdmin.getPassword(),
            testAdmin.getAuthorities()
        );
    }

    @Test
    void createAdmin_Success() {
        // 配置Mock行为
        when(adminRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(adminRepository.save(any(Admin.class))).thenReturn(testAdmin);

        // 执行测试
        Admin newAdmin = new Admin();
        newAdmin.setUsername("testAdmin");
        newAdmin.setPassword("password");
        newAdmin.setRoles(new HashSet<>(Arrays.asList("ROLE_ADMIN")));

        Admin result = adminService.createAdmin(newAdmin);

        // 验证结果
        assertNotNull(result);
        assertEquals(testAdmin.getUsername(), result.getUsername());
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    void login_Success() {
        // 配置Mock行为
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(jwtService.generateToken(any(Authentication.class))).thenReturn("jwt.token.here");
        when(adminRepository.findByUsername(anyString())).thenReturn(Optional.of(testAdmin));

        // 执行测试
        String token = adminService.login(loginRequest);

        // 验证结果
        assertNotNull(token);
        assertEquals("jwt.token.here", token);
        verify(authenticationManager).authenticate(any(Authentication.class));
        verify(jwtService).generateToken(any(Authentication.class));
    }

    @Test
    void login_InvalidCredentials() {
        // 配置Mock行为
        when(authenticationManager.authenticate(any(Authentication.class)))
            .thenThrow(new BadCredentialsException("Invalid credentials"));

        // 执行测试并验证异常
        assertThrows(BadCredentialsException.class, () -> adminService.login(loginRequest));
    }

    @Test
    void updateAdmin_Success() {
        // 配置Mock行为
        when(adminRepository.findById(anyLong())).thenReturn(Optional.of(testAdmin));
        when(adminRepository.save(any(Admin.class))).thenReturn(testAdmin);

        // 执行测试
        Admin updateRequest = new Admin();
        updateRequest.setUsername("updatedAdmin");
        updateRequest.setRoles(new HashSet<>(Arrays.asList("ROLE_ADMIN", "ROLE_SUPER_ADMIN")));

        Admin result = adminService.updateAdmin(1L, updateRequest);

        // 验证结果
        assertNotNull(result);
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    void deleteAdmin_Success() {
        // 配置Mock行为
        when(adminRepository.findById(anyLong())).thenReturn(Optional.of(testAdmin));
        doNothing().when(adminRepository).delete(any(Admin.class));

        // 执行测试
        assertDoesNotThrow(() -> adminService.deleteAdmin(1L));

        // 验证调用
        verify(adminRepository).delete(any(Admin.class));
    }

    @Test
    void findAdminById_Success() {
        // 配置Mock行为
        when(adminRepository.findById(anyLong())).thenReturn(Optional.of(testAdmin));

        // 执行测试
        Admin result = adminService.findAdminById(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(testAdmin.getUsername(), result.getUsername());
    }

    @Test
    void findAdminById_NotFound() {
        // 配置Mock行为
        when(adminRepository.findById(anyLong())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(UserNotFoundException.class, () -> adminService.findAdminById(1L));
    }

    @Test
    void findAllAdmins_Success() {
        // 配置Mock行为
        List<Admin> adminList = Collections.singletonList(testAdmin);
        when(adminRepository.findAll()).thenReturn(adminList);

        // 执行测试
        List<Admin> results = adminService.findAllAdmins();

        // 验证结果
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(testAdmin.getUsername(), results.get(0).getUsername());
    }

    @Test
    void toggleAdminStatus_Success() {
        // 配置Mock行为
        when(adminRepository.findById(anyLong())).thenReturn(Optional.of(testAdmin));
        when(adminRepository.save(any(Admin.class))).thenReturn(testAdmin);

        // 执行测试
        Admin result = adminService.toggleAdminStatus(1L, false);

        // 验证结果
        assertNotNull(result);
        assertFalse(result.isEnabled());
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    void updateLastLoginTime_Success() {
        // 配置Mock行为
        when(adminRepository.findById(anyLong())).thenReturn(Optional.of(testAdmin));
        when(adminRepository.save(any(Admin.class))).thenReturn(testAdmin);

        // 执行测试
        Admin result = adminService.updateLastLoginTime(1L);

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getLastLoginAt());
        verify(adminRepository).save(any(Admin.class));
    }
} 