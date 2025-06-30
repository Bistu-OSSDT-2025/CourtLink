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
@Tag(name = "后台管理", description = "系统管理接口")
public class AdminController {

    private final SystemConfigService systemConfigService;

    @GetMapping("/config")
    @Operation(summary = "获取系统配置列表")
    @PreAuthorize("hasRole('ADMIN')")
    public List<SystemConfig> getConfigs() {
        return systemConfigService.list();
    }

    @PostMapping("/config")
    @Operation(summary = "新增系统配置")
    @PreAuthorize("hasRole('ADMIN')")
    public boolean addConfig(@RequestBody SystemConfig config) {
        return systemConfigService.save(config);
    }

    @PutMapping("/config/{id}")
    @Operation(summary = "更新系统配置")
    @PreAuthorize("hasRole('ADMIN')")
    public boolean updateConfig(@PathVariable Long id, @RequestBody SystemConfig config) {
        config.setId(id);
        return systemConfigService.updateById(config);
    }

    @DeleteMapping("/config/{id}")
    @Operation(summary = "删除系统配置")
    @PreAuthorize("hasRole('ADMIN')")
    public boolean deleteConfig(@PathVariable Long id) {
        return systemConfigService.removeById(id);
    }
} 