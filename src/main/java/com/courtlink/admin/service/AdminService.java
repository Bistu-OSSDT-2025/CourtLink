package com.courtlink.admin.service;

import com.courtlink.admin.dto.*;
import com.courtlink.admin.entity.Admin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AdminService {
    
    // Authentication related
    AdminLoginResponse login(AdminLoginRequest request);
    boolean logout(String token);
    boolean isTokenValid(String token);
    Admin getCurrentAdmin();
    
    // Admin management
    AdminDTO createAdmin(AdminCreateRequest request);
    AdminDTO updateAdmin(Long id, AdminCreateRequest request);
    boolean deleteAdmin(Long id);
    Optional<AdminDTO> getAdminById(Long id);
    Optional<AdminDTO> getAdminByUsername(String username);
    Optional<AdminDTO> getAdminByEmail(String email);
    
    // Query and pagination
    Page<AdminDTO> getAllAdmins(Pageable pageable);
    Page<AdminDTO> getAdminsByRole(Admin.AdminRole role, Pageable pageable);
    Page<AdminDTO> getAdminsByStatus(Admin.AdminStatus status, Pageable pageable);
    Page<AdminDTO> searchAdminsByName(String name, Pageable pageable);
    List<AdminDTO> getActiveAdmins();
    List<AdminDTO> getLockedAdmins();
    
    // Status management
    boolean activateAdmin(Long id);
    boolean deactivateAdmin(Long id);
    boolean suspendAdmin(Long id);
    boolean lockAdmin(Long id);
    boolean unlockAdmin(Long id);
    boolean resetPassword(Long id, String newPassword);
    
    // Role management
    boolean changeAdminRole(Long id, Admin.AdminRole newRole);
    boolean hasPermission(Admin admin, String permission);
    boolean isSuperAdmin(Admin admin);
    
    // Statistics related
    Map<String, Object> getAdminStatistics();
    Map<Admin.AdminRole, Long> getAdminCountByRole();
    Map<Admin.AdminStatus, Long> getAdminCountByStatus();
    Long getTotalAdminCount();
    Long getActiveAdminCount();
    Long getLockedAdminCount();
    Long getTodayCreatedAdminCount();
    
    // Security related
    boolean checkPassword(String rawPassword, String encodedPassword);
    String encodePassword(String rawPassword);
    void recordFailedLogin(String usernameOrEmail);
    void recordSuccessfulLogin(Admin admin);
    
    // Validation methods
    boolean isUsernameExists(String username);
    boolean isEmailExists(String email);
    boolean canDeleteAdmin(Long id);
    boolean canChangeRole(Admin currentAdmin, Admin targetAdmin, Admin.AdminRole newRole);
} 
