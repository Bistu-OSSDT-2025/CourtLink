package com.courtlink.config;

import com.courtlink.admin.entity.Admin;
import com.courtlink.admin.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        initializeDefaultAdmin();
    }
    
    private void initializeDefaultAdmin() {
        // Check if any admin exists
        if (adminRepository.count() == 0) {
            // Create default super admin
            Admin superAdmin = new Admin();
            superAdmin.setUsername("admin");
            superAdmin.setEmail("admin@courtlink.com");
            superAdmin.setPassword(passwordEncoder.encode("admin123"));
            superAdmin.setFullName("Super Administrator");
            superAdmin.setRole(Admin.AdminRole.SUPER_ADMIN);
            superAdmin.setStatus(Admin.AdminStatus.ACTIVE);
            
            adminRepository.save(superAdmin);
            
            System.out.println("=".repeat(60));
            System.out.println("? CourtLink Admin System Initialized Successfully!");
            System.out.println("=".repeat(60));
            System.out.println("Default Super Admin Account Created:");
            System.out.println("Username: admin");
            System.out.println("Password: admin123");
            System.out.println("Email: admin@courtlink.com");
            System.out.println("=".repeat(60));
            System.out.println("? Admin Dashboard: http://localhost:8080/api/admin/dashboard");
            System.out.println("? Admin APIs: http://localhost:8080/swagger-ui.html");
            System.out.println("=".repeat(60));
        }
    }
} 
