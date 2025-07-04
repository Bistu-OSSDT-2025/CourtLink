package com.courtlink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
<<<<<<< HEAD
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
=======
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.courtlink")
@EnableJpaRepositories(basePackages = "com.courtlink")
@EntityScan(basePackages = "com.courtlink")
@EnableCaching
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
public class CourtLinkApplication {
    public static void main(String[] args) {
        SpringApplication.run(CourtLinkApplication.class, args);
    }
} 