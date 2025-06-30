package com.bistu.ossdt.courtlink.user.repository;

import com.bistu.ossdt.courtlink.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 检查邮箱是否被其他用户使用（更新时用）
     */
    boolean existsByEmailAndIdNot(String email, Long id);
    
    /**
     * 检查用户名是否被其他用户使用（更新时用）
     */
    boolean existsByUsernameAndIdNot(String username, Long id);
} 