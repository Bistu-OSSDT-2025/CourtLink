package com.bistu.ossdt.courtlink.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户登录响应对象")
public class LoginResponse {
    
    @Schema(description = "访问令牌", example = "simple-token-1-1234567890")
    private String token;
    
    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType = "Bearer";
    
    @Schema(description = "用户信息")
    private UserDTO user;
    
    public LoginResponse(String token, UserDTO user) {
        this.token = token;
        this.user = user;
    }
} 