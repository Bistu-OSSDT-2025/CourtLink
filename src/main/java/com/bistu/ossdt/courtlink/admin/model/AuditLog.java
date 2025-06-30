package com.bistu.ossdt.courtlink.admin.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("audit_log")
public class AuditLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String operationType;
    private String operationTarget;
    private String operatorId;
    private String operatorName;
    private String oldValue;
    private String newValue;
    private String ipAddress;
    private LocalDateTime operationTime;
    private String remarks;
} 