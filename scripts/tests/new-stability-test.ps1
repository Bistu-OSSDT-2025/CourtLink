# CourtLink 主分支全新稳定性测试
# 测试日期: 2025-07-01
# 版本: 1.0 (全新测试)

param(
    [int]$TestRounds = 15,
    [string]$BaseUrl = "http://localhost:8080",
    [int]$TimeoutSeconds = 10
)

# 测试结果统计
$TestStats = @{
    StartTime = Get-Date
    TestRounds = $TestRounds
    BasicTests = @{ Total = 0; Passed = 0; Failed = 0 }
    CrudTests = @{ Total = 0; Passed = 0; Failed = 0 }
    ExceptionTests = @{ Total = 0; Passed = 0; Failed = 0 }
    TestDetails = @()
    FailureDetails = @()
}

function Write-TestLog {
    param($Message, $Level = "INFO")
    $timestamp = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
    $color = switch($Level) {
        "INFO" { "White" }
        "SUCCESS" { "Green" }
        "WARNING" { "Yellow" }
        "ERROR" { "Red" }
        "HEADER" { "Cyan" }
        default { "Gray" }
    }
    Write-Host "[$timestamp] $Message" -ForegroundColor $color
}

function Invoke-ApiCall {
    param(
        [string]$Url,
        [string]$Method = "GET",
        [hashtable]$Body = $null,
        [int]$Timeout = 10
    )
    
    try {
        $params = @{
            Uri = $Url
            Method = $Method
            TimeoutSec = $Timeout
            Headers = @{ "Content-Type" = "application/json" }
        }
        
        if ($Body -and $Method -ne "GET") {
            $params.Body = ($Body | ConvertTo-Json -Depth 10)
        }
        
        $response = Invoke-RestMethod @params
        
        return @{
            Success = $true
            StatusCode = 200
            Data = $response
            ResponseTime = (Measure-Command { $response }).TotalMilliseconds
        }
    }
    catch {
        $statusCode = 0
        if ($_.Exception.Response) {
            $statusCode = [int]$_.Exception.Response.StatusCode
        }
        
        return @{
            Success = $false
            StatusCode = $statusCode
            Error = $_.Exception.Message
            ResponseTime = 0
        }
    }
}

function Test-ApplicationHealth {
    Write-TestLog "开始健康检查测试..." "HEADER"
    
    $healthTest = Invoke-ApiCall "$BaseUrl/actuator/health"
    $TestStats.BasicTests.Total++
    
    if ($healthTest.Success -and $healthTest.Data.status -eq "UP") {
        Write-TestLog "健康检查通过 - 状态: UP" "SUCCESS"
        $TestStats.BasicTests.Passed++
        return $true
    } else {
        Write-TestLog "健康检查失败 - 错误: $($healthTest.Error)" "ERROR"
        $TestStats.BasicTests.Failed++
        $TestStats.FailureDetails += "健康检查失败: $($healthTest.Error)"
        return $false
    }
}

function Test-BasicEndpoints {
    Write-TestLog "开始基础端点测试..." "HEADER"
    
    # 测试用户列表端点
    $userListTest = Invoke-ApiCall "$BaseUrl/api/users"
    $TestStats.BasicTests.Total++
    
    if ($userListTest.Success) {
        Write-TestLog "用户列表端点测试通过" "SUCCESS"
        $TestStats.BasicTests.Passed++
    } else {
        Write-TestLog "用户列表端点测试失败 - 错误: $($userListTest.Error)" "ERROR"
        $TestStats.BasicTests.Failed++
        $TestStats.FailureDetails += "用户列表端点失败: $($userListTest.Error)"
    }
    
    # 测试场地列表端点
    $courtListTest = Invoke-ApiCall "$BaseUrl/api/courts"
    $TestStats.BasicTests.Total++
    
    if ($courtListTest.Success) {
        Write-TestLog "场地列表端点测试通过" "SUCCESS"
        $TestStats.BasicTests.Passed++
    } else {
        Write-TestLog "场地列表端点测试失败 - 错误: $($courtListTest.Error)" "ERROR"
        $TestStats.BasicTests.Failed++
        $TestStats.FailureDetails += "场地列表端点失败: $($courtListTest.Error)"
    }
    
    # 测试Swagger文档端点
    $swaggerTest = Invoke-ApiCall "$BaseUrl/swagger-ui/index.html"
    $TestStats.BasicTests.Total++
    
    if ($swaggerTest.Success) {
        Write-TestLog "Swagger文档端点测试通过" "SUCCESS"
        $TestStats.BasicTests.Passed++
    } else {
        Write-TestLog "Swagger文档端点测试失败 - 错误: $($swaggerTest.Error)" "ERROR"
        $TestStats.BasicTests.Failed++
        $TestStats.FailureDetails += "Swagger文档端点失败: $($swaggerTest.Error)"
    }
}

function Test-UserCrudOperations {
    param([int]$RoundNumber)
    
    Write-TestLog "开始第 $RoundNumber 轮用户CRUD测试..." "HEADER"
    $roundSuccess = $true
    
    # 生成唯一测试数据
    $timestamp = (Get-Date).Ticks
    $randomId = Get-Random -Min 1000 -Max 9999
    
    $testUser = @{
        username = "testuser_${RoundNumber}_${randomId}"
        email = "test_${RoundNumber}_${randomId}@example.com"
        password = "password123"
        realName = "测试用户_${RoundNumber}"
        phoneNumber = "1380000000$RoundNumber"
    }
    
    # 1. 用户注册测试
    Write-TestLog "  执行用户注册..." "INFO"
    $registerTest = Invoke-ApiCall "$BaseUrl/api/users/register" "POST" $testUser
    $TestStats.CrudTests.Total++
    
    if ($registerTest.Success -and $registerTest.Data.id) {
        $userId = $registerTest.Data.id
        Write-TestLog "  用户注册成功 - ID: $userId" "SUCCESS"
        $TestStats.CrudTests.Passed++
        
        # 2. 用户查询测试
        Write-TestLog "  执行用户查询..." "INFO"
        Start-Sleep -Milliseconds 100
        $queryTest = Invoke-ApiCall "$BaseUrl/api/users/$userId"
        $TestStats.CrudTests.Total++
        
        if ($queryTest.Success -and $queryTest.Data.username -eq $testUser.username) {
            Write-TestLog "  用户查询成功 - 数据匹配" "SUCCESS"
            $TestStats.CrudTests.Passed++
        } else {
            Write-TestLog "  用户查询失败 - 数据不匹配或查询失败" "ERROR"
            $TestStats.CrudTests.Failed++
            $TestStats.FailureDetails += "第${RoundNumber}轮用户查询失败"
            $roundSuccess = $false
        }
        
        # 3. 用户名检查测试
        Write-TestLog "  执行用户名检查..." "INFO"
        $usernameCheckTest = Invoke-ApiCall "$BaseUrl/api/users/check-username/$($testUser.username)"
        $TestStats.CrudTests.Total++
        
        if ($usernameCheckTest.Success) {
            Write-TestLog "  用户名检查成功" "SUCCESS"
            $TestStats.CrudTests.Passed++
        } else {
            Write-TestLog "  用户名检查失败" "ERROR"
            $TestStats.CrudTests.Failed++
            $TestStats.FailureDetails += "第${RoundNumber}轮用户名检查失败"
            $roundSuccess = $false
        }
        
        # 4. 用户删除测试
        Write-TestLog "  执行用户删除..." "INFO"
        $deleteTest = Invoke-ApiCall "$BaseUrl/api/users/$userId" "DELETE"
        $TestStats.CrudTests.Total++
        
        if ($deleteTest.Success) {
            Write-TestLog "  用户删除成功" "SUCCESS"
            $TestStats.CrudTests.Passed++
        } else {
            Write-TestLog "  用户删除失败" "ERROR"
            $TestStats.CrudTests.Failed++
            $TestStats.FailureDetails += "第${RoundNumber}轮用户删除失败"
            $roundSuccess = $false
        }
        
    } else {
        Write-TestLog "  用户注册失败 - 错误: $($registerTest.Error)" "ERROR"
        $TestStats.CrudTests.Failed++
        $TestStats.FailureDetails += "第${RoundNumber}轮用户注册失败: $($registerTest.Error)"
        $roundSuccess = $false
    }
    
    if ($roundSuccess) {
        Write-TestLog "第 $RoundNumber 轮测试完成 - 成功" "SUCCESS"
    } else {
        Write-TestLog "第 $RoundNumber 轮测试完成 - 失败" "ERROR"
    }
    
    $TestStats.TestDetails += @{
        Round = $RoundNumber
        Success = $roundSuccess
        Timestamp = Get-Date
    }
    
    return $roundSuccess
}

function Test-CourtOperations {
    Write-TestLog "开始场地管理测试..." "HEADER"
    
    # 测试场地创建
    $testCourt = @{
        name = "测试场地_$(Get-Random)"
        location = "测试位置"
        description = "测试描述"
        pricePerHour = 100.0
        status = "AVAILABLE"
    }
    
    $courtCreateTest = Invoke-ApiCall "$BaseUrl/api/courts" "POST" $testCourt
    $TestStats.CrudTests.Total++
    
    if ($courtCreateTest.Success -and $courtCreateTest.Data.id) {
        $courtId = $courtCreateTest.Data.id
        Write-TestLog "场地创建成功 - ID: $courtId" "SUCCESS"
        $TestStats.CrudTests.Passed++
        
        # 测试场地查询
        $courtQueryTest = Invoke-ApiCall "$BaseUrl/api/courts/$courtId"
        $TestStats.CrudTests.Total++
        
        if ($courtQueryTest.Success) {
            Write-TestLog "场地查询成功" "SUCCESS"
            $TestStats.CrudTests.Passed++
        } else {
            Write-TestLog "场地查询失败" "ERROR"
            $TestStats.CrudTests.Failed++
            $TestStats.FailureDetails += "场地查询失败"
        }
        
        # 测试场地删除
        $courtDeleteTest = Invoke-ApiCall "$BaseUrl/api/courts/$courtId" "DELETE"
        $TestStats.CrudTests.Total++
        
        if ($courtDeleteTest.Success) {
            Write-TestLog "场地删除成功" "SUCCESS"
            $TestStats.CrudTests.Passed++
        } else {
            Write-TestLog "场地删除失败" "ERROR" 
            $TestStats.CrudTests.Failed++
            $TestStats.FailureDetails += "场地删除失败"
        }
    } else {
        Write-TestLog "场地创建失败 - 错误: $($courtCreateTest.Error)" "ERROR"
        $TestStats.CrudTests.Failed++
        $TestStats.FailureDetails += "场地创建失败: $($courtCreateTest.Error)"
    }
}

function Test-ExceptionHandling {
    Write-TestLog "开始异常处理测试..." "HEADER"
    
    # 测试404错误处理
    $notFoundTest = Invoke-ApiCall "$BaseUrl/api/users/999999"
    $TestStats.ExceptionTests.Total++
    
    if (!$notFoundTest.Success -and $notFoundTest.StatusCode -eq 404) {
        Write-TestLog "404错误处理测试通过" "SUCCESS"
        $TestStats.ExceptionTests.Passed++
    } else {
        Write-TestLog "404错误处理测试失败" "ERROR"
        $TestStats.ExceptionTests.Failed++
        $TestStats.FailureDetails += "404错误处理测试失败"
    }
    
    # 测试400错误处理（无效数据）
    $invalidUser = @{
        username = "a"  # 太短
        email = "invalid"  # 无效邮箱
        password = "123"  # 太短
    }
    
    $badRequestTest = Invoke-ApiCall "$BaseUrl/api/users/register" "POST" $invalidUser
    $TestStats.ExceptionTests.Total++
    
    if (!$badRequestTest.Success -and $badRequestTest.StatusCode -eq 400) {
        Write-TestLog "400错误处理测试通过" "SUCCESS"
        $TestStats.ExceptionTests.Passed++
    } else {
        Write-TestLog "400错误处理测试失败" "ERROR"
        $TestStats.ExceptionTests.Failed++
        $TestStats.FailureDetails += "400错误处理测试失败"
    }
}

# 主测试流程开始
Write-TestLog "========================================" "HEADER"
Write-TestLog "CourtLink 主分支全新稳定性测试开始" "HEADER"  
Write-TestLog "测试时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" "HEADER"
Write-TestLog "测试轮次: $TestRounds" "HEADER"
Write-TestLog "目标URL: $BaseUrl" "HEADER"
Write-TestLog "========================================" "HEADER"

# 等待应用启动
Write-TestLog "等待应用启动..." "INFO"
$appReady = $false
$maxWaitTime = 60
$waitTime = 0

while (-not $appReady -and $waitTime -lt $maxWaitTime) {
    try {
        $healthCheck = Invoke-RestMethod "$BaseUrl/actuator/health" -TimeoutSec 5
        if ($healthCheck.status -eq "UP") {
            $appReady = $true
            Write-TestLog "应用已启动并就绪!" "SUCCESS"
        }
    }
    catch {
        Start-Sleep 2
        $waitTime += 2
        Write-Host "." -NoNewline -ForegroundColor Gray
    }
}

if (-not $appReady) {
    Write-TestLog "应用启动超时，测试终止" "ERROR"
    exit 1
}

# 执行基础测试
Test-ApplicationHealth
Test-BasicEndpoints

# 执行多轮CRUD测试
$successfulRounds = 0
for ($round = 1; $round -le $TestRounds; $round++) {
    if (Test-UserCrudOperations -RoundNumber $round) {
        $successfulRounds++
    }
    
    # 每5轮休息一下
    if ($round % 5 -eq 0 -and $round -lt $TestRounds) {
        Write-TestLog "第 $round 轮完成，短暂休息..." "INFO"
        Start-Sleep -Seconds 1
    } else {
        Start-Sleep -Milliseconds 300
    }
}

# 执行场地管理测试
Test-CourtOperations

# 执行异常处理测试
Test-ExceptionHandling

# 生成测试报告
$TestStats.EndTime = Get-Date
$TestStats.Duration = $TestStats.EndTime - $TestStats.StartTime

$totalTests = $TestStats.BasicTests.Total + $TestStats.CrudTests.Total + $TestStats.ExceptionTests.Total
$totalPassed = $TestStats.BasicTests.Passed + $TestStats.CrudTests.Passed + $TestStats.ExceptionTests.Passed
$totalFailed = $TestStats.BasicTests.Failed + $TestStats.CrudTests.Failed + $TestStats.ExceptionTests.Failed

$overallSuccessRate = if ($totalTests -gt 0) { 
    [math]::Round(($totalPassed / $totalTests) * 100, 2) 
} else { 0 }

$roundSuccessRate = if ($TestRounds -gt 0) { 
    [math]::Round(($successfulRounds / $TestRounds) * 100, 2) 
} else { 0 }

# 输出测试结果
Write-TestLog "========================================" "HEADER"
Write-TestLog "测试结果汇总" "HEADER"
Write-TestLog "========================================" "HEADER"

Write-TestLog "基础功能测试: $($TestStats.BasicTests.Passed)/$($TestStats.BasicTests.Total) 通过" "INFO"
Write-TestLog "CRUD功能测试: $($TestStats.CrudTests.Passed)/$($TestStats.CrudTests.Total) 通过" "INFO"  
Write-TestLog "异常处理测试: $($TestStats.ExceptionTests.Passed)/$($TestStats.ExceptionTests.Total) 通过" "INFO"
Write-TestLog "CRUD轮次测试: $successfulRounds/$TestRounds 轮成功" "INFO"

Write-TestLog "总体统计:" "HEADER"
Write-TestLog "  总测试数: $totalTests" "INFO"
Write-TestLog "  通过测试: $totalPassed" "SUCCESS"
Write-TestLog "  失败测试: $totalFailed" $(if($totalFailed -gt 0) {"ERROR"} else {"SUCCESS"})
Write-TestLog "  整体成功率: $overallSuccessRate%" $(if($overallSuccessRate -ge 95) {"SUCCESS"} elseif($overallSuccessRate -ge 80) {"WARNING"} else {"ERROR"})
Write-TestLog "  轮次成功率: $roundSuccessRate%" $(if($roundSuccessRate -ge 95) {"SUCCESS"} elseif($roundSuccessRate -ge 80) {"WARNING"} else {"ERROR"})
Write-TestLog "  测试耗时: $([int]$TestStats.Duration.TotalSeconds) 秒" "INFO"

# 系统评级
$systemGrade = "F"
$gradeColor = "ERROR"
$recommendation = "系统不稳定，不建议部署"

if ($overallSuccessRate -ge 98 -and $roundSuccessRate -ge 95) {
    $systemGrade = "A+"
    $gradeColor = "SUCCESS"
    $recommendation = "系统极其稳定，强烈推荐立即部署到生产环境"
} elseif ($overallSuccessRate -ge 95 -and $roundSuccessRate -ge 90) {
    $systemGrade = "A"
    $gradeColor = "SUCCESS"
    $recommendation = "系统稳定，推荐部署到生产环境"
} elseif ($overallSuccessRate -ge 90 -and $roundSuccessRate -ge 85) {
    $systemGrade = "B+"
    $gradeColor = "WARNING"
    $recommendation = "系统较稳定，建议小规模测试后部署"
} elseif ($overallSuccessRate -ge 85) {
    $systemGrade = "B"
    $gradeColor = "WARNING"
    $recommendation = "系统基本稳定，需要进一步优化"
} elseif ($overallSuccessRate -ge 70) {
    $systemGrade = "C"
    $gradeColor = "ERROR"
    $recommendation = "系统不够稳定，需要修复问题"
} else {
    $systemGrade = "D"
    $gradeColor = "ERROR"
    $recommendation = "系统存在严重问题，禁止部署"
}

Write-TestLog "========================================" "HEADER"
Write-TestLog "系统稳定性评级: $systemGrade" $gradeColor
Write-TestLog "部署建议: $recommendation" $gradeColor
Write-TestLog "========================================" "HEADER"

# 输出失败详情
if ($TestStats.FailureDetails.Count -gt 0) {
    Write-TestLog "失败详情:" "ERROR"
    foreach ($failure in $TestStats.FailureDetails) {
        Write-TestLog "  - $failure" "ERROR"
    }
}

# 保存详细结果
$detailedResult = @{
    TestMetadata = @{
        TestName = "CourtLink 主分支全新稳定性测试"
        Version = "1.0"
        Date = (Get-Date).ToString("yyyy-MM-dd")
        Time = (Get-Date).ToString("HH:mm:ss")
        Duration = $TestStats.Duration.TotalSeconds
        TestRounds = $TestRounds
        BaseUrl = $BaseUrl
    }
    Results = @{
        BasicTests = $TestStats.BasicTests
        CrudTests = $TestStats.CrudTests
        ExceptionTests = $TestStats.ExceptionTests
        OverallStats = @{
            TotalTests = $totalTests
            TotalPassed = $totalPassed
            TotalFailed = $totalFailed
            OverallSuccessRate = $overallSuccessRate
            RoundSuccessRate = $roundSuccessRate
            SuccessfulRounds = $successfulRounds
        }
        SystemGrade = @{
            Grade = $systemGrade
            Recommendation = $recommendation
        }
        FailureDetails = $TestStats.FailureDetails
        TestDetails = $TestStats.TestDetails
    }
} | ConvertTo-Json -Depth 10

$resultFileName = "new-stability-test-results-$(Get-Date -Format 'yyyyMMdd-HHmmss').json"
$detailedResult | Out-File $resultFileName -Encoding UTF8

Write-TestLog "详细测试结果已保存至: $resultFileName" "INFO"
Write-TestLog "测试完成时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" "INFO" 