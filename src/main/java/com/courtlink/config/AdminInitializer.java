package com.courtlink.config;

import com.courtlink.admin.entity.Admin;
import com.courtlink.admin.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements ApplicationRunner {
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("=== AdminInitializer Starting ===");
        
        try {
            long adminCount = adminRepository.count();
            System.out.println("Current admin count: " + adminCount);
            
            if (adminCount == 0) {
                System.out.println("Creating default admin account...");
                
                Admin admin = new Admin();
                admin.setUsername("admin");
                admin.setEmail("admin@courtlink.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setFullName("Default Administrator");
                admin.setRole(Admin.AdminRole.SUPER_ADMIN);
                admin.setStatus(Admin.AdminStatus.ACTIVE);
                admin.setIsLocked(false);
                admin.setLoginAttempts(0);
                
                Admin savedAdmin = adminRepository.save(admin);
                System.out.println("? Default admin created with ID: " + savedAdmin.getId());
                System.out.println("Username: admin");
                System.out.println("Password: admin123");
                System.out.println("? Admin Dashboard: http://localhost:8080/api/admin/dashboard");
            } else {
                System.out.println("??  Admin accounts already exist (" + adminCount + ")");
            }
        } catch (Exception e) {
            System.err.println("? Error in AdminInitializer: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== AdminInitializer Complete ===");
    }
} 
