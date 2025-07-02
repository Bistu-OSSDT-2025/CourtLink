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
} 