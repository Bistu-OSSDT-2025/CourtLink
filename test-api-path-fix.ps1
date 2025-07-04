#!/usr/bin/env pwsh

Write-Host "=== 测试API路径修复 ===" -ForegroundColor Green
Write-Host ""

$baseUrl = "http://localhost:8080/api/v1"
$testDate = (Get-Date).AddDays(1).ToString("yyyy-MM-dd")

Write-Host "1. 测试场地信息获取（公共API）..." -ForegroundColor Yellow
try {
    $courtsResponse = Invoke-RestMethod -Uri "$baseUrl/courts/booking?date=$testDate" -Method GET
    
    if ($courtsResponse.success) {
        Write-Host "✅ 场地信息获取成功" -ForegroundColor Green
        Write-Host "   场地数量: $($courtsResponse.data.Count)" -ForegroundColor Cyan
        
        if ($courtsResponse.data.Count -gt 0) {
            $court = $courtsResponse.data[0]
            Write-Host "   测试场地: $($court.name)" -ForegroundColor Cyan
            Write-Host "   可用时间段: $($court.timeSlots.Count)" -ForegroundColor Cyan
            
            $availableSlots = $court.timeSlots | Where-Object { $_.available -eq $true }
            Write-Host "   可用时间段: $($availableSlots.Count)" -ForegroundColor Cyan
        }
    } else {
        Write-Host "❌ 场地信息获取失败: $($courtsResponse.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ 场地信息获取请求失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "2. 测试预约API路径可访问性..." -ForegroundColor Yellow
try {
    # 尝试不带认证的预约请求，期望得到401错误（而不是404）
    $appointmentRequest = @{
        courtId = 1
        appointmentDate = $testDate
        startTime = "09:00"
        endTime = "10:00"
        timeSlotIds = @(1)
        note = "测试预约"
    }
    
    $response = Invoke-RestMethod -Uri "$baseUrl/appointments" -Method POST -Body ($appointmentRequest | ConvertTo-Json) -ContentType "application/json"
    Write-Host "❌ 预约请求意外成功，这不应该发生" -ForegroundColor Red
} catch {
    $statusCode = $_.Exception.Response.StatusCode
    Write-Host "   HTTP状态码: $statusCode" -ForegroundColor Cyan
    
    if ($statusCode -eq 401) {
        Write-Host "✅ 预约API路径正确 - 返回401认证错误（这是预期的）" -ForegroundColor Green
    } elseif ($statusCode -eq 404) {
        Write-Host "❌ 预约API路径错误 - 返回404找不到资源" -ForegroundColor Red
    } else {
        Write-Host "⚠️ 预约API返回其他错误: $statusCode" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "=== 测试完成 ===" -ForegroundColor Green
Write-Host "如果看到场地信息获取成功和预约API路径正确，说明API路径修复已完成！" -ForegroundColor Green
Write-Host ""
Write-Host "现在请在浏览器中进行实际的预约测试:" -ForegroundColor Cyan
Write-Host "1. 访问 http://localhost:3007" -ForegroundColor Cyan
Write-Host "2. 登录到用户账户" -ForegroundColor Cyan
Write-Host "3. 进行预约测试" -ForegroundColor Cyan 