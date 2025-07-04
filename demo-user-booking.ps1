# 🏸 羽毛球场地预约系统 - 用户预约功能演示
$baseUrl = "http://localhost:8082"

Write-Host "🏸 ============================================== 🏸" -ForegroundColor Cyan
Write-Host "      羽毛球场地预约系统 - 用户预约功能演示" -ForegroundColor Green
Write-Host "🏸 ============================================== 🏸" -ForegroundColor Cyan

# 显示系统功能说明
Write-Host "`n✨ 系统功能特点：" -ForegroundColor Yellow
Write-Host "📍 运动类型选择（羽毛球、网球、篮球、乒乓球）" -ForegroundColor White
Write-Host "📍 日期选择器（只能预约今天及以后的日期）" -ForegroundColor White
Write-Host "📍 实时场地状态显示（可预约/已预约/不可用）" -ForegroundColor White
Write-Host "📍 多时段选择预约" -ForegroundColor White
Write-Host "📍 预约确认和总价计算" -ForegroundColor White
Write-Host "📍 数据同步管理员场地管理系统" -ForegroundColor White

Write-Host "`n🌐 服务状态检查：" -ForegroundColor Yellow

# 检查后端服务
try {
    $healthCheck = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -Method GET -TimeoutSec 5
    Write-Host "✅ 后端服务：运行正常 (localhost:8082)" -ForegroundColor Green
} catch {
    Write-Host "❌ 后端服务：未启动或异常" -ForegroundColor Red
    Write-Host "   请确保后端 Spring Boot 应用正在运行" -ForegroundColor Yellow
    return
}

# 检查前端服务
try {
    $frontendCheck = Invoke-WebRequest -Uri "http://localhost:3001" -Method GET -TimeoutSec 5
    Write-Host "✅ 前端服务：运行正常 (localhost:3001)" -ForegroundColor Green
} catch {
    Write-Host "⚠️  前端服务：未检测到 (localhost:3001)" -ForegroundColor Yellow
    Write-Host "   请运行: cd frontend && npm run dev" -ForegroundColor Cyan
}

Write-Host "`n👤 用户认证测试：" -ForegroundColor Yellow

# 注册新用户（如果需要）
$testUser = "demo-user-$(Get-Random -Maximum 1000)"
$registerData = @{
    username = $testUser
    password = "password123"
    email = "$testUser@example.com"
    fullName = "演示用户"
} | ConvertTo-Json -Compress

$headers = @{
    "Content-Type" = "application/json"
}

try {
    $registerResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method POST -Body $registerData -Headers $headers
    Write-Host "✅ 用户注册成功：$testUser" -ForegroundColor Green
} catch {
    Write-Host "⚠️  用户可能已存在，尝试直接登录" -ForegroundColor Yellow
}

# 用户登录
$loginData = @{
    username = $testUser
    password = "password123"
} | ConvertTo-Json -Compress

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -Body $loginData -Headers $headers
    Write-Host "✅ 用户登录成功" -ForegroundColor Green
    
    $authHeaders = @{
        "Content-Type" = "application/json"
        "Authorization" = "Bearer $($loginResponse.token)"
    }
} catch {
    Write-Host "❌ 用户登录失败" -ForegroundColor Red
    return
}

Write-Host "`n📅 场地预约数据获取：" -ForegroundColor Yellow
$today = Get-Date -Format "yyyy-MM-dd"

try {
    # 使用管理员API获取场地数据（只读）
    $adminLoginData = @{
        usernameOrEmail = "admin"
        password = "admin123"
    } | ConvertTo-Json -Compress
    
    $adminResponse = Invoke-RestMethod -Uri "$baseUrl/api/v1/admin/login" -Method POST -Body $adminLoginData -Headers $headers
    $adminHeaders = @{
        "Content-Type" = "application/json"
        "Authorization" = "Bearer $($adminResponse.token)"
    }
    
    $courtsData = Invoke-RestMethod -Uri "$baseUrl/api/v1/admin/courts/management?date=$today" -Method GET -Headers $adminHeaders
    Write-Host "✅ 成功获取场地预约信息" -ForegroundColor Green
    Write-Host "📊 场地数量：$($courtsData.Length)" -ForegroundColor Cyan
    
    foreach ($court in $courtsData) {
        $availableSlots = $court.timeSlots | Where-Object { $_.isOpen -eq $true -and $_.available -eq $false }
        $occupiedSlots = $court.timeSlots | Where-Object { $_.available -eq $true }
        $closedSlots = $court.timeSlots | Where-Object { $_.isOpen -eq $false }
        
        Write-Host "  🏸 $($court.name) - ¥$($court.pricePerHour)/时" -ForegroundColor White
        Write-Host "     🟢 可预约：$($availableSlots.Length)个时段  🟡 已预约：$($occupiedSlots.Length)个时段  ⚫ 关闭：$($closedSlots.Length)个时段" -ForegroundColor Gray
    }
    
    # 演示预约功能
    Write-Host "`n💡 预约功能演示：" -ForegroundColor Yellow
    
    if ($courtsData.Length -gt 0) {
        $firstCourt = $courtsData[0]
        $availableSlots = $firstCourt.timeSlots | Where-Object { $_.isOpen -eq $true -and $_.available -eq $false }
        
        if ($availableSlots.Length -gt 0) {
            $selectedSlot = $availableSlots[0]
            
            Write-Host "📝 尝试预约：$($firstCourt.name)" -ForegroundColor Cyan
            Write-Host "⏰ 时间段：$($selectedSlot.startTime) - $($selectedSlot.endTime)" -ForegroundColor Cyan
            Write-Host "💰 价格：¥$($firstCourt.pricePerHour)" -ForegroundColor Cyan
            
            # 创建预约
            $appointmentData = @{
                courtId = $firstCourt.id
                startTime = "$today" + "T" + $selectedSlot.startTime
                endTime = "$today" + "T" + $selectedSlot.endTime
                amount = $firstCourt.pricePerHour
            } | ConvertTo-Json -Compress
            
            try {
                $appointmentResponse = Invoke-RestMethod -Uri "$baseUrl/api/appointments" -Method POST -Body $appointmentData -Headers $authHeaders
                Write-Host "✅ 预约创建成功！" -ForegroundColor Green
                Write-Host "📋 预约详情：$($appointmentResponse | ConvertTo-Json)" -ForegroundColor Gray
            } catch {
                Write-Host "❌ 预约创建失败：$($_.Exception.Message)" -ForegroundColor Red
                Write-Host "   可能原因：时间段已被预约或服务器错误" -ForegroundColor Yellow
            }
        } else {
            Write-Host "⚠️  当前没有可用的时间段进行演示" -ForegroundColor Yellow
        }
    }
    
} catch {
    Write-Host "❌ 获取场地数据失败：$($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n🌟 前端访问说明：" -ForegroundColor Yellow
Write-Host "1. 用户登录页面：http://localhost:3001/login" -ForegroundColor Cyan
Write-Host "2. 用户预约页面：http://localhost:3001/booking" -ForegroundColor Cyan
Write-Host "3. 管理员登录页面：http://localhost:3001/admin/login" -ForegroundColor Cyan
Write-Host "4. 管理员场地管理：http://localhost:3001/admin/courts" -ForegroundColor Cyan

Write-Host "`n👥 测试账号：" -ForegroundColor Yellow
Write-Host "用户账号：$testUser / password123" -ForegroundColor White
Write-Host "管理员账号：admin / admin123" -ForegroundColor White

Write-Host "`n🏸 ============================================== 🏸" -ForegroundColor Cyan
Write-Host "         羽毛球场地预约系统 - 演示完成" -ForegroundColor Green
Write-Host "🏸 ============================================== 🏸" -ForegroundColor Cyan

Write-Host "`n📋 系统架构总结：" -ForegroundColor Yellow
Write-Host "• 后端：Spring Boot + H2数据库 + JWT认证" -ForegroundColor White
Write-Host "• 前端：Vue.js + 原生CSS + Vite" -ForegroundColor White
Write-Host "• 特色：运动类型选择、实时状态同步、多时段预约" -ForegroundColor White
Write-Host "• 数据：与管理员系统同步，确保数据一致性" -ForegroundColor White
