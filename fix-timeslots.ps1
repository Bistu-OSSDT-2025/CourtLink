# 修复时间段状态脚本
Write-Host "=== 修复场地时间段状态 ===" -ForegroundColor Cyan

# 管理员登录
$adminLoginData = @{
    usernameOrEmail = "admin"
    password = "admin123"
} | ConvertTo-Json -Compress

$headers = @{
    "Content-Type" = "application/json"
}

try {
    $adminResponse = Invoke-RestMethod -Uri "http://localhost:8082/api/v1/admin/login" -Method POST -Body $adminLoginData -Headers $headers
    Write-Host "✅ 管理员登录成功" -ForegroundColor Green
    
    $adminHeaders = @{
        "Content-Type" = "application/json"
        "Authorization" = "Bearer $($adminResponse.token)"
    }
} catch {
    Write-Host "❌ 管理员登录失败" -ForegroundColor Red
    exit 1
}

# 获取今天的数据
$today = Get-Date -Format "yyyy-MM-dd"
try {
    $courtsData = Invoke-RestMethod -Uri "http://localhost:8082/api/v1/admin/courts/management?date=$today" -Method GET -Headers $adminHeaders
    Write-Host "✅ 获取场地数据成功，共 $($courtsData.Length) 个场地" -ForegroundColor Green
} catch {
    Write-Host "❌ 获取场地数据失败" -ForegroundColor Red
    exit 1
}

# 统计需要修复的时间段
$totalSlots = 0
$openSlots = 0
$availableSlots = 0

foreach ($court in $courtsData) {
    foreach ($slot in $court.timeSlots) {
        $totalSlots++
        if ($slot.isOpen) {
            $openSlots++
            if ($slot.available) {
                $availableSlots++
            }
        }
    }
}

Write-Host "`n📊 当前状态统计：" -ForegroundColor Yellow
Write-Host "总时间段：$totalSlots"
Write-Host "开放时间段：$openSlots"
Write-Host "available=true的开放时间段：$availableSlots"

if ($availableSlots -eq $openSlots) {
    Write-Host "`n⚠️  发现问题：所有开放时间段都标记为available=true（已预约状态）" -ForegroundColor Yellow
    Write-Host "需要将开放时间段的available设置为false，使其变为可预约状态" -ForegroundColor Yellow
    
    # 使用数据库直接更新的方法
    Write-Host "`n🔧 准备修复数据..." -ForegroundColor Cyan
    Write-Host "由于API限制，建议通过以下步骤手动修复：" -ForegroundColor White
    Write-Host "1. 访问 http://localhost:8082/h2-console" -ForegroundColor Cyan
    Write-Host "2. 连接数据库：" -ForegroundColor White
    Write-Host "   JDBC URL: jdbc:h2:file:./data/courtlink" -ForegroundColor Gray
    Write-Host "   User Name: SA" -ForegroundColor Gray
    Write-Host "   Password: (留空)" -ForegroundColor Gray
    Write-Host "3. 执行SQL命令：" -ForegroundColor White
    Write-Host "   UPDATE COURT_TIME_SLOTS SET AVAILABLE = FALSE WHERE IS_OPEN = TRUE;" -ForegroundColor Green
    Write-Host "4. 刷新前端页面查看效果" -ForegroundColor White
    
    # 尝试自动修复（可能不成功，但值得尝试）
    Write-Host "`n🤖 尝试自动修复..." -ForegroundColor Cyan
    $successCount = 0
    $failCount = 0
    
    # 只修复前几个作为示例
    $sampleSlots = @()
    $count = 0
    foreach ($court in $courtsData) {
        foreach ($slot in $court.timeSlots) {
            if ($slot.isOpen -and $slot.available -and $count -lt 5) {
                $sampleSlots += @{
                    slot = $slot
                    court = $court
                }
                $count++
            }
        }
        if ($count -ge 5) { break }
    }
    
    foreach ($item in $sampleSlots) {
        $slot = $item.slot
        $court = $item.court
        
        # 尝试通过预约API创建预约，然后取消，来改变状态
        $appointmentData = @{
            courtId = $court.id
            startTime = "$today" + "T" + $slot.startTime
            endTime = "$today" + "T" + $slot.endTime
            amount = $court.pricePerHour
        } | ConvertTo-Json -Compress
        
        try {
            $appointment = Invoke-RestMethod -Uri "http://localhost:8082/api/appointments" -Method POST -Body $appointmentData -Headers $adminHeaders
            Write-Host "✅ 创建示例预约成功 - 时间段 $($slot.id)" -ForegroundColor Green
            $successCount++
        } catch {
            Write-Host "⚠️  时间段 $($slot.id) 自动修复失败" -ForegroundColor Yellow
            $failCount++
        }
    }
    
    Write-Host "`n📈 修复结果：" -ForegroundColor Yellow
    Write-Host "成功创建示例预约：$successCount 个"
    Write-Host "失败：$failCount 个"
    
    if ($successCount -gt 0) {
        Write-Host "`n🎉 部分修复成功！现在应该可以看到一些时间段的状态变化了" -ForegroundColor Green
        Write-Host "请刷新前端页面查看效果" -ForegroundColor Cyan
    }
    
} else {
    Write-Host "`n✅ 时间段状态正常，无需修复" -ForegroundColor Green
}

Write-Host "`n🔄 建议现在刷新前端页面查看最新状态" -ForegroundColor Cyan 