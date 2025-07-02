package com.courtlink;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 应用集成测试
 * 验证基本功能是否正常工作
 */
@SpringBootTest(
    classes = CourtLinkApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
public class ApplicationIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void contextLoads() {
        // 验证Spring Boot应用上下文能够成功加载
        assertNotNull(restTemplate);
        assertTrue(port > 0);
    }

    @Test
    public void healthCheckEndpointWorks() {
        // 测试健康检查端点
        String url = "http://localhost:" + port + "/api/health/simple";
        ResponseEntity<Map<String, String>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, String>>() {}
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        Map<String, String> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("OK", responseBody.get("status"));
    }

    @Test
    public void swaggerUIAccessible() {
        // 测试Swagger UI是否可访问
        String url = "http://localhost:" + port + "/swagger-ui/index.html";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void apiDocsAccessible() {
        // 测试API文档是否可访问
        String url = "http://localhost:" + port + "/v3/api-docs";
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            url, 
            HttpMethod.GET, 
            null, 
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertTrue(responseBody.containsKey("openapi"));
    }

    @Test
    public void courtEndpointAccessible() {
        // 测试场地接口是否可访问
        String url = "http://localhost:" + port + "/api/courts";
        ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
        
        // 应该返回200或其他有效状态码（不是404）
        assertNotEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void userHealthEndpointAccessible() {
        // 测试用户健康检查接口是否可访问
        String url = "http://localhost:" + port + "/api/health/live";
        ResponseEntity<Map<String, String>> response = restTemplate.exchange(
            url, 
            HttpMethod.GET, 
            null, 
            new ParameterizedTypeReference<Map<String, String>>() {}
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        Map<String, String> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("OK", responseBody.get("status"));
    }
} 