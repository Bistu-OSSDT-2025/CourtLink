package com.courtlink.admin.service;

import com.courtlink.admin.dto.AdminLoginRequest;
import com.courtlink.admin.entity.Admin;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface AdminService extends UserDetailsService {
    String login(AdminLoginRequest request);
    Admin getCurrentAdmin();
    void updateLastLoginTime(String username);
    boolean isAdmin();
    boolean isSuperAdmin();
    List<Admin> findAll();
    Admin findByUsername(String username);
    Admin findAdminById(long id);
    Admin createAdmin(Admin admin);
    Admin updateAdmin(long id, Admin admin);
    void deleteAdmin(long id);
    List<Admin> findAllAdmins();
    Admin toggleAdminStatus(long id, boolean enabled);
    Admin updateLastLoginTime(long id);
} 