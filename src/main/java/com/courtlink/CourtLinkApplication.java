package com.courtlink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.courtlink")
@EnableJpaRepositories(basePackages = "com.courtlink")
@EntityScan(basePackages = "com.courtlink")
@EnableCaching
public class CourtLinkApplication {
    public static void main(String[] args) {
        SpringApplication.run(CourtLinkApplication.class, args);
    }
} 