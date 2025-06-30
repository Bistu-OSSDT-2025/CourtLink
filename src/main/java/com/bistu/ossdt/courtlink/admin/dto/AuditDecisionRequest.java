package com.bistu.ossdt.courtlink.admin.dto;

import com.bistu.ossdt.courtlink.admin.model.AuditStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuditDecisionRequest {
    @NotNull(message = "���״̬����Ϊ��")
    private AuditStatus status;
    
    private String auditReason;
    private String remarks;
} 