package com.bistu.ossdt.courtlink.admin.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("court_audit")
public class CourtAudit {
    @TableId(type = IdType.AUTO)
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
    
    // Audit related information
    private String facilityDescription;
    private String location;
    private Double rentalPrice;
    private String contactInfo;
    private String attachments; // JSON format for attachment information
} 