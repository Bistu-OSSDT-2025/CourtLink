package com.courtlink.issuetracker.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ȱ��״̬")
public enum IssueStatus {
    NEW("�½�"),
    IN_PROGRESS("������"),
    RESOLVED("�ѽ��"),
    CLOSED("�ѹر�");

    private final String description;

    IssueStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 