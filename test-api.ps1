#!/usr/bin/env pwsh

# CourtLink API测试脚本
Write-Host "=== CourtLink API 功能测试 ===" -ForegroundColor Green

$baseUrl = "http://localhost:8080"
$testUser = @{
    username = "testuser_$(Get-Date -Format 'yyyyMMddHHmmss')"
    email = "test_$(Get-Date -Format 'yyyyMMddHHmmss')@example.com"
    password = "testpassword123"
    fullName = "测试用户"
    phoneNumber = "13800138000"
}

Write-Host "测试用户信息:" -ForegroundColor Cyan
Write-Host "用户名: $($testUser.username)" -ForegroundColor Yellow
Write-Host "邮箱: $($testUser.email)" -ForegroundColor Yellow
Write-Host "应用URL: $baseUrl" -ForegroundColor Yellow

# 测试应用是否启动
Write-Host "`n检查应用状态..." -ForegroundColor Yellow
try {
    $healthResponse = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -Method Get -TimeoutSec 10
    Write-Host "✓ 应用状态: $($healthResponse.status)" -ForegroundColor Green
} catch {
    Write-Host "✗ 应用未启动或无法访问" -ForegroundColor Red
    Write-Host "请确保应用运行在 $baseUrl" -ForegroundColor Yellow
    exit 1
}

Write-Host "`n开始API测试..." -ForegroundColor Green 