package com.bistu.ossdt.courtlink.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bistu.ossdt.courtlink.admin.dto.AuditDecisionRequest;
import com.bistu.ossdt.courtlink.admin.dto.CourtAuditDTO;
import com.bistu.ossdt.courtlink.admin.model.AuditStatus;
import com.bistu.ossdt.courtlink.admin.model.CourtAudit;

public interface CourtAuditService extends IService<CourtAudit> {
    
    /**
     * ��ҳ��ȡ����˵ĳ�������
     */
    IPage<CourtAuditDTO> getPendingAudits(int page, int size);
    
    /**
     * ��ҳ��ȡָ��״̬����˼�¼
     */
    IPage<CourtAuditDTO> getAuditsByStatus(AuditStatus status, int page, int size);
    
    /**
     * ��ȡ������˼�¼
     */
    IPage<CourtAuditDTO> getAllAudits(int page, int size);
    
    /**
     * ����ID��ȡ�������
     */
    CourtAuditDTO getAuditById(Long id);
    
    /**
     * ��˳�������
     */
    boolean auditCourtApplication(Long auditId, AuditDecisionRequest request, String auditorId, String auditorName);
    
    /**
     * �������
     */
    boolean batchAudit(java.util.List<Long> auditIds, AuditDecisionRequest request, String auditorId, String auditorName);
    
    /**
     * ��ȡ���ͳ����Ϣ
     */
    java.util.Map<String, Long> getAuditStatistics();
} 