<<<<<<< HEAD
package com.courtlink.admin.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class AdminLoginRequest {
    @NotBlank(message = "用户名或邮箱不能为空")
    private String usernameOrEmail;

    @NotBlank(message = "密码不能为空")
    private String password;
=======
package com.courtlink.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginRequest {
    @NotBlank(message = "Username cannot be empty")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    private String password;
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
} 