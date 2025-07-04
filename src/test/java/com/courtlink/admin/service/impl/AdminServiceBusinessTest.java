package com.courtlink.admin.service.impl;

import com.courtlink.admin.dto.AdminLoginRequest;
import com.courtlink.admin.entity.Admin;
import com.courtlink.admin.exception.UnauthorizedException;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminServiceBusinessTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminServiceImpl adminService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
    }

    @Test
    void testLoginWithCorrectCredentials() {
        // Arrange
        String username = "testadmin";
        String password = "password";
        AdminLoginRequest request = new AdminLoginRequest(username, password);

        Admin admin = new Admin();
        admin.setId(1L);
        admin.setUsername(username);
        admin.setPassword(password);
        admin.setEnabled(true);
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_ADMIN");
        admin.setRoles(roles);

        Authentication authentication = new UsernamePasswordAuthenticationToken(admin, password, admin.getAuthorities());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtService.generateToken(admin)).thenReturn("jwt.token.here");

        // Act
        String token = adminService.login(request);

        // Assert
        assertNotNull(token);
        assertEquals("jwt.token.here", token);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(admin);
    }

    @Test
    void testLoginWithNonexistentUser() {
        // Arrange
        String username = "nonexistent";
        String password = "password";
        AdminLoginRequest request = new AdminLoginRequest(username, password);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> adminService.login(request));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void testLoginWithDisabledAccount() {
        // Arrange
        String username = "disabled";
        String password = "password";
        AdminLoginRequest request = new AdminLoginRequest(username, password);

        Admin admin = new Admin();
        admin.setId(1L);
        admin.setUsername(username);
        admin.setPassword(password);
        admin.setEnabled(false);

        Authentication authentication = new UsernamePasswordAuthenticationToken(admin, password, admin.getAuthorities());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> adminService.login(request));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void testConcurrentLogin() {
        // Arrange
        String username = "testadmin";
        String password = "password";
        AdminLoginRequest request = new AdminLoginRequest(username, password);

        Admin admin = new Admin();
        admin.setId(1L);
        admin.setUsername(username);
        admin.setPassword(password);
        admin.setEnabled(true);
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_ADMIN");
        admin.setRoles(roles);

        Authentication authentication = new UsernamePasswordAuthenticationToken(admin, password, admin.getAuthorities());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtService.generateToken(admin)).thenReturn("jwt.token.here");

        // Act
        String token1 = adminService.login(request);
        String token2 = adminService.login(request);

        // Assert
        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2);
        verify(authenticationManager, times(2)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(2)).generateToken(admin);
    }

    @Test
    void testRoleBasedAccess() {
        // Arrange
        Admin admin = new Admin();
        admin.setId(1L);
        admin.setUsername("testadmin");
        admin.setEnabled(true);
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_ADMIN");
        admin.setRoles(roles);

        Authentication authentication = new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(adminRepository.findByUsername(admin.getUsername())).thenReturn(Optional.of(admin));

        // Act & Assert
        assertTrue(adminService.isAdmin());
        assertFalse(adminService.isSuperAdmin());
    }

    @Test
    void testTransactionRollback() {
        // Arrange
        Admin admin = new Admin();
        admin.setId(1L);
        admin.setUsername("testadmin");
        admin.setPassword("password");
        admin.setEnabled(true);

        when(adminRepository.save(any(Admin.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> adminService.createAdmin(admin));
        verify(adminRepository).save(admin);
    }
} 