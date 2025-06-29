package com.bistu.ossdt.courtlink.user.dto;

import com.bistu.ossdt.courtlink.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
// import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    
    private Long id;
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;
    
    @NotBlank(message = "密码不能为空", groups = {CreateGroup.class})
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;
    
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phoneNumber;
    
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;
    
    private String avatar;
    
    private User.UserStatus status;
    
    private User.UserRole role;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    // 验证组
    public interface CreateGroup {}
    public interface UpdateGroup {}
} 