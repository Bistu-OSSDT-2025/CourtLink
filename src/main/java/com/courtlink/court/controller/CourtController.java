package com.courtlink.court.controller;

import com.courtlink.court.dto.CourtDTO;
import com.courtlink.court.dto.CourtRequest;
import com.courtlink.court.dto.CourtBatchRequest;
import com.courtlink.court.dto.CourtManagementDTO;
import com.courtlink.court.entity.CourtStatus;
import com.courtlink.court.entity.Court;
import com.courtlink.court.service.CourtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courts")
@RequiredArgsConstructor
@Tag(name = "场地管理", description = "场地管理相关接口")
public class CourtController {

    private final CourtService courtService;

    // ==================== 基础CRUD操作 ====================
    
    @PostMapping
    @Operation(summary = "创建场地")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<CourtDTO> createCourt(@Valid @RequestBody CourtRequest request) {
        return ResponseEntity.ok(courtService.createCourt(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新场地信息")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<CourtDTO> updateCourt(
            @PathVariable Long id,
            @Valid @RequestBody CourtRequest request) {
        return ResponseEntity.ok(courtService.updateCourt(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取场地详情")
    public ResponseEntity<CourtDTO> getCourtById(@PathVariable Long id) {
        return ResponseEntity.ok(courtService.getCourtById(id));
    }

    @GetMapping
    @Operation(summary = "获取所有场地")
    public ResponseEntity<List<CourtDTO>> getAllCourts() {
        return ResponseEntity.ok(courtService.getAllCourts());
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "按状态查询场地")
    public ResponseEntity<List<CourtDTO>> getCourtsByStatus(@PathVariable CourtStatus status) {
        return ResponseEntity.ok(courtService.getCourtsByStatus(status));
    }

    @GetMapping("/type/{courtType}")
    @Operation(summary = "按类型查询场地")
    public ResponseEntity<List<CourtDTO>> getCourtsByType(@PathVariable Court.CourtType courtType) {
        return ResponseEntity.ok(courtService.getCourtsByType(courtType));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除场地")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteCourt(@PathVariable Long id) {
        courtService.deleteCourt(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新场地状态")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<CourtDTO> updateCourtStatus(
            @PathVariable Long id,
            @RequestParam CourtStatus status) {
        return ResponseEntity.ok(courtService.updateCourtStatus(id, status));
    }

    @PutMapping("/{id}/toggle")
    @Operation(summary = "启用/禁用场地")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<CourtDTO> toggleCourtEnabled(@PathVariable Long id) {
        return ResponseEntity.ok(courtService.toggleCourtEnabled(id));
    }

    // ==================== 批量管理操作 ====================
    
    @PostMapping("/batch")
    @Operation(summary = "批量创建场地", description = "一次性创建多个相似的场地")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<CourtDTO>> createMultipleCourts(
            @Valid @RequestBody CourtBatchRequest request) {
        return ResponseEntity.ok(courtService.createMultipleCourts(request));
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除场地", description = "删除多个指定的场地")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteMultipleCourts(
            @RequestBody @Parameter(description = "要删除的场地ID列表") List<Long> courtIds) {
        courtService.deleteMultipleCourts(courtIds);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/batch/status")
    @Operation(summary = "批量更新场地状态", description = "同时更新多个场地的状态")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<CourtDTO>> batchUpdateStatus(
            @RequestBody @Parameter(description = "要更新的场地ID列表") List<Long> courtIds,
            @RequestParam @Parameter(description = "新的状态") CourtStatus status) {
        return ResponseEntity.ok(courtService.batchUpdateStatus(courtIds, status));
    }

    @PutMapping("/batch/toggle")
    @Operation(summary = "批量切换启用状态", description = "批量切换场地的启用/禁用状态")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<CourtDTO>> batchToggleEnabled(
            @RequestBody @Parameter(description = "要切换状态的场地ID列表") List<Long> courtIds) {
        return ResponseEntity.ok(courtService.batchToggleEnabled(courtIds));
    }

    // ==================== 查询和统计 ====================
    
    @GetMapping("/management/info")
    @Operation(summary = "获取场地管理统计信息", description = "获取场地系统的详细统计和健康状态")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<CourtManagementDTO> getCourtManagementInfo() {
        return ResponseEntity.ok(courtService.getCourtManagementInfo());
    }

    @GetMapping("/location/{location}")
    @Operation(summary = "按位置查询场地")
    public ResponseEntity<List<CourtDTO>> getCourtsByLocation(
            @PathVariable Court.CourtLocation location) {
        return ResponseEntity.ok(courtService.getCourtsByLocation(location));
    }

    @GetMapping("/available")
    @Operation(summary = "获取所有可用场地", description = "获取启用且状态为可用的场地")
    public ResponseEntity<List<CourtDTO>> getAvailableCourts() {
        return ResponseEntity.ok(courtService.getAvailableCourts());
    }

    @GetMapping("/maintenance")
    @Operation(summary = "获取维护中的场地")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<CourtDTO>> getCourtsInMaintenance() {
        return ResponseEntity.ok(courtService.getCourtsInMaintenance());
    }

    // ==================== 维护管理 ====================
    
    @PutMapping("/{id}/maintenance/start")
    @Operation(summary = "开始维护模式", description = "将场地设置为维护状态")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<CourtDTO> setMaintenanceMode(
            @PathVariable Long id,
            @RequestParam @Parameter(description = "维护原因") String reason) {
        return ResponseEntity.ok(courtService.setMaintenanceMode(id, reason));
    }

    @PutMapping("/{id}/maintenance/end")
    @Operation(summary = "结束维护模式", description = "结束场地维护，恢复可用状态")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<CourtDTO> endMaintenanceMode(@PathVariable Long id) {
        return ResponseEntity.ok(courtService.endMaintenanceMode(id));
    }
} 