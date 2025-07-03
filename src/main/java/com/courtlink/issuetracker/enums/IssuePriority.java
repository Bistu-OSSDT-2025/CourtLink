package com.courtlink.issuetracker.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ȱ�����ȼ�")
public enum IssuePriority {
    LOW("��"),
    MEDIUM("��"),
    HIGH("��"),
    CRITICAL("����");

    private final String description;

    IssuePriority(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 