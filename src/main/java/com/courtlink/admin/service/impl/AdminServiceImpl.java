package com.courtlink.admin.service.impl;

import com.courtlink.admin.dto.AdminLoginRequest;
import com.courtlink.admin.dto.AdminLoginResponse;
import com.courtlink.admin.dto.AdminResponse;
import com.courtlink.admin.entity.Admin;
import com.courtlink.admin.repository.AdminRepository;
import com.courtlink.admin.service.AdminService;
import com.courtlink.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional
    public AdminLoginResponse login(AdminLoginRequest request) {
        Admin admin = adminRepository.findByUsernameOrEmailAndActive(request.getUsernameOrEmail())
                .orElseThrow(() -> new UsernameNotFoundException("管理员不存在或已被禁用"));

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new BadCredentialsException("密码错误");
        }

        // 更新最后登录时间
        admin.setLastLoginAt(LocalDateTime.now());
        adminRepository.save(admin);

        // 生成JWT token
        String token = jwtService.generateToken(new AdminUserDetails(admin));

        return new AdminLoginResponse(token, AdminResponse.fromAdmin(admin));
    }

    @Override
    public AdminResponse getProfile(String username) {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("管理员不存在"));
        return AdminResponse.fromAdmin(admin);
    }

    @Override
    public List<AdminResponse> getAllAdmins() {
        return adminRepository.findAllActiveAdmins()
                .stream()
                .map(AdminResponse::fromAdmin)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AdminResponse createAdmin(Admin admin) {
        if (adminRepository.existsByUsername(admin.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (adminRepository.existsByEmail(admin.getEmail())) {
            throw new IllegalArgumentException("邮箱已存在");
        }

        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        Admin savedAdmin = adminRepository.save(admin);
        return AdminResponse.fromAdmin(savedAdmin);
    }

    @Override
    @Transactional
    public AdminResponse updateAdmin(Long id, Admin admin) {
        Admin existingAdmin = adminRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("管理员不存在"));

        existingAdmin.setFullName(admin.getFullName());
        existingAdmin.setEmail(admin.getEmail());
        existingAdmin.setRole(admin.getRole());
        
        if (admin.getPassword() != null && !admin.getPassword().isEmpty()) {
            existingAdmin.setPassword(passwordEncoder.encode(admin.getPassword()));
        }

        Admin updatedAdmin = adminRepository.save(existingAdmin);
        return AdminResponse.fromAdmin(updatedAdmin);
    }

    @Override
    @Transactional
    public void deleteAdmin(Long id) {
        if (!adminRepository.existsById(id)) {
            throw new UsernameNotFoundException("管理员不存在");
        }
        adminRepository.deleteById(id);
    }

    @Override
    @Transactional
    public AdminResponse activateAdmin(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("管理员不存在"));
        admin.setIsActive(true);
        Admin updatedAdmin = adminRepository.save(admin);
        return AdminResponse.fromAdmin(updatedAdmin);
    }

    @Override
    @Transactional
    public AdminResponse deactivateAdmin(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("管理员不存在"));
        admin.setIsActive(false);
        Admin updatedAdmin = adminRepository.save(admin);
        return AdminResponse.fromAdmin(updatedAdmin);
    }

    @Override
    public Optional<Admin> findByUsernameOrEmail(String usernameOrEmail) {
        return adminRepository.findByUsernameOrEmailAndActive(usernameOrEmail);
    }
} 