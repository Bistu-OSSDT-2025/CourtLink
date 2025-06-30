package com.bistu.ossdt.courtlink.admin.model;

public enum AuditStatus {
    PENDING("�����"),
    APPROVED("��ͨ��"),
    REJECTED("�Ѳ���");

    private final String description;

    AuditStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 