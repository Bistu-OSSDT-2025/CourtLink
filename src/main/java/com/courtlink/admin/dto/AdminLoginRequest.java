package com.courtlink.admin.dto;

import jakarta.validation.constraints.NotBlank;

public class AdminLoginRequest {
    
    @NotBlank(message = "Username or email cannot be empty")
    private String usernameOrEmail;
    
    @NotBlank(message = "Password cannot be empty")
    private String password;
    
    private Boolean rememberMe = false;
    
    public AdminLoginRequest() {}
    
    public AdminLoginRequest(String usernameOrEmail, String password) {
        this.usernameOrEmail = usernameOrEmail;
        this.password = password;
    }
    
    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }
    
    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Boolean getRememberMe() {
        return rememberMe;
    }
    
    public void setRememberMe(Boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
} 
