package com.courtlink.issuetracker.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "缺陷所属模块")
public enum IssueModule {
    USER("用户模块"),
    COURT("场地模块"),
    BOOKING("预订模块"),
    PAYMENT("支付模块"),
    FRONTEND("前端界面"),
    OTHER("其他");

    private final String description;

    IssueModule(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 