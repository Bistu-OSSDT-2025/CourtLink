package com.courtlink.admin.service.impl;

import com.courtlink.admin.entity.Admin;
import com.courtlink.admin.exception.UnauthorizedException;
import com.courtlink.admin.exception.UserNotFoundException;
import com.courtlink.admin.repository.AdminRepository;
import com.courtlink.admin.service.AdminService;
import com.courtlink.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AdminServiceCoreTest extends BaseAdminServiceTest {

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
    private Admin testSuperAdmin;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testAdmin = new Admin();
        testAdmin.setId(1L);
        testAdmin.setUsername("testAdmin");
        testAdmin.setPassword("password");
        testAdmin.setEmail("test@example.com");
        Set<String> adminRoles = new HashSet<>();
        adminRoles.add("ROLE_ADMIN");
        testAdmin.setRoles(adminRoles);
        testAdmin.setEnabled(true);

        testSuperAdmin = new Admin();
        testSuperAdmin.setId(2L);
        testSuperAdmin.setUsername("superAdmin");
        testSuperAdmin.setPassword("password");
        testSuperAdmin.setEmail("super@example.com");
        Set<String> superAdminRoles = new HashSet<>();
        superAdminRoles.add("ROLE_SUPER_ADMIN");
        testSuperAdmin.setRoles(superAdminRoles);
        testSuperAdmin.setEnabled(true);
    }

    @Test
    void findByUsername_Success() {
        when(adminRepository.findByUsername(anyString())).thenReturn(Optional.of(testAdmin));

        Admin foundAdmin = adminService.findByUsername("testAdmin");

        assertNotNull(foundAdmin);
        assertEquals("testAdmin", foundAdmin.getUsername());
        assertTrue(foundAdmin.getRoles().contains("ROLE_ADMIN"));
    }

    @Test
    void createAdmin_Success() {
        Admin newAdmin = new Admin();
        newAdmin.setUsername("newAdmin");
        newAdmin.setPassword("password");
        newAdmin.setEmail("new@example.com");
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_ADMIN");
        newAdmin.setRoles(roles);

        when(adminRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> {
            Admin savedAdmin = invocation.getArgument(0);
            savedAdmin.setId(3L);
            return savedAdmin;
        });

        Admin result = adminService.createAdmin(newAdmin);

        assertNotNull(result);
        assertEquals("newAdmin", result.getUsername());
        assertEquals("new@example.com", result.getEmail());
        assertTrue(result.getRoles().contains("ROLE_ADMIN"));
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    void validateAdminRole_Success() {
        Admin admin = new Admin();
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_ADMIN");
        admin.setRoles(roles);

        assertTrue(admin.getRoles().contains("ROLE_ADMIN"));
        assertFalse(admin.getRoles().contains("ROLE_SUPER_ADMIN"));
    }

    @Test
    void testFindByUsername() {
        when(adminRepository.findByUsername("testAdmin")).thenReturn(Optional.of(testAdmin));
        when(adminRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        Admin foundAdmin = adminService.findByUsername("testAdmin");
        assertNotNull(foundAdmin);
        assertEquals("testAdmin", foundAdmin.getUsername());
        assertTrue(foundAdmin.getRoles().contains("ROLE_ADMIN"));

        assertThrows(UserNotFoundException.class, () -> adminService.findByUsername("nonexistent"));
    }

    @Test
    void testCreateAdmin() {
        Admin newAdmin = new Admin();
        newAdmin.setUsername("newAdmin");
        newAdmin.setPassword("NewPass@123");
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_ADMIN");
        newAdmin.setRoles(roles);
        newAdmin.setEnabled(true);
        newAdmin.setEmail("newadmin@test.com");
        newAdmin.setPhone("13822222222");
        newAdmin.setRealName("新管理员");

        when(adminRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn("encodedPassword");
        when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> {
            Admin savedAdmin = invocation.getArgument(0);
            savedAdmin.setId(3L);
            return savedAdmin;
        });

        Admin savedAdmin = adminService.createAdmin(newAdmin);
        
        assertNotNull(savedAdmin);
        assertEquals("newAdmin", savedAdmin.getUsername());
        assertEquals("newadmin@test.com", savedAdmin.getEmail());
        assertEquals("13822222222", savedAdmin.getPhone());
        assertEquals("新管理员", savedAdmin.getRealName());
        assertTrue(savedAdmin.isEnabled());
        verify(adminRepository).save(any(Admin.class));
        verify(passwordEncoder).encode(any(String.class));
    }

    @Test
    void testUpdateAdmin() {
        when(adminRepository.findById(1L)).thenReturn(Optional.of(testAdmin));
        when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Admin updateRequest = new Admin();
        updateRequest.setEmail("newemail@test.com");
        updateRequest.setPhone("13811111111");
        updateRequest.setRealName("新名字");
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_ADMIN");
        updateRequest.setRoles(roles);
        
        Admin updatedAdmin = adminService.updateAdmin(1L, updateRequest);
        
        assertEquals("newemail@test.com", updatedAdmin.getEmail());
        assertEquals("13811111111", updatedAdmin.getPhone());
        assertEquals("新名字", updatedAdmin.getRealName());
        assertTrue(updatedAdmin.getRoles().contains("ROLE_ADMIN"));
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    void testDeleteAdmin() {
        when(adminRepository.findById(1L)).thenReturn(Optional.of(testAdmin));
        doNothing().when(adminRepository).delete(any(Admin.class));

        adminService.deleteAdmin(1L);
        
        verify(adminRepository).delete(testAdmin);
    }

    @Test
    void testFindAllAdmins() {
        when(adminRepository.findAll()).thenReturn(Arrays.asList(testAdmin, testSuperAdmin));

        List<Admin> allAdmins = adminService.findAllAdmins();
        
        assertEquals(2, allAdmins.size());
        assertTrue(allAdmins.stream().anyMatch(admin -> admin.getRoles().contains("ROLE_ADMIN")));
        assertTrue(allAdmins.stream().anyMatch(admin -> admin.getRoles().contains("ROLE_SUPER_ADMIN")));
    }

    @Test
    void testToggleAdminStatus() {
        when(adminRepository.findById(1L)).thenReturn(Optional.of(testAdmin));
        when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Admin disabledAdmin = adminService.toggleAdminStatus(1L, false);
        assertFalse(disabledAdmin.isEnabled());

        Admin enabledAdmin = adminService.toggleAdminStatus(1L, true);
        assertTrue(enabledAdmin.isEnabled());
    }

    @Test
    void testUpdateLastLoginTime() {
        when(adminRepository.findById(1L)).thenReturn(Optional.of(testAdmin));
        when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Admin updatedAdmin = adminService.updateLastLoginTime(1L);
        
        assertNotNull(updatedAdmin.getLastLoginAt());
        verify(adminRepository).save(any(Admin.class));
    }
} 