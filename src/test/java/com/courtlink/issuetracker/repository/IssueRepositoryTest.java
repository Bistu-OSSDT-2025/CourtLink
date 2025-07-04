package com.courtlink.issuetracker.repository;

import com.courtlink.issuetracker.entity.Issue;
import com.courtlink.issuetracker.enums.IssueModule;
import com.courtlink.issuetracker.enums.IssuePriority;
import com.courtlink.issuetracker.enums.IssueStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class IssueRepositoryTest {

    @Autowired
    private IssueRepository issueRepository;

    private Issue testIssue;

    @BeforeEach
    void setUp() {
        testIssue = new Issue();
        testIssue.setTitle("≤‚ ‘±ÍÃ‚");
        testIssue.setDescription("≤‚ ‘√Ë ˆ");
        testIssue.setScreenshotUrl("http://example.com/screenshot.png");
        testIssue.setStatus(IssueStatus.NEW);
        testIssue.setPriority(IssuePriority.HIGH);
        testIssue.setModule(IssueModule.FRONTEND);

        issueRepository.save(testIssue);
    }

    @Test
    void findByFilters_NoFilters_ReturnsAllIssues() {
        var result = issueRepository.findByFilters(
            null,
            null,
            null,
            PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo(testIssue.getTitle());
    }

    @Test
    void findByFilters_WithStatus_ReturnsFilteredIssues() {
        var result = issueRepository.findByFilters(
            IssueStatus.NEW,
            null,
            null,
            PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(IssueStatus.NEW);

        result = issueRepository.findByFilters(
            IssueStatus.CLOSED,
            null,
            null,
            PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findByFilters_WithPriority_ReturnsFilteredIssues() {
        var result = issueRepository.findByFilters(
            null,
            IssuePriority.HIGH,
            null,
            PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPriority()).isEqualTo(IssuePriority.HIGH);

        result = issueRepository.findByFilters(
            null,
            IssuePriority.LOW,
            null,
            PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findByFilters_WithModule_ReturnsFilteredIssues() {
        var result = issueRepository.findByFilters(
            null,
            null,
            IssueModule.FRONTEND,
            PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getModule()).isEqualTo(IssueModule.FRONTEND);

        result = issueRepository.findByFilters(
            null,
            null,
            IssueModule.BACKEND,
            PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findByFilters_WithAllFilters_ReturnsFilteredIssues() {
        var result = issueRepository.findByFilters(
            IssueStatus.NEW,
            IssuePriority.HIGH,
            IssueModule.FRONTEND,
            PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(IssueStatus.NEW);
        assertThat(result.getContent().get(0).getPriority()).isEqualTo(IssuePriority.HIGH);
        assertThat(result.getContent().get(0).getModule()).isEqualTo(IssueModule.FRONTEND);
    }
} 