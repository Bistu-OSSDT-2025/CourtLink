package com.courtlink.admin.service.impl;

import com.courtlink.admin.dto.AdminLoginRequest;
import com.courtlink.admin.entity.Admin;
import com.courtlink.admin.exception.UnauthorizedException;
import com.courtlink.admin.exception.UserNotFoundException;
import com.courtlink.admin.repository.AdminRepository;
import com.courtlink.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class AdminServiceBusinessTest extends BaseAdminServiceTest {

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

    @Override
    void setUp() {
        super.setUp();
        loginRequest = new AdminLoginRequest();
        loginRequest.setUsername("testAdmin");
        loginRequest.setPassword("password");

        authentication = new UsernamePasswordAuthenticationToken(
            testAdmin, loginRequest.getPassword(), testAdmin.getAuthorities()
        );

        SecurityContextHolder.clearContext();
    }

    @Test
    void testLoginWithCorrectCredentials() {
        // 设置 Mock 行为
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(jwtService.generateToken(any(Authentication.class))).thenReturn("test.jwt.token");
        when(adminRepository.findByUsername("testAdmin")).thenReturn(Optional.of(testAdmin));

        // 测试登录
        String token = adminService.login(loginRequest);

        assertNotNull(token);
        assertEquals("test.jwt.token", token);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(authenticationManager).authenticate(any(Authentication.class));
        verify(jwtService).generateToken(any(Authentication.class));
    }

    @Test
    void testLoginWithWrongPassword() {
        // 设置 Mock 行为
        when(authenticationManager.authenticate(any(Authentication.class)))
            .thenThrow(new BadCredentialsException("Invalid credentials"));

        // 测试错误密码登录
        loginRequest.setPassword("wrongpassword");
        assertThrows(BadCredentialsException.class, () -> adminService.login(loginRequest));
    }

    @Test
    void testLoginWithNonexistentUser() {
        // 设置 Mock 行为
        when(adminRepository.findByUsername("nonexistent"))
            .thenReturn(Optional.empty());

        // 测试不存在的用户登录
        loginRequest.setUsername("nonexistent");
        assertThrows(UsernameNotFoundException.class, () -> adminService.loadUserByUsername(loginRequest.getUsername()));
    }

    @Test
    void testLoginWithDisabledAccount() {
        // 设置 Mock 行为
        testAdmin.setEnabled(false);
        when(authenticationManager.authenticate(any(Authentication.class)))
            .thenThrow(new BadCredentialsException("Account is disabled"));

        // 测试禁用账号登录
        assertThrows(BadCredentialsException.class, () -> adminService.login(loginRequest));
    }

    @Test
    void testConcurrentLogin() throws InterruptedException {
        // 设置 Mock 行为
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(jwtService.generateToken(any(Authentication.class))).thenReturn("test.jwt.token");
        when(adminRepository.findByUsername("testAdmin")).thenReturn(Optional.of(testAdmin));

        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        // 测试并发登录
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    String token = adminService.login(loginRequest);
                    assertNotNull(token);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        verify(authenticationManager, times(threadCount)).authenticate(any(Authentication.class));
        verify(jwtService, times(threadCount)).generateToken(any(Authentication.class));
    }

    @Test
    void testPasswordValidation() {
        when(passwordEncoder.encode(any(String.class))).thenReturn("encodedPassword");
        when(adminRepository.save(any(Admin.class))).thenReturn(testAdmin);

        Admin newAdmin = new Admin();
        newAdmin.setUsername("newAdmin");
        newAdmin.setPassword("password");
        newAdmin.setEmail("new@example.com");
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_ADMIN");
        newAdmin.setRoles(roles);

        Admin savedAdmin = adminService.createAdmin(newAdmin);
        
        assertNotNull(savedAdmin);
        verify(passwordEncoder).encode("password");
    }

    @Test
    void testRoleBasedAccess() {
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertTrue(adminService.isAdmin());
        assertFalse(adminService.isSuperAdmin());

        testAdmin.setRoles(new HashSet<>(Set.of("ROLE_SUPER_ADMIN")));
        authentication = new UsernamePasswordAuthenticationToken(
            testAdmin, loginRequest.getPassword(), testAdmin.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertFalse(adminService.isAdmin());
        assertTrue(adminService.isSuperAdmin());
    }

    @Test
    void testSessionHandling() {
        // 设置 Mock 行为
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(jwtService.generateToken(any(Authentication.class))).thenReturn("test.jwt.token");
        when(adminRepository.findByUsername("testAdmin")).thenReturn(Optional.of(testAdmin));

        // 测试会话处理
        adminService.login(loginRequest);
        
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(currentAuth);
        assertTrue(currentAuth.isAuthenticated());
        assertEquals(testAdmin, currentAuth.getPrincipal());
    }

    @Test
    void testTransactionRollback() {
        // 设置 Mock 行为
        when(adminRepository.findByUsername("testAdmin")).thenReturn(Optional.of(testAdmin));
        when(adminRepository.save(any(Admin.class))).thenThrow(new RuntimeException("Database error"));

        // 测试事务回滚
        assertThrows(RuntimeException.class, () -> adminService.updateLastLoginTime("testAdmin"));
    }

    @Test
    void testAdminProfileUpdate() {
        // 设置 Mock 行为
        when(adminRepository.findById(1L)).thenReturn(Optional.of(testAdmin));
        when(adminRepository.save(any(Admin.class))).thenReturn(testAdmin);

        // 测试管理员信息更新
        Admin updateRequest = new Admin();
        updateRequest.setEmail("updated@example.com");
        updateRequest.setPassword("password");
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_ADMIN");
        updateRequest.setRoles(roles);

        Admin updatedAdmin = adminService.updateAdmin(1L, updateRequest);

        assertEquals("updated@example.com", updatedAdmin.getEmail());
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    void createAdmin_Success() {
        when(adminRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(adminRepository.save(any(Admin.class))).thenReturn(testAdmin);

        Admin newAdmin = new Admin();
        newAdmin.setUsername("newAdmin");
        newAdmin.setPassword("password");
        newAdmin.setEmail("new@example.com");
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_ADMIN");
        newAdmin.setRoles(roles);

        Admin result = adminService.createAdmin(newAdmin);

        assertNotNull(result);
        assertEquals("newAdmin", result.getUsername());
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    void updateAdmin_Success() {
        when(adminRepository.findById(anyLong())).thenReturn(Optional.of(testAdmin));
        when(adminRepository.save(any(Admin.class))).thenReturn(testAdmin);

        testAdmin.setEmail("updated@example.com");
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_ADMIN");
        testAdmin.setRoles(roles);

        Admin result = adminService.updateAdmin(1L, testAdmin);

        assertNotNull(result);
        assertEquals("updated@example.com", result.getEmail());
        verify(adminRepository).save(any(Admin.class));
    }
} 