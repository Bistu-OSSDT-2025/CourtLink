package com.courtlink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CourtLinkApplication {
    public static void main(String[] args) {
        SpringApplication.run(CourtLinkApplication.class, args);
    }
} 