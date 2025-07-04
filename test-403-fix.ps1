#!/usr/bin/env pwsh

Write-Host "=== 测试403权限问题修复 ===" -ForegroundColor Green
Write-Host ""

$baseUrl = "http://localhost:8080/api/v1"
$testDate = (Get-Date).AddDays(1).ToString("yyyy-MM-dd")

# 等待服务启动
Write-Host "等待服务启动..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

# 检查服务是否启动
Write-Host "检查服务状态..." -ForegroundColor Yellow
try {
    $healthCheck = Invoke-RestMethod -Uri "$baseUrl/courts/booking?date=$testDate" -Method GET -TimeoutSec 10
    if ($healthCheck.success) {
        Write-Host "✅ 服务启动成功" -ForegroundColor Green
    } else {
        Write-Host "❌ 服务响应异常" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ 服务尚未启动或不可用" -ForegroundColor Red
    exit 1
}

# 尝试注册和登录用户
Write-Host ""
Write-Host "1. 注册测试用户..." -ForegroundColor Yellow
try {
    $registerResponse = Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method POST -Body (@{
        username = "testuser403"
        password = "password123"
        email = "test403@example.com"
        fullName = "Test User 403"
    } | ConvertTo-Json) -ContentType "application/json"
    
    if ($registerResponse.success) {
        Write-Host "✅ 用户注册成功" -ForegroundColor Green
    } else {
        Write-Host "⚠️ 用户可能已存在" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠️ 注册失败，用户可能已存在" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "2. 用户登录..." -ForegroundColor Yellow
try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -Body (@{
        username = "testuser403"
        password = "password123"
    } | ConvertTo-Json) -ContentType "application/json"
    
    if ($loginResponse.success) {
        Write-Host "✅ 用户登录成功" -ForegroundColor Green
        $token = $loginResponse.token
        $headers = @{
            "Authorization" = "Bearer $token"
            "Content-Type" = "application/json"
        }
        Write-Host "   Token: $($token.Substring(0, 20))..." -ForegroundColor Cyan
    } else {
        Write-Host "❌ 用户登录失败: $($loginResponse.message)" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ 登录请求失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 获取场地信息
Write-Host ""
Write-Host "3. 获取场地信息..." -ForegroundColor Yellow
try {
    $courtsResponse = Invoke-RestMethod -Uri "$baseUrl/courts/booking?date=$testDate" -Method GET
    
    if ($courtsResponse.success -and $courtsResponse.data.Count -gt 0) {
        Write-Host "✅ 场地信息获取成功" -ForegroundColor Green
        $court = $courtsResponse.data[0]
        $availableSlots = $court.timeSlots | Where-Object { $_.available -eq $true }
        
        if ($availableSlots.Count -gt 0) {
            Write-Host "   可用时间段: $($availableSlots.Count) 个" -ForegroundColor Cyan
            $testSlot = $availableSlots[0]
            Write-Host "   测试时间段: $($testSlot.startTime) - $($testSlot.endTime)" -ForegroundColor Cyan
        } else {
            Write-Host "❌ 没有可用的时间段" -ForegroundColor Red
            exit 1
        }
    } else {
        Write-Host "❌ 场地信息获取失败" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ 场地信息获取失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 测试预约API权限
Write-Host ""
Write-Host "4. 测试预约API权限..." -ForegroundColor Yellow
$appointmentRequest = @{
    courtId = $court.id
    appointmentDate = $testDate
    startTime = $testSlot.startTime
    endTime = $testSlot.endTime
    timeSlotIds = @($testSlot.id)
    note = "测试预约 - 403权限修复"
}

try {
    $appointmentResponse = Invoke-RestMethod -Uri "$baseUrl/appointments" -Method POST -Body ($appointmentRequest | ConvertTo-Json) -Headers $headers
    
    if ($appointmentResponse.success) {
        Write-Host "✅ 预约创建成功! 403权限问题已修复!" -ForegroundColor Green
        Write-Host "   预约ID: $($appointmentResponse.appointment.id)" -ForegroundColor Cyan
        Write-Host "   场地: $($appointmentResponse.appointment.courtName)" -ForegroundColor Cyan
        Write-Host "   总价: ¥$($appointmentResponse.appointment.totalPrice)" -ForegroundColor Cyan
        
        if ($appointmentResponse.payment) {
            Write-Host "   支付ID: $($appointmentResponse.payment.paymentId)" -ForegroundColor Cyan
            Write-Host "   支付金额: ¥$($appointmentResponse.payment.amount)" -ForegroundColor Cyan
        }
    } else {
        Write-Host "❌ 预约创建失败: $($appointmentResponse.message)" -ForegroundColor Red
    }
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "❌ 预约请求失败，状态码: $statusCode" -ForegroundColor Red
    
    if ($statusCode -eq 403) {
        Write-Host "   仍然是403权限错误，权限配置可能需要进一步调整" -ForegroundColor Red
    } elseif ($statusCode -eq 401) {
        Write-Host "   401认证错误，Token可能无效" -ForegroundColor Red
    } elseif ($statusCode -eq 400) {
        Write-Host "   400参数错误，请求参数可能有问题" -ForegroundColor Red
    } else {
        Write-Host "   其他错误: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "=== 测试完成 ===" -ForegroundColor Green
Write-Host "如果看到预约创建成功，说明403权限问题已修复!" -ForegroundColor Green
Write-Host ""
Write-Host "现在可以在浏览器中测试预约和支付功能了！" -ForegroundColor Cyan 