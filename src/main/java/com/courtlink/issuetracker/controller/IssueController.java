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

@Tag(name = "ȱ�ݹ���", description = "ȱ��׷��ϵͳ��ؽӿ�")
@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
public class IssueController {

    private final IssueService issueService;

    @Operation(summary = "����ȱ��")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IssueResponse createIssue(@Valid @RequestBody IssueRequest request) {
        return issueService.createIssue(request);
    }

    @Operation(summary = "��ѯȱ���б�")
    @GetMapping
    public Page<IssueResponse> findIssues(
        @Parameter(description = "ȱ��״̬") 
        @RequestParam(required = false) IssueStatus status,
        
        @Parameter(description = "���ȼ�") 
        @RequestParam(required = false) IssuePriority priority,
        
        @Parameter(description = "����ģ��") 
        @RequestParam(required = false) IssueModule module,
        
        @Parameter(description = "��ҳ����") 
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return issueService.findIssues(status, priority, module, pageable);
    }

    @Operation(summary = "��ȡȱ������")
    @GetMapping("/{id}")
    public IssueResponse getIssue(
        @Parameter(description = "ȱ��ID") 
        @PathVariable Long id
    ) {
        return issueService.getIssue(id);
    }

    @Operation(summary = "����ȱ��״̬")
    @PatchMapping("/{id}/status")
    public IssueResponse updateIssueStatus(
        @Parameter(description = "ȱ��ID") 
        @PathVariable Long id,
        
        @Parameter(description = "��״̬") 
        @RequestParam IssueStatus status
    ) {
        return issueService.updateIssueStatus(id, status);
    }
} 