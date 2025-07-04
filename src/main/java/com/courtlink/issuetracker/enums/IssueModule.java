package com.courtlink.issuetracker.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ȱ������ģ��")
public enum IssueModule {
    USER("�û�ģ��"),
    COURT("����ģ��"),
    BOOKING("Ԥ��ģ��"),
    PAYMENT("֧��ģ��"),
    FRONTEND("ǰ�˽���"),
    OTHER("����");

    private final String description;

    IssueModule(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 