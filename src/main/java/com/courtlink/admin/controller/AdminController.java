package com.courtlink.admin.controller;

import com.courtlink.admin.dto.AdminLoginRequest;
import com.courtlink.admin.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "管理员接口", description = "管理员登录和权限管理相关接口")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/login")
    @Operation(summary = "管理员登录")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody AdminLoginRequest request) {
        String token = adminService.login(request);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @GetMapping("/profile")
    @Operation(summary = "获取当前管理员信息")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> getCurrentAdmin() {
        return ResponseEntity.ok(adminService.getCurrentAdmin());
    }

    @GetMapping("/check-auth")
    @Operation(summary = "检查管理员权限")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, Boolean>> checkAuth() {
        return ResponseEntity.ok(Map.of(
            "isAdmin", adminService.isAdmin(),
            "isSuperAdmin", adminService.isSuperAdmin()
        ));
    }
} 