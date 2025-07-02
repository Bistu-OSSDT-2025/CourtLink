package com.courtlink.admin.dto;

import com.courtlink.admin.entity.Admin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDTO {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String realName;
    private Set<String> roles = new HashSet<>();
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    public static AdminDTO fromAdmin(Admin admin) {
        AdminDTO dto = new AdminDTO();
        dto.setId(admin.getId());
        dto.setUsername(admin.getUsername());
        dto.setEmail(admin.getEmail());
        dto.setPhone(admin.getPhone());
        dto.setRealName(admin.getRealName());
        dto.setRoles(admin.getRoles());
        dto.setEnabled(admin.isEnabled());
        dto.setCreatedAt(admin.getCreatedAt());
        dto.setLastLoginAt(admin.getLastLoginAt());
        return dto;
    }
} 