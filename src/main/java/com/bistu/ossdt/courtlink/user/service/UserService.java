package com.bistu.ossdt.courtlink.user.service;

import com.bistu.ossdt.courtlink.user.dto.LoginRequest;
import com.bistu.ossdt.courtlink.user.dto.LoginResponse;
import com.bistu.ossdt.courtlink.user.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    
    /**
     * 用户注册
     */
    UserDTO register(UserDTO userDTO);
    
    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest loginRequest);
    
    /**
     * 根据用户名查找用户
     */
    UserDTO findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     */
    UserDTO findByEmail(String email);
    
    /**
     * 根据ID查找用户
     */
    UserDTO findById(Long id);
    
    /**
     * 更新用户信息
     */
    UserDTO update(Long id, UserDTO userDTO);
    
    /**
     * 删除用户
     */
    void delete(Long id);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 获取所有用户列表
     */
    List<UserDTO> findAll();
    
    /**
     * 分页获取用户列表
     */
    Page<UserDTO> findAll(Pageable pageable);
    
    /**
     * 验证密码
     */
    boolean validatePassword(Long id, String password);
    
    /**
     * 更改密码
     */
    void changePassword(Long id, String oldPassword, String newPassword);
    
    /**
     * 重置密码
     */
    void resetPassword(Long id, String newPassword);
    
    /**
     * 启用/禁用用户
     */
    void toggleUserStatus(Long id);
} 