package com.bistu.ossdt.courtlink.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bistu.ossdt.courtlink.admin.model.AuditStatus;
import com.bistu.ossdt.courtlink.admin.model.CourtAudit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CourtAuditMapper extends BaseMapper<CourtAudit> {
    
    @Select("SELECT COUNT(*) FROM court_audit WHERE status = #{status}")
    long countByStatus(AuditStatus status);
    
    @Select("SELECT * FROM court_audit WHERE status = #{status} ORDER BY submit_time DESC")
    IPage<CourtAudit> selectByStatus(Page<CourtAudit> page, AuditStatus status);
    
    @Select("SELECT * FROM court_audit WHERE applicant_id = #{applicantId} ORDER BY submit_time DESC")
    IPage<CourtAudit> selectByApplicantId(Page<CourtAudit> page, String applicantId);
} 