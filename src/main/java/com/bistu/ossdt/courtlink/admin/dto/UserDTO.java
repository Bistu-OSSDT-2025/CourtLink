package com.bistu.ossdt.courtlink.admin.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String realName;
    private String role;
    private Boolean enabled;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;
} 