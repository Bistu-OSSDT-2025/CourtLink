#!/usr/bin/env pwsh

# CourtLink健康检查脚本
Write-Host "=== CourtLink 健康检查 ===" -ForegroundColor Green

$baseUrl = "http://localhost:8080"
$timeout = 30

# 检查应用是否启动
Write-Host "检查应用状态..." -ForegroundColor Yellow

try {
    # 健康检查端点
    $healthResponse = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -Method Get -TimeoutSec $timeout
    
    if ($healthResponse.status -eq "UP") {
        Write-Host "✓ 应用健康状态: UP" -ForegroundColor Green
    } else {
        Write-Host "✗ 应用健康状态: $($healthResponse.status)" -ForegroundColor Red
        exit 1
    }
    
    # 测试用户API端点
    Write-Host "检查用户管理API..." -ForegroundColor Yellow
    
    # 测试用户列表端点
    try {
        $usersResponse = Invoke-RestMethod -Uri "$baseUrl/api/users/list" -Method Get -TimeoutSec $timeout
        Write-Host "✓ 用户列表API: 正常" -ForegroundColor Green
    } catch {
        Write-Host "✗ 用户列表API: 异常 - $($_.Exception.Message)" -ForegroundColor Red
    }
    
    # 测试用户名检查端点
    try {
        $checkResponse = Invoke-RestMethod -Uri "$baseUrl/api/users/check-username?username=testuser" -Method Get -TimeoutSec $timeout
        Write-Host "✓ 用户名检查API: 正常" -ForegroundColor Green
    } catch {
        Write-Host "✗ 用户名检查API: 异常 - $($_.Exception.Message)" -ForegroundColor Red
    }
    
    Write-Host "=== 健康检查完成 ===" -ForegroundColor Green
    Write-Host "应用URL: $baseUrl" -ForegroundColor Cyan
    Write-Host "Swagger文档: $baseUrl/swagger-ui.html" -ForegroundColor Cyan
    Write-Host "H2控制台: $baseUrl/h2-console" -ForegroundColor Cyan
    
} catch {
    Write-Host "✗ 应用未启动或无法访问: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "请确保应用已启动并运行在端口8080" -ForegroundColor Yellow
    exit 1
} 