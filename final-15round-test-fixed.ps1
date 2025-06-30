# CourtLink 15轮次最终稳定性测试
param(
    [int]$Rounds = 15,
    [string]$BaseUrl = "http://localhost:8080"
)

# 全局结果统计
$TestResults = @{
    StartTime = Get-Date
    SuccessfulRounds = 0
    FailedRounds = 0
    Stats = @{
        Health = @{ Pass = 0; Fail = 0 }
        Register = @{ Pass = 0; Fail = 0 }
        Query = @{ Pass = 0; Fail = 0 }
        Exception = @{ Pass = 0; Fail = 0 }
        Cleanup = @{ Pass = 0; Fail = 0 }
    }
    Failures = @()
}

function Write-Color($Message, $Color = "White") {
    Write-Host $Message -ForegroundColor $Color
}

function Test-RestApi {
    param($Uri, $Method = "GET", $Body = $null)
    
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

function New-TestUser($Round) {
    $ts = [int][double]::Parse((Get-Date -UFormat %s))
    $rand = Get-Random -Min 1000 -Max 9999
    
    return @{
        username = "final_r${Round}_${ts}_${rand}"
        email = "final_r${Round}_${ts}_${rand}@test.com"
        password = "password123"
        realName = "Test User $Round"
        phoneNumber = "13800000001"
    }
}

Write-Color "`n=== CourtLink 15轮次最终稳定性测试 ===" "Cyan"
Write-Color "开始时间: $(Get-Date)" "Gray"
Write-Color "测试轮次: $Rounds" "Gray"
Write-Color "===========================================" "Cyan"

# 主测试循环
for ($i = 1; $i -le $Rounds; $i++) {
    $roundStart = Get-Date
    $roundOK = $true
    $errors = @()
    
    Write-Color "`n--- 第 $i 轮测试 ---" "Yellow"
    
    # 1. 健康检查
    Write-Host "  [1/5] 健康检查..." -NoNewline
    $health = Test-RestApi "$BaseUrl/actuator/health"
    
    if ($health.Success -and $health.Data.status -eq "UP") {
        Write-Color " OK" "Green"
        $TestResults.Stats.Health.Pass++
    } else {
        Write-Color " FAIL" "Red"
        $TestResults.Stats.Health.Fail++
        $roundOK = $false
        $errors += "Health check failed"
    }
    
    # 2. 用户注册
    Write-Host "  [2/5] 用户注册..." -NoNewline
    $user = New-TestUser $i
    $reg = Test-RestApi "$BaseUrl/api/users/register" "POST" $user
    
    $userId = $null
    if ($reg.Success -and $reg.Data.id) {
        Write-Color " OK (ID: $($reg.Data.id))" "Green"
        $TestResults.Stats.Register.Pass++
        $userId = $reg.Data.id
    } else {
        Write-Color " FAIL" "Red"
        $TestResults.Stats.Register.Fail++
        $roundOK = $false
        $errors += "User registration failed"
    }
    
    Start-Sleep -Milliseconds 100
    
    # 3. 用户查询
    if ($userId) {
        Write-Host "  [3/5] 用户查询..." -NoNewline
        $query = Test-RestApi "$BaseUrl/api/users/$userId"
        
        if ($query.Success -and $query.Data.username -eq $user.username) {
            Write-Color " OK" "Green"
            $TestResults.Stats.Query.Pass++
        } else {
            Write-Color " FAIL" "Red"
            $TestResults.Stats.Query.Fail++
            $roundOK = $false
            $errors += "User query failed"
        }
    } else {
        Write-Color "  [3/5] 用户查询... SKIP" "Yellow"
        $TestResults.Stats.Query.Fail++
    }
    
    # 4. 异常测试
    Write-Host "  [4/5] 异常测试..." -NoNewline
    
    $notFound = Test-RestApi "$BaseUrl/api/users/999999"
    $test1 = (!$notFound.Success -and $notFound.StatusCode -eq 404)
    
    $invalid = @{ username = "ab"; email = "bad"; password = "123" }
    $badReg = Test-RestApi "$BaseUrl/api/users/register" "POST" $invalid
    $test2 = (!$badReg.Success -and $badReg.StatusCode -eq 400)
    
    if ($test1 -and $test2) {
        Write-Color " OK (404+400)" "Green"
        $TestResults.Stats.Exception.Pass++
    } else {
        Write-Color " FAIL" "Red"
        $TestResults.Stats.Exception.Fail++
        $roundOK = $false
        $errors += "Exception test failed"
    }
    
    # 5. 清理数据
    if ($userId) {
        Write-Host "  [5/5] 数据清理..." -NoNewline
        $delete = Test-RestApi "$BaseUrl/api/users/$userId" "DELETE"
        
        if ($delete.Success) {
            Write-Color " OK" "Green"
            $TestResults.Stats.Cleanup.Pass++
        } else {
            Write-Color " FAIL" "Red"
            $TestResults.Stats.Cleanup.Fail++
            $roundOK = $false
            $errors += "Cleanup failed"
        }
    } else {
        Write-Color "  [5/5] 数据清理... SKIP" "Yellow"
    }
    
    # 轮次结果
    $duration = (Get-Date) - $roundStart
    if ($roundOK) {
        Write-Color "  PASS Round $i (${[int]$duration.TotalMilliseconds}ms)" "Green"
        $TestResults.SuccessfulRounds++
    } else {
        Write-Color "  FAIL Round $i (${[int]$duration.TotalMilliseconds}ms)" "Red"
        $TestResults.FailedRounds++
        $TestResults.Failures += @{
            Round = $i
            Errors = $errors
        }
    }
    
    # 轮次间控制
    if ($i -lt $Rounds) {
        if ($i % 5 -eq 0) {
            Write-Color "  Resource cleanup after round $i..." "Cyan"
            [System.GC]::Collect()
            Start-Sleep -Milliseconds 1000
        } else {
            Start-Sleep -Milliseconds 300
        }
    }
}

# 生成报告
$TestResults.EndTime = Get-Date
$TestResults.Duration = $TestResults.EndTime - $TestResults.StartTime
$successRate = [math]::Round(($TestResults.SuccessfulRounds / $Rounds) * 100, 2)

Write-Color "`n===========================================" "Cyan"
Write-Color "=== 15轮次测试最终报告 ===" "Cyan"
Write-Color "===========================================" "Cyan"

Write-Color "`n总体结果:" "White"
Write-Color "  总轮次: $Rounds" "Gray"
Write-Color "  成功: $($TestResults.SuccessfulRounds)" "Green"
Write-Color "  失败: $($TestResults.FailedRounds)" "Red"
Write-Color "  成功率: $successRate%" $(if ($successRate -ge 95) { "Green" } elseif ($successRate -ge 85) { "Yellow" } else { "Red" })
Write-Color "  总时长: $([int]$TestResults.Duration.TotalSeconds)秒" "Gray"

Write-Color "`n详细统计:" "White"
foreach ($test in $TestResults.Stats.Keys) {
    $s = $TestResults.Stats[$test]
    $total = $s.Pass + $s.Fail
    $rate = if ($total -gt 0) { [math]::Round(($s.Pass / $total) * 100, 1) } else { 0 }
    $color = if ($rate -eq 100) { "Green" } elseif ($rate -ge 90) { "Yellow" } else { "Red" }
    Write-Color "  $test`: $($s.Pass)/$total ($rate%)" $color
}

if ($TestResults.Failures.Count -gt 0) {
    Write-Color "`n失败详情:" "Red"
    foreach ($fail in $TestResults.Failures) {
        Write-Color "  Round $($fail.Round): $($fail.Errors -join ', ')" "Red"
    }
}

# 稳定性评级
Write-Color "`n系统稳定性评级:" "White"
if ($successRate -eq 100) {
    Write-Color "  等级: A+ (完美)" "Green"
    Write-Color "  建议: 立即部署生产环境" "Green"
} elseif ($successRate -ge 95) {
    Write-Color "  等级: A (优秀)" "Green"
    Write-Color "  建议: 推荐部署生产环境" "Green"
} elseif ($successRate -ge 90) {
    Write-Color "  等级: A- (良好)" "Yellow"
    Write-Color "  建议: 可部署，建议监控" "Yellow"
} elseif ($successRate -ge 80) {
    Write-Color "  等级: B (尚可)" "Yellow"
    Write-Color "  建议: 修复问题后部署" "Yellow"
} else {
    Write-Color "  等级: C (需改进)" "Red"
    Write-Color "  建议: 不建议部署" "Red"
}

# 保存结果
$summary = @{
    TestDate = $TestResults.StartTime.ToString("yyyy-MM-dd HH:mm:ss")
    TotalRounds = $Rounds
    SuccessfulRounds = $TestResults.SuccessfulRounds
    SuccessRate = $successRate
    Duration = $TestResults.Duration.TotalSeconds
    Stats = $TestResults.Stats
    Failures = $TestResults.Failures
}

$resultFile = "final-15round-results.json"
$summary | ConvertTo-Json -Depth 3 | Out-File $resultFile -Encoding UTF8

Write-Color "`n结果已保存到: $resultFile" "Cyan"
Write-Color "`n=== 15轮次测试完成 ===" "Cyan" 