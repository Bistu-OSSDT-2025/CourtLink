package com.courtlink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.courtlink",
    "com.bistu.ossdt.courtlink"
})
@EnableJpaRepositories(basePackages = {
    "com.courtlink.repository",
    "com.bistu.ossdt.courtlink.user.repository"
})
@EntityScan(basePackages = {
    "com.courtlink.entity",
    "com.bistu.ossdt.courtlink.user.entity"
})
public class CourtLinkApplication {
    public static void main(String[] args) {
        SpringApplication.run(CourtLinkApplication.class, args);
    }
} 