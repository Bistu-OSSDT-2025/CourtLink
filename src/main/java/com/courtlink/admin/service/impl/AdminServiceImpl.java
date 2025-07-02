package com.courtlink.admin.service.impl;

import com.courtlink.admin.dto.AdminLoginRequest;
import com.courtlink.admin.entity.Admin;
import com.courtlink.admin.repository.AdminRepository;
import com.courtlink.admin.service.AdminService;
import com.courtlink.admin.util.AdminServiceUtils;
import com.courtlink.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return AdminServiceUtils.findAdminByUsernameOrThrow(adminRepository, username);
    }

    @Override
    public String login(AdminLoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        updateLastLoginTime(request.getUsername());
        return jwtService.generateToken(authentication);
    }

    @Override
    public Admin getCurrentAdmin() {
        return AdminServiceUtils.getCurrentAuthenticatedAdmin();
    }

    @Override
    public void updateLastLoginTime(String username) {
        Admin admin = findByUsername(username);
        admin.setLastLoginAt(LocalDateTime.now());
        adminRepository.save(admin);
    }

    @Override
    public boolean isAdmin() {
        return AdminServiceUtils.hasRole(getCurrentAdmin(), "ROLE_ADMIN");
    }

    @Override
    public boolean isSuperAdmin() {
        return AdminServiceUtils.hasRole(getCurrentAdmin(), "ROLE_SUPER_ADMIN");
    }

    @Override
    public List<Admin> findAll() {
        return adminRepository.findAll();
    }

    @Override
    public Admin findByUsername(String username) {
        return AdminServiceUtils.findAdminByUsernameOrThrow(adminRepository, username);
    }

    @Override
    public Admin findAdminById(long id) {
        return AdminServiceUtils.findAdminByIdOrThrow(adminRepository, id);
    }

    @Override
    public Admin createAdmin(Admin admin) {
        AdminServiceUtils.validateAdminExists(adminRepository, admin.getUsername());
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        return adminRepository.save(admin);
    }

    @Override
    public Admin updateAdmin(long id, Admin admin) {
        Admin existingAdmin = findAdminById(id);
        AdminServiceUtils.updateAdminFields(existingAdmin, admin);
        return adminRepository.save(existingAdmin);
    }

    @Override
    public void deleteAdmin(long id) {
        Admin admin = findAdminById(id);
        adminRepository.delete(admin);
    }

    @Override
    public List<Admin> findAllAdmins() {
        return adminRepository.findAll();
    }

    @Override
    public Admin toggleAdminStatus(long id, boolean enabled) {
        Admin admin = findAdminById(id);
        admin.setEnabled(enabled);
        return adminRepository.save(admin);
    }

    @Override
    public Admin updateLastLoginTime(long id) {
        Admin admin = findAdminById(id);
        admin.setLastLoginAt(LocalDateTime.now());
        return adminRepository.save(admin);
    }
} 