package com.bistu.ossdt.courtlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bistu.ossdt.courtlink.admin.dto.CreateUserRequest;
import com.bistu.ossdt.courtlink.admin.dto.UserDTO;
import com.bistu.ossdt.courtlink.admin.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户管理相关接口")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "创建用户")
    public UserDTO createUser(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    public UserDTO updateUser(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody CreateUserRequest request) {
        return userService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public void deleteUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        userService.deleteUser(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情")
    public UserDTO getUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping
    @Operation(summary = "获取用户列表")
    public IPage<UserDTO> getUsers(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "搜索关键字") @RequestParam(required = false) String search) {
        return userService.getUsers(page, size, search);
    }

    @PutMapping("/{id}/toggle-status")
    @Operation(summary = "切换用户状态")
    public boolean toggleUserStatus(@Parameter(description = "用户ID") @PathVariable Long id) {
        return userService.toggleUserStatus(id);
    }

    @GetMapping("/check-username")
    @Operation(summary = "检查用户名是否存在")
    public boolean checkUsername(@Parameter(description = "用户名") @RequestParam String username) {
        return userService.isUsernameExists(username);
    }

    @GetMapping("/check-email")
    @Operation(summary = "检查邮箱是否存在")
    public boolean checkEmail(@Parameter(description = "邮箱") @RequestParam String email) {
        return userService.isEmailExists(email);
    }
} 