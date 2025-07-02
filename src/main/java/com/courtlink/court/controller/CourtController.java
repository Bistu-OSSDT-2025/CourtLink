package com.courtlink.court.controller;

import com.courtlink.court.dto.CourtDTO;
import com.courtlink.court.dto.CourtRequest;
import com.courtlink.court.entity.CourtStatus;
import com.courtlink.court.entity.Court;
import com.courtlink.court.service.CourtService;
import io.swagger.v3.oas.annotations.Operation;
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
} 