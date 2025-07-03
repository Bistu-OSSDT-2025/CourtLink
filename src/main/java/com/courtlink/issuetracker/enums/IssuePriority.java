package com.courtlink.issuetracker.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "缺陷优先级")
public enum IssuePriority {
    LOW("低"),
    MEDIUM("中"),
    HIGH("高"),
    CRITICAL("紧急");

    private final String description;

    IssuePriority(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 