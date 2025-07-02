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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Mock
    private Authentication authentication;

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

        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
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

        when(adminRepository.findByUsername("newAdmin")).thenReturn(Optional.empty());
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
        Admin admin = createTestAdmin();
        when(adminRepository.findByUsername(anyString())).thenReturn(Optional.of(admin));

        Optional<Admin> result = adminService.findByUsername("testAdmin");
        assertTrue(result.isPresent());
        assertEquals("testAdmin", result.get().getUsername());
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
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    void testDeleteAdmin() {
        when(adminRepository.findById(1L)).thenReturn(Optional.of(testAdmin));
        doNothing().when(adminRepository).deleteById(1L);

        adminService.deleteAdmin(1L);

        verify(adminRepository).deleteById(1L);
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

        adminService.toggleAdminStatus(1L);
        assertFalse(testAdmin.isEnabled());

        adminService.toggleAdminStatus(1L);
        assertTrue(testAdmin.isEnabled());

        verify(adminRepository, times(2)).save(testAdmin);
    }

    @Test
    void testUpdateLastLoginTime() {
        when(adminRepository.findByUsername("testAdmin")).thenReturn(Optional.of(testAdmin));
        when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        adminService.updateLastLoginTime("testAdmin");

        assertNotNull(testAdmin.getLastLoginAt());
        verify(adminRepository).save(testAdmin);
    }

    @Test
    void testGetCurrentAdmin() {
        when(authentication.getPrincipal()).thenReturn(testAdmin);
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(adminRepository.findByUsername(testAdmin.getUsername())).thenReturn(Optional.of(testAdmin));

        Admin result = adminService.getCurrentAdmin();

        assertNotNull(result);
        assertEquals("testAdmin", result.getUsername());
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
        Admin admin = createTestAdmin();
        Authentication auth = new UsernamePasswordAuthenticationToken(admin.getUsername(), null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(adminRepository.findByUsername(anyString())).thenReturn(Optional.of(admin));

        Admin result = adminService.getCurrentAdmin();
        assertNotNull(result);
        assertEquals("testAdmin", result.getUsername());
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

        when(adminRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> adminService.getCurrentAdmin());
    }

    @Test
    void testFindAll() {
        List<Admin> admins = Arrays.asList(createTestAdmin(), createTestAdmin());
        when(adminRepository.findAll()).thenReturn(admins);

        List<Admin> result = adminService.findAll();
        assertEquals(2, result.size());
    }

    @Test
    void testFindAdminByIdSuccess() {
        Admin admin = createTestAdmin();
        when(adminRepository.findById(anyLong())).thenReturn(Optional.of(admin));

        Admin result = adminService.findAdminById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testFindAdminByIdNotFound() {
        when(adminRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> adminService.findAdminById(1L));
    }

    @Test
    void testToggleAdminStatusSuccess() {
        Admin admin = createTestAdmin();
        when(adminRepository.findById(anyLong())).thenReturn(Optional.of(admin));
        when(adminRepository.save(any(Admin.class))).thenReturn(admin);

        adminService.toggleAdminStatus(1L);
        assertFalse(admin.isEnabled());

        adminService.toggleAdminStatus(1L);
        assertTrue(admin.isEnabled());
    }

    @Test
    void testToggleAdminStatusNotFound() {
        when(adminRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> adminService.toggleAdminStatus(1L));
    }

    @Test
    void testUpdateLastLoginTime() {
        Admin admin = createTestAdmin();
        when(adminRepository.findByUsername(anyString())).thenReturn(Optional.of(admin));
        when(adminRepository.save(any(Admin.class))).thenReturn(admin);

        adminService.updateLastLoginTime(admin.getUsername());
        assertNotNull(admin.getLastLoginAt());
        verify(adminRepository, times(1)).save(admin);
    }

    @Test
    void testUpdateLastLoginTimeNotFound() {
        when(adminRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> adminService.updateLastLoginTime("nonexistent"));
    }
} 