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
@Tag(name = "User Management", description = "User management API")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create user")
    public UserDTO createUser(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user")
    public UserDTO updateUser(@PathVariable Long id, @Valid @RequestBody CreateUserRequest request) {
        return userService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user details")
    public UserDTO getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping
    @Operation(summary = "Get user list")
    public IPage<UserDTO> getUsers(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Search keyword") @RequestParam(required = false) String search) {
        return userService.getUsers(page, size, search);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Toggle user status")
    public boolean toggleUserStatus(@PathVariable Long id) {
        return userService.toggleUserStatus(id);
    }

    @GetMapping("/check/username")
    @Operation(summary = "Check if username exists")
    public boolean checkUsername(@RequestParam String username) {
        return userService.isUsernameExists(username);
    }

    @GetMapping("/check/email")
    @Operation(summary = "Check if email exists")
    public boolean checkEmail(@RequestParam String email) {
        return userService.isEmailExists(email);
    }
} 