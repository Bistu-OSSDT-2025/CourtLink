package com.bistu.ossdt.courtlink.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bistu.ossdt.courtlink.admin.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT COUNT(*) FROM users WHERE username = #{username}")
    int countByUsername(String username);

    @Select("SELECT COUNT(*) FROM users WHERE email = #{email}")
    int countByEmail(String email);
    
    @Select("SELECT COUNT(*) FROM users WHERE enabled = #{enabled}")
    long countByEnabled(Boolean enabled);
    
    @Select("SELECT COUNT(*) FROM users WHERE role = #{role}")
    long countByRole(String role);
    
    @Select("SELECT COUNT(*) FROM users WHERE create_time >= #{startTime} AND create_time < #{endTime}")
    long countByCreateTimeRange(LocalDateTime startTime, LocalDateTime endTime);
} 