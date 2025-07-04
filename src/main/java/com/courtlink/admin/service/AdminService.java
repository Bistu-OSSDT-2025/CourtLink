<<<<<<< HEAD
package com.courtlink.admin.service;

import com.courtlink.admin.dto.AdminLoginRequest;
import com.courtlink.admin.dto.AdminLoginResponse;
import com.courtlink.admin.dto.AdminResponse;
import com.courtlink.admin.entity.Admin;

import java.util.List;
import java.util.Optional;

public interface AdminService {
    AdminLoginResponse login(AdminLoginRequest request);
    AdminResponse getProfile(String username);
    List<AdminResponse> getAllAdmins();
    AdminResponse createAdmin(Admin admin);
    AdminResponse updateAdmin(Long id, Admin admin);
    void deleteAdmin(Long id);
    AdminResponse activateAdmin(Long id);
    AdminResponse deactivateAdmin(Long id);
    Optional<Admin> findByUsernameOrEmail(String usernameOrEmail);
=======
package com.courtlink.admin.service;

import com.courtlink.admin.dto.AdminLoginRequest;
import com.courtlink.admin.entity.Admin;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface AdminService extends UserDetailsService {
    String login(AdminLoginRequest request);
    Admin createAdmin(Admin admin);
    Admin updateAdmin(Long id, Admin adminDetails);
    void deleteAdmin(Long id);
    Admin getCurrentAdmin();
    List<Admin> findAll();
    List<Admin> findAllAdmins();
    Admin findAdminById(Long id);
    Optional<Admin> findByUsername(String username);
    boolean isAdmin();
    boolean isSuperAdmin();
    void toggleAdminStatus(Long id);
    void updateLastLoginTime(Admin admin);
    void updateLastLoginTime(String username);
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
} 