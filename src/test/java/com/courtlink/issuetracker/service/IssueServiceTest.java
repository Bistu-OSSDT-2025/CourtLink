package com.courtlink.issuetracker.service;

import com.courtlink.issuetracker.dto.IssueRequest;
import com.courtlink.issuetracker.entity.Issue;
import com.courtlink.issuetracker.enums.IssueModule;
import com.courtlink.issuetracker.enums.IssuePriority;
import com.courtlink.issuetracker.enums.IssueStatus;
import com.courtlink.issuetracker.repository.IssueRepository;
import com.courtlink.issuetracker.service.impl.IssueServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IssueServiceTest {

    @Mock
    private IssueRepository issueRepository;

    @InjectMocks
    private IssueServiceImpl issueService;

    private Issue testIssue;
    private IssueRequest testRequest;

    @BeforeEach
    void setUp() {
        testRequest = new IssueRequest(
            "≤‚ ‘±ÍÃ‚",
            "≤‚ ‘√Ë ˆ",
            "http://example.com/screenshot.png",
            IssuePriority.HIGH,
            IssueModule.FRONTEND
        );

        testIssue = new Issue();
        testIssue.setId(1L);
        testIssue.setTitle(testRequest.title());
        testIssue.setDescription(testRequest.description());
        testIssue.setScreenshotUrl(testRequest.screenshotUrl());
        testIssue.setStatus(IssueStatus.NEW);
        testIssue.setPriority(testRequest.priority());
        testIssue.setModule(testRequest.module());
    }

    @Test
    void createIssue_ValidRequest_ReturnsCreatedIssue() {
        when(issueRepository.save(any(Issue.class))).thenReturn(testIssue);

        var response = issueService.createIssue(testRequest);

        assertThat(response.id()).isEqualTo(testIssue.getId());
        assertThat(response.title()).isEqualTo(testIssue.getTitle());
        assertThat(response.status()).isEqualTo(IssueStatus.NEW);
        verify(issueRepository).save(any(Issue.class));
    }

    @Test
    void findIssues_ValidFilters_ReturnsPagedIssues() {
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(List.of(testIssue));

        when(issueRepository.findByFilters(
            IssueStatus.NEW,
            IssuePriority.HIGH,
            IssueModule.FRONTEND,
            pageable
        )).thenReturn(page);

        var response = issueService.findIssues(
            IssueStatus.NEW,
            IssuePriority.HIGH,
            IssueModule.FRONTEND,
            pageable
        );

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).id()).isEqualTo(testIssue.getId());
        verify(issueRepository).findByFilters(any(), any(), any(), any());
    }

    @Test
    void getIssue_ExistingId_ReturnsIssue() {
        when(issueRepository.findById(1L)).thenReturn(Optional.of(testIssue));

        var response = issueService.getIssue(1L);

        assertThat(response.id()).isEqualTo(testIssue.getId());
        assertThat(response.title()).isEqualTo(testIssue.getTitle());
        verify(issueRepository).findById(1L);
    }

    @Test
    void getIssue_NonExistingId_ThrowsException() {
        when(issueRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> issueService.getIssue(1L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Issue not found: 1");
    }

    @Test
    void updateIssueStatus_ExistingIssue_ReturnsUpdatedIssue() {
        when(issueRepository.findById(1L)).thenReturn(Optional.of(testIssue));
        when(issueRepository.save(any(Issue.class))).thenReturn(testIssue);

        var response = issueService.updateIssueStatus(1L, IssueStatus.IN_PROGRESS);

        assertThat(response.id()).isEqualTo(testIssue.getId());
        assertThat(response.status()).isEqualTo(testIssue.getStatus());
        verify(issueRepository).findById(1L);
        verify(issueRepository).save(testIssue);
    }

    @Test
    void updateIssueStatus_NonExistingIssue_ThrowsException() {
        when(issueRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> issueService.updateIssueStatus(1L, IssueStatus.IN_PROGRESS))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Issue not found: 1");
    }
} 