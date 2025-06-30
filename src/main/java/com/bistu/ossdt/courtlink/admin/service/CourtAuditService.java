package com.bistu.ossdt.courtlink.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bistu.ossdt.courtlink.admin.dto.AuditDecisionRequest;
import com.bistu.ossdt.courtlink.admin.dto.CourtAuditDTO;
import com.bistu.ossdt.courtlink.admin.model.AuditStatus;
import com.bistu.ossdt.courtlink.admin.model.CourtAudit;

public interface CourtAuditService extends IService<CourtAudit> {
    
    /**
     * 分页获取待审核的场地申请
     */
    IPage<CourtAuditDTO> getPendingAudits(int page, int size);
    
    /**
     * 分页获取指定状态的审核记录
     */
    IPage<CourtAuditDTO> getAuditsByStatus(AuditStatus status, int page, int size);
    
    /**
     * 获取所有审核记录
     */
    IPage<CourtAuditDTO> getAllAudits(int page, int size);
    
    /**
     * 根据ID获取审核详情
     */
    CourtAuditDTO getAuditById(Long id);
    
    /**
     * 审核场地申请
     */
    boolean auditCourtApplication(Long auditId, AuditDecisionRequest request, String auditorId, String auditorName);
    
    /**
     * 批量审核
     */
    boolean batchAudit(java.util.List<Long> auditIds, AuditDecisionRequest request, String auditorId, String auditorName);
    
    /**
     * 获取审核统计信息
     */
    java.util.Map<String, Long> getAuditStatistics();
} 