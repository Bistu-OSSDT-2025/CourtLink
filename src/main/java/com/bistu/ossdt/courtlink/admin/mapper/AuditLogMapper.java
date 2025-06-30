package com.bistu.ossdt.courtlink.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bistu.ossdt.courtlink.admin.model.AuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
    
    @Select("SELECT COUNT(*) FROM audit_log WHERE operation_time >= #{startTime} AND operation_time < #{endTime}")
    long countByOperationTimeRange(LocalDateTime startTime, LocalDateTime endTime);
    
    @Select("SELECT COUNT(*) FROM audit_log WHERE operation_type = #{operationType}")
    long countByOperationType(String operationType);
} 