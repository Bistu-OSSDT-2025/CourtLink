package com.courtlink.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3 Configuration for Spring Boot 3
 * Replaces the old Swagger 2.x configuration
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI courtLinkOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort + "/api")
                                .description("Local Development Server")
                ))
                .info(new Info()
                        .title("CourtLink API")
                        .description("羽毛球场地预约管理系统 API 文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("CourtLink Team")
                                .email("support@courtlink.com")
                                .url("https://courtlink.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")));
    }
} 