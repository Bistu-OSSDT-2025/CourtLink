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
} 