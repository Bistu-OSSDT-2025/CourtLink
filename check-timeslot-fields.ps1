#!/usr/bin/env pwsh

Write-Host "=== 检查时间段字段状态 ===" -ForegroundColor Green

# 管理员登录
Write-Host "`n1. 管理员登录..." -ForegroundColor Blue
$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/login" -Method POST -ContentType "application/json" -Body '{"usernameOrEmail": "admin", "password": "admin123"}'
$adminToken = $loginResponse.token
$headers = @{ "Authorization" = "Bearer $adminToken" }

# 获取管理员视图数据
Write-Host "`n2. 获取管理员视图数据..." -ForegroundColor Blue
$adminData = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/courts/management" -Method GET -Headers $headers

# 获取用户视图数据
Write-Host "`n3. 获取用户视图数据..." -ForegroundColor Blue
$userData = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/courts/booking" -Method GET

# 详细分析每个时间段
Write-Host "`n4. 时间段字段状态分析..." -ForegroundColor Blue

$totalSlots = 0
$openSlots = 0
$availableSlots = 0
$openAndAvailableSlots = 0

Write-Host "`n管理员视图中的时间段状态:" -ForegroundColor Yellow
$adminData | ForEach-Object {
    $court = $_
    Write-Host "场地: $($court.name)" -ForegroundColor Cyan
    
    $court.timeSlots | ForEach-Object {
        $slot = $_
        $totalSlots++
        
        if ($slot.isOpen) { $openSlots++ }
        if ($slot.available) { $availableSlots++ }
        if ($slot.isOpen -and $slot.available) { $openAndAvailableSlots++ }
        
        # 显示前5个时间段的详细状态
        if ($totalSlots -le 5) {
            Write-Host "  ID:$($slot.id) 时间:$($slot.startTime)-$($slot.endTime) isOpen:$($slot.isOpen) available:$($slot.available)" -ForegroundColor Gray
        }
    }
}

Write-Host "`n用户视图中的时间段数量:" -ForegroundColor Yellow
$userAvailableCount = 0
$userData | ForEach-Object {
    $court = $_
    $userAvailableCount += $court.timeSlots.Count
    Write-Host "场地: $($court.name) - 可见时间段: $($court.timeSlots.Count)" -ForegroundColor Cyan
}

Write-Host "`n=== 统计汇总 ===" -ForegroundColor Green
Write-Host "总时间段: $totalSlots"
Write-Host "开放状态 (isOpen=true): $openSlots"
Write-Host "可用状态 (available=true): $availableSlots"
Write-Host "开放且可用 (isOpen=true AND available=true): $openAndAvailableSlots"
Write-Host "用户可见时间段: $userAvailableCount"

Write-Host "`n=== 问题诊断 ===" -ForegroundColor Red
if ($openSlots -gt $userAvailableCount) {
    Write-Host "❌ 问题确认：管理员开放了 $openSlots 个时间段，但用户只能看到 $userAvailableCount 个"
    Write-Host "   这表明存在 isOpen=true 但 available=false 的时间段"
    
    # 找出problematic时间段
    Write-Host "`n有问题的时间段:" -ForegroundColor Yellow
    $problemCount = 0
    $adminData | ForEach-Object {
        $court = $_
        $court.timeSlots | ForEach-Object {
            $slot = $_
            if ($slot.isOpen -and -not $slot.available) {
                $problemCount++
                Write-Host "  场地:$($court.name) ID:$($slot.id) 时间:$($slot.startTime)-$($slot.endTime) [isOpen=true, available=false]" -ForegroundColor Red
            }
        }
    }
    Write-Host "发现 $problemCount 个有问题的时间段"
} else {
    Write-Host "✅ 数据一致性正常"
} 