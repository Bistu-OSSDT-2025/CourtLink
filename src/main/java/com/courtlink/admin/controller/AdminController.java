package com.courtlink.admin.controller;

import com.courtlink.admin.dto.AdminLoginRequest;
import com.courtlink.admin.entity.Admin;
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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "管理员接口", description = "管理员登录和权限管理相关接口")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/auth/login")
    @Operation(summary = "管理员登录")
    public ResponseEntity<String> login(@Valid @RequestBody AdminLoginRequest request) {
        return ResponseEntity.ok(adminService.login(request));
    }

    @GetMapping("/admin/profile")
    @Operation(summary = "获取当前管理员信息")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Admin> getProfile() {
        return ResponseEntity.ok(adminService.getCurrentAdmin());
    }

    @PutMapping("/admin/profile")
    @Operation(summary = "更新当前管理员信息")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Admin> updateProfile(@Valid @RequestBody Admin adminDetails) {
        Admin currentAdmin = adminService.getCurrentAdmin();
        Admin updatedAdmin = adminService.updateAdmin(currentAdmin.getId(), adminDetails);
        return ResponseEntity.ok(updatedAdmin);
    }

    @GetMapping("/super-admin/dashboard")
    @Operation(summary = "获取超级管理员仪表盘信息")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> getDashboard() {
        return ResponseEntity.ok("Super Admin Dashboard");
    }

    @GetMapping("/public/info")
    @Operation(summary = "获取公共信息")
    public ResponseEntity<String> getPublicInfo() {
        return ResponseEntity.ok("Public Information");
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