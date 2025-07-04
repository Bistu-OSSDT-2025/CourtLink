#!/usr/bin/env pwsh

Write-Host "=== CourtLink 10轮稳定性测试 ===" -ForegroundColor Green

$baseUrl = "http://localhost:8080"
$totalRounds = 10
$allResults = @()

$testStartTime = Get-Date
Write-Host "测试开始时间: $($testStartTime.ToString('yyyy-MM-dd HH:mm:ss'))" -ForegroundColor Cyan

function Test-API {
    param([string]$Name, [string]$Url, [string]$Method = "GET", [object]$Body = $null, [int]$ExpectedStatus = 200)
    
    try {
        $params = @{ Uri = $Url; Method = $Method; TimeoutSec = 10 }
        if ($Body) {
            $params.Body = ($Body | ConvertTo-Json)
            $params.ContentType = "application/json"
        }
        
        $response = Invoke-RestMethod @params
        return @{ Success = $true; Response = $response }
    } catch {
        $statusCode = if ($_.Exception.Response) { $_.Exception.Response.StatusCode.value__ } else { 0 }
        if ($ExpectedStatus -ne 200 -and $statusCode -eq $ExpectedStatus) {
            return @{ Success = $true; Response = "Expected error" }
        }
        return @{ Success = $false; Error = $_.Exception.Message; StatusCode = $statusCode }
    }
}

for ($round = 1; $round -le $totalRounds; $round++) {
    Write-Host "`n=== 第 $round 轮测试 ===" -ForegroundColor Yellow
    $roundTests = @()
    
    # 生成测试用户
    $testUser = @{
        username = "user_r${round}_$(Get-Random -Maximum 9999)"
        email = "test_r${round}_$(Get-Random -Maximum 9999)@example.com"
        password = "testpass123"
        fullName = "Test User $round"
        phoneNumber = "1380013800$round"
    }
    
    # 1. 健康检查
    Write-Host "1. 健康检查..." -NoNewline
    $result = Test-API "健康检查" "$baseUrl/actuator/health"
    if ($result.Success) {
        Write-Host " ✓" -ForegroundColor Green
        $roundTests += @{ Test = "健康检查"; Status = "成功"; Round = $round }
    } else {
        Write-Host " ✗" -ForegroundColor Red
        $roundTests += @{ Test = "健康检查"; Status = "失败"; Round = $round }
    }
    
    # 2. 用户注册
    Write-Host "2. 用户注册..." -NoNewline
    $registerResult = Test-API "用户注册" "$baseUrl/api/users/register" "POST" $testUser
    $userId = $null
    if ($registerResult.Success) {
        $userId = $registerResult.Response.id
        Write-Host " ✓ (ID: $userId)" -ForegroundColor Green
        $roundTests += @{ Test = "用户注册"; Status = "成功"; Round = $round }
    } else {
        Write-Host " ✗" -ForegroundColor Red
        $roundTests += @{ Test = "用户注册"; Status = "失败"; Round = $round }
    }
    
    if ($userId) {
        # 3. 用户登录
        Write-Host "3. 用户登录..." -NoNewline
        $loginData = @{ username = $testUser.username; password = $testUser.password }
        $loginResult = Test-API "用户登录" "$baseUrl/api/users/login" "POST" $loginData
        if ($loginResult.Success) {
            Write-Host " ✓" -ForegroundColor Green
            $roundTests += @{ Test = "用户登录"; Status = "成功"; Round = $round }
        } else {
            Write-Host " ✗" -ForegroundColor Red
            $roundTests += @{ Test = "用户登录"; Status = "失败"; Round = $round }
        }
        
        # 4. 获取用户信息
        Write-Host "4. 获取用户信息..." -NoNewline
        $getUserResult = Test-API "获取用户信息" "$baseUrl/api/users/$userId"
        if ($getUserResult.Success) {
            Write-Host " ✓" -ForegroundColor Green
            $roundTests += @{ Test = "获取用户信息"; Status = "成功"; Round = $round }
        } else {
            Write-Host " ✗" -ForegroundColor Red
            $roundTests += @{ Test = "获取用户信息"; Status = "失败"; Round = $round }
        }
        
        # 5. 密码验证
        Write-Host "5. 密码验证..." -NoNewline
        $passwordCheck = @{ password = $testUser.password }
        $passwordResult = Test-API "密码验证" "$baseUrl/api/users/$userId/validate-password" "POST" $passwordCheck
        if ($passwordResult.Success) {
            Write-Host " ✓" -ForegroundColor Green
            $roundTests += @{ Test = "密码验证"; Status = "成功"; Round = $round }
        } else {
            Write-Host " ✗" -ForegroundColor Red
            $roundTests += @{ Test = "密码验证"; Status = "失败"; Round = $round }
        }
        
        # 6. 用户激活
        Write-Host "6. 用户激活..." -NoNewline
        $activateResult = Test-API "用户激活" "$baseUrl/api/users/$userId/activate" "POST"
        if ($activateResult.Success) {
            Write-Host " ✓" -ForegroundColor Green
            $roundTests += @{ Test = "用户激活"; Status = "成功"; Round = $round }
        } else {
            Write-Host " ✗" -ForegroundColor Red
            $roundTests += @{ Test = "用户激活"; Status = "失败"; Round = $round }
        }
        
        # 7. 用户停用
        Write-Host "7. 用户停用..." -NoNewline
        $deactivateResult = Test-API "用户停用" "$baseUrl/api/users/$userId/deactivate" "POST"
        if ($deactivateResult.Success) {
            Write-Host " ✓" -ForegroundColor Green
            $roundTests += @{ Test = "用户停用"; Status = "成功"; Round = $round }
        } else {
            Write-Host " ✗" -ForegroundColor Red
            $roundTests += @{ Test = "用户停用"; Status = "失败"; Round = $round }
        }
        
        # 重新激活
        Test-API "重新激活" "$baseUrl/api/users/$userId/activate" "POST" | Out-Null
        
        # 8. 删除用户
        Write-Host "8. 删除用户..." -NoNewline
        $deleteResult = Test-API "删除用户" "$baseUrl/api/users/$userId" "DELETE"
        if ($deleteResult.Success) {
            Write-Host " ✓" -ForegroundColor Green
            $roundTests += @{ Test = "删除用户"; Status = "成功"; Round = $round }
        } else {
            Write-Host " ✗" -ForegroundColor Red
            $roundTests += @{ Test = "删除用户"; Status = "失败"; Round = $round }
        }
    }
    
    # 9. 异常测试 - 不存在用户
    Write-Host "9. 异常测试-不存在用户..." -NoNewline
    $invalidResult = Test-API "查询不存在用户" "$baseUrl/api/users/999999" "GET" $null 400
    if ($invalidResult.Success) {
        Write-Host " ✓" -ForegroundColor Green
        $roundTests += @{ Test = "异常测试-不存在用户"; Status = "成功"; Round = $round }
    } else {
        Write-Host " ✗" -ForegroundColor Red
        $roundTests += @{ Test = "异常测试-不存在用户"; Status = "失败"; Round = $round }
    }
    
    # 10. 异常测试 - 无效注册
    Write-Host "10. 异常测试-无效注册..." -NoNewline
    $invalidUser = @{ username = ""; email = "invalid"; password = "" }
    $invalidRegResult = Test-API "无效注册" "$baseUrl/api/users/register" "POST" $invalidUser 400
    if ($invalidRegResult.Success) {
        Write-Host " ✓" -ForegroundColor Green
        $roundTests += @{ Test = "异常测试-无效注册"; Status = "成功"; Round = $round }
    } else {
        Write-Host " ✗" -ForegroundColor Red
        $roundTests += @{ Test = "异常测试-无效注册"; Status = "失败"; Round = $round }
    }
    
    $successCount = ($roundTests | Where-Object { $_.Status -eq "成功" }).Count
    $totalTests = $roundTests.Count
    $successRate = if ($totalTests -gt 0) { ($successCount / $totalTests * 100) } else { 0 }
    
    Write-Host "第 $round 轮: $successCount/$totalTests ($($successRate.ToString('F1'))%)" -ForegroundColor Cyan
    $allResults += $roundTests
    
    Start-Sleep -Seconds 1
}

$testEndTime = Get-Date
$totalDuration = ($testEndTime - $testStartTime).TotalSeconds

# 生成报告
Write-Host "`n" + "="*50 -ForegroundColor Green
Write-Host "10轮稳定性测试完成报告" -ForegroundColor Green
Write-Host "="*50 -ForegroundColor Green

$totalSuccess = ($allResults | Where-Object { $_.Status -eq "成功" }).Count
$totalTests = $allResults.Count
$overallRate = if ($totalTests -gt 0) { ($totalSuccess / $totalTests * 100) } else { 0 }

Write-Host "测试时间: $($testStartTime.ToString('HH:mm:ss')) - $($testEndTime.ToString('HH:mm:ss'))" -ForegroundColor White
Write-Host "总耗时: $($totalDuration.ToString('F1')) 秒" -ForegroundColor White
Write-Host "总测试轮数: $totalRounds" -ForegroundColor White
Write-Host "总测试项目: $totalTests" -ForegroundColor White
Write-Host "成功项目: $totalSuccess" -ForegroundColor Green
Write-Host "失败项目: $($totalTests - $totalSuccess)" -ForegroundColor Red
Write-Host "整体成功率: $($overallRate.ToString('F1'))%" -ForegroundColor $(if ($overallRate -eq 100) { "Green" } else { "Yellow" })

# 功能稳定性统计
Write-Host "`n功能模块稳定性:" -ForegroundColor Cyan
$functions = $allResults | Group-Object { $_.Test } | ForEach-Object {
    $successCount = ($_.Group | Where-Object { $_.Status -eq "成功" }).Count
    $total = $_.Count
    $rate = if ($total -gt 0) { ($successCount / $total * 100) } else { 0 }
    "$($_.Name): $successCount/$total ($($rate.ToString('F1'))%)"
}
$functions | ForEach-Object { Write-Host $_ -ForegroundColor White }

$stabilityGrade = if ($overallRate -eq 100) { "A+ (完美)" }
                 elseif ($overallRate -ge 95) { "A (优秀)" }
                 elseif ($overallRate -ge 90) { "B (良好)" }
                 else { "C (需改进)" }

Write-Host "`n稳定性等级: $stabilityGrade" -ForegroundColor Green

$failures = $allResults | Where-Object { $_.Status -eq "失败" }
if ($failures.Count -gt 0) {
    Write-Host "`n失败详情:" -ForegroundColor Red
    $failures | ForEach-Object { Write-Host "第$($_.Round)轮 - $($_.Test)" -ForegroundColor Red }
} else {
    Write-Host "`n🎉 所有测试均通过！系统稳定性优秀！" -ForegroundColor Green
}

Write-Host "`n稳定性测试完成！" -ForegroundColor Green 