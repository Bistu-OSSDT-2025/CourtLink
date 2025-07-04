package com.courtlink.user.service.impl;

import com.courtlink.user.entity.User;
import com.courtlink.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Order(2) // 在AdminInitializationService之后运行
public class UserInitializationService implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeTestUsers();
    }

    private void initializeTestUsers() {
        // 检查是否已有用户
        if (userRepository.count() == 0) {
            log.info("正在创建测试用户...");
            
            // 创建测试用户1
            User testUser1 = new User();
            testUser1.setUsername("testuser");
            testUser1.setEmail("testuser@example.com");
            testUser1.setPassword(passwordEncoder.encode("test123"));
            userRepository.save(testUser1);
            
            // 创建测试用户2
            User testUser2 = new User();
            testUser2.setUsername("testbooking");
            testUser2.setEmail("testbooking@example.com");
            testUser2.setPassword(passwordEncoder.encode("test123"));
            userRepository.save(testUser2);
            
            // 创建测试用户3
            User testUser3 = new User();
            testUser3.setUsername("qwert");
            testUser3.setEmail("qwert@example.com");
            testUser3.setPassword(passwordEncoder.encode("test123"));
            userRepository.save(testUser3);
            
            log.info("测试用户创建成功！");
            log.info("用户1: testuser / test123");
            log.info("用户2: testbooking / test123");
            log.info("用户3: qwert / test123");
        } else {
            log.info("测试用户已存在，跳过初始化");
        }
    }
} 