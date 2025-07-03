package com.courtlink.issuetracker.controller;

import com.courtlink.issuetracker.dto.IssueRequest;
import com.courtlink.issuetracker.dto.IssueResponse;
import com.courtlink.issuetracker.enums.IssueModule;
import com.courtlink.issuetracker.enums.IssuePriority;
import com.courtlink.issuetracker.enums.IssueStatus;
import com.courtlink.issuetracker.service.IssueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "缺陷管理", description = "缺陷追踪系统相关接口")
@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
public class IssueController {

    private final IssueService issueService;

    @Operation(summary = "创建缺陷")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IssueResponse createIssue(@Valid @RequestBody IssueRequest request) {
        return issueService.createIssue(request);
    }

    @Operation(summary = "查询缺陷列表")
    @GetMapping
    public Page<IssueResponse> findIssues(
        @Parameter(description = "缺陷状态") 
        @RequestParam(required = false) IssueStatus status,
        
        @Parameter(description = "优先级") 
        @RequestParam(required = false) IssuePriority priority,
        
        @Parameter(description = "所属模块") 
        @RequestParam(required = false) IssueModule module,
        
        @Parameter(description = "分页参数") 
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return issueService.findIssues(status, priority, module, pageable);
    }

    @Operation(summary = "获取缺陷详情")
    @GetMapping("/{id}")
    public IssueResponse getIssue(
        @Parameter(description = "缺陷ID") 
        @PathVariable Long id
    ) {
        return issueService.getIssue(id);
    }

    @Operation(summary = "更新缺陷状态")
    @PatchMapping("/{id}/status")
    public IssueResponse updateIssueStatus(
        @Parameter(description = "缺陷ID") 
        @PathVariable Long id,
        
        @Parameter(description = "新状态") 
        @RequestParam IssueStatus status
    ) {
        return issueService.updateIssueStatus(id, status);
    }
} 