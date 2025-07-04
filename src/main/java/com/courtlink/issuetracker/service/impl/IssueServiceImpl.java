package com.courtlink.issuetracker.service.impl;

import com.courtlink.issuetracker.dto.IssueRequest;
import com.courtlink.issuetracker.dto.IssueResponse;
import com.courtlink.issuetracker.entity.Issue;
import com.courtlink.issuetracker.enums.IssueModule;
import com.courtlink.issuetracker.enums.IssuePriority;
import com.courtlink.issuetracker.enums.IssueStatus;
import com.courtlink.issuetracker.repository.IssueRepository;
import com.courtlink.issuetracker.service.IssueService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IssueServiceImpl implements IssueService {

    private final IssueRepository issueRepository;

    @Override
    @Transactional
    public IssueResponse createIssue(IssueRequest request) {
        log.info("Creating new issue: {}", request.title());
        
        var issue = new Issue();
        issue.setTitle(request.title());
        issue.setDescription(request.description());
        issue.setScreenshotUrl(request.screenshotUrl());
        issue.setPriority(request.priority());
        issue.setModule(request.module());
        
        var savedIssue = issueRepository.save(issue);
        log.info("Created issue with ID: {}", savedIssue.getId());
        
        return IssueResponse.from(savedIssue);
    }

    @Override
    public Page<IssueResponse> findIssues(
        IssueStatus status,
        IssuePriority priority,
        IssueModule module,
        Pageable pageable
    ) {
        return issueRepository.findByFilters(status, priority, module, pageable)
            .map(IssueResponse::from);
    }

    @Override
    public IssueResponse getIssue(Long id) {
        return issueRepository.findById(id)
            .map(IssueResponse::from)
            .orElseThrow(() -> new EntityNotFoundException("Issue not found: " + id));
    }

    @Override
    @Transactional
    public IssueResponse updateIssueStatus(Long id, IssueStatus status) {
        log.info("Updating issue {} status to {}", id, status);
        
        var issue = issueRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Issue not found: " + id));
            
        issue.setStatus(status);
        var updatedIssue = issueRepository.save(issue);
        
        log.info("Updated issue {} status to {}", id, status);
        return IssueResponse.from(updatedIssue);
    }
} 