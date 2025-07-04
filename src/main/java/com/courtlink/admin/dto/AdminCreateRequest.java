package com.courtlink.admin.dto;

import com.courtlink.admin.entity.Admin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminCreateRequest(
    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, max = 50, message = "Username length must be between 3-50 characters")
    String username,
    
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    String email,
    
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, message = "Password must be at least 6 characters")
    String password,
    
    @NotBlank(message = "Full name cannot be empty")
    String fullName,
    
    Admin.AdminRole role
) {
    public AdminCreateRequest {
        if (role == null) {
            role = Admin.AdminRole.ADMIN;
        }
    }
} 
