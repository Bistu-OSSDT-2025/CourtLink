package com.courtlink.admin.service.impl;

import com.courtlink.admin.entity.Admin;
import com.courtlink.admin.repository.AdminRepository;
import com.courtlink.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public abstract class BaseAdminServiceTest {

    @Mock
    protected AdminRepository adminRepository;

    @Mock
    protected JwtService jwtService;

    @Mock
    protected AuthenticationManager authenticationManager;

    @Mock
    protected PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    protected Admin createTestAdmin() {
        Admin admin = new Admin();
        admin.setId(1L);
        admin.setUsername("testAdmin");
        admin.setPassword("password");
        admin.setEmail("test@example.com");
        admin.setRoles(createTestRoles());
        admin.setEnabled(true);
        admin.setCreatedAt(new Date());
        admin.setUpdatedAt(new Date());
        admin.setPhone("1234567890");
        admin.setRealName("Test Admin");
        admin.setLastLoginAt(new Date());
        return admin;
    }

    protected Set<String> createTestRoles() {
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_ADMIN");
        return roles;
    }
} 