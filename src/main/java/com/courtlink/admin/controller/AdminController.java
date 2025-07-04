package com.courtlink.admin.controller;

import com.courtlink.admin.dto.*;
// import com.courtlink.admin.entity.Admin;
import com.courtlink.admin.service.AdminService;
// import org.springdoc.core.annotations.ParameterObject;
import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "����Ա����", description = "����Ա�˻���������֤����Ȩ��ؽӿ�")
public class AdminController {
    
    @Autowired
    private AdminService adminService;
    
    @PostMapping("/auth/login")
    @Operation(summary = "����Ա��¼", description = "����Ա�˻���¼��֤")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "��¼�ɹ�"),
        @ApiResponse(responseCode = "401", description = "��¼ʧ�ܣ��û������������"),
        @ApiResponse(responseCode = "403", description = "�˻��ѱ�����")
    })
    public ResponseEntity<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        AdminLoginResponse response = adminService.login(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
    
    @PostMapping("/auth/logout")
    @Operation(summary = "����Ա�ǳ�", description = "����Ա�˻���ȫ�ǳ�")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "�ǳ��ɹ�"),
        @ApiResponse(responseCode = "400", description = "�ǳ�ʧ��")
    })
    public ResponseEntity<Map<String, Object>> logout(@RequestHeader("Authorization") String token) {
        boolean success = adminService.logout(token);
        
        Map<String, Object> response = Map.of(
            "success", success,
            "message", success ? "�ǳ��ɹ�" : "�ǳ�ʧ��"
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/admins")
    @Operation(summary = "��������Ա", description = "�����µĹ���Ա�˻�")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "�����ɹ�"),
        @ApiResponse(responseCode = "400", description = "�����������"),
        @ApiResponse(responseCode = "409", description = "�û����Ѵ���")
    })
    public ResponseEntity<Map<String, Object>> createAdmin(@Valid @RequestBody AdminCreateRequest request) {
        try {
            AdminDTO admin = adminService.createAdmin(request);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "����Ա�����ɹ�",
                "data", admin
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "��������Աʧ��: " + e.getMessage()
            );
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/admins")
    @Operation(summary = "��ȡ����Ա�б�", description = "��ҳ��ȡ���й���Ա�˻���Ϣ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "��ѯ�ɹ�"),
        @ApiResponse(responseCode = "403", description = "û��Ȩ�޲鿴����Ա�б�")
    })
    public ResponseEntity<Map<String, Object>> getAllAdmins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<AdminDTO> adminPage = adminService.getAllAdmins(pageable);
        
        Map<String, Object> response = Map.of(
            "success", true,
            "data", adminPage.getContent(),
            "pagination", Map.of(
                "currentPage", adminPage.getNumber(),
                "totalPages", adminPage.getTotalPages(),
                "totalElements", adminPage.getTotalElements(),
                "size", adminPage.getSize()
            )
        );
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/admins/{id}")
    @Operation(summary = "��ȡ����Ա����", description = "����ID��ȡ����Ա��ϸ��Ϣ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "��ѯ�ɹ�"),
        @ApiResponse(responseCode = "404", description = "����Ա������")
    })
    public ResponseEntity<Map<String, Object>> getAdminById(@PathVariable Long id) {
        Optional<AdminDTO> admin = adminService.getAdminById(id);
        
        if (admin.isPresent()) {
            Map<String, Object> response = Map.of(
                "success", true,
                "data", admin.get()
            );
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "����Ա������"
            );
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/admins/{id}")
    @Operation(summary = "���¹���Ա��Ϣ", description = "����ָ������Ա���˻���Ϣ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "���³ɹ�"),
        @ApiResponse(responseCode = "400", description = "�����������"),
        @ApiResponse(responseCode = "404", description = "����Ա������")
    })
    public ResponseEntity<Map<String, Object>> updateAdmin(@PathVariable Long id, 
                                                           @Valid @RequestBody AdminCreateRequest request) {
        try {
            AdminDTO admin = adminService.updateAdmin(id, request);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "����Ա��Ϣ���³ɹ�",
                "data", admin
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "���¹���Ա��Ϣʧ��: " + e.getMessage()
            );
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @DeleteMapping("/admins/{id}")
    @Operation(summary = "ɾ������Ա", description = "ɾ��ָ���Ĺ���Ա�˻�")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "ɾ���ɹ�"),
        @ApiResponse(responseCode = "400", description = "ɾ��ʧ��"),
        @ApiResponse(responseCode = "404", description = "����Ա������")
    })
    public ResponseEntity<Map<String, Object>> deleteAdmin(@PathVariable Long id) {
        boolean success = adminService.deleteAdmin(id);
        
        if (success) {
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "����Աɾ���ɹ�"
            );
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "ɾ������Աʧ��"
            );
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "Get Admin Statistics")
    public ResponseEntity<Map<String, Object>> getAdminStatistics() {
        Map<String, Object> stats = adminService.getAdminStatistics();
        
        Map<String, Object> response = Map.of(
            "success", true,
            "data", stats
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/admins/{id}/activate")
    @Operation(summary = "Activate Admin")
    public ResponseEntity<Map<String, Object>> activateAdmin(@PathVariable Long id) {
        boolean success = adminService.activateAdmin(id);
        
        Map<String, Object> response = Map.of(
            "success", success,
            "message", success ? "Admin activated successfully" : "Failed to activate admin"
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/admins/{id}/deactivate")
    @Operation(summary = "Deactivate Admin")
    public ResponseEntity<Map<String, Object>> deactivateAdmin(@PathVariable Long id) {
        boolean success = adminService.deactivateAdmin(id);
        
        Map<String, Object> response = Map.of(
            "success", success,
            "message", success ? "Admin deactivated successfully" : "Failed to deactivate admin"
        );
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/system/health")
    @Operation(summary = "Admin System Health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        Long totalAdmins = adminService.getTotalAdminCount();
        Long activeAdmins = adminService.getActiveAdminCount();
        Long lockedAdmins = adminService.getLockedAdminCount();
        
        String healthStatus = "HEALTHY";
        if (activeAdmins == 0) {
            healthStatus = "CRITICAL";
        } else if (lockedAdmins > activeAdmins / 2) {
            healthStatus = "WARNING";
        }
        
        Map<String, Object> response = Map.of(
            "success", true,
            "health", healthStatus,
            "data", Map.of(
                "totalAdmins", totalAdmins,
                "activeAdmins", activeAdmins,
                "lockedAdmins", lockedAdmins,
                "healthScore", activeAdmins * 100 / Math.max(totalAdmins, 1)
            )
        );
        
        return ResponseEntity.ok(response);
    }
}
