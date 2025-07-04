#!/usr/bin/env pwsh

Write-Host "=== 场地管理数据一致性测试 ===" -ForegroundColor Green

# 1. 管理员登录
Write-Host "`n1. 管理员登录..." -ForegroundColor Blue
$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/login" -Method POST -ContentType "application/json" -Body '{"username": "admin", "password": "admin123"}'
$adminToken = $loginResponse.token
Write-Host "管理员登录成功" -ForegroundColor Green

# 2. 获取管理员视图的场地数据
Write-Host "`n2. 获取管理员视图的场地数据..." -ForegroundColor Blue
$headers = @{ "Authorization" = "Bearer $adminToken" }
$adminData = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/courts/management" -Method GET -Headers $headers

$totalSlots = 0
$openSlots = 0
$adminData | ForEach-Object {
    $court = $_
    Write-Host "管理员看到场地: $($court.name)" -ForegroundColor Cyan
    $court.timeSlots | ForEach-Object {
        $totalSlots++
        if ($_.isOpen) {
            $openSlots++
        }
    }
}
Write-Host "管理员看到: $totalSlots 个时间段, $openSlots 个开放" -ForegroundColor Yellow

# 3. 获取用户视图的场地数据
Write-Host "`n3. 获取用户视图的场地数据..." -ForegroundColor Blue
$userData = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/courts/booking" -Method GET

$userTotalSlots = 0
$userAvailableSlots = 0
$userData | ForEach-Object {
    $court = $_
    Write-Host "用户看到场地: $($court.name)" -ForegroundColor Cyan
    $court.timeSlots | ForEach-Object {
        $userTotalSlots++
        if ($_.available) {
            $userAvailableSlots++
        }
    }
}
Write-Host "用户看到: $userTotalSlots 个时间段, $userAvailableSlots 个可用" -ForegroundColor Yellow

# 4. 数据一致性验证
Write-Host "`n4. 数据一致性验证..." -ForegroundColor Blue
if ($openSlots -eq $userTotalSlots) {
    Write-Host "✅ 数据一致性验证通过" -ForegroundColor Green
} else {
    Write-Host "❌ 数据一致性验证失败" -ForegroundColor Red
    Write-Host "管理员开放的时间段: $openSlots" -ForegroundColor Red
    Write-Host "用户看到的时间段: $userTotalSlots" -ForegroundColor Red
}

# 5. 测试批量开放操作
Write-Host "`n5. 测试批量开放操作..." -ForegroundColor Blue
$updates = @()
$adminData | ForEach-Object {
    $court = $_
    $court.timeSlots | ForEach-Object {
        if (-not $_.isOpen) {
            $updates += @{
                timeSlotId = $_.id
                open = $true
                note = "测试开放"
            }
        }
    }
}

if ($updates.Count -gt 0) {
    Write-Host "准备开放 $($updates.Count) 个时间段" -ForegroundColor Yellow
    $batchResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/courts/time-slots/batch-update" -Method PUT -Headers $headers -ContentType "application/json" -Body ($updates | ConvertTo-Json)
    Write-Host "批量开放操作完成" -ForegroundColor Green
    
    # 重新验证数据
    Write-Host "`n6. 重新验证数据..." -ForegroundColor Blue
    $adminData2 = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/courts/management" -Method GET -Headers $headers
    $userData2 = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/courts/booking" -Method GET
    
    $totalSlots2 = 0
    $openSlots2 = 0
    $adminData2 | ForEach-Object {
        $court = $_
        $court.timeSlots | ForEach-Object {
            $totalSlots2++
            if ($_.isOpen) {
                $openSlots2++
            }
        }
    }
    
    $userTotalSlots2 = 0
    $userAvailableSlots2 = 0
    $userData2 | ForEach-Object {
        $court = $_
        $court.timeSlots | ForEach-Object {
            $userTotalSlots2++
            if ($_.available) {
                $userAvailableSlots2++
            }
        }
    }
    
    Write-Host "管理员看到: $totalSlots2 个时间段, $openSlots2 个开放" -ForegroundColor Yellow
    Write-Host "用户看到: $userTotalSlots2 个时间段, $userAvailableSlots2 个可用" -ForegroundColor Yellow
    
    if ($openSlots2 -eq $userTotalSlots2) {
        Write-Host "✅ 更新后数据一致性验证通过" -ForegroundColor Green
    } else {
        Write-Host "❌ 更新后数据一致性验证失败" -ForegroundColor Red
    }
} else {
    Write-Host "所有时间段已经开放" -ForegroundColor Green
}

Write-Host "`n=== 测试完成 ===" -ForegroundColor Green 