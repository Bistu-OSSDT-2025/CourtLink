<<<<<<< HEAD
package com.courtlink.admin.service.impl;

import com.courtlink.admin.entity.Admin;
import com.courtlink.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminInitializationService implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeDefaultAdmin();
    }

    private void initializeDefaultAdmin() {
        // 检查是否已有管理员
        if (adminRepository.count() == 0) {
            log.info("正在创建默认超级管理员...");
            
            Admin superAdmin = new Admin();
            superAdmin.setUsername("admin");
            superAdmin.setEmail("admin@courtlink.com");
            superAdmin.setPassword(passwordEncoder.encode("admin123"));
            superAdmin.setFullName("系统管理员");
            superAdmin.setRole(Admin.AdminRole.SUPER_ADMIN);
            superAdmin.setIsActive(true);

            adminRepository.save(superAdmin);
            
            log.info("默认超级管理员创建成功！");
            log.info("用户名: admin");
            log.info("密码: admin123");
            log.info("请在生产环境中修改默认密码！");
        } else {
            log.info("管理员已存在，跳过初始化");
        }
    }
=======
package com.courtlink.admin.service.impl;

import com.courtlink.admin.entity.Admin;
import com.courtlink.admin.repository.AdminRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminInitializationService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        createSuperAdminIfNotExists();
        createDefaultAdminIfNotExists();
    }

    private void createSuperAdminIfNotExists() {
        if (adminRepository.findByUsername("superadmin").isPresent()) {
            return;
        }

        Admin superAdmin = new Admin();
        superAdmin.setUsername("superadmin");
        superAdmin.setPassword(passwordEncoder.encode("superadmin123"));
        superAdmin.setEmail("superadmin@courtlink.com");
        superAdmin.getRoles().add("ROLE_SUPER_ADMIN");
        superAdmin.setEnabled(true);

        adminRepository.save(superAdmin);
    }

    private void createDefaultAdminIfNotExists() {
        if (adminRepository.findByUsername("admin").isPresent()) {
            return;
        }

        Admin admin = new Admin();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setEmail("admin@courtlink.com");
        admin.getRoles().add("ROLE_ADMIN");
        admin.setEnabled(true);

        adminRepository.save(admin);
    }
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
} 