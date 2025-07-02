package com.courtlink.auth.service.impl;

import com.courtlink.auth.dto.LoginRequest;
import com.courtlink.auth.dto.RegisterRequest;
import com.courtlink.auth.service.AuthService;
import com.courtlink.user.entity.User;
import com.courtlink.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ResponseEntity<?> register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "用户名已被使用"));
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "邮箱已被使用"));
        }

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        return ResponseEntity.ok()
            .body(Map.of("message", "注册成功"));
    }

    @Override
    public ResponseEntity<?> login(LoginRequest request) {
        return userRepository.findByUsername(request.getUsername())
            .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
            .map(user -> {
                Map<String, Object> response = new HashMap<>();
                response.put("token", "dummy-token-" + user.getId()); // 这里应该使用真实的JWT token
                response.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail()
                ));
                return ResponseEntity.ok(response);
            })
            .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "用户名或密码错误")));
    }
} 