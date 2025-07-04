package com.courtlink.admin.service.impl;

import com.courtlink.admin.dto.AdminLoginRequest;
import com.courtlink.admin.entity.Admin;
import com.courtlink.admin.exception.UnauthorizedException;
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
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceBranchTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AdminServiceImpl adminService;

    private Admin testAdmin;
    private AdminLoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testAdmin = new Admin();
        testAdmin.setId(1L);
        testAdmin.setUsername("testadmin");
        testAdmin.setPassword("password");
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_ADMIN");
        testAdmin.setRoles(roles);
        testAdmin.setEnabled(true);

        loginRequest = new AdminLoginRequest();
        loginRequest.setUsername("testadmin");
        loginRequest.setPassword("password");

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void login_Success() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(testAdmin, null));
        when(adminRepository.findByUsername(loginRequest.getUsername()))
                .thenReturn(Optional.of(testAdmin));
        when(jwtService.generateToken(any(), any())).thenReturn("token");

        String token = adminService.login(loginRequest);

        assertNotNull(token);
        assertEquals("token", token);
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    void login_InvalidCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, () -> adminService.login(loginRequest));
    }

    @Test
    void getCurrentAdmin_Success() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testadmin");
        when(adminRepository.findByUsername("testadmin")).thenReturn(Optional.of(testAdmin));

        Admin result = adminService.getCurrentAdmin();

        assertNotNull(result);
        assertEquals("testadmin", result.getUsername());
    }

    @Test
    void getCurrentAdmin_NotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> adminService.getCurrentAdmin());
    }

    @Test
    void isAdmin_True() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testadmin");
        when(adminRepository.findByUsername("testadmin")).thenReturn(Optional.of(testAdmin));

        assertTrue(adminService.isAdmin());
    }

    @Test
    void isAdmin_False() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testadmin");
        testAdmin.setRoles(new HashSet<>(Collections.singletonList("ROLE_USER")));
        when(adminRepository.findByUsername("testadmin")).thenReturn(Optional.of(testAdmin));

        assertFalse(adminService.isAdmin());
    }

    @Test
    void isSuperAdmin_True() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testadmin");
        testAdmin.setRoles(new HashSet<>(Collections.singletonList("ROLE_SUPER_ADMIN")));
        when(adminRepository.findByUsername("testadmin")).thenReturn(Optional.of(testAdmin));

        assertTrue(adminService.isSuperAdmin());
    }

    @Test
    void isSuperAdmin_False() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testadmin");
        when(adminRepository.findByUsername("testadmin")).thenReturn(Optional.of(testAdmin));

        assertFalse(adminService.isSuperAdmin());
    }

    @Test
    void toggleAdminStatus_Success() {
        when(adminRepository.findById(1L)).thenReturn(Optional.of(testAdmin));
        when(adminRepository.save(any(Admin.class))).thenReturn(testAdmin);

        adminService.toggleAdminStatus(1L);

        assertFalse(testAdmin.isEnabled());
        verify(adminRepository).save(testAdmin);
    }

    @Test
    void updateLastLoginTime_Success() {
        when(adminRepository.save(any(Admin.class))).thenReturn(testAdmin);

        adminService.updateLastLoginTime(testAdmin);

        assertNotNull(testAdmin.getLastLoginAt());
        verify(adminRepository).save(testAdmin);
    }
} 