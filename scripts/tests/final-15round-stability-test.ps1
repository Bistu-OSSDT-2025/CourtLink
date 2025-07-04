# CourtLink系统15轮次最终稳定性测试脚本
# 测试所有核心功能的稳定性和可靠性

param(
    [int]$Rounds = 15,
    [string]$BaseUrl = "http://localhost:8080"
)

# 测试结果统计
$Global:TestResults = @{
    StartTime = Get-Date
    TotalRounds = $Rounds
    SuccessfulRounds = 0
    FailedRounds = 0
    TestStats = @{
        HealthCheck = @{ Success = 0; Failed = 0 }
        UserRegistration = @{ Success = 0; Failed = 0 }
        UserQuery = @{ Success = 0; Failed = 0 }
        ExceptionTest = @{ Success = 0; Failed = 0 }
        DataCleanup = @{ Success = 0; Failed = 0 }
    }
    FailureDetails = @()
}

# 颜色输出函数
function Write-TestOutput($Message, $Color = "White") {
    Write-Host $Message -ForegroundColor $Color
}

# 安全的REST请求函数
function Invoke-SafeRestMethod {
    param(
        [string]$Uri,
        [string]$Method = "GET",
        [hashtable]$Body = $null
    )
    
    try {
        $headers = @{"Content-Type" = "application/json"}
        
        if ($Body -and $Method -ne "GET") {
            $response = Invoke-RestMethod -Uri $Uri -Method $Method -Body ($Body | ConvertTo-Json) -Headers $headers
        } else {
            $response = Invoke-RestMethod -Uri $Uri -Method $Method -Headers $headers
        }
        
        return @{ Success = $true; Data = $response; StatusCode = 200 }
    }
    catch {
        $statusCode = 0
        if ($_.Exception.Response) {
            $statusCode = [int]$_.Exception.Response.StatusCode
        }
        
        return @{ 
            Success = $false
            Error = $_.Exception.Message
            StatusCode = $statusCode
        }
    }
}

# 生成唯一测试用户数据
function New-TestUserData($Round) {
    $timestamp = [int][double]::Parse((Get-Date -UFormat %s))
    $random = Get-Random -Minimum 1000 -Maximum 9999
    
    return @{
        username = "final_test_r${Round}_${timestamp}_${random}"
        email = "final_test_r${Round}_${timestamp}_${random}@example.com"
        password = "password123"
        realName = "Final Test User ${Round}"
        phoneNumber = "138${Round.ToString().PadLeft(8, '0')}"
    }
}

Write-TestOutput "`n=== CourtLink 15轮次最终稳定性测试 ===" "Cyan"
Write-TestOutput "开始时间: $(Get-Date)" "Gray"
Write-TestOutput "基础URL: $BaseUrl" "Gray"
Write-TestOutput "测试轮次: $Rounds 轮" "Gray"
Write-TestOutput "============================================" "Cyan"

# 主测试循环
for ($round = 1; $round -le $Rounds; $round++) {
    $roundStart = Get-Date
    $roundSuccess = $true
    $roundErrors = @()
    
    Write-TestOutput "`n--- 第 $round 轮测试 ---" "Yellow"
    
    # 1. 健康检查测试
    Write-Host "  [1/5] 健康检查测试..." -NoNewline
    $healthResult = Invoke-SafeRestMethod -Uri "$BaseUrl/actuator/health"
    
    if ($healthResult.Success -and $healthResult.Data.status -eq "UP") {
        Write-TestOutput " ✓" "Green"
        $Global:TestResults.TestStats.HealthCheck.Success++
    } else {
        Write-TestOutput " ✗ ($($healthResult.Error))" "Red"
        $Global:TestResults.TestStats.HealthCheck.Failed++
        $roundSuccess = $false
        $roundErrors += "健康检查失败"
    }
    
    # 2. 用户注册测试
    Write-Host "  [2/5] 用户注册测试..." -NoNewline
    $userData = New-TestUserData $round
    $registerResult = Invoke-SafeRestMethod -Uri "$BaseUrl/api/users/register" -Method "POST" -Body $userData
    
    $userId = $null
    if ($registerResult.Success -and $registerResult.Data.id) {
        Write-TestOutput " ✓ (ID: $($registerResult.Data.id))" "Green"
        $Global:TestResults.TestStats.UserRegistration.Success++
        $userId = $registerResult.Data.id
    } else {
        Write-TestOutput " ✗ ($($registerResult.Error))" "Red"
        $Global:TestResults.TestStats.UserRegistration.Failed++
        $roundSuccess = $false
        $roundErrors += "用户注册失败"
    }
    
    # 短暂延迟确保数据一致性
    Start-Sleep -Milliseconds 150
    
    # 3. 用户查询测试
    if ($userId) {
        Write-Host "  [3/5] 用户查询测试..." -NoNewline
        $queryResult = Invoke-SafeRestMethod -Uri "$BaseUrl/api/users/$userId"
        
        if ($queryResult.Success -and $queryResult.Data.username -eq $userData.username) {
            Write-TestOutput " ✓" "Green"
            $Global:TestResults.TestStats.UserQuery.Success++
        } else {
            Write-TestOutput " ✗ ($($queryResult.Error))" "Red"
            $Global:TestResults.TestStats.UserQuery.Failed++
            $roundSuccess = $false
            $roundErrors += "用户查询失败"
        }
    } else {
        Write-TestOutput "  [3/5] 用户查询测试... ⏭ 跳过(注册失败)" "Yellow"
        $Global:TestResults.TestStats.UserQuery.Failed++
    }
    
    # 4. 异常处理测试
    Write-Host "  [4/5] 异常处理测试..." -NoNewline
    
    # 测试查询不存在的用户
    $notFoundResult = Invoke-SafeRestMethod -Uri "$BaseUrl/api/users/999999"
    $exceptionTest1 = (!$notFoundResult.Success -and $notFoundResult.StatusCode -eq 404)
    
    # 测试无效注册
    $invalidUser = @{
        username = "ab"  # 太短
        email = "invalid"  # 无效格式
        password = "123"  # 太短
    }
    $invalidResult = Invoke-SafeRestMethod -Uri "$BaseUrl/api/users/register" -Method "POST" -Body $invalidUser
    $exceptionTest2 = (!$invalidResult.Success -and $invalidResult.StatusCode -eq 400)
    
    if ($exceptionTest1 -and $exceptionTest2) {
        Write-TestOutput " ✓ (404+400)" "Green"
        $Global:TestResults.TestStats.ExceptionTest.Success++
    } else {
        Write-TestOutput " ✗ (404:$exceptionTest1, 400:$exceptionTest2)" "Red"
        $Global:TestResults.TestStats.ExceptionTest.Failed++
        $roundSuccess = $false
        $roundErrors += "异常处理测试失败"
    }
    
    # 5. 数据清理
    if ($userId) {
        Write-Host "  [5/5] 数据清理..." -NoNewline
        $deleteResult = Invoke-SafeRestMethod -Uri "$BaseUrl/api/users/$userId" -Method "DELETE"
        
        if ($deleteResult.Success) {
            Write-TestOutput " ✓" "Green"
            $Global:TestResults.TestStats.DataCleanup.Success++
        } else {
            Write-TestOutput " ✗ ($($deleteResult.Error))" "Red"
            $Global:TestResults.TestStats.DataCleanup.Failed++
            $roundSuccess = $false
            $roundErrors += "数据清理失败"
        }
    } else {
        Write-TestOutput "  [5/5] 数据清理... ⏭ 跳过(无数据)" "Yellow"
    }
    
    # 轮次结果
    $roundDuration = (Get-Date) - $roundStart
    if ($roundSuccess) {
        Write-TestOutput "  ✅ 第 $round 轮测试通过 (${($roundDuration.TotalMilliseconds.ToString("F0"))}ms)" "Green"
        $Global:TestResults.SuccessfulRounds++
    } else {
        Write-TestOutput "  ❌ 第 $round 轮测试失败 (${($roundDuration.TotalMilliseconds.ToString("F0"))}ms)" "Red"
        $Global:TestResults.FailedRounds++
        $Global:TestResults.FailureDetails += @{
            Round = $round
            Errors = $roundErrors
            Duration = $roundDuration.TotalMilliseconds
        }
    }
    
    # 轮次间控制
    if ($round -lt $Rounds) {
        if ($round % 5 -eq 0) {
            Write-TestOutput "  🔄 第${round}轮完成，系统资源回收..." "Cyan"
            [System.GC]::Collect()
            Start-Sleep -Milliseconds 1000
        } else {
            Start-Sleep -Milliseconds 400
        }
    }
}

# 生成最终报告
$Global:TestResults.EndTime = Get-Date
$Global:TestResults.TotalDuration = $Global:TestResults.EndTime - $Global:TestResults.StartTime
$successRate = [math]::Round(($Global:TestResults.SuccessfulRounds / $Global:TestResults.TotalRounds) * 100, 2)

Write-TestOutput "`n============================================" "Cyan"
Write-TestOutput "=== 15轮次最终稳定性测试报告 ===" "Cyan"
Write-TestOutput "============================================" "Cyan"

Write-TestOutput "`n📊 总体结果:" "White"
Write-TestOutput "  • 总测试轮次: $($Global:TestResults.TotalRounds)" "Gray"
Write-TestOutput "  • 成功轮次: $($Global:TestResults.SuccessfulRounds)" "Green"
Write-TestOutput "  • 失败轮次: $($Global:TestResults.FailedRounds)" "Red"
Write-TestOutput "  • 成功率: $successRate%" $(if ($successRate -ge 95) { "Green" } elseif ($successRate -ge 85) { "Yellow" } else { "Red" })
Write-TestOutput "  • 总测试时长: $($Global:TestResults.TotalDuration.TotalSeconds.ToString("F1"))秒" "Gray"

Write-TestOutput "`n📈 分项统计:" "White"
foreach ($testName in $Global:TestResults.TestStats.Keys) {
    $stats = $Global:TestResults.TestStats[$testName]
    $total = $stats.Success + $stats.Failed
    $rate = if ($total -gt 0) { [math]::Round(($stats.Success / $total) * 100, 1) } else { 0 }
    $color = if ($rate -eq 100) { "Green" } elseif ($rate -ge 90) { "Yellow" } else { "Red" }
    
    Write-TestOutput "  • $testName`: $($stats.Success)/$total 通过 ($rate%)" $color
}

# 失败详情
if ($Global:TestResults.FailureDetails.Count -gt 0) {
    Write-TestOutput "`n❌ 失败详情:" "Red"
    foreach ($failure in $Global:TestResults.FailureDetails) {
        Write-TestOutput "  第$($failure.Round)轮: $($failure.Errors -join ', ')" "Red"
    }
}

# 系统稳定性评级
Write-TestOutput "`n🎯 系统稳定性评级:" "White"
$grade = ""
$recommendation = ""

if ($successRate -eq 100) {
    $grade = "A+ 级 (完美)"
    $recommendation = "✅ 强烈推荐立即部署到生产环境"
    $color = "Green"
} elseif ($successRate -ge 95) {
    $grade = "A 级 (优秀)"
    $recommendation = "✅ 推荐部署到生产环境"
    $color = "Green"
} elseif ($successRate -ge 90) {
    $grade = "A- 级 (良好)"
    $recommendation = "✅ 可以部署到生产环境，建议监控"
    $color = "Yellow"
} elseif ($successRate -ge 80) {
    $grade = "B 级 (尚可)"
    $recommendation = "⚠️ 建议修复问题后部署"
    $color = "Yellow"
} else {
    $grade = "C 级 (需改进)"
    $recommendation = "❌ 不建议部署，需要重大修复"
    $color = "Red"
}

Write-TestOutput "  等级: $grade" $color
Write-TestOutput "  建议: $recommendation" $color

# 保存结果到JSON
$testSummary = @{
    TestInfo = @{
        TestDate = $Global:TestResults.StartTime.ToString("yyyy-MM-dd HH:mm:ss")
        TestDuration = $Global:TestResults.TotalDuration.TotalSeconds
        TotalRounds = $Global:TestResults.TotalRounds
    }
    Results = @{
        SuccessfulRounds = $Global:TestResults.SuccessfulRounds
        FailedRounds = $Global:TestResults.FailedRounds
        SuccessRate = $successRate
        StabilityGrade = $grade
    }
    DetailedStats = $Global:TestResults.TestStats
    FailureDetails = $Global:TestResults.FailureDetails
}

$resultFile = "final-15round-test-results.json"
$testSummary | ConvertTo-Json -Depth 4 | Out-File -FilePath $resultFile -Encoding UTF8

Write-TestOutput "`n💾 详细结果已保存到: $resultFile" "Cyan"
Write-TestOutput "`n=== 15轮次最终稳定性测试完成 ===" "Cyan" 