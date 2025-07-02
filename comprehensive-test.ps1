# 羽毛球场地预约系统综合测试脚本
# 作者：CourtLink Team
# 版本：1.0.0
# 日期：2025-07-02

# 设置错误操作首选项
$ErrorActionPreference = "Stop"

# 定义颜色函数
function Write-ColorOutput($ForegroundColor) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    if ($args) {
        Write-Output $args
    }
    $host.UI.RawUI.ForegroundColor = $fc
}

function Run-Tests {
    param (
        [string]$TestType,
        [string]$TestCommand
    )
    Write-ColorOutput Green "开始执行 $TestType..."
    try {
        Invoke-Expression $TestCommand
        Write-ColorOutput Green "$TestType 执行完成"
        return $true
    }
    catch {
        Write-ColorOutput Red "$TestType 执行失败: $_"
        return $false
    }
}

# 1. 环境准备
Write-ColorOutput Cyan "=== 1. 环境准备 ==="
Write-ColorOutput Yellow "正在清理测试环境..."
./mvnw clean

# 2. 执行集成测试
Write-ColorOutput Cyan "=== 2. 执行集成测试 ==="

# 2.1 API集成测试
$integrationTestResult = Run-Tests "API集成测试" "./mvnw test -Dtest=*IntegrationTest"
if (-not $integrationTestResult) {
    Write-ColorOutput Red "API集成测试失败，终止测试流程"
    exit 1
}

# 2.2 控制器测试
$controllerTestResult = Run-Tests "控制器测试" "./mvnw test -Dtest=*ControllerTest"
if (-not $controllerTestResult) {
    Write-ColorOutput Red "控制器测试失败，终止测试流程"
    exit 1
}

# 3. 执行端到端测试
Write-ColorOutput Cyan "=== 3. 执行端到端测试 ==="

# 3.1 启动应用程序
Write-ColorOutput Yellow "正在启动应用程序..."
Start-Process powershell -ArgumentList ".\mvnw spring-boot:run" -NoNewWindow

# 等待应用程序启动
Write-ColorOutput Yellow "等待应用程序启动..."
Start-Sleep -Seconds 30

# 3.2 执行端到端测试场景

# 场景1：用户注册和登录
Write-ColorOutput Yellow "测试场景1：用户注册和登录"
$registrationTest = Run-Tests "用户注册测试" "curl -X POST http://localhost:8090/api/users/register -H 'Content-Type: application/json' -d '{`"username`":`"testuser`",`"password`":`"password123`",`"email`":`"test@example.com`"}'"
if (-not $registrationTest) {
    Write-ColorOutput Red "用户注册测试失败"
}

# 场景2：场地预约流程
Write-ColorOutput Yellow "测试场景2：场地预约流程"
$bookingTest = Run-Tests "场地预约测试" "curl -X POST http://localhost:8090/api/appointments -H 'Content-Type: application/json' -d '{`"userId`":`"testuser`",`"courtId`":`"1`",`"startTime`":`"2025-07-03T10:00:00`",`"endTime`":`"2025-07-03T11:00:00`"}'"
if (-not $bookingTest) {
    Write-ColorOutput Red "场地预约测试失败"
}

# 场景3：支付流程
Write-ColorOutput Yellow "测试场景3：支付流程"
$paymentTest = Run-Tests "支付流程测试" "curl -X POST http://localhost:8090/api/payments/pay -H 'Content-Type: application/json' -d '{`"appointmentId`":`"1`",`"amount`":`"100.00`",`"paymentMethod`":`"CREDIT_CARD`"}'"
if (-not $paymentTest) {
    Write-ColorOutput Red "支付流程测试失败"
}

# 4. 性能测试
Write-ColorOutput Cyan "=== 4. 执行性能测试 ==="

# 4.1 并发预约测试
Write-ColorOutput Yellow "执行并发预约测试..."
for ($i = 1; $i -le 10; $i++) {
    Start-Job -ScriptBlock {
        curl -X POST http://localhost:8090/api/appointments -H 'Content-Type: application/json' -d '{
            "userId": "testuser",
            "courtId": "1",
            "startTime": "2025-07-03T10:00:00",
            "endTime": "2025-07-03T11:00:00"
        }'
    }
}

Get-Job | Wait-Job
Get-Job | Receive-Job
Remove-Job *

# 5. 清理测试数据
Write-ColorOutput Cyan "=== 5. 清理测试数据 ==="
Write-ColorOutput Yellow "正在清理测试数据..."

# 停止应用程序
Write-ColorOutput Yellow "正在停止应用程序..."
Stop-Process -Name "java" -ErrorAction SilentlyContinue

# 6. 生成测试报告
Write-ColorOutput Cyan "=== 6. 生成测试报告 ==="
./mvnw surefire-report:report

# 完成测试
Write-ColorOutput Green "=== 测试完成 ==="
Write-ColorOutput Yellow "请查看测试报告了解详细结果" 