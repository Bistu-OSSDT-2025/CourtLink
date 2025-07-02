package com.courtlink.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class JwtServiceTest {

    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    private final String SECRET_KEY = "testSecretKey123456789012345678901234567890";
    private final long EXPIRATION = 86400000; // 24 hours

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", EXPIRATION);
    }

    @Test
    void testGenerateTokenForAdmin() {
        // 准备测试数据
        String username = "admin";
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        UserDetails userDetails = new User(username, "", authorities);

        when(authentication.getPrincipal()).thenReturn(userDetails);

        // 生成令牌
        String token = jwtService.generateToken(authentication);

        // 验证令牌
        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token, userDetails));
        assertEquals(username, jwtService.extractUsername(token));
    }

    @Test
    void testGenerateTokenForSuperAdmin() {
        // 准备测试数据
        String username = "superadmin";
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
        UserDetails userDetails = new User(username, "", authorities);

        when(authentication.getPrincipal()).thenReturn(userDetails);

        // 生成令牌
        String token = jwtService.generateToken(authentication);

        // 验证令牌
        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token, userDetails));
        assertEquals(username, jwtService.extractUsername(token));
    }

    @Test
    void testGenerateTokenWithMultipleRoles() {
        // 准备测试数据
        String username = "multiuser";
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        UserDetails userDetails = new User(username, "", authorities);

        when(authentication.getPrincipal()).thenReturn(userDetails);

        // 生成令牌
        String token = jwtService.generateToken(authentication);

        // 验证令牌
        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token, userDetails));
        assertEquals(username, jwtService.extractUsername(token));
    }

    @Test
    void testTokenExpiration() throws InterruptedException {
        // 设置较短的过期时间
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1000L); // 1 second

        // 准备测试数据
        String username = "testuser";
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        UserDetails userDetails = new User(username, "", authorities);

        when(authentication.getPrincipal()).thenReturn(userDetails);

        // 生成令牌
        String token = jwtService.generateToken(authentication);

        // 验证令牌（立即）
        assertTrue(jwtService.isTokenValid(token, userDetails));

        // 等待令牌过期
        Thread.sleep(1100);

        // 验证令牌（过期后）
        assertFalse(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void testInvalidToken() {
        // 准备测试数据
        String username = "testuser";
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        UserDetails userDetails = new User(username, "", authorities);

        // 测试无效的令牌
        String invalidToken = "invalid.token.string";
        assertThrows(RuntimeException.class, () -> jwtService.isTokenValid(invalidToken, userDetails));

        // 测试空令牌
        assertThrows(IllegalArgumentException.class, () -> jwtService.isTokenValid("", userDetails));
        assertThrows(IllegalArgumentException.class, () -> jwtService.isTokenValid(null, userDetails));
    }
} 