package com.courtlink.issuetracker.service;

import com.courtlink.issuetracker.dto.IssueRequest;
import com.courtlink.issuetracker.dto.IssueResponse;
import com.courtlink.issuetracker.enums.IssueModule;
import com.courtlink.issuetracker.enums.IssuePriority;
import com.courtlink.issuetracker.enums.IssueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IssueService {
    IssueResponse createIssue(IssueRequest request);
    
    Page<IssueResponse> findIssues(
        IssueStatus status,
        IssuePriority priority,
        IssueModule module,
        Pageable pageable
    );
    
    IssueResponse getIssue(Long id);
    
    IssueResponse updateIssueStatus(Long id, IssueStatus status);
} 