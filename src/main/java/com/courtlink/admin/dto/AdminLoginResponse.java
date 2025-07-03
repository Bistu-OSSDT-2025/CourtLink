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