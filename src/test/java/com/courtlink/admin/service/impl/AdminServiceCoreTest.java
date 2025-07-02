package com.courtlink.admin.service.impl;

import com.courtlink.admin.entity.Admin;
import com.courtlink.admin.exception.UnauthorizedException;
import com.courtlink.admin.exception.UserNotFoundException;
import com.courtlink.admin.repository.AdminRepository;
import com.courtlink.admin.service.AdminService;
import com.courtlink.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceCoreTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AdminServiceImpl adminService;

    private Admin testAdmin;
    private Admin testSuperAdmin;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        testAdmin = new Admin();
        testAdmin.setId(1L);
        testAdmin.setUsername("testAdmin");
        testAdmin.setPassword("password");
        testAdmin.setEmail("test@example.com");
        Set<String> adminRoles = new HashSet<>();
        adminRoles.add("ROLE_ADMIN");
        testAdmin.setRoles(adminRoles);
        testAdmin.setEnabled(true);
        testAdmin.setCreatedAt(new Date());
        testAdmin.setUpdatedAt(new Date());
        testAdmin.setPhone("1234567890");
        testAdmin.setRealName("Test Admin");
        testAdmin.setLastLoginAt(new Date());

        testSuperAdmin = new Admin();
        testSuperAdmin.setId(2L);
        testSuperAdmin.setUsername("superAdmin");
        testSuperAdmin.setPassword("password");
        testSuperAdmin.setEmail("super@example.com");
        Set<String> superAdminRoles = new HashSet<>();
        superAdminRoles.add("ROLE_SUPER_ADMIN");
        testSuperAdmin.setRoles(superAdminRoles);
        testSuperAdmin.setEnabled(true);
        testSuperAdmin.setCreatedAt(new Date());
        testSuperAdmin.setUpdatedAt(new Date());
        testSuperAdmin.setPhone("0987654321");
        testSuperAdmin.setRealName("Super Admin");
        testSuperAdmin.setLastLoginAt(new Date());
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

    @Test
    void findByUsername_Success() {
        when(adminRepository.findByUsername("testAdmin")).thenReturn(Optional.of(testAdmin));

        Optional<Admin> foundAdmin = adminService.findByUsername("testAdmin");

        assertTrue(foundAdmin.isPresent());
        assertEquals("testAdmin", foundAdmin.get().getUsername());
        assertTrue(foundAdmin.get().getRoles().contains("ROLE_ADMIN"));
        verify(adminRepository).findByUsername("testAdmin");
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

        when(adminRepository.findByUsername("newAdmin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(adminRepository.save(any(Admin.class))).thenAnswer(i -> {
            Admin savedAdmin = (Admin) i.getArgument(0);
            savedAdmin.setId(2L);
            return savedAdmin;
        });

        Admin result = adminService.createAdmin(newAdmin);

        assertNotNull(result);
        assertEquals("newAdmin", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        assertEquals(2L, result.getId());
        assertTrue(result.isEnabled());
        verify(passwordEncoder).encode("password");
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

        Optional<Admin> result = adminService.findByUsername("testAdmin");
        assertTrue(result.isPresent());
        assertEquals("testAdmin", result.get().getUsername());
        verify(adminRepository).findByUsername("testAdmin");
    }

    @Test
    void testCreateAdmin() {
        Admin newAdmin = new Admin();
        newAdmin.setUsername("newAdmin");
        newAdmin.setPassword("password");
        newAdmin.setEmail("new@example.com");
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_ADMIN");
        newAdmin.setRoles(roles);

        when(adminRepository.findByUsername("newAdmin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(adminRepository.save(any(Admin.class))).thenAnswer(i -> {
            Admin savedAdmin = (Admin) i.getArgument(0);
            savedAdmin.setId(2L);
            return savedAdmin;
        });

        Admin result = adminService.createAdmin(newAdmin);

        assertNotNull(result);
        assertEquals("newAdmin", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        assertEquals(2L, result.getId());
        assertTrue(result.isEnabled());
        verify(passwordEncoder).encode("password");
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    void testUpdateAdmin() {
        when(adminRepository.findById(1L)).thenReturn(Optional.of(testAdmin));
        when(adminRepository.save(any(Admin.class))).thenAnswer(i -> i.getArgument(0));

        Admin updateAdmin = new Admin();
        updateAdmin.setEmail("updated@example.com");
        updateAdmin.setRoles(testAdmin.getRoles());

        Admin result = adminService.updateAdmin(1L, updateAdmin);

        assertNotNull(result);
        assertEquals("updated@example.com", result.getEmail());
        assertEquals(testAdmin.getRoles(), result.getRoles());
        verify(adminRepository).findById(1L);
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    void testDeleteAdmin() {
        when(adminRepository.findById(1L)).thenReturn(Optional.of(testAdmin));
        doNothing().when(adminRepository).delete(testAdmin);

        adminService.deleteAdmin(1L);

        verify(adminRepository).findById(1L);
        verify(adminRepository).delete(testAdmin);
    }

    @Test
    void testFindAllAdmins() {
        when(adminRepository.findAll()).thenReturn(Arrays.asList(testAdmin, testSuperAdmin));

        List<Admin> allAdmins = adminService.findAllAdmins();
        
        assertEquals(2, allAdmins.size());
        assertTrue(allAdmins.stream().anyMatch(admin -> admin.getRoles().contains("ROLE_ADMIN")));
        assertTrue(allAdmins.stream().anyMatch(admin -> admin.getRoles().contains("ROLE_SUPER_ADMIN")));
        verify(adminRepository).findAll();
    }

    @Test
    void testToggleAdminStatus() {
        when(adminRepository.findById(1L)).thenReturn(Optional.of(testAdmin));
        when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        adminService.toggleAdminStatus(1L);
        assertFalse(testAdmin.isEnabled());

        adminService.toggleAdminStatus(1L);
        assertTrue(testAdmin.isEnabled());

        verify(adminRepository, times(2)).findById(1L);
        verify(adminRepository, times(2)).save(testAdmin);
    }

    @Test
    void testGetCurrentAdmin() {
        when(authentication.getName()).thenReturn("testAdmin");
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(adminRepository.findByUsername("testAdmin")).thenReturn(Optional.of(testAdmin));

        Admin result = adminService.getCurrentAdmin();

        assertNotNull(result);
        assertEquals("testAdmin", result.getUsername());
        verify(adminRepository).findByUsername("testAdmin");
    }

    @Test
    void testGetCurrentAdmin_NotAuthenticated() {
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThrows(UnauthorizedException.class, () -> adminService.getCurrentAdmin());
    }

    @Test
    void testGetCurrentAdmin_NullAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(null);

        assertThrows(UnauthorizedException.class, () -> adminService.getCurrentAdmin());
    }

    @Test
    void testFindByUsernameNotFound() {
        when(adminRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        Optional<Admin> result = adminService.findByUsername("nonexistent");
        assertFalse(result.isPresent());
    }

    @Test
    void testGetCurrentAdminSuccess() {
        Authentication auth = new UsernamePasswordAuthenticationToken("testAdmin", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(adminRepository.findByUsername("testAdmin")).thenReturn(Optional.of(testAdmin));

        Admin result = adminService.getCurrentAdmin();
        assertNotNull(result);
        assertEquals("testAdmin", result.getUsername());
        verify(adminRepository).findByUsername("testAdmin");
    }

    @Test
    void testGetCurrentAdminNoAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(null);
        assertThrows(UnauthorizedException.class, () -> adminService.getCurrentAdmin());
    }

    @Test
    void testGetCurrentAdminNotFound() {
        Authentication auth = new UsernamePasswordAuthenticationToken("testAdmin", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(adminRepository.findByUsername("testAdmin")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> adminService.getCurrentAdmin());
        verify(adminRepository).findByUsername("testAdmin");
    }

    @Test
    void testFindAll() {
        List<Admin> admins = Arrays.asList(testAdmin, testSuperAdmin);
        when(adminRepository.findAll()).thenReturn(admins);

        List<Admin> result = adminService.findAll();
        assertEquals(2, result.size());
        verify(adminRepository).findAll();
    }

    @Test
    void testFindAdminByIdSuccess() {
        when(adminRepository.findById(1L)).thenReturn(Optional.of(testAdmin));

        Admin result = adminService.findAdminById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(adminRepository).findById(1L);
    }

    @Test
    void testFindAdminByIdNotFound() {
        when(adminRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> adminService.findAdminById(1L));
        verify(adminRepository).findById(1L);
    }

    @Test
    void testToggleAdminStatusSuccess() {
        when(adminRepository.findById(1L)).thenReturn(Optional.of(testAdmin));
        when(adminRepository.save(any(Admin.class))).thenReturn(testAdmin);

        adminService.toggleAdminStatus(1L);
        assertFalse(testAdmin.isEnabled());

        adminService.toggleAdminStatus(1L);
        assertTrue(testAdmin.isEnabled());

        verify(adminRepository, times(2)).findById(1L);
        verify(adminRepository, times(2)).save(testAdmin);
    }

    @Test
    void testToggleAdminStatusNotFound() {
        when(adminRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> adminService.toggleAdminStatus(1L));
        verify(adminRepository).findById(1L);
    }

    @Test
    void testUpdateLastLoginTime() {
        when(adminRepository.findByUsername("testAdmin")).thenReturn(Optional.of(testAdmin));
        when(adminRepository.save(any(Admin.class))).thenReturn(testAdmin);

        adminService.updateLastLoginTime("testAdmin");
        assertNotNull(testAdmin.getLastLoginAt());
        verify(adminRepository).findByUsername("testAdmin");
        verify(adminRepository).save(testAdmin);
    }

    @Test
    void testUpdateLastLoginTimeNotFound() {
        when(adminRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> adminService.updateLastLoginTime("nonexistent"));
        verify(adminRepository).findByUsername("nonexistent");
    }
} 