package com.bistu.ossdt.courtlink.admin.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bistu.ossdt.courtlink.admin.dto.AuditDecisionRequest;
import com.bistu.ossdt.courtlink.admin.dto.CourtAuditDTO;
import com.bistu.ossdt.courtlink.admin.mapper.CourtAuditMapper;
import com.bistu.ossdt.courtlink.admin.model.AuditStatus;
import com.bistu.ossdt.courtlink.admin.model.CourtAudit;
import com.bistu.ossdt.courtlink.admin.service.CourtAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CourtAuditServiceImpl extends ServiceImpl<CourtAuditMapper, CourtAudit> implements CourtAuditService {

    @Override
    public IPage<CourtAuditDTO> getPendingAudits(int page, int size) {
        return getAuditsByStatus(AuditStatus.PENDING, page, size);
    }

    @Override
    public IPage<CourtAuditDTO> getAuditsByStatus(AuditStatus status, int page, int size) {
        Page<CourtAudit> auditPage = new Page<>(page, size);
        IPage<CourtAudit> audits = baseMapper.selectByStatus(auditPage, status);
        return audits.convert(this::convertToDTO);
    }

    @Override
    public IPage<CourtAuditDTO> getAllAudits(int page, int size) {
        Page<CourtAudit> auditPage = new Page<>(page, size);
        IPage<CourtAudit> audits = page(auditPage);
        return audits.convert(this::convertToDTO);
    }

    @Override
    public CourtAuditDTO getAuditById(Long id) {
        CourtAudit audit = getById(id);
        return audit != null ? convertToDTO(audit) : null;
    }

    @Override
    @Transactional
    public boolean auditCourtApplication(Long auditId, AuditDecisionRequest request, String auditorId, String auditorName) {
        CourtAudit audit = getById(auditId);
        if (audit == null) {
            throw new IllegalArgumentException("Audit record not found");
        }
        
        if (audit.getStatus() != AuditStatus.PENDING) {
            throw new IllegalStateException("Application has already been audited");
        }
        
        audit.setStatus(request.getStatus());
        audit.setAuditReason(request.getAuditReason());
        audit.setRemarks(request.getRemarks());
        audit.setAuditorId(auditorId);
        audit.setAuditorName(auditorName);
        audit.setAuditTime(LocalDateTime.now());
        
        return updateById(audit);
    }

    @Override
    @Transactional
    public boolean batchAudit(List<Long> auditIds, AuditDecisionRequest request, String auditorId, String auditorName) {
        List<CourtAudit> audits = listByIds(auditIds);
        
        for (CourtAudit audit : audits) {
            if (audit.getStatus() == AuditStatus.PENDING) {
                audit.setStatus(request.getStatus());
                audit.setAuditReason(request.getAuditReason());
                audit.setRemarks(request.getRemarks());
                audit.setAuditorId(auditorId);
                audit.setAuditorName(auditorName);
                audit.setAuditTime(LocalDateTime.now());
            }
        }
        
        return updateBatchById(audits);
    }

    @Override
    public Map<String, Long> getAuditStatistics() {
        Map<String, Long> stats = new HashMap<>();
        
        stats.put("PENDING", baseMapper.countByStatus(AuditStatus.PENDING));
        stats.put("APPROVED", baseMapper.countByStatus(AuditStatus.APPROVED));
        stats.put("REJECTED", baseMapper.countByStatus(AuditStatus.REJECTED));
        stats.put("TOTAL", baseMapper.selectCount(null));
        
        return stats;
    }

    private CourtAuditDTO convertToDTO(CourtAudit audit) {
        CourtAuditDTO dto = new CourtAuditDTO();
        BeanUtils.copyProperties(audit, dto);
        return dto;
    }
} 