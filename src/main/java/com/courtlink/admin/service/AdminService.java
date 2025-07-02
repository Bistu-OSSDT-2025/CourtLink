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
} 