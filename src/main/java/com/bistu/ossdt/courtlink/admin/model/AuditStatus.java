package com.bistu.ossdt.courtlink.admin.model;

public enum AuditStatus {
    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected");

    private final String description;

    AuditStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 