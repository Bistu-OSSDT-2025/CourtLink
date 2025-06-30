#!/usr/bin/env pwsh
# =================================================================
# 10轮次稳定性功能测试脚本
# 测试 CourtLink 用户管理系统的稳定性
# =================================================================

# 测试配置
$BASE_URL = "http://localhost:8080"
$ROUNDS = 10
$DELAY_BETWEEN_ROUNDS = 2  # 轮次间延迟（秒）
$DELAY_BETWEEN_TESTS = 0.5  # 测试间延迟（秒）

# 测试结果统计
$global:testResults = @{
    totalTests = 0
    passedTests = 0
    failedTests = 0
    roundResults = @()
    errors = @()
}

# 测试数据
$testUsers = @(
    @{
        username = "testuser1"
        email = "testuser1@test.com"
        password = "password123"
        name = "测试用户1"
        phone = "13800138001"
    },
    @{
        username = "testuser2"
        email = "testuser2@test.com"
        password = "password456"
        name = "测试用户2"
        phone = "13800138002"
    }
)

# 颜色输出函数
function Write-ColorOutput($text, $color) {
    $originalColor = $Host.UI.RawUI.ForegroundColor
    $Host.UI.RawUI.ForegroundColor = $color
    Write-Output $text
    $Host.UI.RawUI.ForegroundColor = $originalColor
}

# HTTP请求函数
function Invoke-ApiRequest {
    param(
        [string]$Method = "GET",
        [string]$Url,
        [hashtable]$Body = $null,
        [hashtable]$Headers = @{"Content-Type" = "application/json"}
    )
    
    try {
        $params = @{
            Method = $Method
            Uri = $Url
            Headers = $Headers
            UseBasicParsing = $true
            TimeoutSec = 30
        }
        
        if ($Body) {
            $params.Body = ($Body | ConvertTo-Json -Depth 10)
        }
        
        $response = Invoke-WebRequest @params
        return @{
            Success = $true
            StatusCode = $response.StatusCode
            Content = $response.Content | ConvertFrom-Json -ErrorAction SilentlyContinue
            Headers = $response.Headers
            ResponseTime = $null
        }
    }
    catch {
        return @{
            Success = $false
            Error = $_.Exception.Message
            StatusCode = if ($_.Exception.Response) { $_.Exception.Response.StatusCode } else { "N/A" }
            ResponseTime = $null
        }
    }
}

# 单个测试函数
function Test-Endpoint {
    param(
        [string]$TestName,
        [string]$Method = "GET",
        [string]$Endpoint,
        [hashtable]$Body = $null,
        [int]$ExpectedStatusCode = 200,
        [scriptblock]$ValidationScript = $null
    )
    
    $url = "$BASE_URL$Endpoint"
    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    
    $result = Invoke-ApiRequest -Method $Method -Url $url -Body $Body
    $stopwatch.Stop()
    $responseTime = $stopwatch.ElapsedMilliseconds
    
    $global:testResults.totalTests++
    
    $isSuccess = $result.Success -and ($result.StatusCode -eq $ExpectedStatusCode)
    
    # 执行自定义验证
    if ($isSuccess -and $ValidationScript) {
        try {
            $validationResult = & $ValidationScript $result.Content
            $isSuccess = $isSuccess -and $validationResult
        }
        catch {
            $isSuccess = $false
            $result.Error = "验证失败: $($_.Exception.Message)"
        }
    }
    
    if ($isSuccess) {
        $global:testResults.passedTests++
        Write-ColorOutput "  ✓ $TestName - ${responseTime}ms" "Green"
    }
    else {
        $global:testResults.failedTests++
        $errorMsg = if ($result.Error) { $result.Error } else { "状态码: $($result.StatusCode)" }
        Write-ColorOutput "  ✗ $TestName - 失败: $errorMsg" "Red"
        $global:testResults.errors += "$TestName - $errorMsg"
    }
    
    return @{
        TestName = $TestName
        Success = $isSuccess
        ResponseTime = $responseTime
        StatusCode = $result.StatusCode
        Error = $result.Error
    }
}

# 清理函数
function Clear-TestData {
    Write-ColorOutput "🧹 清理测试数据..." "Yellow"
    
    # 获取所有用户并删除测试用户
    $users = Invoke-ApiRequest -Url "$BASE_URL/api/users/list"
    if ($users.Success -and $users.Content) {
        foreach ($user in $users.Content) {
            if ($user.username -like "testuser*") {
                $deleteResult = Invoke-ApiRequest -Method "DELETE" -Url "$BASE_URL/api/users/$($user.id)"
                if ($deleteResult.Success) {
                    Write-ColorOutput "  删除测试用户: $($user.username)" "Gray"
                }
            }
        }
    }
    
    Start-Sleep -Seconds 1
}

# 执行单轮测试
function Run-SingleRound {
    param([int]$RoundNumber)
    
    Write-ColorOutput "🔄 第 $RoundNumber 轮测试开始" "Cyan"
    
    $roundTests = @()
    
    # 1. 健康检查测试
    $roundTests += Test-Endpoint -TestName "健康检查-状态" -Endpoint "/api/health/status" -ValidationScript {
        param($content)
        return $content.status -eq "UP"
    }
    
    $roundTests += Test-Endpoint -TestName "健康检查-Ping" -Endpoint "/api/health/ping" -ValidationScript {
        param($content)
        return $content.status -eq "OK"
    }
    
    $roundTests += Test-Endpoint -TestName "健康检查-信息" -Endpoint "/api/health/info" -ValidationScript {
        param($content)
        return $content.application -eq "CourtLink"
    }
    
    Start-Sleep -Seconds $DELAY_BETWEEN_TESTS
    
    # 2. 用户注册测试
    $testUser1 = $testUsers[0]
    $roundTests += Test-Endpoint -TestName "用户注册-用户1" -Method "POST" -Endpoint "/api/users/register" -Body $testUser1 -ExpectedStatusCode 200 -ValidationScript {
        param($content)
        return $content.username -eq $testUser1.username
    }
    
    $testUser2 = $testUsers[1]
    $roundTests += Test-Endpoint -TestName "用户注册-用户2" -Method "POST" -Endpoint "/api/users/register" -Body $testUser2 -ExpectedStatusCode 200 -ValidationScript {
        param($content)
        return $content.username -eq $testUser2.username
    }
    
    Start-Sleep -Seconds $DELAY_BETWEEN_TESTS
    
    # 3. 用户登录测试
    $loginRequest1 = @{
        username = $testUser1.username
        password = $testUser1.password
    }
    $roundTests += Test-Endpoint -TestName "用户登录-用户1" -Method "POST" -Endpoint "/api/users/login" -Body $loginRequest1 -ValidationScript {
        param($content)
        return $content.username -eq $testUser1.username -and $content.token
    }
    
    $loginRequest2 = @{
        username = $testUser2.username
        password = $testUser2.password
    }
    $roundTests += Test-Endpoint -TestName "用户登录-用户2" -Method "POST" -Endpoint "/api/users/login" -Body $loginRequest2 -ValidationScript {
        param($content)
        return $content.username -eq $testUser2.username -and $content.token
    }
    
    Start-Sleep -Seconds $DELAY_BETWEEN_TESTS
    
    # 4. 获取用户列表
    $roundTests += Test-Endpoint -TestName "获取用户列表" -Endpoint "/api/users/list" -ValidationScript {
        param($content)
        return $content.Count -ge 2
    }
    
    # 5. 用户名存在性检查
    $roundTests += Test-Endpoint -TestName "检查用户名存在-存在" -Endpoint "/api/users/check-username?username=$($testUser1.username)" -ValidationScript {
        param($content)
        return $content.exists -eq $true
    }
    
    $roundTests += Test-Endpoint -TestName "检查用户名存在-不存在" -Endpoint "/api/users/check-username?username=nonexistentuser" -ValidationScript {
        param($content)
        return $content.exists -eq $false
    }
    
    # 6. 邮箱存在性检查
    $roundTests += Test-Endpoint -TestName "检查邮箱存在-存在" -Endpoint "/api/users/check-email?email=$($testUser1.email)" -ValidationScript {
        param($content)
        return $content.exists -eq $true
    }
    
    $roundTests += Test-Endpoint -TestName "检查邮箱存在-不存在" -Endpoint "/api/users/check-email?email=nonexistent@test.com" -ValidationScript {
        param($content)
        return $content.exists -eq $false
    }
    
    Start-Sleep -Seconds $DELAY_BETWEEN_TESTS
    
    # 7. 根据用户名获取用户信息
    $roundTests += Test-Endpoint -TestName "根据用户名获取用户" -Endpoint "/api/users/username/$($testUser1.username)" -ValidationScript {
        param($content)
        return $content.username -eq $testUser1.username
    }
    
    # 8. 根据邮箱获取用户信息
    $roundTests += Test-Endpoint -TestName "根据邮箱获取用户" -Endpoint "/api/users/email/$($testUser1.email)" -ValidationScript {
        param($content)
        return $content.email -eq $testUser1.email
    }
    
    # 计算本轮统计
    $passedInRound = ($roundTests | Where-Object { $_.Success }).Count
    $totalInRound = $roundTests.Count
    $avgResponseTime = ($roundTests | Measure-Object -Property ResponseTime -Average).Average
    
    $roundResult = @{
        Round = $RoundNumber
        TotalTests = $totalInRound
        PassedTests = $passedInRound
        FailedTests = $totalInRound - $passedInRound
        SuccessRate = [math]::Round(($passedInRound / $totalInRound) * 100, 2)
        AvgResponseTime = [math]::Round($avgResponseTime, 2)
        Tests = $roundTests
    }
    
    $global:testResults.roundResults += $roundResult
    
    Write-ColorOutput "  📊 第 $RoundNumber 轮结果: $passedInRound/$totalInRound 通过 (成功率: $($roundResult.SuccessRate)%)" "Yellow"
    
    # 清理本轮测试数据
    Clear-TestData
    
    return $roundResult
}

# 生成测试报告
function Generate-TestReport {
    $totalTime = [math]::Round(((Get-Date) - $global:startTime).TotalSeconds, 2)
    $overallSuccessRate = [math]::Round(($global:testResults.passedTests / $global:testResults.totalTests) * 100, 2)
    $avgResponseTime = ($global:testResults.roundResults | Measure-Object -Property AvgResponseTime -Average).Average
    
    $report = @"

🏆 ===== 10轮次稳定性测试完整报告 =====

📈 整体统计:
  - 总测试次数: $($global:testResults.totalTests)
  - 通过测试: $($global:testResults.passedTests)
  - 失败测试: $($global:testResults.failedTests)
  - 整体成功率: $overallSuccessRate%
  - 平均响应时间: $([math]::Round($avgResponseTime, 2))ms
  - 总测试时间: ${totalTime}秒

📊 各轮次详细结果:
"@
    
    foreach ($round in $global:testResults.roundResults) {
        $report += "`n  轮次 $($round.Round): $($round.PassedTests)/$($round.TotalTests) 通过 (成功率: $($round.SuccessRate)%) - 平均响应时间: $($round.AvgResponseTime)ms"
    }
    
    if ($global:testResults.errors.Count -gt 0) {
        $report += "`n`n❌ 错误详情:"
        foreach ($error in $global:testResults.errors) {
            $report += "`n  - $error"
        }
    }
    
    $report += "`n`n🎯 稳定性评估:"
    if ($overallSuccessRate -ge 95) {
        $report += "`n  优秀 - 系统非常稳定，各项功能正常"
    } elseif ($overallSuccessRate -ge 85) {
        $report += "`n  良好 - 系统基本稳定，少量问题需要关注"
    } elseif ($overallSuccessRate -ge 70) {
        $report += "`n  一般 - 系统存在一些稳定性问题，建议优化"
    } else {
        $report += "`n  较差 - 系统稳定性有重大问题，需要紧急修复"
    }
    
    $report += "`n`n测试时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
    $report += "`n============================================"
    
    return $report
}

# 主测试函数
function Start-StabilityTest {
    Write-ColorOutput "🚀 开始 10轮次稳定性功能测试" "Magenta"
    Write-ColorOutput "📋 测试目标: $BASE_URL" "Yellow"
    Write-ColorOutput "⏱️  测试配置: $ROUNDS 轮次，轮次间间隔 $DELAY_BETWEEN_ROUNDS 秒" "Yellow"
    Write-ColorOutput "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" "Gray"
    
    $global:startTime = Get-Date
    
    # 初始清理
    Clear-TestData
    
    # 执行各轮测试
    for ($i = 1; $i -le $ROUNDS; $i++) {
        try {
            Run-SingleRound -RoundNumber $i
            
            if ($i -lt $ROUNDS) {
                Write-ColorOutput "⏳ 等待 $DELAY_BETWEEN_ROUNDS 秒后开始下一轮..." "Gray"
                Start-Sleep -Seconds $DELAY_BETWEEN_ROUNDS
            }
        }
        catch {
            Write-ColorOutput "❌ 第 $i 轮测试出现异常: $($_.Exception.Message)" "Red"
            $global:testResults.errors += "第 $i 轮测试异常: $($_.Exception.Message)"
        }
    }
    
    # 生成并显示报告
    $report = Generate-TestReport
    Write-ColorOutput $report "White"
    
    # 保存报告到文件
    $reportFile = "stability-test-report-$(Get-Date -Format 'yyyyMMdd-HHmmss').txt"
    $report | Out-File -FilePath $reportFile -Encoding UTF8
    Write-ColorOutput "📄 测试报告已保存到: $reportFile" "Green"
    
    # 保存JSON格式的详细结果
    $jsonResults = @{
        summary = @{
            totalTests = $global:testResults.totalTests
            passedTests = $global:testResults.passedTests
            failedTests = $global:testResults.failedTests
            successRate = [math]::Round(($global:testResults.passedTests / $global:testResults.totalTests) * 100, 2)
            totalTime = [math]::Round(((Get-Date) - $global:startTime).TotalSeconds, 2)
            timestamp = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
        }
        rounds = $global:testResults.roundResults
        errors = $global:testResults.errors
    }
    
    $jsonFile = "stability-test-results-$(Get-Date -Format 'yyyyMMdd-HHmmss').json"
    $jsonResults | ConvertTo-Json -Depth 10 | Out-File -FilePath $jsonFile -Encoding UTF8
    Write-ColorOutput "📊 JSON结果已保存到: $jsonFile" "Green"
    
    return $global:testResults
}

# 检查应用是否运行
function Test-ApplicationRunning {
    try {
        $response = Invoke-WebRequest -Uri "$BASE_URL/api/health/ping" -UseBasicParsing -TimeoutSec 5
        return $response.StatusCode -eq 200
    }
    catch {
        return $false
    }
}

# 脚本主入口
Write-ColorOutput "🔍 检查应用状态..." "Yellow"

if (-not (Test-ApplicationRunning)) {
    Write-ColorOutput "❌ 应用未运行或无法访问: $BASE_URL" "Red"
    Write-ColorOutput "请确保 CourtLink 应用正在运行在端口 8080" "Yellow"
    Write-ColorOutput "启动命令: mvn spring-boot:run 或 java -jar target/courtlink.jar" "Yellow"
    exit 1
}

Write-ColorOutput "✅ 应用运行正常，开始稳定性测试..." "Green"

# 开始测试
$results = Start-StabilityTest

# 返回退出码
if ($results.failedTests -eq 0) {
    Write-ColorOutput "`n🎉 所有测试通过！系统稳定性优秀！" "Green"
    exit 0
} else {
    Write-ColorOutput "`n⚠️  存在 $($results.failedTests) 个失败测试，请检查系统状态" "Yellow"
    exit 1
} 