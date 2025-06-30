package com.bistu.ossdt.courtlink.admin.model;

public enum AuditStatus {
    PENDING("待审核"),
    APPROVED("已通过"),
    REJECTED("已驳回");

    private final String description;

    AuditStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 