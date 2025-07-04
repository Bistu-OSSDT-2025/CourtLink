package com.courtlink.admin.service.impl;

import com.courtlink.admin.dto.*;
import com.courtlink.admin.entity.Admin;
import com.courtlink.admin.repository.AdminRepository;
import com.courtlink.admin.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private final ThreadLocal<Admin> currentAdmin = new ThreadLocal<>();
    
    @Override
    public AdminLoginResponse login(AdminLoginRequest request) {
        Optional<Admin> adminOpt = adminRepository.findByUsernameOrEmail(request.usernameOrEmail());
        
        if (adminOpt.isEmpty()) {
            recordFailedLogin(request.usernameOrEmail());
            return AdminLoginResponse.failure("Invalid username or email");
        }
        
        Admin admin = adminOpt.get();
        
        if (!admin.isActive()) {
            return AdminLoginResponse.failure("Account is not active");
        }
        
        if (admin.getIsLocked()) {
            return AdminLoginResponse.failure("Account is locked");
        }
        
        if (!checkPassword(request.password(), admin.getPassword())) {
            admin.incrementLoginAttempts();
            adminRepository.save(admin);
            return AdminLoginResponse.failure("Invalid password");
        }
        
        String token = generateToken(admin);
        recordSuccessfulLogin(admin);
        return AdminLoginResponse.of(admin, token);
    }
    
    @Override
    public boolean logout(String token) {
        currentAdmin.remove();
        return true;
    }
    
    @Override
    public boolean isTokenValid(String token) {
        return token != null && token.startsWith("ADMIN_TOKEN_");
    }
    
    @Override
    public Admin getCurrentAdmin() {
        return currentAdmin.get();
    }
    
    @Override
    public AdminDTO createAdmin(AdminCreateRequest request) {
        if (isUsernameExists(request.username())) {
            throw new RuntimeException("Username already exists");
        }
        
        if (isEmailExists(request.email())) {
            throw new RuntimeException("Email already exists");
        }
        
        Admin admin = new Admin();
        admin.setUsername(request.username());
        admin.setEmail(request.email());
        admin.setPassword(encodePassword(request.password()));
        admin.setFullName(request.fullName());
        admin.setRole(request.role());
        admin.setStatus(Admin.AdminStatus.ACTIVE);
        
        Admin savedAdmin = adminRepository.save(admin);
        return new AdminDTO(savedAdmin);
    }
    
    @Override
    public AdminDTO updateAdmin(Long id, AdminCreateRequest request) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        
        if (!admin.getUsername().equals(request.username()) && isUsernameExists(request.username())) {
            throw new RuntimeException("Username already exists");
        }
        
        if (!admin.getEmail().equals(request.email()) && isEmailExists(request.email())) {
            throw new RuntimeException("Email already exists");
        }
        
        admin.setUsername(request.username());
        admin.setEmail(request.email());
        admin.setFullName(request.fullName());
        admin.setRole(request.role());
        
        if (request.password() != null && !request.password().trim().isEmpty()) {
            admin.setPassword(encodePassword(request.password()));
        }
        
        Admin savedAdmin = adminRepository.save(admin);
        return new AdminDTO(savedAdmin);
    }
    
    @Override
    public boolean deleteAdmin(Long id) {
        if (!canDeleteAdmin(id)) {
            return false;
        }
        adminRepository.deleteById(id);
        return true;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<AdminDTO> getAdminById(Long id) {
        return adminRepository.findById(id).map(AdminDTO::new);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<AdminDTO> getAdminByUsername(String username) {
        return adminRepository.findByUsername(username).map(AdminDTO::new);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<AdminDTO> getAdminByEmail(String email) {
        return adminRepository.findByEmail(email).map(AdminDTO::new);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AdminDTO> getAllAdmins(Pageable pageable) {
        return adminRepository.findAll(pageable).map(AdminDTO::new);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AdminDTO> getAdminsByRole(Admin.AdminRole role, Pageable pageable) {
        return adminRepository.findByRoleAndStatus(role, Admin.AdminStatus.ACTIVE, pageable).map(AdminDTO::new);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AdminDTO> getAdminsByStatus(Admin.AdminStatus status, Pageable pageable) {
        return adminRepository.findByRoleAndStatus(Admin.AdminRole.ADMIN, status, pageable).map(AdminDTO::new);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AdminDTO> searchAdminsByName(String name, Pageable pageable) {
        return adminRepository.findByNameContaining(name, pageable).map(AdminDTO::new);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AdminDTO> getActiveAdmins() {
        return adminRepository.findActiveAdmins().stream()
                .map(AdminDTO::new)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AdminDTO> getLockedAdmins() {
        return adminRepository.findByIsLockedTrue().stream()
                .map(AdminDTO::new)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean activateAdmin(Long id) {
        return updateAdminStatus(id, Admin.AdminStatus.ACTIVE);
    }
    
    @Override
    public boolean deactivateAdmin(Long id) {
        return updateAdminStatus(id, Admin.AdminStatus.INACTIVE);
    }
    
    @Override
    public boolean suspendAdmin(Long id) {
        return updateAdminStatus(id, Admin.AdminStatus.SUSPENDED);
    }
    
    @Override
    public boolean lockAdmin(Long id) {
        return updateAdminLockStatus(id, true);
    }
    
    @Override
    public boolean unlockAdmin(Long id) {
        return updateAdminLockStatus(id, false);
    }
    
    @Override
    public boolean resetPassword(Long id, String newPassword) {
        Optional<Admin> adminOpt = adminRepository.findById(id);
        if (adminOpt.isEmpty()) {
            return false;
        }
        
        Admin admin = adminOpt.get();
        admin.setPassword(encodePassword(newPassword));
        admin.setLoginAttempts(0);
        admin.setIsLocked(false);
        
        adminRepository.save(admin);
        return true;
    }
    
    @Override
    public boolean changeAdminRole(Long id, Admin.AdminRole newRole) {
        Optional<Admin> adminOpt = adminRepository.findById(id);
        if (adminOpt.isEmpty()) {
            return false;
        }
        
        Admin admin = adminOpt.get();
        Admin currentAdmin = getCurrentAdmin();
        
        if (!canChangeRole(currentAdmin, admin, newRole)) {
            return false;
        }
        
        admin.setRole(newRole);
        adminRepository.save(admin);
        return true;
    }
    
    @Override
    public boolean hasPermission(Admin admin, String permission) {
        if (admin == null) return false;
        if (admin.isSuperAdmin()) return true;
        return admin.isAdmin() && admin.isActive();
    }
    
    @Override
    public boolean isSuperAdmin(Admin admin) {
        return admin != null && admin.isSuperAdmin();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAdminStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", getTotalAdminCount());
        stats.put("activeCount", getActiveAdminCount());
        stats.put("lockedCount", getLockedAdminCount());
        stats.put("todayCreatedCount", getTodayCreatedAdminCount());
        stats.put("roleStats", getAdminCountByRole());
        stats.put("statusStats", getAdminCountByStatus());
        return stats;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<Admin.AdminRole, Long> getAdminCountByRole() {
        List<Object[]> results = adminRepository.countByRole();
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (Admin.AdminRole) row[0],
                        row -> (Long) row[1]
                ));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<Admin.AdminStatus, Long> getAdminCountByStatus() {
        List<Object[]> results = adminRepository.countByStatus();
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (Admin.AdminStatus) row[0],
                        row -> (Long) row[1]
                ));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long getTotalAdminCount() {
        return adminRepository.count();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long getActiveAdminCount() {
        return adminRepository.countActiveAdmins();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long getLockedAdminCount() {
        return adminRepository.countByIsLockedTrue();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long getTodayCreatedAdminCount() {
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);
        return adminRepository.countTodayCreated(startOfDay, endOfDay);
    }
    
    @Override
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    
    @Override
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
    
    @Override
    public void recordFailedLogin(String usernameOrEmail) {
        Optional<Admin> adminOpt = adminRepository.findByUsernameOrEmail(usernameOrEmail);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            admin.incrementLoginAttempts();
            adminRepository.save(admin);
        }
    }
    
    @Override
    public void recordSuccessfulLogin(Admin admin) {
        admin.resetLoginAttempts();
        adminRepository.save(admin);
        currentAdmin.set(admin);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isUsernameExists(String username) {
        return adminRepository.existsByUsername(username);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isEmailExists(String email) {
        return adminRepository.existsByEmail(email);
    }
    
    @Override
    public boolean canDeleteAdmin(Long id) {
        Optional<Admin> adminOpt = adminRepository.findById(id);
        if (adminOpt.isEmpty()) {
            return false;
        }
        
        Admin admin = adminOpt.get();
        if (admin.isSuperAdmin()) {
            return false;
        }
        
        long adminCount = adminRepository.countActiveAdmins();
        return adminCount > 1;
    }
    
    @Override
    public boolean canChangeRole(Admin currentAdmin, Admin targetAdmin, Admin.AdminRole newRole) {
        if (currentAdmin == null || targetAdmin == null) {
            return false;
        }
        
        if (newRole == Admin.AdminRole.SUPER_ADMIN && !currentAdmin.isSuperAdmin()) {
            return false;
        }
        
        if (currentAdmin.getId().equals(targetAdmin.getId()) && 
            newRole.ordinal() > currentAdmin.getRole().ordinal()) {
            return false;
        }
        
        return true;
    }
    
    private boolean updateAdminStatus(Long id, Admin.AdminStatus status) {
        Optional<Admin> adminOpt = adminRepository.findById(id);
        if (adminOpt.isEmpty()) {
            return false;
        }
        
        Admin admin = adminOpt.get();
        admin.setStatus(status);
        adminRepository.save(admin);
        return true;
    }
    
    private boolean updateAdminLockStatus(Long id, boolean locked) {
        Optional<Admin> adminOpt = adminRepository.findById(id);
        if (adminOpt.isEmpty()) {
            return false;
        }
        
        Admin admin = adminOpt.get();
        admin.setIsLocked(locked);
        if (!locked) {
            admin.setLoginAttempts(0);
        }
        adminRepository.save(admin);
        return true;
    }
    
    private String generateToken(Admin admin) {
        return "ADMIN_TOKEN_" + admin.getId() + "_" + System.currentTimeMillis();
    }
}
