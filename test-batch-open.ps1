#!/usr/bin/env pwsh

Write-Host "=== 测试批量开放操作 ===" -ForegroundColor Green

# 管理员登录
Write-Host "`n1. 管理员登录..." -ForegroundColor Blue
$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/login" -Method POST -ContentType "application/json" -Body '{"usernameOrEmail": "admin", "password": "admin123"}'
$adminToken = $loginResponse.token
$headers = @{ "Authorization" = "Bearer $adminToken"; "Content-Type" = "application/json" }

# 获取当前状态
Write-Host "`n2. 获取批量开放前的状态..." -ForegroundColor Blue
$beforeData = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/courts/management" -Method GET -Headers $headers

$totalSlots = 0
$openSlots = 0
$closedSlots = 0

$beforeData | ForEach-Object {
    $court = $_
    $court.timeSlots | ForEach-Object {
        $slot = $_
        $totalSlots++
        if ($slot.isOpen) { $openSlots++ } else { $closedSlots++ }
    }
}

Write-Host "批量开放前状态："
Write-Host "  总时间段: $totalSlots"
Write-Host "  开放时间段: $openSlots"
Write-Host "  关闭时间段: $closedSlots"

# 准备批量开放请求
Write-Host "`n3. 准备批量开放请求..." -ForegroundColor Blue
$batchUpdates = @()

$beforeData | ForEach-Object {
    $court = $_
    $court.timeSlots | ForEach-Object {
        $slot = $_
        if (-not $slot.isOpen) {
            $batchUpdates += @{
                timeSlotId = $slot.id
                open = $true
                note = "批量开放测试"
            }
        }
    }
}

Write-Host "准备开放 $($batchUpdates.Count) 个时间段"

# 执行批量开放
if ($batchUpdates.Count -gt 0) {
    Write-Host "`n4. 执行批量开放..." -ForegroundColor Blue
    $batchJson = $batchUpdates | ConvertTo-Json -Depth 3
    Write-Host "发送的JSON数据（前200字符）: $($batchJson.Substring(0, [Math]::Min(200, $batchJson.Length)))"
    
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/courts/time-slots/batch-update" -Method PUT -Headers $headers -Body $batchJson
        Write-Host "✅ 批量开放请求成功发送" -ForegroundColor Green
    } catch {
        Write-Host "❌ 批量开放请求失败: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "状态码: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
        return
    }
    
    # 等待处理完成
    Start-Sleep -Seconds 2
    
    # 检查结果
    Write-Host "`n5. 检查批量开放结果..." -ForegroundColor Blue
    $afterData = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/courts/management" -Method GET -Headers $headers
    
    $afterTotalSlots = 0
    $afterOpenSlots = 0
    $afterClosedSlots = 0
    
    $afterData | ForEach-Object {
        $court = $_
        $court.timeSlots | ForEach-Object {
            $slot = $_
            $afterTotalSlots++
            if ($slot.isOpen) { $afterOpenSlots++ } else { $afterClosedSlots++ }
        }
    }
    
    Write-Host "批量开放后状态："
    Write-Host "  总时间段: $afterTotalSlots"
    Write-Host "  开放时间段: $afterOpenSlots"
    Write-Host "  关闭时间段: $afterClosedSlots"
    
    # 结果分析
    $actuallyOpened = $afterOpenSlots - $openSlots
    Write-Host "`n=== 结果分析 ===" -ForegroundColor Yellow
    Write-Host "计划开放: $($batchUpdates.Count) 个时间段"
    Write-Host "实际开放: $actuallyOpened 个时间段"
    
    if ($actuallyOpened -eq $batchUpdates.Count) {
        Write-Host "✅ 批量开放操作成功！" -ForegroundColor Green
    } else {
        Write-Host "❌ 批量开放操作部分失败！" -ForegroundColor Red
        Write-Host "   只有 $actuallyOpened 个时间段被成功开放，而不是预期的 $($batchUpdates.Count) 个"
    }
    
    # 验证用户端是否能看到开放的时间段
    Write-Host "`n6. 验证用户端可见性..." -ForegroundColor Blue
    $userVisible = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/courts/booking" -Method GET
    
    $userVisibleCount = 0
    $userVisible | ForEach-Object {
        $court = $_
        $userVisibleCount += $court.timeSlots.Count
    }
    
    Write-Host "用户可见时间段: $userVisibleCount"
    
    if ($userVisibleCount -eq $afterOpenSlots) {
        Write-Host "✅ 用户端数据一致性正常" -ForegroundColor Green
    } else {
        Write-Host "❌ 用户端数据一致性有问题！" -ForegroundColor Red
        Write-Host "   管理员看到 $afterOpenSlots 个开放时间段，但用户只能看到 $userVisibleCount 个"
    }
} else {
    Write-Host "所有时间段都已经开放，无需批量开放" -ForegroundColor Yellow
} 