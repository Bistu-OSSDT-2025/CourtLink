package com.courtlink.admin.util;

import com.courtlink.admin.entity.Admin;
import com.courtlink.admin.exception.UnauthorizedException;
import com.courtlink.admin.exception.UserNotFoundException;
import com.courtlink.admin.repository.AdminRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class AdminServiceUtils {

    public static Admin getCurrentAuthenticatedAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("未登录或登录已过期");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Admin)) {
            throw new UnauthorizedException("当前用户不是管理员");
        }
        return (Admin) principal;
    }

    public static Admin findAdminByIdOrThrow(AdminRepository adminRepository, long id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("管理员不存在: " + id));
    }

    public static Admin findAdminByUsernameOrThrow(AdminRepository adminRepository, String username) {
        return adminRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("用户不存在: " + username));
    }

    public static void validateAdminExists(AdminRepository adminRepository, String username) {
        Optional<Admin> existingAdmin = adminRepository.findByUsername(username);
        if (existingAdmin.isPresent()) {
            throw new IllegalArgumentException("用户名已存在: " + username);
        }
    }

    public static boolean hasRole(Admin admin, String role) {
        return admin.getRoles().contains(role);
    }

    public static void updateAdminFields(Admin existingAdmin, Admin updateRequest) {
        if (updateRequest.getEmail() != null) {
            existingAdmin.setEmail(updateRequest.getEmail());
        }
        if (updateRequest.getRoles() != null && !updateRequest.getRoles().isEmpty()) {
            existingAdmin.setRoles(updateRequest.getRoles());
        }
        if (updateRequest.getPhone() != null) {
            existingAdmin.setPhone(updateRequest.getPhone());
        }
        if (updateRequest.getRealName() != null) {
            existingAdmin.setRealName(updateRequest.getRealName());
        }
    }
} 