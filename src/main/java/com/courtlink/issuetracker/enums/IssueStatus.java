package com.courtlink.issuetracker.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "缺陷状态")
public enum IssueStatus {
    NEW("新建"),
    IN_PROGRESS("处理中"),
    RESOLVED("已解决"),
    CLOSED("已关闭");

    private final String description;

    IssueStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 