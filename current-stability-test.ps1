# CourtLink 当前系统稳定性测试
param(
    [int]$Rounds = 10,
    [string]$BaseUrl = "http://localhost:8080"
)

$TestResults = @{
    StartTime = Get-Date
    TotalTests = 0
    PassedTests = 0
    FailedTests = 0
    Details = @()
}

function Write-TestResult($TestName, $Success, $Message = "") {
    $TestResults.TotalTests++
    $result = if ($Success) { "PASS"; $TestResults.PassedTests++ } else { "FAIL"; $TestResults.FailedTests++ }
    $color = if ($Success) { "Green" } else { "Red" }
    
    Write-Host "[$result] $TestName" -ForegroundColor $color
    if ($Message) { Write-Host "    $Message" -ForegroundColor Gray }
    
    $TestResults.Details += @{
        Test = $TestName
        Result = $result
        Message = $Message
        Timestamp = Get-Date
    }
}

function Test-Endpoint {
    param($Uri, $Method = "GET", $Body = $null, $ExpectedStatus = 200)
    
    try {
        $headers = @{"Content-Type" = "application/json"}
        $params = @{
            Uri = $Uri
            Method = $Method
            Headers = $headers
            TimeoutSec = 10
        }
        
        if ($Body -and $Method -ne "GET") {
            $params.Body = ($Body | ConvertTo-Json)
        }
        
        $response = Invoke-RestMethod @params
        return @{ Success = $true; Data = $response; StatusCode = 200 }
    }
    catch {
        $statusCode = if ($_.Exception.Response) { [int]$_.Exception.Response.StatusCode } else { 0 }
        return @{ Success = $false; Error = $_.Exception.Message; StatusCode = $statusCode }
    }
}

Write-Host "`n=== CourtLink 系统稳定性测试 ===" -ForegroundColor Cyan
Write-Host "测试开始时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Gray
Write-Host "测试轮次: $Rounds" -ForegroundColor Gray
Write-Host "=" * 50 -ForegroundColor Cyan

# 等待应用启动
Write-Host "`n等待应用启动..." -ForegroundColor Yellow
$maxWait = 60
$waited = 0
$appReady = $false

while ($waited -lt $maxWait -and -not $appReady) {
    try {
        $health = Invoke-RestMethod "$BaseUrl/actuator/health" -TimeoutSec 5
        if ($health.status -eq "UP") {
            $appReady = $true
            Write-Host "应用已就绪!" -ForegroundColor Green
        }
    }
    catch {
        Start-Sleep -Seconds 2
        $waited += 2
        Write-Host "." -NoNewline -ForegroundColor Gray
    }
}

if (-not $appReady) {
    Write-Host "`n应用启动超时，无法进行测试" -ForegroundColor Red
    exit 1
}

# 基础功能测试
Write-Host "`n=== 基础功能测试 ===" -ForegroundColor Yellow

# 1. 健康检查
$health = Test-Endpoint "$BaseUrl/actuator/health"
Write-TestResult "健康检查" ($health.Success -and $health.Data.status -eq "UP") $health.Data.status

# 2. Swagger UI
$swagger = Test-Endpoint "$BaseUrl/swagger-ui/index.html"
Write-TestResult "Swagger UI访问" $swagger.Success

# 3. 用户管理端点测试
Write-Host "`n=== 用户管理功能测试 ===" -ForegroundColor Yellow

# 用户列表
$userList = Test-Endpoint "$BaseUrl/api/users"
Write-TestResult "用户列表查询" $userList.Success

# 多轮次CRUD测试
Write-Host "`n=== $Rounds 轮次CRUD稳定性测试 ===" -ForegroundColor Yellow

$successfulRounds = 0
for ($i = 1; $i -le $Rounds; $i++) {
    Write-Host "`n--- 第 $i 轮测试 ---" -ForegroundColor Cyan
    $roundSuccess = $true
    
    # 生成测试用户
    $testUser = @{
        username = "test_$(Get-Random -Min 1000 -Max 9999)"
        email = "test$(Get-Random)@example.com"
        password = "password123"
        realName = "测试用户$i"
        phoneNumber = "13800000001"
    }
    
    # 用户注册
    $register = Test-Endpoint "$BaseUrl/api/users/register" "POST" $testUser
    if ($register.Success -and $register.Data.id) {
        Write-TestResult "  用户注册" $true "用户ID: $($register.Data.id)"
        $userId = $register.Data.id
        
        # 用户查询
        Start-Sleep -Milliseconds 100
        $query = Test-Endpoint "$BaseUrl/api/users/$userId"
        if ($query.Success -and $query.Data.username -eq $testUser.username) {
            Write-TestResult "  用户查询" $true "用户名匹配"
        } else {
            Write-TestResult "  用户查询" $false "数据不匹配或查询失败"
            $roundSuccess = $false
        }
        
        # 数据清理
        $delete = Test-Endpoint "$BaseUrl/api/users/$userId" "DELETE"
        Write-TestResult "  数据清理" $delete.Success
        
    } else {
        Write-TestResult "  用户注册" $false $register.Error
        $roundSuccess = $false
    }
    
    if ($roundSuccess) {
        $successfulRounds++
        Write-Host "  轮次 $i: 成功" -ForegroundColor Green
    } else {
        Write-Host "  轮次 $i: 失败" -ForegroundColor Red
    }
    
    # 轮次间延迟
    if ($i -lt $Rounds) {
        Start-Sleep -Milliseconds 200
    }
}

# 异常处理测试
Write-Host "`n=== 异常处理测试 ===" -ForegroundColor Yellow

# 404测试
$notFound = Test-Endpoint "$BaseUrl/api/users/999999"
Write-TestResult "404错误处理" (!$notFound.Success -and $notFound.StatusCode -eq 404)

# 无效数据测试
$invalidUser = @{ username = "ab"; email = "invalid"; password = "123" }
$badRequest = Test-Endpoint "$BaseUrl/api/users/register" "POST" $invalidUser
Write-TestResult "400错误处理" (!$badRequest.Success -and $badRequest.StatusCode -eq 400)

# 生成最终报告
$TestResults.EndTime = Get-Date
$TestResults.Duration = $TestResults.EndTime - $TestResults.StartTime
$successRate = if ($TestResults.TotalTests -gt 0) { 
    [math]::Round(($TestResults.PassedTests / $TestResults.TotalTests) * 100, 1) 
} else { 0 }
$roundSuccessRate = [math]::Round(($successfulRounds / $Rounds) * 100, 1)

Write-Host "`n" + "=" * 50 -ForegroundColor Cyan
Write-Host "=== 测试结果汇总 ===" -ForegroundColor Cyan
Write-Host "=" * 50 -ForegroundColor Cyan

Write-Host "`n📊 总体统计" -ForegroundColor Yellow
Write-Host "  总测试数: $($TestResults.TotalTests)" -ForegroundColor White
Write-Host "  通过测试: $($TestResults.PassedTests)" -ForegroundColor Green
Write-Host "  失败测试: $($TestResults.FailedTests)" -ForegroundColor Red
$successRateColor = if($successRate -ge 95) {"Green"} elseif($successRate -ge 80) {"Yellow"} else {"Red"}
Write-Host "  成功率: $successRate%" -ForegroundColor $successRateColor

Write-Host "`n🔄 CRUD轮次测试" -ForegroundColor Yellow
Write-Host "  成功轮次: $successfulRounds/$Rounds" -ForegroundColor White
$roundSuccessRateColor = if($roundSuccessRate -eq 100) {"Green"} elseif($roundSuccessRate -ge 80) {"Yellow"} else {"Red"}
Write-Host "  轮次成功率: $roundSuccessRate%" -ForegroundColor $roundSuccessRateColor

Write-Host "`n⏱️  测试耗时: $([int]$TestResults.Duration.TotalSeconds)秒" -ForegroundColor White

# 系统评级
$grade = "F"
$gradeColor = "Red"
$recommendation = "不建议部署"

if ($successRate -ge 95 -and $roundSuccessRate -ge 90) {
    $grade = "A+"
    $gradeColor = "Green"
    $recommendation = "强烈推荐立即部署到生产环境"
} elseif ($successRate -ge 90 -and $roundSuccessRate -ge 80) {
    $grade = "A"
    $gradeColor = "Green"
    $recommendation = "推荐部署到生产环境"
} elseif ($successRate -ge 85 -and $roundSuccessRate -ge 70) {
    $grade = "B+"
    $gradeColor = "Yellow"
    $recommendation = "建议修复问题后部署"
} elseif ($successRate -ge 75) {
    $grade = "B"
    $gradeColor = "Yellow"
    $recommendation = "需要进一步测试和优化"
} elseif ($successRate -ge 60) {
    $grade = "C"
    $gradeColor = "Red"
    $recommendation = "存在较多问题，需要修复"
} else {
    $grade = "D"
    $gradeColor = "Red"
    $recommendation = "系统不稳定，禁止部署"
}

Write-Host "`n🏆 系统稳定性评级: $grade" -ForegroundColor $gradeColor
Write-Host "💡 部署建议: $recommendation" -ForegroundColor $gradeColor

Write-Host "`n测试完成时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Gray

# 保存详细结果到JSON
$jsonResult = @{
    TestSummary = @{
        StartTime = $TestResults.StartTime.ToString("yyyy-MM-dd HH:mm:ss")
        EndTime = $TestResults.EndTime.ToString("yyyy-MM-dd HH:mm:ss")
        Duration = $TestResults.Duration.TotalSeconds
        TotalTests = $TestResults.TotalTests
        PassedTests = $TestResults.PassedTests
        FailedTests = $TestResults.FailedTests
        SuccessRate = $successRate
        RoundSuccessRate = $roundSuccessRate
        SystemGrade = $grade
        Recommendation = $recommendation
    }
    TestDetails = $TestResults.Details
} | ConvertTo-Json -Depth 3

$jsonResult | Out-File "current-stability-test-results.json" -Encoding UTF8
Write-Host "`n📄 详细结果已保存到: current-stability-test-results.json" -ForegroundColor Gray 