package com.courtlink.issuetracker.dto;

import com.courtlink.issuetracker.entity.Issue;
import com.courtlink.issuetracker.enums.IssueModule;
import com.courtlink.issuetracker.enums.IssuePriority;
import com.courtlink.issuetracker.enums.IssueStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "缺陷响应")
public record IssueResponse(
    @Schema(description = "缺陷ID")
    Long id,

    @Schema(description = "缺陷标题")
    String title,

    @Schema(description = "缺陷描述")
    String description,

    @Schema(description = "截图URL")
    String screenshotUrl,

    @Schema(description = "状态")
    IssueStatus status,

    @Schema(description = "优先级")
    IssuePriority priority,

    @Schema(description = "所属模块")
    IssueModule module,

    @Schema(description = "创建时间")
    LocalDateTime createdAt,

    @Schema(description = "更新时间")
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