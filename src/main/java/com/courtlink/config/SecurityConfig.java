package com.courtlink.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private CorsConfigurationSource corsConfigurationSource;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {
        MvcRequestMatcher.Builder mvc = new MvcRequestMatcher.Builder(introspector);
        
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource)) // Use CorsConfig configuration
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Public MVC endpoints
                .requestMatchers(
                    mvc.pattern("/"),
                    mvc.pattern("/api/health/**"),
                    mvc.pattern("/api/debug/**"),
                    mvc.pattern("/api/users/**"),
                    mvc.pattern("/api/courts/**"),
                    mvc.pattern("/api/admin/auth/login"),
                    mvc.pattern("/api/admin/dashboard"),
                    mvc.pattern("/api/admin/system/health"),
                    mvc.pattern("/swagger-ui/**"),
                    mvc.pattern("/swagger-ui.html"),
                    mvc.pattern("/v3/api-docs/**"),
                    mvc.pattern("/webjars/**"),
                    mvc.pattern("/api/payments/**"),
                    mvc.pattern("/api/appointments/**")
                ).permitAll()
                // H2 Console (non-MVC servlet)
                .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
                // Admin endpoints require authentication
                .requestMatchers(mvc.pattern("/api/admin/**")).hasRole("ADMIN")
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .headers(headers -> headers.frameOptions().disable()); // For H2 console
        
        return http.build();
    }
} 
