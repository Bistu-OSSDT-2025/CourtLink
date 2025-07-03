package com.courtlink.security;

import com.courtlink.admin.service.impl.AdminUserDetails;
import com.courtlink.admin.repository.AdminRepository;
import com.courtlink.admin.entity.Admin;
import com.courtlink.user.service.impl.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Primary
@RequiredArgsConstructor
public class CompositeUserDetailsService implements UserDetailsService {

    private final CustomUserDetailsService customUserDetailsService;
    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 首先尝试在管理员表中查找
        try {
            Admin admin = adminRepository.findByUsernameOrEmail(username, username)
                .orElse(null);
            
            if (admin != null && admin.getIsActive()) {
                return new AdminUserDetails(admin);
            }
        } catch (Exception e) {
            // 忽略管理员查找错误，继续查找普通用户
        }
        
        // 如果不是管理员，则在普通用户表中查找
        try {
            return customUserDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            // 如果两个表都没找到，抛出异常
            throw new UsernameNotFoundException("用户未找到: " + username);
        }
    }
} 