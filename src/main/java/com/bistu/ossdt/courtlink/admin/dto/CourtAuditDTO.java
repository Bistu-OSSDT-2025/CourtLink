package com.bistu.ossdt.courtlink.admin.dto;

import com.bistu.ossdt.courtlink.admin.model.AuditStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourtAuditDTO {
    private Long id;
    private Long courtId;
    private String courtName;
    private String applicantId;
    private String applicantName;
    private AuditStatus status;
    private String auditReason;
    private String auditorId;
    private String auditorName;
    private LocalDateTime submitTime;
    private LocalDateTime auditTime;
    private String remarks;
    
    // 审核相关信息
    private String facilityDescription;
    private String location;
    private Double rentalPrice;
    private String contactInfo;
    private String attachments;
} 