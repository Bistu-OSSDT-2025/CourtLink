package com.courtlink.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableAutoConfiguration
@EnableWebMvc
@ComponentScan(basePackages = {
    "com.courtlink.admin",
    "com.courtlink.booking",
    "com.courtlink.user",
    "com.courtlink.config",
    "com.courtlink.controller",
    "com.courtlink.service"
})
@Import({SecurityConfig.class, TestSecurityConfig.class, SwaggerConfig.class})
public class TestConfig {
    // ʹ������֧������
} 