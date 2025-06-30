package com.bistu.ossdt.courtlink.user.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    /**
     * 基础健康检查
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // 检查应用状态
            health.put("status", "UP");
            health.put("timestamp", LocalDateTime.now());
            health.put("application", "CourtLink");
            health.put("version", "1.0.0");
            
            // 检查数据库连接
            try (Connection connection = dataSource.getConnection()) {
                if (connection.isValid(5)) {
                    health.put("database", "UP");
                } else {
                    health.put("database", "DOWN");
                    health.put("status", "DOWN");
                }
            } catch (Exception e) {
                log.error("数据库连接检查失败", e);
                health.put("database", "DOWN");
                health.put("status", "DOWN");
                health.put("error", e.getMessage());
            }
            
            // 系统信息
            Runtime runtime = Runtime.getRuntime();
            Map<String, Object> system = new HashMap<>();
            system.put("totalMemory", runtime.totalMemory() / 1024 / 1024 + "MB");
            system.put("freeMemory", runtime.freeMemory() / 1024 / 1024 + "MB");
            system.put("maxMemory", runtime.maxMemory() / 1024 / 1024 + "MB");
            system.put("processors", runtime.availableProcessors());
            health.put("system", system);
            
            if ("UP".equals(health.get("status"))) {
                return ResponseEntity.ok(health);
            } else {
                return ResponseEntity.status(503).body(health);
            }
            
        } catch (Exception e) {
            log.error("健康检查失败", e);
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            health.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(503).body(health);
        }
    }

    /**
     * 快速检查
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "CourtLink is running");
        return ResponseEntity.ok(response);
    }

    /**
     * 版本信息
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("application", "CourtLink");
        info.put("description", "Badminton Court Booking System");
        info.put("version", "1.0.0");
        info.put("build", LocalDateTime.now().toString());
        info.put("java_version", System.getProperty("java.version"));
        info.put("spring_version", org.springframework.core.SpringVersion.getVersion());
        return ResponseEntity.ok(info);
    }
} 