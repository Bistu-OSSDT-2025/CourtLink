package com.courtlink.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应对象")
public class LoginResponse {
    @Schema(description = "访问令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
    
    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType;
    
    @Schema(description = "用户信息")
    private UserDTO user;

    public LoginResponse(String token) {
        this.token = token;
        this.tokenType = "Bearer";
    }
} 