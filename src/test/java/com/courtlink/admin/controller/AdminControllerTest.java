package com.courtlink.admin.controller;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.courtlink.CourtLinkApplication;
import com.courtlink.admin.dto.AdminLoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(
    classes = CourtLinkApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@AutoConfigureWebMvc
public class AdminControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testAdminLoginEndpointExists() {
        // Test that the admin login endpoint is accessible
        String url = "http://localhost:" + port + "/api/admin/auth/login";
        
        AdminLoginRequest loginRequest = new AdminLoginRequest();
        loginRequest.setUsernameOrEmail("admin");
        loginRequest.setPassword("admin123");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AdminLoginRequest> request = new HttpEntity<>(loginRequest, headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(
            url, 
            HttpMethod.POST, 
            request, 
            Map.class
        );
        
        // Should not return 404 (endpoint should exist)
        assertNotEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        // Should return either success or authentication error
        assertTrue(response.getStatusCode() == HttpStatus.OK || 
                  response.getStatusCode() == HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void testAdminDashboardEndpointExists() {
        // Test that the admin dashboard endpoint is accessible
        String url = "http://localhost:" + port + "/api/admin/dashboard";
        
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        // Should not return 404 (endpoint should exist)
        assertNotEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        // Should return the dashboard or require authentication
        assertTrue(response.getStatusCode() == HttpStatus.OK || 
                  response.getStatusCode() == HttpStatus.FORBIDDEN ||
                  response.getStatusCode() == HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void testAdminStatisticsEndpointExists() {
        // Test that the admin statistics endpoint exists
        String url = "http://localhost:" + port + "/api/admin/statistics";
        
        ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
        
        // Should not return 404 (endpoint should exist)
        assertNotEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        // Should require authentication (return 401 or 403)
        assertTrue(response.getStatusCode() == HttpStatus.UNAUTHORIZED ||
                  response.getStatusCode() == HttpStatus.FORBIDDEN);
    }

    @Test
    public void testAdminSystemHealthEndpointExists() {
        // Test that the admin system health endpoint exists
        String url = "http://localhost:" + port + "/api/admin/system/health";
        
        ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
        
        // Should not return 404 (endpoint should exist)
        assertNotEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        // Should return health info or require authentication
        assertTrue(response.getStatusCode() == HttpStatus.OK ||
                  response.getStatusCode() == HttpStatus.UNAUTHORIZED ||
                  response.getStatusCode() == HttpStatus.FORBIDDEN);
    }

    @Test
    public void testUnauthorizedAccessToAdminEndpoints() {
        // Test that admin endpoints require authentication
        String[] adminEndpoints = {
            "/api/admin/admins",
            "/api/admin/admins/1",
            "/api/admin/statistics"
        };
        
        for (String endpoint : adminEndpoints) {
            String url = "http://localhost:" + port + endpoint;
            ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
            
            // Should require authentication
            assertTrue(response.getStatusCode() == HttpStatus.UNAUTHORIZED ||
                      response.getStatusCode() == HttpStatus.FORBIDDEN,
                      "Endpoint " + endpoint + " should require authentication");
        }
    }
} 