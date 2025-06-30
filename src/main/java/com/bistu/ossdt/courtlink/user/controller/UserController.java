package com.bistu.ossdt.courtlink.user.controller;

import com.bistu.ossdt.courtlink.user.dto.LoginRequest;
import com.bistu.ossdt.courtlink.user.dto.LoginResponse;
import com.bistu.ossdt.courtlink.user.dto.UserDTO;
import com.bistu.ossdt.courtlink.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
@Tag(name = "User Management", description = "用户管理相关接口")
public class UserController {
    
    private final UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册新用户账户")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "注册成功"),
        @ApiResponse(responseCode = "400", description = "注册信息验证失败"),
        @ApiResponse(responseCode = "409", description = "用户名或邮箱已存在")
    })
    public ResponseEntity<UserDTO> register(
            @Parameter(description = "用户注册信息", required = true)
            @Valid @RequestBody UserDTO userDTO) {
        log.info("用户注册请求: {}", userDTO.getUsername());
        UserDTO result = userService.register(userDTO);
        return ResponseEntity.ok(result);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户账户登录验证")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "登录成功"),
        @ApiResponse(responseCode = "400", description = "登录信息验证失败"),
        @ApiResponse(responseCode = "401", description = "用户名或密码错误")
    })
    public ResponseEntity<LoginResponse> login(
            @Parameter(description = "用户登录信息", required = true)
            @Valid @RequestBody LoginRequest loginRequest) {
        log.info("用户登录请求: {}", loginRequest.getUsername());
        LoginResponse result = userService.login(loginRequest);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    public ResponseEntity<UserDTO> getUser(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long id) {
        UserDTO result = userService.findById(id);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取所有用户（分页）
     */
    @GetMapping
    @Operation(summary = "获取用户列表", description = "分页获取所有用户信息")
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @Parameter(description = "分页参数") Pageable pageable) {
        Page<UserDTO> result = userService.findAll(pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取所有用户（列表）
     */
    @GetMapping("/list")
    @Operation(summary = "获取所有用户", description = "获取所有用户信息列表")
    public ResponseEntity<List<UserDTO>> getAllUsersList() {
        List<UserDTO> result = userService.findAll();
        return ResponseEntity.ok(result);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新用户信息", description = "更新指定用户的信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "400", description = "参数验证失败"),
        @ApiResponse(responseCode = "404", description = "用户不存在"),
        @ApiResponse(responseCode = "409", description = "用户名或邮箱已被使用")
    })
    public ResponseEntity<UserDTO> updateUser(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long id, 
            @Parameter(description = "用户更新信息", required = true)
            @Valid @RequestBody UserDTO userDTO) {
        log.info("更新用户信息请求: {}", id);
        UserDTO result = userService.update(id, userDTO);
        return ResponseEntity.ok(result);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "删除指定用户账户")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功"),
        @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long id) {
        log.info("删除用户请求: {}", id);
        userService.delete(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check-username")
    @Operation(summary = "检查用户名", description = "检查用户名是否已被使用")
    public ResponseEntity<Map<String, Boolean>> checkUsername(
            @Parameter(description = "用户名", required = true, example = "john_doe")
            @RequestParam String username) {
        boolean exists = userService.existsByUsername(username);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /**
     * 检查用户名是否存在（路径参数方式）
     */
    @GetMapping("/exists/username/{username}")
    @Operation(summary = "检查用户名存在性", description = "通过路径参数检查用户名是否存在")
    public ResponseEntity<Boolean> checkUsernameExists(
            @Parameter(description = "用户名", required = true, example = "john_doe")
            @PathVariable String username) {
        boolean exists = userService.existsByUsername(username);
        return ResponseEntity.ok(exists);
    }

    /**
     * 检查邮箱是否已被使用
     */
    @GetMapping("/check-email")
    @Operation(summary = "检查邮箱", description = "检查邮箱是否已被注册")
    public ResponseEntity<Map<String, Boolean>> checkEmail(
            @Parameter(description = "邮箱地址", required = true, example = "john@example.com")
            @RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /**
     * 检查邮箱是否存在（路径参数方式）
     */
    @GetMapping("/exists/email/{email}")
    @Operation(summary = "检查邮箱存在性", description = "通过路径参数检查邮箱是否存在")
    public ResponseEntity<Boolean> checkEmailExists(
            @Parameter(description = "邮箱地址", required = true, example = "john@example.com")
            @PathVariable String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }

    /**
     * 根据用户名获取用户信息
     */
    @GetMapping("/username/{username}")
    @Operation(summary = "根据用户名获取用户", description = "通过用户名查询用户信息")
    public ResponseEntity<UserDTO> getUserByUsername(
            @Parameter(description = "用户名", required = true, example = "john_doe")
            @PathVariable String username) {
        UserDTO result = userService.findByUsername(username);
        return ResponseEntity.ok(result);
    }

    /**
     * 根据邮箱获取用户信息
     */
    @GetMapping("/email/{email}")
    @Operation(summary = "根据邮箱获取用户", description = "通过邮箱查询用户信息")
    public ResponseEntity<UserDTO> getUserByEmail(
            @Parameter(description = "邮箱地址", required = true, example = "john@example.com")
            @PathVariable String email) {
        UserDTO result = userService.findByEmail(email);
        return ResponseEntity.ok(result);
    }

    /**
     * 验证密码
     */
    @PostMapping("/{id}/validate-password")
    @Operation(summary = "验证密码", description = "验证用户密码是否正确")
    public ResponseEntity<Map<String, Boolean>> validatePassword(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long id, 
            @Parameter(description = "密码验证请求", required = true)
            @RequestBody Map<String, String> request) {
        String password = request.get("password");
        boolean valid = userService.validatePassword(id, password);
        return ResponseEntity.ok(Map.of("valid", valid));
    }

    /**
     * 验证密码（另一种格式）
     */
    @PostMapping("/verify-password")
    @Operation(summary = "验证用户密码", description = "验证指定用户的密码")
    public ResponseEntity<Boolean> verifyPassword(
            @Parameter(description = "密码验证请求", required = true)
            @RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        String password = request.get("password").toString();
        boolean valid = userService.validatePassword(userId, password);
        return ResponseEntity.ok(valid);
    }

    /**
     * 更改密码
     */
    @PostMapping("/{id}/change-password")
    @Operation(summary = "更改密码", description = "更改用户登录密码")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "密码更改成功"),
        @ApiResponse(responseCode = "400", description = "原密码错误"),
        @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    public ResponseEntity<Map<String, String>> changePassword(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "密码更改请求", required = true)
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

    /**
     * 简单健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", java.time.LocalDateTime.now());
        health.put("application", "CourtLink");
        health.put("message", "用户管理模块正常运行");
        return ResponseEntity.ok(health);
    }
} 