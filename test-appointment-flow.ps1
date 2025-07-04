# 测试预定流程脚本
# 测试预定成功后的支付流程和10分钟超时机制

Write-Host "=== 测试预定流程 ===" -ForegroundColor Green

# 服务器地址
$baseUrl = "http://localhost:8080/api/v1"

# 测试用户登录
Write-Host "1. 测试用户登录..." -ForegroundColor Yellow
$loginData = @{
    username = "testuser"
    password = "test123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $loginData -ContentType "application/json"
    $token = $loginResponse.token
    Write-Host "登录成功，Token: $($token.Substring(0, 20))..." -ForegroundColor Green
} catch {
    Write-Host "登录失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 获取可用场地
Write-Host "2. 获取可用场地..." -ForegroundColor Yellow
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

try {
    $today = Get-Date -Format "yyyy-MM-dd"
    $courtsResponse = Invoke-RestMethod -Uri "$baseUrl/courts/booking?date=$today" -Method Get -Headers $headers
    Write-Host "获取到 $($courtsResponse.Length) 个场地" -ForegroundColor Green
} catch {
    Write-Host "获取场地失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 创建预定
Write-Host "3. 创建预定..." -ForegroundColor Yellow
$appointmentData = @{
    courtId = 1
    timeSlotId = 1
    appointmentDate = $today
    note = "测试预定"
} | ConvertTo-Json

try {
    $appointmentResponse = Invoke-RestMethod -Uri "$baseUrl/appointments" -Method Post -Body $appointmentData -Headers $headers
    Write-Host "预定创建成功:" -ForegroundColor Green
    Write-Host "- 预定ID: $($appointmentResponse.appointment.id)"
    Write-Host "- 支付ID: $($appointmentResponse.payment.paymentId)"
    Write-Host "- 金额: ￥$($appointmentResponse.payment.amount)"
    Write-Host "- 过期时间: $($appointmentResponse.payment.expireTime)"
    
    $paymentId = $appointmentResponse.payment.paymentId
    $appointmentId = $appointmentResponse.appointment.id
} catch {
    Write-Host "创建预定失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 测试支付状态查询
Write-Host "4. 查询支付状态..." -ForegroundColor Yellow
try {
    $paymentStatusResponse = Invoke-RestMethod -Uri "$baseUrl/payments/$paymentId/status" -Method Get -Headers $headers
    Write-Host "支付状态: $($paymentStatusResponse.status)" -ForegroundColor Green
} catch {
    Write-Host "查询支付状态失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 等待一分钟，测试定时任务
Write-Host "5. 等待定时任务检查..." -ForegroundColor Yellow
Write-Host "等待1分钟以测试定时任务..." -ForegroundColor Cyan
Start-Sleep -Seconds 60

# 再次查询支付状态
Write-Host "6. 再次查询支付状态..." -ForegroundColor Yellow
try {
    $paymentStatusResponse = Invoke-RestMethod -Uri "$baseUrl/payments/$paymentId/status" -Method Get -Headers $headers
    Write-Host "支付状态: $($paymentStatusResponse.status)" -ForegroundColor Green
} catch {
    Write-Host "查询支付状态失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 测试取消支付（如果还未超时）
Write-Host "7. 测试取消支付..." -ForegroundColor Yellow
try {
    $cancelResponse = Invoke-RestMethod -Uri "$baseUrl/payments/$paymentId/cancel" -Method Post -Headers $headers
    Write-Host "取消支付成功" -ForegroundColor Green
} catch {
    Write-Host "取消支付失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "=== 测试完成 ===" -ForegroundColor Green
Write-Host "请检查日志文件以确认定时任务正常运行" -ForegroundColor Cyan 