package com.courtlink.admin.service.impl;

import com.courtlink.admin.dto.AdminLoginRequest;
import com.courtlink.admin.entity.Admin;
import com.courtlink.admin.exception.UnauthorizedException;
import com.courtlink.admin.exception.UserNotFoundException;
import com.courtlink.admin.repository.AdminRepository;
import com.courtlink.admin.service.AdminService;
import com.courtlink.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService, UserDetailsService {

    private final AdminRepository adminRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Override
    public String login(AdminLoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        Admin admin = (Admin) authentication.getPrincipal();
        if (!admin.isEnabled()) {
            throw new UnauthorizedException("Account is disabled");
        }

        updateLastLoginTime(admin);
        return jwtService.generateToken(admin);
    }

    @Override
    @Transactional
    public Admin createAdmin(Admin admin) {
        if (adminRepository.findByUsername(admin.getUsername()).isPresent()) {
            throw new UnauthorizedException("Username already exists");
        }

        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        admin.setEnabled(true);
        return adminRepository.save(admin);
    }

    @Override
    @Transactional
    public Admin updateAdmin(Long id, Admin adminDetails) {
        Admin admin = findAdminById(id);

        admin.setEmail(adminDetails.getEmail());
        admin.setRoles(adminDetails.getRoles());
        admin.setEnabled(adminDetails.isEnabled());
        admin.setPhone(adminDetails.getPhone());
        admin.setRealName(adminDetails.getRealName());

        return adminRepository.save(admin);
    }

    @Override
    @Transactional
    public void deleteAdmin(Long id) {
        Admin admin = findAdminById(id);
        adminRepository.delete(admin);
    }

    @Override
    public Admin getCurrentAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("No authenticated user found");
        }

        String username = authentication.getName();
        return findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Admin not found"));
    }

    @Override
    public List<Admin> findAll() {
        return adminRepository.findAll();
    }

    @Override
    public List<Admin> findAllAdmins() {
        return adminRepository.findAll();
    }

    @Override
    public Admin findAdminById(Long id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Admin not found"));
    }

    @Override
    public Optional<Admin> findByUsername(String username) {
        return adminRepository.findByUsername(username);
    }

    @Override
    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || 
                              a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }

    @Override
    public boolean isSuperAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }

    @Override
    @Transactional
    public void toggleAdminStatus(Long id) {
        Admin admin = findAdminById(id);
        admin.setEnabled(!admin.isEnabled());
        adminRepository.save(admin);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public void updateLastLoginTime(Admin admin) {
        admin.setLastLoginAt(new Date());
        adminRepository.save(admin);
    }

    @Override
    public void updateLastLoginTime(String username) {
        Admin admin = findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Admin not found"));
        updateLastLoginTime(admin);
    }
} 