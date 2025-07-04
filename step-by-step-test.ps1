#!/usr/bin/env pwsh

Write-Host "=== 逐步诊断测试 ===" -ForegroundColor Green

$baseUrl = "http://localhost:8080/api/v1"

Write-Host "步骤1: 测试服务器连通性" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/courts/booking?date=2025-07-05" -Method GET
    Write-Host "✅ 服务器响应正常，场地数: $($response.data.Count)" -ForegroundColor Green
} catch {
    Write-Host "❌ 服务器连接失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "步骤2: 测试预约API路径（无认证）" -ForegroundColor Yellow
try {
    $testRequest = @{
        courtId = 1
        appointmentDate = "2025-07-05"
        startTime = "09:00"
        endTime = "10:00"
        timeSlotIds = @(1)
        note = "test"
    }
    
    Invoke-RestMethod -Uri "$baseUrl/appointments" -Method POST -Body ($testRequest | ConvertTo-Json) -ContentType "application/json"
    Write-Host "❌ 预约API应该要求认证，但返回成功" -ForegroundColor Red
} catch {
    $status = $_.Exception.Response.StatusCode.value__
    Write-Host "HTTP状态码: $status" -ForegroundColor Cyan
    
    if ($status -eq 401) {
        Write-Host "✅ 返回401认证错误 - API路径正确" -ForegroundColor Green
    } elseif ($status -eq 403) {
        Write-Host "✅ 返回403权限错误 - API路径正确" -ForegroundColor Green
    } elseif ($status -eq 404) {
        Write-Host "❌ 返回404错误 - API路径可能错误" -ForegroundColor Red
    } else {
        Write-Host "⚠️ 返回状态码: $status" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "=== 诊断完成 ===" -ForegroundColor Green 