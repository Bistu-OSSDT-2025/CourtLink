package com.courtlink.admin.dto;

import com.courtlink.admin.entity.Admin;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private Admin.AdminRole role;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    public static AdminResponse fromAdmin(Admin admin) {
        AdminResponse response = new AdminResponse();
        response.setId(admin.getId());
        response.setUsername(admin.getUsername());
        response.setEmail(admin.getEmail());
        response.setFullName(admin.getFullName());
        response.setRole(admin.getRole());
        response.setIsActive(admin.getIsActive());
        response.setCreatedAt(admin.getCreatedAt());
        response.setLastLoginAt(admin.getLastLoginAt());
        return response;
    }
} 