package com.courtlink.controller;

import com.courtlink.dto.CourtDTO;
import com.courtlink.service.CourtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courts")
@RequiredArgsConstructor
@Tag(name = "Court Management", description = "场地管理接口")
public class CourtController {

    private final CourtService courtService;

    @GetMapping
    @Operation(summary = "获取所有场地", description = "获取系统中所有场地的列表")
    public ResponseEntity<List<CourtDTO>> getAllCourts() {
        return ResponseEntity.ok(courtService.getAllCourts());
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取场地详情", description = "根据ID获取场地详细信息")
    public ResponseEntity<CourtDTO> getCourtById(@PathVariable Long id) {
        return ResponseEntity.ok(courtService.getCourtById(id));
    }

    @PostMapping
    @Operation(summary = "创建场地", description = "创建新的场地")
    public ResponseEntity<CourtDTO> createCourt(@RequestBody CourtDTO courtDTO) {
        return ResponseEntity.ok(courtService.createCourt(courtDTO));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新场地", description = "更新现有场地的信息")
    public ResponseEntity<CourtDTO> updateCourt(@PathVariable Long id, @RequestBody CourtDTO courtDTO) {
        return ResponseEntity.ok(courtService.updateCourt(id, courtDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除场地", description = "删除指定的场地")
    public ResponseEntity<Void> deleteCourt(@PathVariable Long id) {
        courtService.deleteCourt(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新场地状态", description = "更新场地的状态（可用、维护中等）")
    public ResponseEntity<CourtDTO> updateCourtStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(courtService.updateCourtStatus(id, status));
    }

    @GetMapping("/search")
    @Operation(summary = "搜索场地", description = "根据关键词和状态搜索场地")
    public ResponseEntity<List<CourtDTO>> searchCourts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(courtService.searchCourts(keyword, status));
    }
} 