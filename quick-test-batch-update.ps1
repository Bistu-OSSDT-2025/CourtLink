#!/usr/bin/env pwsh

Write-Host "=== 快速测试批量更新功能 ===" -ForegroundColor Green

# 1. 管理员登录
Write-Host "`n1. 管理员登录..." -ForegroundColor Blue
$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/login" -Method POST -ContentType "application/json" -Body '{"usernameOrEmail": "admin", "password": "admin123"}'
$adminToken = $loginResponse.token
Write-Host "管理员登录成功，Token: $($adminToken.Substring(0, 20))..." -ForegroundColor Green

# 2. 获取管理员视图数据
Write-Host "`n2. 获取管理员视图数据..." -ForegroundColor Blue
$headers = @{ "Authorization" = "Bearer $adminToken" }
$adminData = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/courts/management" -Method GET -Headers $headers

Write-Host "管理员看到的数据:" -ForegroundColor Yellow
$totalSlots = 0
$openSlots = 0
$adminData | ForEach-Object {
    $court = $_
    Write-Host "  场地: $($court.name)" -ForegroundColor Cyan
    $court.timeSlots | ForEach-Object {
        $slot = $_
        $totalSlots++
        if ($slot.isOpen) { $openSlots++ }
        Write-Host "    时间段 $($slot.id): $($slot.startTime)-$($slot.endTime), 开放: $($slot.isOpen), 可用: $($slot.available)" -ForegroundColor Gray
    }
}
Write-Host "管理员统计: 总时间段 $totalSlots, 开放时间段 $openSlots" -ForegroundColor Yellow

# 3. 选择第一个关闭的时间段进行测试
Write-Host "`n3. 查找第一个关闭的时间段进行测试..." -ForegroundColor Blue
$testSlot = $null
$adminData | ForEach-Object {
    $court = $_
    $court.timeSlots | ForEach-Object {
        $slot = $_
        if (-not $slot.isOpen -and $slot.available -and $testSlot -eq $null) {
            $testSlot = $slot
        }
    }
}

if ($testSlot) {
    Write-Host "找到测试时间段: ID=$($testSlot.id), 时间=$($testSlot.startTime)-$($testSlot.endTime)" -ForegroundColor Green
    
    # 4. 执行批量更新开放操作
    Write-Host "`n4. 执行批量更新开放操作..." -ForegroundColor Blue
    $updateData = @(
        @{
            timeSlotId = $testSlot.id
            open = $true
            note = "测试开放"
        }
    )
    
    $updateJson = $updateData | ConvertTo-Json
    Write-Host "发送更新数据: $updateJson" -ForegroundColor Gray
    
    try {
        $updateResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/courts/time-slots/batch-update" -Method PUT -ContentType "application/json" -Headers $headers -Body $updateJson
        Write-Host "批量更新成功响应: $($updateResponse | ConvertTo-Json)" -ForegroundColor Green
    } catch {
        Write-Host "批量更新失败: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "错误详情: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    }
    
    # 5. 再次获取管理员数据验证
    Write-Host "`n5. 验证管理员数据..." -ForegroundColor Blue
    $adminDataAfter = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/courts/management" -Method GET -Headers $headers
    
    $updatedSlot = $null
    $adminDataAfter | ForEach-Object {
        $court = $_
        $court.timeSlots | ForEach-Object {
            $slot = $_
            if ($slot.id -eq $testSlot.id) {
                $updatedSlot = $slot
            }
        }
    }
    
    if ($updatedSlot) {
        Write-Host "更新后的时间段状态:" -ForegroundColor Yellow
        Write-Host "  ID: $($updatedSlot.id)" -ForegroundColor Gray
        Write-Host "  开放状态: $($updatedSlot.isOpen)" -ForegroundColor Gray
        Write-Host "  可用状态: $($updatedSlot.available)" -ForegroundColor Gray
        Write-Host "  备注: $($updatedSlot.note)" -ForegroundColor Gray
    }
    
    # 6. 获取用户视图数据对比
    Write-Host "`n6. 获取用户视图数据对比..." -ForegroundColor Blue
    $userData = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/courts/booking" -Method GET
    
    Write-Host "用户看到的数据:" -ForegroundColor Yellow
    $userTotalSlots = 0
    $userAvailableSlots = 0
    $userData | ForEach-Object {
        $court = $_
        Write-Host "  场地: $($court.name)" -ForegroundColor Cyan
        $court.timeSlots | ForEach-Object {
            $slot = $_
            $userTotalSlots++
            if ($slot.available) { $userAvailableSlots++ }
            Write-Host "    时间段 $($slot.id): $($slot.startTime)-$($slot.endTime), 可用: $($slot.available)" -ForegroundColor Gray
        }
    }
    Write-Host "用户统计: 总时间段 $userTotalSlots, 可用时间段 $userAvailableSlots" -ForegroundColor Yellow
    
    # 7. 数据一致性检查
    Write-Host "`n7. 数据一致性检查..." -ForegroundColor Blue
    $userTestSlot = $null
    $userData | ForEach-Object {
        $court = $_
        $court.timeSlots | ForEach-Object {
            $slot = $_
            if ($slot.id -eq $testSlot.id) {
                $userTestSlot = $slot
            }
        }
    }
    
    if ($userTestSlot) {
        Write-Host "测试时间段数据对比:" -ForegroundColor Yellow
        Write-Host "  管理员视图 - 开放: $($updatedSlot.isOpen), 可用: $($updatedSlot.available)" -ForegroundColor Gray
        Write-Host "  用户视图 - 可用: $($userTestSlot.available)" -ForegroundColor Gray
        
        if ($updatedSlot.isOpen -and $updatedSlot.available -and $userTestSlot.available) {
            Write-Host "✅ 数据一致性验证通过" -ForegroundColor Green
        } else {
            Write-Host "❌ 数据一致性验证失败" -ForegroundColor Red
        }
    } else {
        Write-Host "❌ 用户视图中未找到测试时间段" -ForegroundColor Red
    }
    
} else {
    Write-Host "未找到可测试的关闭时间段" -ForegroundColor Red
}

Write-Host "`n=== 测试完成 ===" -ForegroundColor Cyan 