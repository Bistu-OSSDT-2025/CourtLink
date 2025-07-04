package com.courtlink.config;

<<<<<<< HEAD
import com.courtlink.security.JwtAuthenticationFilter;
import com.courtlink.security.CompositeUserDetailsService;
import lombok.RequiredArgsConstructor;
=======
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
<<<<<<< HEAD
import org.springframework.http.HttpMethod;
=======
import com.courtlink.security.JwtAuthenticationFilter;
import com.courtlink.security.CompositeUserDetailsService;
import lombok.RequiredArgsConstructor;
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CompositeUserDetailsService compositeUserDetailsService;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configure(http))
            .csrf(csrf -> csrf.disable())
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(form -> form.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
<<<<<<< HEAD
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/admin/login").permitAll()
                .requestMatchers("/api/v1/courts", "/api/v1/courts/available", "/api/v1/courts/{id}", "/api/v1/courts/booking").permitAll()
                .requestMatchers("/api/v1/appointments").hasAnyRole("USER", "ADMIN", "SUPER_ADMIN")
                .requestMatchers("/api/v1/appointments/**").hasAnyRole("USER", "ADMIN", "SUPER_ADMIN")
                .requestMatchers("/api/v1/payments/**").hasAnyRole("USER", "ADMIN", "SUPER_ADMIN")
                .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MODERATOR")
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
=======
                .requestMatchers(
                    "/api/auth/**",
                    "/api/v1/admin/login",
                    "/api/courts/**",
                    "/h2-console/**",
                    "/error",
                    "/actuator/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .headers(headers -> headers.frameOptions(frame -> frame.disable()));
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(compositeUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
} 