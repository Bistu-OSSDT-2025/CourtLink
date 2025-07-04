#!/usr/bin/env pwsh

Write-Host "=== 基础API测试 ===" -ForegroundColor Green
Write-Host ""

$baseUrl = "http://localhost:8080/api/v1"
$testDate = (Get-Date).AddDays(1).ToString("yyyy-MM-dd")

Write-Host "1. 测试公共API（不需要认证）..." -ForegroundColor Yellow
try {
    $courtsResponse = Invoke-RestMethod -Uri "$baseUrl/courts/booking?date=$testDate" -Method GET
    
    if ($courtsResponse.success) {
        Write-Host "✅ 公共API工作正常" -ForegroundColor Green
        Write-Host "   场地数量: $($courtsResponse.data.Count)" -ForegroundColor Cyan
    } else {
        Write-Host "❌ 公共API响应异常" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ 公共API请求失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "2. 测试预约API路径（期望401/403错误）..." -ForegroundColor Yellow
try {
    # 不带认证的预约请求，期望得到401或403错误
    $appointmentRequest = @{
        courtId = 1
        appointmentDate = $testDate
        startTime = "09:00"
        endTime = "10:00"
        timeSlotIds = @(1)
        note = "测试预约"
    }
    
    $response = Invoke-RestMethod -Uri "$baseUrl/appointments" -Method POST -Body ($appointmentRequest | ConvertTo-Json) -ContentType "application/json"
    Write-Host "❌ 预约API意外成功（应该要求认证）" -ForegroundColor Red
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "   HTTP状态码: $statusCode" -ForegroundColor Cyan
    
    if ($statusCode -eq 401) {
        Write-Host "✅ 返回401认证错误（API路径正确）" -ForegroundColor Green
    } elseif ($statusCode -eq 403) {
        Write-Host "✅ 返回403权限错误（API路径正确）" -ForegroundColor Green
    } elseif ($statusCode -eq 404) {
        Write-Host "❌ 返回404错误（API路径可能不正确）" -ForegroundColor Red
    } else {
        Write-Host "⚠️ 返回其他错误: $statusCode" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "3. 测试用户注册（创建测试用户）..." -ForegroundColor Yellow
try {
    $registerResponse = Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method POST -Body (@{
        username = "testuser999"
        password = "password123"
        email = "test999@example.com"
        fullName = "Test User 999"
    } | ConvertTo-Json) -ContentType "application/json"
    
    if ($registerResponse.success) {
        Write-Host "✅ 用户注册成功" -ForegroundColor Green
        
        # 立即尝试登录
        Write-Host ""
        Write-Host "4. 测试新用户登录..." -ForegroundColor Yellow
        $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -Body (@{
            username = "testuser999"
            password = "password123"
        } | ConvertTo-Json) -ContentType "application/json"
        
        if ($loginResponse.success) {
            Write-Host "✅ 新用户登录成功" -ForegroundColor Green
            Write-Host "   Token: $($loginResponse.token.Substring(0, 20))..." -ForegroundColor Cyan
            
            # 测试带认证的预约API
            Write-Host ""
            Write-Host "5. 测试带认证的预约API..." -ForegroundColor Yellow
            $headers = @{
                "Authorization" = "Bearer $($loginResponse.token)"
                "Content-Type" = "application/json"
            }
            
            try {
                $appointmentResponse = Invoke-RestMethod -Uri "$baseUrl/appointments" -Method POST -Body ($appointmentRequest | ConvertTo-Json) -Headers $headers
                
                if ($appointmentResponse.success) {
                    Write-Host "🎉 预约创建成功! 403权限问题已修复!" -ForegroundColor Green
                    Write-Host "   现在用户可以正常预约和跳转到支付界面了！" -ForegroundColor Green
                } else {
                    Write-Host "❌ 预约创建失败: $($appointmentResponse.message)" -ForegroundColor Red
                }
            } catch {
                $statusCode = $_.Exception.Response.StatusCode.value__
                Write-Host "❌ 预约请求失败，状态码: $statusCode" -ForegroundColor Red
                
                if ($statusCode -eq 403) {
                    Write-Host "   ❌ 仍然是403权限错误，需要进一步调试" -ForegroundColor Red
                } else {
                    Write-Host "   其他错误: $($_.Exception.Message)" -ForegroundColor Red
                }
            }
            
        } else {
            Write-Host "❌ 新用户登录失败: $($loginResponse.message)" -ForegroundColor Red
        }
    } else {
        Write-Host "⚠️ 用户注册失败，可能已存在: $($registerResponse.message)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠️ 注册请求失败: $($_.Exception.Message)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== 测试完成 ===" -ForegroundColor Green 