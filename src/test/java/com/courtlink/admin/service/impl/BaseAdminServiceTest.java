package com.courtlink.admin.service.impl;

import com.courtlink.admin.entity.Admin;
import com.courtlink.admin.repository.AdminRepository;
import com.courtlink.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public abstract class BaseAdminServiceTest {

    @Mock
    protected AdminRepository adminRepository;

    @Mock
    protected AuthenticationManager authenticationManager;

    @Mock
    protected JwtService jwtService;

    @Mock
    protected PasswordEncoder passwordEncoder;

    protected Admin testAdmin;
    protected Admin testSuperAdmin;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        initializeTestAdmin();
        initializeTestSuperAdmin();
    }

    protected void initializeTestAdmin() {
        testAdmin = new Admin();
        testAdmin.setId(1L);
        testAdmin.setUsername("testAdmin");
        testAdmin.setPassword("password");
        testAdmin.setEmail("test@example.com");
        Set<String> adminRoles = new HashSet<>();
        adminRoles.add("ROLE_ADMIN");
        testAdmin.setRoles(adminRoles);
        testAdmin.setEnabled(true);
        testAdmin.setCreatedAt(LocalDateTime.now());
    }

    protected void initializeTestSuperAdmin() {
        testSuperAdmin = new Admin();
        testSuperAdmin.setId(2L);
        testSuperAdmin.setUsername("superAdmin");
        testSuperAdmin.setPassword("password");
        testSuperAdmin.setEmail("super@example.com");
        Set<String> superAdminRoles = new HashSet<>();
        superAdminRoles.add("ROLE_SUPER_ADMIN");
        testSuperAdmin.setRoles(superAdminRoles);
        testSuperAdmin.setEnabled(true);
        testSuperAdmin.setCreatedAt(LocalDateTime.now());
    }
} 