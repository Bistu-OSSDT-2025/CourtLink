#!/usr/bin/env pwsh

Write-Host "=== 调试用户API数据 ===" -ForegroundColor Green

# 1. 检查用户API返回的原始数据
Write-Host "`n1. 检查用户API原始数据..." -ForegroundColor Blue
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/courts/booking" -Method GET -ContentType "application/json"
    $userData = $response.Content | ConvertFrom-Json
    Write-Host "✅ 用户API响应成功" -ForegroundColor Green
    Write-Host "状态码: $($response.StatusCode)" -ForegroundColor Gray
    Write-Host "响应内容类型: $($response.Headers.'Content-Type')" -ForegroundColor Gray
    
    # 详细分析每个场地的数据
    Write-Host "`n场地数据详情:" -ForegroundColor Yellow
    $totalUserSlots = 0
    $userData | ForEach-Object {
        $court = $_
        Write-Host "场地: $($court.name)" -ForegroundColor Cyan
        Write-Host "  描述: $($court.description)" -ForegroundColor Gray
        Write-Host "  价格: ¥$($court.pricePerHour)/小时" -ForegroundColor Gray
        Write-Host "  场地可用: $($court.available)" -ForegroundColor Gray
        Write-Host "  时间段数量: $($court.timeSlots.Count)" -ForegroundColor Gray
        
        if ($court.timeSlots.Count -gt 0) {
            Write-Host "  时间段详情:" -ForegroundColor Gray
            $court.timeSlots | ForEach-Object {
                $slot = $_
                $totalUserSlots++
                Write-Host "    ID:$($slot.id) 时间:$($slot.startTime)-$($slot.endTime) 可用:$($slot.available)" -ForegroundColor DarkGray
            }
        } else {
            Write-Host "  ❌ 该场地没有任何时间段" -ForegroundColor Red
        }
        Write-Host ""
    }
    
    Write-Host "用户可见时间段总数: $totalUserSlots" -ForegroundColor Yellow
    
} catch {
    Write-Host "❌ 用户API访问失败: $($_.Exception.Message)" -ForegroundColor Red
    return
}

# 2. 对比管理员API数据
Write-Host "`n2. 对比管理员API数据..." -ForegroundColor Blue
try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/login" -Method POST -ContentType "application/json" -Body '{"usernameOrEmail": "admin", "password": "admin123"}'
    $adminToken = $loginResponse.token
    $headers = @{ "Authorization" = "Bearer $adminToken" }
    
    $adminData = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/courts/management" -Method GET -Headers $headers
    Write-Host "✅ 管理员API响应成功" -ForegroundColor Green
    
    $totalAdminSlots = 0
    $openAdminSlots = 0
    $adminData | ForEach-Object {
        $court = $_
        $court.timeSlots | ForEach-Object {
            $slot = $_
            $totalAdminSlots++
            if ($slot.isOpen) { $openAdminSlots++ }
        }
    }
    
    Write-Host "管理员视图统计:" -ForegroundColor Yellow
    Write-Host "  总时间段: $totalAdminSlots" -ForegroundColor Gray
    Write-Host "  开放时间段: $openAdminSlots" -ForegroundColor Gray
    
} catch {
    Write-Host "❌ 管理员API访问失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 3. 检查数据一致性
Write-Host "`n3. 数据一致性检查..." -ForegroundColor Blue
if ($totalUserSlots -eq $openAdminSlots) {
    Write-Host "✅ 数据一致性正常" -ForegroundColor Green
    Write-Host "   用户看到 $totalUserSlots 个时间段，管理员开放了 $openAdminSlots 个时间段" -ForegroundColor Green
} else {
    Write-Host "❌ 数据一致性异常" -ForegroundColor Red
    Write-Host "   用户看到 $totalUserSlots 个时间段，但管理员开放了 $openAdminSlots 个时间段" -ForegroundColor Red
}

# 4. 检查前端缓存
Write-Host "`n4. 前端缓存检查建议..." -ForegroundColor Blue
Write-Host "如果数据一致性正常但前端显示异常，可能的原因:" -ForegroundColor Yellow
Write-Host "1. 浏览器缓存问题 - 建议清除浏览器缓存或使用无痕模式" -ForegroundColor Gray
Write-Host "2. 前端状态管理问题 - 检查Vue组件的响应式数据更新" -ForegroundColor Gray
Write-Host "3. API请求缓存 - 检查axios配置是否有缓存设置" -ForegroundColor Gray
Write-Host "4. 时间戳问题 - 检查API是否使用了正确的日期参数" -ForegroundColor Gray

# 5. 实时监控API请求
Write-Host "`n5. 实时监控建议..." -ForegroundColor Blue
Write-Host "可以在浏览器开发者工具的Network面板中查看:" -ForegroundColor Yellow
Write-Host "- GET /api/v1/courts/booking 的响应内容" -ForegroundColor Gray
Write-Host "- 检查响应数据是否与后端实际返回的数据一致" -ForegroundColor Gray
Write-Host "- 检查是否有错误的HTTP状态码" -ForegroundColor Gray

Write-Host "`n=== 调试完成 ===" -ForegroundColor Green 