package com.courtlink.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private UserDetails userDetails;
    private final String testSecret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long testExpiration = 86400000; // 24 hours

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secretKey", testSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", testExpiration);
        userDetails = new User("testuser", "password", new ArrayList<>());
    }

    @Test
    void testGenerateAndValidateToken() {
        String token = jwtService.generateToken(userDetails);
        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void testExtractUsername() {
        String token = jwtService.generateToken(userDetails);
        String username = jwtService.extractUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    void testExpiredToken() {
        String token = jwtService.generateExpiredToken(userDetails);
        assertFalse(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void testInvalidToken() {
        String token = jwtService.generateToken(userDetails);
        UserDetails otherUser = new User("otheruser", "password", new ArrayList<>());
        assertFalse(jwtService.isTokenValid(token, otherUser));
    }
} 