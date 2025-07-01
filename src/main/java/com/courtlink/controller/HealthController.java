package com.courtlink.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 提供系统健康状态检查端点
 */
@Slf4j
@RestController
@RequestMapping("/api")
@Tag(name = "Health Check", description = "系统健康检查接口")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "系统健康检查", description = "检查系统整体运行状态")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("application", "CourtLink");
        health.put("version", "1.0.0");
        
        log.debug("Health check completed - Status: UP");
        return ResponseEntity.ok(health);
    }

    @GetMapping("/health/simple")
    @Operation(summary = "简单健康检查", description = "简单的服务可用性检查")
    public ResponseEntity<String> simpleHealth() {
        log.debug("Simple health check completed");
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/health/ready")
    @Operation(summary = "就绪检查", description = "检查服务是否准备好接收请求")
    public ResponseEntity<Map<String, Object>> ready() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "READY");
        status.put("timestamp", LocalDateTime.now());
        
        log.debug("Ready check completed");
        return ResponseEntity.ok(status);
    }

    @GetMapping("/health/live")
    @Operation(summary = "存活检查", description = "检查服务是否存活")
    public ResponseEntity<Map<String, Object>> live() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "LIVE");
        status.put("timestamp", LocalDateTime.now());
        
        log.debug("Live check completed");
        return ResponseEntity.ok(status);
    }
} 