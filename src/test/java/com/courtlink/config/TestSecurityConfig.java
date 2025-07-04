package com.courtlink.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;
import static org.springframework.security.config.Customizer.withDefaults;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {
    
    @Bean
    @Primary
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(withDefaults())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/admin/**", 
                               "/api/courts/**",
                               "/api/users/**",
                               "/api/health/**",
                               "/api/appointments/**",
                               "/api/payments/**",
                               "/v3/api-docs/**",
                               "/swagger-ui/**",
                               "/swagger-ui.html",
                               "/h2-console/**").permitAll()
                .anyRequest().authenticated()
            )
            .headers(headers -> headers.frameOptions().disable());
        
        return http.build();
    }
} 