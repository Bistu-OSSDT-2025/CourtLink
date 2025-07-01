package com.courtlink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CourtLinkApplication {
    public static void main(String[] args) {
        SpringApplication.run(CourtLinkApplication.class, args);
    }
} 