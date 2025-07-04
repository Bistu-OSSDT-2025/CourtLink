#!/usr/bin/env pwsh

# CourtLink 管理员权限测试脚本
Write-Host "=== CourtLink 管理员权限测试 ===" -ForegroundColor Green

$baseUrl = "http://localhost:8080/api/v1"

# 测试管理员登录
Write-Host "1. 测试管理员登录..." -ForegroundColor Yellow
$loginData = @{
    usernameOrEmail = "admin"
    password = "admin123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/admin/login" -Method Post -Body $loginData -ContentType "application/json"
    $token = $loginResponse.token
    $adminInfo = $loginResponse.admin
    Write-Host "✅ 管理员登录成功" -ForegroundColor Green
    Write-Host "   用户名: $($adminInfo.username)" -ForegroundColor Cyan
    Write-Host "   角色: $($adminInfo.role)" -ForegroundColor Cyan
    Write-Host "   Token: $($token.Substring(0, 20))..." -ForegroundColor Cyan
} catch {
    Write-Host "❌ 管理员登录失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 测试权限验证端点
Write-Host "`n2. 测试权限验证..." -ForegroundColor Yellow
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

try {
    $authResponse = Invoke-RestMethod -Uri "$baseUrl/admin/courts/test-auth" -Method Get -Headers $headers
    Write-Host "✅ 权限验证成功" -ForegroundColor Green
    Write-Host "   认证状态: $($authResponse.authenticated)" -ForegroundColor Cyan
    Write-Host "   用户名: $($authResponse.username)" -ForegroundColor Cyan
    Write-Host "   权限: $($authResponse.authorities -join ', ')" -ForegroundColor Cyan
} catch {
    Write-Host "❌ 权限验证失败: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response.StatusCode -eq 403) {
        Write-Host "   这表示权限配置有问题" -ForegroundColor Yellow
    }
}

# 测试场地管理API
Write-Host "`n3. 测试场地管理API..." -ForegroundColor Yellow
try {
    $courtsResponse = Invoke-RestMethod -Uri "$baseUrl/admin/courts/management" -Method Get -Headers $headers
    Write-Host "✅ 场地管理API访问成功" -ForegroundColor Green
    Write-Host "   获取到 $($courtsResponse.Count) 个场地" -ForegroundColor Cyan
} catch {
    Write-Host "❌ 场地管理API访问失败: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response.StatusCode -eq 403) {
        Write-Host "   这表示权限配置有问题" -ForegroundColor Yellow
    }
}

# 测试管理员个人信息
Write-Host "`n4. 测试管理员个人信息..." -ForegroundColor Yellow
try {
    $profileResponse = Invoke-RestMethod -Uri "$baseUrl/admin/profile" -Method Get -Headers $headers
    Write-Host "✅ 管理员个人信息获取成功" -ForegroundColor Green
    Write-Host "   全名: $($profileResponse.fullName)" -ForegroundColor Cyan
    Write-Host "   邮箱: $($profileResponse.email)" -ForegroundColor Cyan
} catch {
    Write-Host "❌ 管理员个人信息获取失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== 测试完成 ===" -ForegroundColor Green
Write-Host "如果所有测试都通过，说明权限修复成功！" -ForegroundColor Green 