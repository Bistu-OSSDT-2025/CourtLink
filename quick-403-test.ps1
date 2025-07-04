#!/usr/bin/env pwsh

Write-Host "=== 快速403权限测试 ===" -ForegroundColor Green
Write-Host ""

$baseUrl = "http://localhost:8080/api/v1"
$testDate = (Get-Date).AddDays(1).ToString("yyyy-MM-dd")

# 使用现有用户登录（从日志中看到rjc用户存在）
Write-Host "1. 尝试登录现有用户..." -ForegroundColor Yellow
try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -Body (@{
        username = "rjc"
        password = "123456"
    } | ConvertTo-Json) -ContentType "application/json"
    
    if ($loginResponse.success) {
        Write-Host "✅ 用户登录成功" -ForegroundColor Green
        $token = $loginResponse.token
        $headers = @{
            "Authorization" = "Bearer $token"
            "Content-Type" = "application/json"
        }
    } else {
        Write-Host "❌ 用户登录失败: $($loginResponse.message)" -ForegroundColor Red
        Write-Host "尝试其他用户..." -ForegroundColor Yellow
        
        # 尝试其他可能的用户
        $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -Body (@{
            username = "user1"
            password = "password"
        } | ConvertTo-Json) -ContentType "application/json"
        
        if ($loginResponse.success) {
            Write-Host "✅ 用户1登录成功" -ForegroundColor Green
            $token = $loginResponse.token
            $headers = @{
                "Authorization" = "Bearer $token"
                "Content-Type" = "application/json"
            }
        } else {
            Write-Host "❌ 无法登录任何用户，跳过认证测试" -ForegroundColor Red
            exit 1
        }
    }
} catch {
    Write-Host "❌ 登录请求失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 获取场地信息进行预约测试
Write-Host ""
Write-Host "2. 获取场地信息..." -ForegroundColor Yellow
try {
    $courtsResponse = Invoke-RestMethod -Uri "$baseUrl/courts/booking?date=$testDate" -Method GET
    
    if ($courtsResponse.success -and $courtsResponse.data.Count -gt 0) {
        Write-Host "✅ 场地信息获取成功" -ForegroundColor Green
        $court = $courtsResponse.data[0]
        $availableSlots = $court.timeSlots | Where-Object { $_.available -eq $true }
        
        if ($availableSlots.Count -gt 0) {
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

# 测试预约API权限（关键测试）
Write-Host ""
Write-Host "3. 测试预约API权限（403权限测试）..." -ForegroundColor Yellow
$appointmentRequest = @{
    courtId = $court.id
    appointmentDate = $testDate
    startTime = $testSlot.startTime
    endTime = $testSlot.endTime
    timeSlotIds = @($testSlot.id)
    note = "403权限修复测试"
}

try {
    $appointmentResponse = Invoke-RestMethod -Uri "$baseUrl/appointments" -Method POST -Body ($appointmentRequest | ConvertTo-Json) -Headers $headers
    
    if ($appointmentResponse.success) {
        Write-Host "🎉 预约创建成功! 403权限问题已修复!" -ForegroundColor Green
        Write-Host "   预约ID: $($appointmentResponse.appointment.id)" -ForegroundColor Cyan
        Write-Host "   场地: $($appointmentResponse.appointment.courtName)" -ForegroundColor Cyan
        Write-Host "   总价: ¥$($appointmentResponse.appointment.totalPrice)" -ForegroundColor Cyan
        
        if ($appointmentResponse.payment) {
            Write-Host "   支付ID: $($appointmentResponse.payment.paymentId)" -ForegroundColor Cyan
            Write-Host "   支付金额: ¥$($appointmentResponse.payment.amount)" -ForegroundColor Cyan
            Write-Host ""
            Write-Host "✅ 支付信息创建成功，现在可以正常跳转到支付界面了！" -ForegroundColor Green
        }
    } else {
        Write-Host "❌ 预约创建失败: $($appointmentResponse.message)" -ForegroundColor Red
    }
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "❌ 预约请求失败，状态码: $statusCode" -ForegroundColor Red
    
    if ($statusCode -eq 403) {
        Write-Host "   ❌ 仍然是403权限错误" -ForegroundColor Red
        Write-Host "   需要进一步检查Spring Security配置" -ForegroundColor Yellow
    } elseif ($statusCode -eq 401) {
        Write-Host "   ❌ 401认证错误，Token可能无效" -ForegroundColor Red
    } elseif ($statusCode -eq 400) {
        Write-Host "   ✅ 400参数错误（这表明API路径正确，不是权限问题）" -ForegroundColor Green
    } else {
        Write-Host "   其他错误: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "=== 测试完成 ===" -ForegroundColor Green 