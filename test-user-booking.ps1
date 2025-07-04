#!/usr/bin/env pwsh

Write-Host "=== 测试普通用户场地预约功能 ===" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080/api/v1"
$today = (Get-Date).ToString("yyyy-MM-dd")

# 测试1: 访问公共场地接口（无需认证）
Write-Host "`n1. 测试公共场地接口..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/courts" -Method GET -ContentType "application/json"
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ 公共场地接口访问成功" -ForegroundColor Green
        $courts = $response.Content | ConvertFrom-Json
        Write-Host "   场地数量: $($courts.Count)" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ 公共场地接口访问失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 测试2: 访问场地预约接口（修复后的端点）
Write-Host "`n2. 测试场地预约接口..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/courts/booking?date=$today" -Method GET -ContentType "application/json"
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ 场地预约接口访问成功" -ForegroundColor Green
        $bookingData = $response.Content | ConvertFrom-Json
        Write-Host "   预约数据已获取" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ 场地预约接口访问失败: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "   状态码: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
}

# 测试3: 注册新用户
Write-Host "`n3. 测试用户注册..." -ForegroundColor Yellow
$timestamp = (Get-Date).ToString("yyyyMMddHHmmss")
$registerData = @{
    username = "testuser$timestamp"
    email = "test$timestamp@example.com"
    password = "password123"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/auth/register" -Method POST -ContentType "application/json" -Body $registerData
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ 用户注册成功" -ForegroundColor Green
        
        # 测试4: 用户登录
        Write-Host "`n4. 测试用户登录..." -ForegroundColor Yellow
        $loginData = @{
            username = "testuser$timestamp"
            password = "password123"
        } | ConvertTo-Json
        
        $loginResponse = Invoke-WebRequest -Uri "$baseUrl/auth/login" -Method POST -ContentType "application/json" -Body $loginData
        if ($loginResponse.StatusCode -eq 200) {
            Write-Host "✅ 用户登录成功" -ForegroundColor Green
            $loginResult = $loginResponse.Content | ConvertFrom-Json
            $userToken = $loginResult.token
            Write-Host "   Token获取成功" -ForegroundColor Gray
            
            # 测试5: 用户访问场地预约接口
            Write-Host "`n5. 测试登录用户访问场地预约..." -ForegroundColor Yellow
            $headers = @{
                "Authorization" = "Bearer $userToken"
                "Content-Type" = "application/json"
            }
            
            $bookingResponse = Invoke-WebRequest -Uri "$baseUrl/courts/booking?date=$today" -Method GET -Headers $headers
            if ($bookingResponse.StatusCode -eq 200) {
                Write-Host "✅ 登录用户访问场地预约成功" -ForegroundColor Green
                $bookingData = $bookingResponse.Content | ConvertFrom-Json
                Write-Host "   预约数据已获取" -ForegroundColor Gray
            }
        }
    }
} catch {
    Write-Host "❌ 用户注册失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== 测试完成 ===" -ForegroundColor Cyan 