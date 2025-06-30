package com.courtlink.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3 Configuration for Spring Boot 3
 * Replaces the old Swagger 2.x configuration
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CourtLink API")
                        .description("Badminton Court Booking System API Documentation")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("CourtLink Team")
                                .email("support@courtlink.com")
                                .url("https://www.courtlink.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.courtlink.com")
                                .description("Production Server")
                ));
    }
} 