#!/usr/bin/env pwsh

# 检查数据库状态脚本
Write-Host "=== 检查数据库状态 ===" -ForegroundColor Green

# 访问H2数据库控制台的URL
$h2ConsoleUrl = "http://localhost:8080/h2-console"

Write-Host "数据库信息:" -ForegroundColor Cyan
Write-Host "URL: jdbc:h2:file:./data/courtlink" -ForegroundColor Gray
Write-Host "用户名: SA" -ForegroundColor Gray  
Write-Host "密码: (空)" -ForegroundColor Gray
Write-Host "H2控制台: $h2ConsoleUrl" -ForegroundColor Gray

Write-Host "`n直接查询数据库状态..." -ForegroundColor Blue

# 使用管理员登录获取数据
Write-Host "1. 管理员登录..." -ForegroundColor Yellow
$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/login" -Method POST -ContentType "application/json" -Body '{"username": "admin", "password": "admin123"}'
$adminToken = $loginResponse.token

# 获取管理员数据
Write-Host "2. 获取管理员数据..." -ForegroundColor Yellow
$headers = @{ "Authorization" = "Bearer $adminToken" }
$adminData = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/courts/management" -Method GET -Headers $headers

# 获取用户数据
Write-Host "3. 获取用户数据..." -ForegroundColor Yellow
$userData = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/courts/booking" -Method GET

# 详细分析数据
Write-Host "`n=== 数据分析 ===" -ForegroundColor Blue

Write-Host "管理员数据详情:" -ForegroundColor Cyan
$adminData | ForEach-Object {
    $court = $_
    Write-Host "场地: $($court.name)" -ForegroundColor Yellow
    Write-Host "  可用: $($court.available)" -ForegroundColor Gray
    Write-Host "  时间段数量: $($court.timeSlots.Count)" -ForegroundColor Gray
    
    $openCount = ($court.timeSlots | Where-Object { $_.isOpen }).Count
    $closedCount = ($court.timeSlots | Where-Object { -not $_.isOpen }).Count
    Write-Host "  开放: $openCount, 关闭: $closedCount" -ForegroundColor Gray
    
    # 显示前3个时间段的状态
    Write-Host "  前3个时间段状态:" -ForegroundColor Gray
    $court.timeSlots | Select-Object -First 3 | ForEach-Object {
        Write-Host "    ID:$($_.id) 时间:$($_.startTime)-$($_.endTime) 开放:$($_.isOpen) 可用:$($_.available)" -ForegroundColor DarkGray
    }
}

Write-Host "`n用户数据详情:" -ForegroundColor Cyan
$userData | ForEach-Object {
    $court = $_
    Write-Host "场地: $($court.name)" -ForegroundColor Yellow
    Write-Host "  可用: $($court.available)" -ForegroundColor Gray
    Write-Host "  时间段数量: $($court.timeSlots.Count)" -ForegroundColor Gray
    
    $availableCount = ($court.timeSlots | Where-Object { $_.available }).Count
    $unavailableCount = ($court.timeSlots | Where-Object { -not $_.available }).Count
    Write-Host "  可用: $availableCount, 不可用: $unavailableCount" -ForegroundColor Gray
}

# 统计对比
Write-Host "`n=== 统计对比 ===" -ForegroundColor Blue

$adminTotalSlots = 0
$adminOpenSlots = 0
$adminData | ForEach-Object {
    $court = $_
    $court.timeSlots | ForEach-Object {
        $adminTotalSlots++
        if ($_.isOpen) {
            $adminOpenSlots++
        }
    }
}

$userTotalSlots = 0
$userAvailableSlots = 0
$userData | ForEach-Object {
    $court = $_
    $court.timeSlots | ForEach-Object {
        $userTotalSlots++
        if ($_.available) {
            $userAvailableSlots++
        }
    }
}

Write-Host "管理员看到: $adminTotalSlots 个时间段, $adminOpenSlots 个开放" -ForegroundColor Yellow
Write-Host "用户看到: $userTotalSlots 个时间段, $userAvailableSlots 个可用" -ForegroundColor Yellow

if ($adminOpenSlots -eq $userTotalSlots) {
    Write-Host "✅ 数据一致性正常" -ForegroundColor Green
} else {
    Write-Host "❌ 数据一致性异常" -ForegroundColor Red
    Write-Host "期望: 管理员开放的时间段数量 = 用户看到的时间段数量" -ForegroundColor Red
}

Write-Host "`n=== 检查完成 ===" -ForegroundColor Green 