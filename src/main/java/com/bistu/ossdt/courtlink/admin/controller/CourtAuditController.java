package com.bistu.ossdt.courtlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bistu.ossdt.courtlink.admin.dto.AuditDecisionRequest;
import com.bistu.ossdt.courtlink.admin.dto.CourtAuditDTO;
import com.bistu.ossdt.courtlink.admin.model.AuditStatus;
import com.bistu.ossdt.courtlink.admin.service.CourtAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/audit")
@RequiredArgsConstructor
@Tag(name = "Court Audit Management", description = "Court application audit API")
@PreAuthorize("hasRole('ADMIN')")
public class CourtAuditController {

    private final CourtAuditService courtAuditService;

    @GetMapping("/pending")
    @Operation(summary = "Get pending audits", description = "Get paginated pending court applications")
    public IPage<CourtAuditDTO> getPendingAudits(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        return courtAuditService.getPendingAudits(page, size);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all audit records", description = "Get paginated all audit records")
    public IPage<CourtAuditDTO> getAllAudits(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        return courtAuditService.getAllAudits(page, size);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get audits by status", description = "Get paginated audit records by status")
    public IPage<CourtAuditDTO> getAuditsByStatus(
            @Parameter(description = "Audit status") @PathVariable AuditStatus status,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        return courtAuditService.getAuditsByStatus(status, page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get audit details", description = "Get audit application details by ID")
    public CourtAuditDTO getAuditDetail(@Parameter(description = "Audit ID") @PathVariable Long id) {
        return courtAuditService.getAuditById(id);
    }

    @PostMapping("/{id}/decision")
    @Operation(summary = "Audit application", description = "Make audit decision for court application")
    public boolean auditApplication(
            @Parameter(description = "Audit ID") @PathVariable Long id,
            @Valid @RequestBody AuditDecisionRequest request) {
        // Should get current user info from security context, hardcoded for now
        String auditorId = "admin";
        String auditorName = "System Administrator";
        return courtAuditService.auditCourtApplication(id, request, auditorId, auditorName);
    }

    @PostMapping("/batch-decision")
    @Operation(summary = "Batch audit", description = "Batch audit multiple court applications")
    public boolean batchAudit(
            @Parameter(description = "Audit ID list") @RequestParam List<Long> auditIds,
            @Valid @RequestBody AuditDecisionRequest request) {
        // Should get current user info from security context, hardcoded for now
        String auditorId = "admin";
        String auditorName = "System Administrator";
        return courtAuditService.batchAudit(auditIds, request, auditorId, auditorName);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get audit statistics", description = "Get statistics for various audit statuses")
    public Map<String, Long> getAuditStatistics() {
        return courtAuditService.getAuditStatistics();
    }
} 