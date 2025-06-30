package com.bistu.ossdt.courtlink.admin.controller;

import com.bistu.ossdt.courtlink.admin.model.SystemConfig;
import com.bistu.ossdt.courtlink.admin.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Management", description = "System management API")
public class AdminController {

    private final SystemConfigService systemConfigService;

    @GetMapping("/config")
    @Operation(summary = "Get system configuration list")
    @PreAuthorize("hasRole('ADMIN')")
    public List<SystemConfig> getConfigs() {
        return systemConfigService.list();
    }

    @PostMapping("/config")
    @Operation(summary = "Add system configuration")
    @PreAuthorize("hasRole('ADMIN')")
    public boolean addConfig(@RequestBody SystemConfig config) {
        return systemConfigService.save(config);
    }

    @PutMapping("/config/{id}")
    @Operation(summary = "Update system configuration")
    @PreAuthorize("hasRole('ADMIN')")
    public boolean updateConfig(@PathVariable Long id, @RequestBody SystemConfig config) {
        config.setId(id);
        return systemConfigService.updateById(config);
    }

    @DeleteMapping("/config/{id}")
    @Operation(summary = "Delete system configuration")
    @PreAuthorize("hasRole('ADMIN')")
    public boolean deleteConfig(@PathVariable Long id) {
        return systemConfigService.removeById(id);
    }
} 