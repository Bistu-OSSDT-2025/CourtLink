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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AdminServiceImplTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminServiceImpl adminService;

    private Admin testAdmin;
    private Admin testSuperAdmin;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();

        testAdmin = new Admin();
        testAdmin.setId(1L);
        testAdmin.setUsername("testAdmin");
        testAdmin.setPassword(passwordEncoder.encode("password"));
        testAdmin.setEmail("test@example.com");
        Set<String> adminRoles = new HashSet<>();
        adminRoles.add("ROLE_ADMIN");
        testAdmin.setRoles(adminRoles);
        testAdmin.setEnabled(true);

        testSuperAdmin = new Admin();
        testSuperAdmin.setId(2L);
        testSuperAdmin.setUsername("superAdmin");
        testSuperAdmin.setPassword(passwordEncoder.encode("password"));
        testSuperAdmin.setEmail("super@example.com");
        Set<String> superAdminRoles = new HashSet<>();
        superAdminRoles.add("ROLE_SUPER_ADMIN");
        testSuperAdmin.setRoles(superAdminRoles);
        testSuperAdmin.setEnabled(true);

        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
    }

    @Test
    void loadUserByUsername_Success() {
        when(adminRepository.findByUsername("testAdmin")).thenReturn(Optional.of(testAdmin));

        Admin result = (Admin) adminService.loadUserByUsername("testAdmin");

        assertNotNull(result);
        assertEquals("testAdmin", result.getUsername());
        assertTrue(result.getRoles().contains("ROLE_ADMIN"));
        verify(adminRepository).findByUsername("testAdmin");
    }

    @Test
    void loadUserByUsername_UserNotFound() {
        when(adminRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> adminService.loadUserByUsername("nonexistent"));
        verify(adminRepository).findByUsername("nonexistent");
    }

    @Test
    void login_Success() {
        AdminLoginRequest request = new AdminLoginRequest();
        request.setUsername("testAdmin");
        request.setPassword("password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testAdmin);
        when(adminRepository.findByUsername("testAdmin")).thenReturn(Optional.of(testAdmin));
        when(jwtService.generateToken(any(), eq(testAdmin))).thenReturn("test.jwt.token");

        String token = adminService.login(request);

        assertNotNull(token);
        assertEquals("test.jwt.token", token);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(any(), eq(testAdmin));
    }

    @Test
    void getCurrentAdmin_Success() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(testAdmin);
        when(adminRepository.findByUsername(testAdmin.getUsername())).thenReturn(Optional.of(testAdmin));

        Admin result = adminService.getCurrentAdmin();

        assertNotNull(result);
        assertEquals(testAdmin.getUsername(), result.getUsername());
        assertTrue(result.getRoles().contains("ROLE_ADMIN"));
    }

    @Test
    void getCurrentAdmin_NotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.isAuthenticated()).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> adminService.getCurrentAdmin());
    }

    @Test
    void updateLastLoginTime_Success() {
        when(adminRepository.findByUsername("testAdmin")).thenReturn(Optional.of(testAdmin));
        when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        adminService.updateLastLoginTime("testAdmin");

        verify(adminRepository).findByUsername("testAdmin");
        verify(adminRepository).save(any(Admin.class));
        assertNotNull(testAdmin.getLastLoginAt());
    }

    @Test
    void isAdmin_True() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(testAdmin);
        when(adminRepository.findByUsername(testAdmin.getUsername())).thenReturn(Optional.of(testAdmin));

        boolean result = adminService.isAdmin();

        assertTrue(result);
    }

    @Test
    void isAdmin_False() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(testSuperAdmin);
        when(adminRepository.findByUsername(testSuperAdmin.getUsername())).thenReturn(Optional.of(testSuperAdmin));

        boolean result = adminService.isAdmin();

        assertFalse(result);
    }

    @Test
    void isSuperAdmin_True() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(testSuperAdmin);
        when(adminRepository.findByUsername(testSuperAdmin.getUsername())).thenReturn(Optional.of(testSuperAdmin));

        boolean result = adminService.isSuperAdmin();

        assertTrue(result);
    }

    @Test
    void isSuperAdmin_False() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(testAdmin);
        when(adminRepository.findByUsername(testAdmin.getUsername())).thenReturn(Optional.of(testAdmin));

        boolean result = adminService.isSuperAdmin();

        assertFalse(result);
    }

    @Test
    void findAll_Success() {
        List<Admin> admins = Arrays.asList(testAdmin, testSuperAdmin);
        when(adminRepository.findAll()).thenReturn(admins);

        List<Admin> result = adminService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(adminRepository).findAll();
    }

    @Test
    void findAdminById_Success() {
        when(adminRepository.findById(1L)).thenReturn(Optional.of(testAdmin));

        Admin result = adminService.findAdminById(1L);

        assertNotNull(result);
        assertEquals(testAdmin.getId(), result.getId());
        assertTrue(result.getRoles().contains("ROLE_ADMIN"));
    }

    @Test
    void findAdminById_NotFound() {
        when(adminRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> adminService.findAdminById(1L));
    }

    @Test
    void updateAdmin_Success() {
        when(adminRepository.findById(1L)).thenReturn(Optional.of(testAdmin));
        when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> {
            Admin admin = invocation.getArgument(0);
            admin.setEmail("updated@example.com");
            return admin;
        });

        Admin updatedAdmin = new Admin();
        updatedAdmin.setEmail("updated@example.com");
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_ADMIN");
        updatedAdmin.setRoles(roles);

        Admin result = adminService.updateAdmin(1L, updatedAdmin);

        assertNotNull(result);
        assertEquals("updated@example.com", result.getEmail());
        verify(adminRepository).save(any(Admin.class));
    }
} 