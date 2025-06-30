# CourtLink 15 Round Ultimate Stability Test - FINAL VERSION
$BaseUrl = "http://localhost:8080"
$Rounds = 15

# Test counters
$SuccessRounds = 0
$FailRounds = 0
$HealthPass = 0
$RegisterPass = 0
$QueryPass = 0
$ExceptionPass = 0
$CleanupPass = 0

Write-Host "`n=== CourtLink 15轮次最终稳定性测试 ===" -ForegroundColor Cyan
Write-Host "开始时间: $(Get-Date)" -ForegroundColor Gray
Write-Host "基础URL: $BaseUrl" -ForegroundColor Gray
Write-Host "总轮次: $Rounds" -ForegroundColor Gray
Write-Host "=============================================" -ForegroundColor Cyan

$StartTime = Get-Date

# Main test loop
for ($round = 1; $round -le $Rounds; $round++) {
    $RoundStart = Get-Date
    $RoundSuccess = $true
    
    Write-Host "`n--- 第 $round 轮测试 ---" -ForegroundColor Yellow
    
    # 1. Health Check
    Write-Host "  [1/5] 健康检查..." -NoNewline
    try {
        $response = Invoke-RestMethod -Uri "$BaseUrl/actuator/health" -Method GET
        if ($response.status -eq "UP") {
            Write-Host " OK" -ForegroundColor Green
            $HealthPass++
        } else {
            Write-Host " FAIL" -ForegroundColor Red
            $RoundSuccess = $false
        }
    } catch {
        Write-Host " FAIL" -ForegroundColor Red
        $RoundSuccess = $false
    }
    
    # 2. User Registration with SHORT username
    Write-Host "  [2/5] 用户注册..." -NoNewline
    $UserId = $null
    try {
        # Generate SHORT unique username (max 15 chars to be safe)
        $ts = ([DateTimeOffset]::Now.ToUnixTimeMilliseconds() % 10000).ToString()
        $rand = (Get-Random -Min 10 -Max 99).ToString()
        $username = "u${round}${ts}${rand}"  # Format: u + round + 4digits + 2digits = max 8 chars
        
        $userData = @{
            username = $username
            email = "${username}@test.com"
            password = "password123"
            realName = "Test User $round"
            phoneNumber = "13800000001"
        }
        
        $headers = @{"Content-Type" = "application/json"}
        $response = Invoke-RestMethod -Uri "$BaseUrl/api/users/register" -Method POST -Body ($userData | ConvertTo-Json) -Headers $headers
        
        if ($response.id) {
            Write-Host " OK (ID: $($response.id))" -ForegroundColor Green
            $RegisterPass++
            $UserId = $response.id
        } else {
            Write-Host " FAIL (No ID)" -ForegroundColor Red
            $RoundSuccess = $false
        }
    } catch {
        Write-Host " FAIL" -ForegroundColor Red
        $RoundSuccess = $false
    }
    
    Start-Sleep -Milliseconds 100
    
    # 3. User Query
    if ($UserId) {
        Write-Host "  [3/5] 用户查询..." -NoNewline
        try {
            $response = Invoke-RestMethod -Uri "$BaseUrl/api/users/$UserId" -Method GET
            if ($response.username -eq $userData.username) {
                Write-Host " OK" -ForegroundColor Green
                $QueryPass++
            } else {
                Write-Host " FAIL" -ForegroundColor Red
                $RoundSuccess = $false
            }
        } catch {
            Write-Host " FAIL" -ForegroundColor Red
            $RoundSuccess = $false
        }
    } else {
        Write-Host "  [3/5] 用户查询... SKIP" -ForegroundColor Yellow
    }
    
    # 4. Exception Test
    Write-Host "  [4/5] 异常测试..." -NoNewline
    $ExceptionTestPass = $true
    
    # Test 404
    try {
        Invoke-RestMethod -Uri "$BaseUrl/api/users/999999" -Method GET
        $ExceptionTestPass = $false
    } catch {
        if ($_.Exception.Response.StatusCode -eq 404) {
            # Expected 404
        } else {
            $ExceptionTestPass = $false
        }
    }
    
    # Test 400
    try {
        $invalidData = @{
            username = "ab"
            email = "invalid"
            password = "123"
        }
        $headers = @{"Content-Type" = "application/json"}
        Invoke-RestMethod -Uri "$BaseUrl/api/users/register" -Method POST -Body ($invalidData | ConvertTo-Json) -Headers $headers
        $ExceptionTestPass = $false
    } catch {
        if ($_.Exception.Response.StatusCode -eq 400) {
            # Expected 400
        } else {
            $ExceptionTestPass = $false
        }
    }
    
    if ($ExceptionTestPass) {
        Write-Host " OK (404+400)" -ForegroundColor Green
        $ExceptionPass++
    } else {
        Write-Host " FAIL" -ForegroundColor Red
        $RoundSuccess = $false
    }
    
    # 5. Cleanup
    if ($UserId) {
        Write-Host "  [5/5] 数据清理..." -NoNewline
        try {
            Invoke-RestMethod -Uri "$BaseUrl/api/users/$UserId" -Method DELETE
            Write-Host " OK" -ForegroundColor Green
            $CleanupPass++
        } catch {
            Write-Host " FAIL" -ForegroundColor Red
            $RoundSuccess = $false
        }
    } else {
        Write-Host "  [5/5] 数据清理... SKIP" -ForegroundColor Yellow
    }
    
    # Round Result
    $RoundDuration = (Get-Date) - $RoundStart
    if ($RoundSuccess) {
        Write-Host "  PASS Round $round (${[int]$RoundDuration.TotalMilliseconds}ms)" -ForegroundColor Green
        $SuccessRounds++
    } else {
        Write-Host "  FAIL Round $round (${[int]$RoundDuration.TotalMilliseconds}ms)" -ForegroundColor Red
        $FailRounds++
    }
    
    # Interval control
    if ($round -lt $Rounds) {
        if ($round % 5 -eq 0) {
            Write-Host "  Resource cleanup after round $round..." -ForegroundColor Cyan
            [System.GC]::Collect()
            Start-Sleep -Milliseconds 1000
        } else {
            Start-Sleep -Milliseconds 300
        }
    }
}

# Final Report
$EndTime = Get-Date
$TotalDuration = $EndTime - $StartTime
$SuccessRate = [math]::Round(($SuccessRounds / $Rounds) * 100, 2)

Write-Host "`n=============================================" -ForegroundColor Cyan
Write-Host "=== 15轮次最终稳定性测试报告 ===" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan

Write-Host "`n总体结果:" -ForegroundColor White
Write-Host "  总轮次: $Rounds" -ForegroundColor Gray
Write-Host "  成功轮次: $SuccessRounds" -ForegroundColor Green
Write-Host "  失败轮次: $FailRounds" -ForegroundColor Red

if ($SuccessRate -eq 100) {
    Write-Host "  成功率: $SuccessRate%" -ForegroundColor Green
} elseif ($SuccessRate -ge 95) {
    Write-Host "  成功率: $SuccessRate%" -ForegroundColor Green
} elseif ($SuccessRate -ge 85) {
    Write-Host "  成功率: $SuccessRate%" -ForegroundColor Yellow
} else {
    Write-Host "  成功率: $SuccessRate%" -ForegroundColor Red
}

Write-Host "  总耗时: $([int]$TotalDuration.TotalSeconds)秒" -ForegroundColor Gray

Write-Host "`n详细统计:" -ForegroundColor White
$healthRate = ($HealthPass/$Rounds*100).ToString("F1")
$regRate = ($RegisterPass/$Rounds*100).ToString("F1")
$queryRate = ($QueryPass/$Rounds*100).ToString("F1")
$excRate = ($ExceptionPass/$Rounds*100).ToString("F1")
$cleanRate = ($CleanupPass/$Rounds*100).ToString("F1")

Write-Host "  健康检查: $HealthPass/$Rounds ($healthRate%)" -ForegroundColor $(if ($HealthPass -eq $Rounds) { "Green" } else { "Red" })
Write-Host "  用户注册: $RegisterPass/$Rounds ($regRate%)" -ForegroundColor $(if ($RegisterPass -eq $Rounds) { "Green" } else { "Red" })
Write-Host "  用户查询: $QueryPass/$Rounds ($queryRate%)" -ForegroundColor $(if ($QueryPass -eq $Rounds) { "Green" } else { "Red" })
Write-Host "  异常测试: $ExceptionPass/$Rounds ($excRate%)" -ForegroundColor $(if ($ExceptionPass -eq $Rounds) { "Green" } else { "Red" })
Write-Host "  数据清理: $CleanupPass/$Rounds ($cleanRate%)" -ForegroundColor $(if ($CleanupPass -eq $Rounds) { "Green" } else { "Red" })

Write-Host "`n系统稳定性评级:" -ForegroundColor White
if ($SuccessRate -eq 100) {
    Write-Host "  等级: A+ (完美)" -ForegroundColor Green
    Write-Host "  建议: 立即部署到生产环境" -ForegroundColor Green
} elseif ($SuccessRate -ge 95) {
    Write-Host "  等级: A (优秀)" -ForegroundColor Green
    Write-Host "  建议: 推荐部署到生产环境" -ForegroundColor Green
} elseif ($SuccessRate -ge 90) {
    Write-Host "  等级: A- (良好)" -ForegroundColor Yellow
    Write-Host "  建议: 可部署，建议监控" -ForegroundColor Yellow
} elseif ($SuccessRate -ge 80) {
    Write-Host "  等级: B (尚可)" -ForegroundColor Yellow
    Write-Host "  建议: 修复问题后部署" -ForegroundColor Yellow
} else {
    Write-Host "  等级: C (需改进)" -ForegroundColor Red
    Write-Host "  建议: 不推荐部署" -ForegroundColor Red
}

# Save results
$Results = @{
    TestDate = $StartTime.ToString("yyyy-MM-dd HH:mm:ss")
    TotalRounds = $Rounds
    SuccessfulRounds = $SuccessRounds
    FailedRounds = $FailRounds
    SuccessRate = $SuccessRate
    Duration = $TotalDuration.TotalSeconds
    DetailedStats = @{
        HealthCheck = @{ Pass = $HealthPass; Total = $Rounds; Rate = $healthRate }
        UserRegistration = @{ Pass = $RegisterPass; Total = $Rounds; Rate = $regRate }
        UserQuery = @{ Pass = $QueryPass; Total = $Rounds; Rate = $queryRate }
        ExceptionTest = @{ Pass = $ExceptionPass; Total = $Rounds; Rate = $excRate }
        Cleanup = @{ Pass = $CleanupPass; Total = $Rounds; Rate = $cleanRate }
    }
}

$ResultFile = "ultimate-15round-test-results.json"
$Results | ConvertTo-Json -Depth 3 | Out-File $ResultFile -Encoding UTF8

Write-Host "`n结果已保存到: $ResultFile" -ForegroundColor Cyan
Write-Host "`n=== 15轮次最终稳定性测试完成 ===" -ForegroundColor Cyan 