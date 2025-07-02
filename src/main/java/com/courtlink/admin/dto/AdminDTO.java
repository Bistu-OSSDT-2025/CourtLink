package com.courtlink.admin.dto;

import com.courtlink.admin.entity.Admin;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class AdminDTO {
    private Long id;
    private String username;
    private String email;
    private Set<String> roles;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String phone;
    private String realName;
    private LocalDateTime lastLoginAt;

    public static AdminDTO fromEntity(Admin admin) {
        AdminDTO dto = new AdminDTO();
        dto.setId(admin.getId());
        dto.setUsername(admin.getUsername());
        dto.setEmail(admin.getEmail());
        dto.setRoles(admin.getRoles());
        dto.setEnabled(admin.isEnabled());
        dto.setCreatedAt(admin.getCreatedAt() != null ? admin.getCreatedAt().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null);
        dto.setUpdatedAt(admin.getUpdatedAt() != null ? admin.getUpdatedAt().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null);
        dto.setPhone(admin.getPhone());
        dto.setRealName(admin.getRealName());
        dto.setLastLoginAt(admin.getLastLoginAt() != null ? admin.getLastLoginAt().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null);
        return dto;
    }
} 