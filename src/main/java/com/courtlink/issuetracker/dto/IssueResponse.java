package com.courtlink.issuetracker.dto;

import com.courtlink.issuetracker.entity.Issue;
import com.courtlink.issuetracker.enums.IssueModule;
import com.courtlink.issuetracker.enums.IssuePriority;
import com.courtlink.issuetracker.enums.IssueStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "ȱ����Ӧ")
public record IssueResponse(
    @Schema(description = "ȱ��ID")
    Long id,

    @Schema(description = "ȱ�ݱ���")
    String title,

    @Schema(description = "ȱ������")
    String description,

    @Schema(description = "��ͼURL")
    String screenshotUrl,

    @Schema(description = "״̬")
    IssueStatus status,

    @Schema(description = "���ȼ�")
    IssuePriority priority,

    @Schema(description = "����ģ��")
    IssueModule module,

    @Schema(description = "����ʱ��")
    LocalDateTime createdAt,

    @Schema(description = "����ʱ��")
    LocalDateTime updatedAt
) {
    public static IssueResponse from(Issue issue) {
        return new IssueResponse(
            issue.getId(),
            issue.getTitle(),
            issue.getDescription(),
            issue.getScreenshotUrl(),
            issue.getStatus(),
            issue.getPriority(),
            issue.getModule(),
            issue.getCreatedAt(),
            issue.getUpdatedAt()
        );
    }
} 