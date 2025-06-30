package com.bistu.ossdt.courtlink.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bistu.ossdt.courtlink.admin.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT COUNT(*) FROM users WHERE username = #{username}")
    int countByUsername(String username);

    @Select("SELECT COUNT(*) FROM users WHERE email = #{email}")
    int countByEmail(String email);
} 