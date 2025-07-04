<<<<<<< HEAD
package com.courtlink.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginResponse {
    private String token;
    private AdminResponse admin;
    private String message;

    public AdminLoginResponse(String token, AdminResponse admin) {
        this.token = token;
        this.admin = admin;
        this.message = "登录成功";
    }
} 
=======
package com.courtlink.admin.dto;

import com.courtlink.admin.entity.Admin;

import java.time.LocalDateTime;

public class AdminLoginResponse {
    
    private Long adminId;
    private String username;
    private String fullName;
    private String email;
    private String role;
    private String token;
    private LocalDateTime loginTime;
    private String message;
    private boolean success;
    
    public AdminLoginResponse() {}
    
    public AdminLoginResponse(Admin admin, String token) {
        this.adminId = admin.getId();
        this.username = admin.getUsername();
        this.fullName = admin.getFullName();
        this.email = admin.getEmail();
        this.role = admin.getRole().getDisplayName();
        this.token = token;
        this.loginTime = LocalDateTime.now();
        this.success = true;
        this.message = "Login successful";
    }
    
    public static AdminLoginResponse failure(String message) {
        AdminLoginResponse response = new AdminLoginResponse();
        response.success = false;
        response.message = message;
        return response;
    }
    
    public Long getAdminId() {
        return adminId;
    }
    
    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public LocalDateTime getLoginTime() {
        return loginTime;
    }
    
    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
} 
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
