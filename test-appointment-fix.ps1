#!/usr/bin/env pwsh

Write-Host "=== 测试预约和支付功能修复 ===" -ForegroundColor Green
Write-Host ""

# 设置测试参数
$baseUrl = "http://localhost:8080/api/v1"
$testUsername = "user1"
$testPassword = "password"
$testDate = (Get-Date).AddDays(1).ToString("yyyy-MM-dd")

Write-Host "1. 尝试注册测试用户..." -ForegroundColor Yellow
try {
    $registerResponse = Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method POST -Body (@{
        username = $testUsername
        password = $testPassword
        email = "test@example.com"
        fullName = "Test User"
    } | ConvertTo-Json) -ContentType "application/json"
    
    if ($registerResponse.success) {
        Write-Host "✅ 测试用户注册成功" -ForegroundColor Green
    } else {
        Write-Host "⚠️ 测试用户可能已存在" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠️ 注册测试用户失败，可能用户已存在" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "2. 测试用户登录..." -ForegroundColor Yellow
$loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -Body (@{
    username = $testUsername
    password = $testPassword
} | ConvertTo-Json) -ContentType "application/json"

if ($loginResponse.success) {
    Write-Host "✅ 用户登录成功" -ForegroundColor Green
    $token = $loginResponse.token
    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }
} else {
    Write-Host "❌ 用户登录失败" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "3. 获取场地信息..." -ForegroundColor Yellow
$courtsResponse = Invoke-RestMethod -Uri "$baseUrl/courts/booking?date=$testDate" -Method GET

if ($courtsResponse.success -and $courtsResponse.data.Count -gt 0) {
    Write-Host "✅ 获取场地信息成功，共 $($courtsResponse.data.Count) 个场地" -ForegroundColor Green
    $court = $courtsResponse.data[0]
    $courtId = $court.id
    $courtName = $court.name
    Write-Host "   使用场地: $courtName (ID: $courtId)" -ForegroundColor Cyan
    
    # 找到可用的时间段
    $availableSlots = $court.timeSlots | Where-Object { $_.available -eq $true }
    if ($availableSlots.Count -gt 0) {
        Write-Host "   可用时间段: $($availableSlots.Count) 个" -ForegroundColor Cyan
        $testSlot = $availableSlots[0]
        $timeSlotId = $testSlot.id
        $startTime = $testSlot.startTime
        $endTime = $testSlot.endTime
        Write-Host "   选择时间段: $startTime - $endTime (ID: $timeSlotId)" -ForegroundColor Cyan
    } else {
        Write-Host "❌ 没有找到可用的时间段" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "❌ 获取场地信息失败" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "4. 测试预约创建..." -ForegroundColor Yellow
$appointmentRequest = @{
    courtId = $courtId
    appointmentDate = $testDate
    startTime = $startTime
    endTime = $endTime
    timeSlotIds = @($timeSlotId)
    note = "测试预约"
}

try {
    $appointmentResponse = Invoke-RestMethod -Uri "$baseUrl/appointments" -Method POST -Body ($appointmentRequest | ConvertTo-Json) -Headers $headers
    
    if ($appointmentResponse.success) {
        Write-Host "✅ 预约创建成功!" -ForegroundColor Green
        Write-Host "   预约ID: $($appointmentResponse.appointment.id)" -ForegroundColor Cyan
        Write-Host "   场地: $($appointmentResponse.appointment.courtName)" -ForegroundColor Cyan
        Write-Host "   日期: $($appointmentResponse.appointment.appointmentDate)" -ForegroundColor Cyan
        Write-Host "   时间: $($appointmentResponse.appointment.startTime) - $($appointmentResponse.appointment.endTime)" -ForegroundColor Cyan
        Write-Host "   状态: $($appointmentResponse.appointment.status)" -ForegroundColor Cyan
        Write-Host "   总价: ¥$($appointmentResponse.appointment.totalPrice)" -ForegroundColor Cyan
        
        # 检查支付信息
        if ($appointmentResponse.payment) {
            Write-Host "✅ 支付信息创建成功!" -ForegroundColor Green
            Write-Host "   支付ID: $($appointmentResponse.payment.paymentId)" -ForegroundColor Cyan
            Write-Host "   金额: ¥$($appointmentResponse.payment.amount)" -ForegroundColor Cyan
            Write-Host "   状态: $($appointmentResponse.payment.status)" -ForegroundColor Cyan
            Write-Host "   过期时间: $($appointmentResponse.payment.expireTime)" -ForegroundColor Cyan
        } else {
            Write-Host "❌ 支付信息创建失败" -ForegroundColor Red
        }
    } else {
        Write-Host "❌ 预约创建失败: $($appointmentResponse.message)" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ 预约创建请求失败: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode
        Write-Host "   HTTP状态码: $statusCode" -ForegroundColor Red
        
        if ($statusCode -eq 400) {
            Write-Host "   这是400错误，可能是参数验证失败" -ForegroundColor Red
        } elseif ($statusCode -eq 401) {
            Write-Host "   这是401错误，可能是认证失败" -ForegroundColor Red
        } elseif ($statusCode -eq 403) {
            Write-Host "   这是403错误，可能是权限不足" -ForegroundColor Red
        } elseif ($statusCode -eq 404) {
            Write-Host "   这是404错误，可能是路径不存在" -ForegroundColor Red
        }
    }
    exit 1
}

Write-Host ""
Write-Host "5. 验证时间段状态..." -ForegroundColor Yellow
$updatedCourtsResponse = Invoke-RestMethod -Uri "$baseUrl/courts/booking?date=$testDate" -Method GET
$updatedCourt = $updatedCourtsResponse.data | Where-Object { $_.id -eq $courtId }
$updatedSlot = $updatedCourt.timeSlots | Where-Object { $_.id -eq $timeSlotId }

if ($updatedSlot.available -eq $false) {
    Write-Host "✅ 时间段状态已正确更新为不可用" -ForegroundColor Green
} else {
    Write-Host "❌ 时间段状态未更新，仍显示为可用" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== 测试完成 ===" -ForegroundColor Green
Write-Host "如果看到上述所有 ✅ 标记，说明预约和支付功能已修复成功!" -ForegroundColor Green
Write-Host ""
Write-Host "现在您可以在浏览器中访问 http://localhost:3007 进行测试" -ForegroundColor Cyan 