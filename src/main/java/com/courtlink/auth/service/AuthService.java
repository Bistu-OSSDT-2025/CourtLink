package com.courtlink.auth.service;

import com.courtlink.auth.dto.LoginRequest;
import com.courtlink.auth.dto.RegisterRequest;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<?> register(RegisterRequest request);
    ResponseEntity<?> login(LoginRequest request);
} 