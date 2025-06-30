package com.bistu.ossdt.courtlink.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
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
@RequestMapping("/api")
@Tag(name = "Health Check", description = "系统健康检查接口")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @Autowired(required = false)
    private BuildProperties buildProperties;

    @GetMapping("/health")
    @Operation(summary = "系统健康检查", description = "检查系统各组件的运行状态")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        Map<String, Object> components = new HashMap<>();
        
        try {
            // 检查数据库连接
            try (Connection connection = dataSource.getConnection()) {
                boolean isValid = connection.isValid(5);
                components.put("database", Map.of(
                    "status", isValid ? "UP" : "DOWN",
                    "details", Map.of(
                        "driver", connection.getMetaData().getDriverName(),
                        "url", connection.getMetaData().getURL()
                    )
                ));
            } catch (Exception e) {
                components.put("database", Map.of(
                    "status", "DOWN",
                    "error", e.getMessage()
                ));
            }

            // 系统信息
            Runtime runtime = Runtime.getRuntime();
            Map<String, Object> systemInfo = new HashMap<>();
            systemInfo.put("processors", runtime.availableProcessors());
            systemInfo.put("totalMemory", runtime.totalMemory());
            systemInfo.put("freeMemory", runtime.freeMemory());
            systemInfo.put("maxMemory", runtime.maxMemory());
            systemInfo.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
            
            components.put("system", Map.of(
                "status", "UP",
                "details", systemInfo
            ));

            // 应用信息
            Map<String, Object> appInfo = new HashMap<>();
            appInfo.put("name", "CourtLink API");
            appInfo.put("environment", "development");
            appInfo.put("timestamp", LocalDateTime.now());
            
            if (buildProperties != null) {
                appInfo.put("version", buildProperties.getVersion());
                appInfo.put("buildTime", buildProperties.getTime());
            }
            
            components.put("application", Map.of(
                "status", "UP",
                "details", appInfo
            ));

            // 整体状态
            boolean allUp = components.values().stream()
                    .allMatch(component -> "UP".equals(((Map<?, ?>) component).get("status")));
            
            health.put("status", allUp ? "UP" : "DOWN");
            health.put("timestamp", LocalDateTime.now());
            health.put("components", components);
            
            log.debug("Health check completed - Status: {}", allUp ? "UP" : "DOWN");
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            log.error("Health check failed", e);
            health.put("status", "DOWN");
            health.put("timestamp", LocalDateTime.now());
            health.put("error", e.getMessage());
            return ResponseEntity.status(503).body(health);
        }
    }

    @GetMapping("/health/simple")
    @Operation(summary = "简单健康检查", description = "简单的服务可用性检查")
    public ResponseEntity<String> simpleHealth() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/health/ready")
    @Operation(summary = "就绪检查", description = "检查服务是否准备好接收请求")
    public ResponseEntity<Map<String, Object>> ready() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "READY");
        status.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(status);
    }

    @GetMapping("/health/live")
    @Operation(summary = "存活检查", description = "检查服务是否存活")
    public ResponseEntity<Map<String, Object>> live() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "LIVE");
        status.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(status);
    }
} 