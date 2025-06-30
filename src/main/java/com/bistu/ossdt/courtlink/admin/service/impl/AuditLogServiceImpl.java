package com.bistu.ossdt.courtlink.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bistu.ossdt.courtlink.admin.mapper.AuditLogMapper;
import com.bistu.ossdt.courtlink.admin.model.AuditLog;
import com.bistu.ossdt.courtlink.admin.service.AuditLogService;
import org.springframework.stereotype.Service;

@Service
public class AuditLogServiceImpl extends ServiceImpl<AuditLogMapper, AuditLog> implements AuditLogService {
} 