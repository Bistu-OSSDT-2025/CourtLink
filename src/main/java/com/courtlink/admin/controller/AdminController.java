package com.courtlink.admin.controller;

import com.courtlink.admin.dto.AdminLoginRequest;
<<<<<<< HEAD
import com.courtlink.admin.dto.AdminLoginResponse;
import com.courtlink.admin.dto.AdminResponse;
import com.courtlink.admin.entity.Admin;
import com.courtlink.admin.service.AdminService;

=======
import com.courtlink.admin.entity.Admin;
import com.courtlink.admin.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
<<<<<<< HEAD
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
=======
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "管理员接口", description = "管理员登录和权限管理相关接口")
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
public class AdminController {

    private final AdminService adminService;

<<<<<<< HEAD
    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        AdminLoginResponse response = adminService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MODERATOR')")
    public ResponseEntity<AdminResponse> getProfile(Authentication authentication) {
        AdminResponse response = adminService.getProfile(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MODERATOR')")
    public ResponseEntity<AdminResponse> updateProfile(
            @Valid @RequestBody Admin admin,
            Authentication authentication) {
        String username = authentication.getName();
        AdminResponse currentAdmin = adminService.getProfile(username);
        AdminResponse response = adminService.updateAdmin(currentAdmin.getId(), admin);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<List<AdminResponse>> getAllAdmins() {
        List<AdminResponse> response = adminService.getAllAdmins();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<AdminResponse> createAdmin(@Valid @RequestBody Admin admin) {
        AdminResponse response = adminService.createAdmin(admin);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<AdminResponse> updateAdmin(
            @PathVariable Long id,
            @Valid @RequestBody Admin admin) {
        AdminResponse response = adminService.updateAdmin(id, admin);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<AdminResponse> activateAdmin(@PathVariable Long id) {
        AdminResponse response = adminService.activateAdmin(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<AdminResponse> deactivateAdmin(@PathVariable Long id) {
        AdminResponse response = adminService.deactivateAdmin(id);
        return ResponseEntity.ok(response);
=======
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
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
    }
} 