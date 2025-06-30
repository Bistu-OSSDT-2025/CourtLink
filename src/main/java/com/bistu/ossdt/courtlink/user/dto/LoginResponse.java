package com.bistu.ossdt.courtlink.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    private String token;
    
    private String tokenType = "Bearer";
    
    private UserDTO user;
    
    public LoginResponse(String token, UserDTO user) {
        this.token = token;
        this.user = user;
    }
} 