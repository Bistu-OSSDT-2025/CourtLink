package com.bistu.ossdt.courtlink.user.controller;

import com.bistu.ossdt.courtlink.user.dto.LoginRequest;
import com.bistu.ossdt.courtlink.user.dto.LoginResponse;
import com.bistu.ossdt.courtlink.user.dto.UserDTO;
import com.bistu.ossdt.courtlink.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    
    private final UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody UserDTO userDTO) {
        log.info("用户注册请求: {}", userDTO.getUsername());
        UserDTO result = userService.register(userDTO);
        return ResponseEntity.ok(result);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("用户登录请求: {}", loginRequest.getUsername());
        LoginResponse result = userService.login(loginRequest);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        UserDTO result = userService.findById(id);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取所有用户（分页）
     */
    @GetMapping
    public ResponseEntity<Page<UserDTO>> getAllUsers(Pageable pageable) {
        Page<UserDTO> result = userService.findAll(pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取所有用户（列表）
     */
    @GetMapping("/list")
    public ResponseEntity<List<UserDTO>> getAllUsersList() {
        List<UserDTO> result = userService.findAll();
        return ResponseEntity.ok(result);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
        log.info("更新用户信息请求: {}", id);
        UserDTO result = userService.update(id, userDTO);
        return ResponseEntity.ok(result);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("删除用户请求: {}", id);
        userService.delete(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@RequestParam String username) {
        boolean exists = userService.existsByUsername(username);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /**
     * 检查用户名是否存在（路径参数方式）
     */
    @GetMapping("/exists/username/{username}")
    public ResponseEntity<Boolean> checkUsernameExists(@PathVariable String username) {
        boolean exists = userService.existsByUsername(username);
        return ResponseEntity.ok(exists);
    }

    /**
     * 检查邮箱是否已被使用
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /**
     * 检查邮箱是否存在（路径参数方式）
     */
    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> checkEmailExists(@PathVariable String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }

    /**
     * 根据用户名获取用户信息
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        UserDTO result = userService.findByUsername(username);
        return ResponseEntity.ok(result);
    }

    /**
     * 根据邮箱获取用户信息
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        UserDTO result = userService.findByEmail(email);
        return ResponseEntity.ok(result);
    }

    /**
     * 验证密码
     */
    @PostMapping("/{id}/validate-password")
    public ResponseEntity<Map<String, Boolean>> validatePassword(
            @PathVariable Long id, 
            @RequestBody Map<String, String> request) {
        String password = request.get("password");
        boolean valid = userService.validatePassword(id, password);
        return ResponseEntity.ok(Map.of("valid", valid));
    }

    /**
     * 验证密码（另一种格式）
     */
    @PostMapping("/verify-password")
    public ResponseEntity<Boolean> verifyPassword(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        String password = request.get("password").toString();
        boolean valid = userService.validatePassword(userId, password);
        return ResponseEntity.ok(valid);
    }

    /**
     * 更改密码
     */
    @PostMapping("/{id}/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        
        log.info("更改密码请求: {}", id);
        userService.changePassword(id, oldPassword, newPassword);
        
        return ResponseEntity.ok(Map.of("message", "密码更改成功"));
    }

    /**
     * 更改密码（另一种格式）
     */
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changeUserPassword(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        String oldPassword = request.get("oldPassword").toString();
        String newPassword = request.get("newPassword").toString();
        
        log.info("更改密码请求: {}", userId);
        userService.changePassword(userId, oldPassword, newPassword);
        
        return ResponseEntity.ok(Map.of("success", true, "message", "密码更改成功"));
    }

    /**
     * 重置密码（管理员功能）
     */
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String newPassword = request.get("newPassword");
        
        log.info("重置密码请求: {}", id);
        userService.resetPassword(id, newPassword);
        
        return ResponseEntity.ok(Map.of("message", "密码重置成功"));
    }

    /**
     * 启用/禁用用户
     */
    @PostMapping("/{id}/toggle-status")
    public ResponseEntity<Map<String, String>> toggleUserStatus(@PathVariable Long id) {
        log.info("切换用户状态请求: {}", id);
        userService.toggleUserStatus(id);
        return ResponseEntity.ok(Map.of("message", "用户状态切换成功"));
    }

    /**
     * 激活用户
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<Map<String, Object>> activateUser(@PathVariable Long id) {
        log.info("激活用户请求: {}", id);
        userService.activateUser(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "用户激活成功"));
    }

    /**
     * 停用用户
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateUser(@PathVariable Long id) {
        log.info("停用用户请求: {}", id);
        userService.deactivateUser(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "用户停用成功"));
    }
} 