package com.courtlink.user.controller;

import com.courtlink.user.dto.LoginRequest;
import com.courtlink.user.dto.LoginResponse;
import com.courtlink.user.dto.UserDTO;
import com.courtlink.user.service.UserService;
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

import java.util.List;
import java.util.Map;

@Tag(name = "用户管理", description = "用户注册、登录、信息管理等接口")
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    
    private final UserService userService;

    @Operation(summary = "用户注册", description = "创建新用户账户")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "注册成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody UserDTO userDTO) {
        log.info("用户注册请求: {}", userDTO.getUsername());
        UserDTO result = userService.register(userDTO);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "用户登录", description = "用户身份验证和登录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "登录成功"),
        @ApiResponse(responseCode = "401", description = "用户名或密码错误"),
        @ApiResponse(responseCode = "403", description = "账户已被禁用")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("用户登录请求: {}", loginRequest.getUsername());
        LoginResponse result = userService.login(loginRequest);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户详细信息")
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        UserDTO result = userService.findById(id);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "获取用户列表（分页）", description = "分页查询用户列表")
    @GetMapping
    public ResponseEntity<Page<UserDTO>> getAllUsers(Pageable pageable) {
        Page<UserDTO> result = userService.findAll(pageable);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "获取所有用户", description = "获取所有用户的完整列表")
    @GetMapping("/list")
    public ResponseEntity<List<UserDTO>> getAllUsersList() {
        List<UserDTO> result = userService.findAll();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "更新用户信息", description = "更新指定用户的信息")
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id, 
            @Valid @RequestBody UserDTO userDTO) {
        log.info("更新用户信息请求: {}", id);
        UserDTO result = userService.update(id, userDTO);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "删除用户", description = "删除指定的用户账户")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        log.info("删除用户请求: {}", id);
        userService.delete(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "检查用户名是否存在", description = "验证用户名是否已被使用")
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsername(
            @Parameter(description = "用户名", required = true) @RequestParam String username) {
        boolean exists = userService.existsByUsername(username);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @Operation(summary = "检查邮箱是否已被使用", description = "验证邮箱地址是否已被注册")
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(
            @Parameter(description = "邮箱地址", required = true) @RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @Operation(summary = "根据用户名获取用户", description = "通过用户名查询用户信息")
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(
            @Parameter(description = "用户名", required = true) @PathVariable String username) {
        UserDTO result = userService.findByUsername(username);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "根据邮箱获取用户", description = "通过邮箱地址查询用户信息")
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(
            @Parameter(description = "邮箱地址", required = true) @PathVariable String email) {
        UserDTO result = userService.findByEmail(email);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "验证密码", description = "验证用户密码是否正确")
    @PostMapping("/{id}/validate-password")
    public ResponseEntity<Map<String, Boolean>> validatePassword(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id, 
            @RequestBody Map<String, String> request) {
        String password = request.get("password");
        boolean valid = userService.validatePassword(id, password);
        return ResponseEntity.ok(Map.of("valid", valid));
    }

    @Operation(summary = "更改密码", description = "用户更改自己的密码")
    @PostMapping("/{id}/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("修改密码请求: {}", id);
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        userService.changePassword(id, oldPassword, newPassword);
        return ResponseEntity.ok(Map.of("message", "密码修改成功"));
    }

    @Operation(summary = "重置密码", description = "管理员重置用户密码")
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("重置密码请求: {}", id);
        String newPassword = request.get("newPassword");
        userService.resetPassword(id, newPassword);
        return ResponseEntity.ok(Map.of("message", "密码重置成功"));
    }

    @Operation(summary = "切换用户状态", description = "启用或禁用用户账户")
    @PostMapping("/{id}/toggle-status")
    public ResponseEntity<Map<String, String>> toggleUserStatus(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        log.info("切换用户状态请求: {}", id);
        userService.toggleUserStatus(id);
        return ResponseEntity.ok(Map.of("message", "用户状态切换成功"));
    }

    @Operation(summary = "激活用户", description = "激活用户账户")
    @PostMapping("/{id}/activate")
    public ResponseEntity<Map<String, String>> activateUser(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        log.info("激活用户请求: {}", id);
        userService.activateUser(id);
        return ResponseEntity.ok(Map.of("message", "用户激活成功"));
    }

    @Operation(summary = "停用用户", description = "停用用户账户")
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Map<String, String>> deactivateUser(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        log.info("停用用户请求: {}", id);
        userService.deactivateUser(id);
        return ResponseEntity.ok(Map.of("message", "用户停用成功"));
    }

    @Operation(summary = "用户模块健康检查", description = "检查用户管理模块状态")
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "module", "用户管理",
            "timestamp", System.currentTimeMillis()
        ));
    }
} 