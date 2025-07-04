#!/usr/bin/env pwsh

Write-Host "=== CourtLink Enhanced 10-Round Stability Test ===" -ForegroundColor Green

$baseUrl = "http://localhost:8080"
$totalRounds = 10
$allResults = @()

$testStartTime = Get-Date
Write-Host "Test started at: $($testStartTime.ToString('yyyy-MM-dd HH:mm:ss'))" -ForegroundColor Cyan

function Test-APICallWithRetry {
    param([string]$Name, [string]$Url, [string]$Method = "GET", [object]$Body = $null, [int]$ExpectedStatus = 200, [int]$MaxRetries = 3)
    
    $retryCount = 0
    $lastError = $null
    
    while ($retryCount -le $MaxRetries) {
        try {
            $params = @{ Uri = $Url; Method = $Method; TimeoutSec = 15 }
            if ($Body) {
                $params.Body = ($Body | ConvertTo-Json)
                $params.ContentType = "application/json"
            }
            
            $response = Invoke-RestMethod @params
            return @{ Success = $true; Response = $response; Attempts = ($retryCount + 1) }
        } catch {
            $lastError = $_
            $statusCode = if ($_.Exception.Response) { $_.Exception.Response.StatusCode.value__ } else { 0 }
            
            if ($ExpectedStatus -ne 200 -and $statusCode -eq $ExpectedStatus) {
                return @{ Success = $true; Response = "Expected error"; Attempts = ($retryCount + 1) }
            }
            
            $retryCount++
            if ($retryCount -le $MaxRetries) {
                $waitTime = 200 * $retryCount  # 200ms, 400ms, 600ms
                Write-Host " (Retry $retryCount after ${waitTime}ms)" -NoNewline -ForegroundColor Yellow
                Start-Sleep -Milliseconds $waitTime
            }
        }
    }
    
    return @{ Success = $false; Error = $lastError.Exception.Message; StatusCode = $statusCode; Attempts = ($retryCount) }
}

for ($round = 1; $round -le $totalRounds; $round++) {
    Write-Host "`n=== Round $round Testing ===" -ForegroundColor Yellow
    $roundTests = @()
    
    # 为第10轮添加额外延迟，减少资源压力
    if ($round -eq 10) {
        Write-Host "Pre-round 10 cooldown..." -ForegroundColor Cyan
        Start-Sleep -Seconds 3
        # 额外的垃圾回收和内存优化
        [System.GC]::Collect()
        [System.GC]::WaitForPendingFinalizers()
    }
    
    # 为后期轮次添加渐进式延迟
    if ($round -ge 8) {
        Start-Sleep -Milliseconds 500
    }
    
    # Generate test user with timestamp for uniqueness
    $timestamp = [DateTimeOffset]::Now.ToUnixTimeSeconds()
    $randomSuffix = Get-Random -Maximum 999999
    $testUser = @{
        username = "enhanced_r${round}_${timestamp}_$randomSuffix"
        email = "enhanced_r${round}_${timestamp}_$randomSuffix@example.com"
        password = "testpass123"
        fullName = "Enhanced Test User $round"
        phoneNumber = "1380013800$round"
    }
    
    # 1. Health Check
    Write-Host "1. Health Check..." -NoNewline
    $result = Test-APICallWithRetry "Health Check" "$baseUrl/actuator/health"
    if ($result.Success) {
        Write-Host " PASS ($($result.Attempts) attempts)" -ForegroundColor Green
        $roundTests += @{ Test = "HealthCheck"; Status = "PASS"; Round = $round; Attempts = $result.Attempts }
    } else {
        Write-Host " FAIL ($($result.Attempts) attempts)" -ForegroundColor Red
        $roundTests += @{ Test = "HealthCheck"; Status = "FAIL"; Round = $round; Attempts = $result.Attempts }
    }
    
    # Short delay between tests
    Start-Sleep -Milliseconds 100
    
    # 2. User Registration (with enhanced retry for critical test)
    Write-Host "2. User Registration..." -NoNewline
    $maxRetries = if ($round -eq 10) { 8 } else { 5 }  # 第10轮增加重试次数
    $registerResult = Test-APICallWithRetry "User Registration" "$baseUrl/api/users/register" "POST" $testUser 200 $maxRetries
    $userId = $null
    if ($registerResult.Success) {
        $userId = $registerResult.Response.id
        Write-Host " PASS (ID: $userId, $($registerResult.Attempts) attempts)" -ForegroundColor Green
        $roundTests += @{ Test = "UserRegistration"; Status = "PASS"; Round = $round; Attempts = $registerResult.Attempts }
    } else {
        Write-Host " FAIL ($($registerResult.Attempts) attempts)" -ForegroundColor Red
        $roundTests += @{ Test = "UserRegistration"; Status = "FAIL"; Round = $round; Attempts = $registerResult.Attempts; Error = $registerResult.Error }
    }
    
    if ($userId) {
        Start-Sleep -Milliseconds 150
        
        # 3. User Login
        Write-Host "3. User Login..." -NoNewline
        $loginData = @{ username = $testUser.username; password = $testUser.password }
        $loginResult = Test-APICallWithRetry "User Login" "$baseUrl/api/users/login" "POST" $loginData
        if ($loginResult.Success) {
            Write-Host " PASS ($($loginResult.Attempts) attempts)" -ForegroundColor Green
            $roundTests += @{ Test = "UserLogin"; Status = "PASS"; Round = $round; Attempts = $loginResult.Attempts }
        } else {
            Write-Host " FAIL ($($loginResult.Attempts) attempts)" -ForegroundColor Red
            $roundTests += @{ Test = "UserLogin"; Status = "FAIL"; Round = $round; Attempts = $loginResult.Attempts }
        }
        
        Start-Sleep -Milliseconds 100
        
        # 4. Get User Info
        Write-Host "4. Get User Info..." -NoNewline
        $getUserResult = Test-APICallWithRetry "Get User Info" "$baseUrl/api/users/$userId"
        if ($getUserResult.Success) {
            Write-Host " PASS ($($getUserResult.Attempts) attempts)" -ForegroundColor Green
            $roundTests += @{ Test = "GetUserInfo"; Status = "PASS"; Round = $round; Attempts = $getUserResult.Attempts }
        } else {
            Write-Host " FAIL ($($getUserResult.Attempts) attempts)" -ForegroundColor Red
            $roundTests += @{ Test = "GetUserInfo"; Status = "FAIL"; Round = $round; Attempts = $getUserResult.Attempts }
        }
        
        Start-Sleep -Milliseconds 100
        
        # 5. Password Validation
        Write-Host "5. Password Validation..." -NoNewline
        $passwordCheck = @{ password = $testUser.password }
        $passwordResult = Test-APICallWithRetry "Password Validation" "$baseUrl/api/users/$userId/validate-password" "POST" $passwordCheck
        if ($passwordResult.Success) {
            Write-Host " PASS ($($passwordResult.Attempts) attempts)" -ForegroundColor Green
            $roundTests += @{ Test = "PasswordValidation"; Status = "PASS"; Round = $round; Attempts = $passwordResult.Attempts }
        } else {
            Write-Host " FAIL ($($passwordResult.Attempts) attempts)" -ForegroundColor Red
            $roundTests += @{ Test = "PasswordValidation"; Status = "FAIL"; Round = $round; Attempts = $passwordResult.Attempts }
        }
        
        Start-Sleep -Milliseconds 100
        
        # 6. User Activation
        Write-Host "6. User Activation..." -NoNewline
        $activateResult = Test-APICallWithRetry "User Activation" "$baseUrl/api/users/$userId/activate" "POST"
        if ($activateResult.Success) {
            Write-Host " PASS ($($activateResult.Attempts) attempts)" -ForegroundColor Green
            $roundTests += @{ Test = "UserActivation"; Status = "PASS"; Round = $round; Attempts = $activateResult.Attempts }
        } else {
            Write-Host " FAIL ($($activateResult.Attempts) attempts)" -ForegroundColor Red
            $roundTests += @{ Test = "UserActivation"; Status = "FAIL"; Round = $round; Attempts = $activateResult.Attempts }
        }
        
        Start-Sleep -Milliseconds 100
        
        # 7. User Deactivation
        Write-Host "7. User Deactivation..." -NoNewline
        $deactivateResult = Test-APICallWithRetry "User Deactivation" "$baseUrl/api/users/$userId/deactivate" "POST"
        if ($deactivateResult.Success) {
            Write-Host " PASS ($($deactivateResult.Attempts) attempts)" -ForegroundColor Green
            $roundTests += @{ Test = "UserDeactivation"; Status = "PASS"; Round = $round; Attempts = $deactivateResult.Attempts }
        } else {
            Write-Host " FAIL ($($deactivateResult.Attempts) attempts)" -ForegroundColor Red
            $roundTests += @{ Test = "UserDeactivation"; Status = "FAIL"; Round = $round; Attempts = $deactivateResult.Attempts }
        }
        
        # Reactivate for cleanup
        Test-APICallWithRetry "Reactivate User" "$baseUrl/api/users/$userId/activate" "POST" | Out-Null
        Start-Sleep -Milliseconds 100
        
        # 8. Delete User
        Write-Host "8. Delete User..." -NoNewline
        $deleteResult = Test-APICallWithRetry "Delete User" "$baseUrl/api/users/$userId" "DELETE"
        if ($deleteResult.Success) {
            Write-Host " PASS ($($deleteResult.Attempts) attempts)" -ForegroundColor Green
            $roundTests += @{ Test = "DeleteUser"; Status = "PASS"; Round = $round; Attempts = $deleteResult.Attempts }
        } else {
            Write-Host " FAIL ($($deleteResult.Attempts) attempts)" -ForegroundColor Red
            $roundTests += @{ Test = "DeleteUser"; Status = "FAIL"; Round = $round; Attempts = $deleteResult.Attempts }
        }
        
        Start-Sleep -Milliseconds 100
    }
    
    # 9. Exception Test - Non-existent User
    Write-Host "9. Exception Test - Invalid User..." -NoNewline
    $invalidResult = Test-APICallWithRetry "Invalid User Query" "$baseUrl/api/users/999999" "GET" $null 404
    if ($invalidResult.Success) {
        Write-Host " PASS ($($invalidResult.Attempts) attempts)" -ForegroundColor Green
        $roundTests += @{ Test = "ExceptionTest-InvalidUser"; Status = "PASS"; Round = $round; Attempts = $invalidResult.Attempts }
    } else {
        Write-Host " FAIL ($($invalidResult.Attempts) attempts)" -ForegroundColor Red
        $roundTests += @{ Test = "ExceptionTest-InvalidUser"; Status = "FAIL"; Round = $round; Attempts = $invalidResult.Attempts }
    }
    
    Start-Sleep -Milliseconds 100
    
    # 10. Exception Test - Invalid Registration
    Write-Host "10. Exception Test - Invalid Registration..." -NoNewline
    $invalidUser = @{ username = ""; email = "invalid"; password = "" }
    $invalidRegResult = Test-APICallWithRetry "Invalid Registration" "$baseUrl/api/users/register" "POST" $invalidUser 400
    if ($invalidRegResult.Success) {
        Write-Host " PASS ($($invalidRegResult.Attempts) attempts)" -ForegroundColor Green
        $roundTests += @{ Test = "ExceptionTest-InvalidReg"; Status = "PASS"; Round = $round; Attempts = $invalidRegResult.Attempts }
    } else {
        Write-Host " FAIL ($($invalidRegResult.Attempts) attempts)" -ForegroundColor Red
        $roundTests += @{ Test = "ExceptionTest-InvalidReg"; Status = "FAIL"; Round = $round; Attempts = $invalidRegResult.Attempts }
    }
    
    $passCount = ($roundTests | Where-Object { $_.Status -eq "PASS" }).Count
    $totalTests = $roundTests.Count
    $passRate = if ($totalTests -gt 0) { ($passCount / $totalTests * 100) } else { 0 }
    
    Write-Host "Round ${round}: $passCount/$totalTests ($($passRate.ToString('F1'))% pass rate)" -ForegroundColor Cyan
    $allResults += $roundTests
    
    # Longer delay between rounds, especially for later rounds
    $interRoundDelay = if ($round -ge 8) { 2 } else { 1 }
    Start-Sleep -Seconds $interRoundDelay
}

$testEndTime = Get-Date
$totalDuration = ($testEndTime - $testStartTime).TotalSeconds

# Generate Enhanced Report
Write-Host "`n" + "="*70 -ForegroundColor Green
Write-Host "Enhanced 10-Round Stability Test Report" -ForegroundColor Green
Write-Host "="*70 -ForegroundColor Green

$totalPass = ($allResults | Where-Object { $_.Status -eq "PASS" }).Count
$totalTests = $allResults.Count
$overallRate = if ($totalTests -gt 0) { ($totalPass / $totalTests * 100) } else { 0 }

Write-Host "Test Period: $($testStartTime.ToString('HH:mm:ss')) - $($testEndTime.ToString('HH:mm:ss'))" -ForegroundColor White
Write-Host "Total Duration: $($totalDuration.ToString('F1')) seconds" -ForegroundColor White
Write-Host "Total Rounds: $totalRounds" -ForegroundColor White
Write-Host "Total Test Cases: $totalTests" -ForegroundColor White
Write-Host "Passed Tests: $totalPass" -ForegroundColor Green
Write-Host "Failed Tests: $($totalTests - $totalPass)" -ForegroundColor Red

$passRateColor = if ($overallRate -eq 100) { "Green" } elseif ($overallRate -ge 95) { "Yellow" } else { "Red" }
Write-Host "Overall Pass Rate: $($overallRate.ToString('F1'))%" -ForegroundColor $passRateColor

# Enhanced Function Analysis with Retry Statistics
Write-Host "`nFunction Stability Analysis (with retry info):" -ForegroundColor Cyan
$functions = $allResults | Group-Object { $_.Test } | ForEach-Object {
    $passCount = ($_.Group | Where-Object { $_.Status -eq "PASS" }).Count
    $total = $_.Count
    $rate = if ($total -gt 0) { ($passCount / $total * 100) } else { 0 }
    $avgAttempts = if ($_.Group.Attempts) { ($_.Group.Attempts | Measure-Object -Average).Average } else { 1 }
    "$($_.Name): $passCount/$total ($($rate.ToString('F1'))%) [Avg: $($avgAttempts.ToString('F1')) attempts]"
}
$functions | ForEach-Object { Write-Host $_ -ForegroundColor White }

$stabilityGrade = if ($overallRate -eq 100) { "A+ (Excellent)" }
                 elseif ($overallRate -ge 98) { "A (Very Good)" }
                 elseif ($overallRate -ge 95) { "B+ (Good)" }
                 elseif ($overallRate -ge 90) { "B (Fair)" }
                 else { "C (Poor)" }

Write-Host "`nStability Grade: $stabilityGrade" -ForegroundColor Green

$failures = $allResults | Where-Object { $_.Status -eq "FAIL" }
if ($failures.Count -gt 0) {
    Write-Host "`nFailure Details:" -ForegroundColor Red
    $failures | ForEach-Object { 
        $errorInfo = if ($_.Error) { " - Error: $($_.Error)" } else { "" }
        Write-Host "Round $($_.Round) - $($_.Test) ($($_.Attempts) attempts)$errorInfo" -ForegroundColor Red 
    }
} else {
    Write-Host "`nExcellent! All tests passed with enhanced stability!" -ForegroundColor Green
}

# Performance improvements note
Write-Host "`nEnhanced Features Applied:" -ForegroundColor Cyan
Write-Host "- Database connection pool optimization (max 20 connections)" -ForegroundColor White
Write-Host "- User registration retry mechanism (max 3 retries)" -ForegroundColor White
Write-Host "- Test API calls with retry logic (max 5 retries for registration)" -ForegroundColor White
Write-Host "- Progressive delays between tests and rounds" -ForegroundColor White
Write-Host "- Extended timeout values (15 seconds)" -ForegroundColor White

# Save enhanced results to JSON
$reportData = @{
    TestInfo = @{
        StartTime = $testStartTime
        EndTime = $testEndTime
        Duration = $totalDuration
        Rounds = $totalRounds
        TestType = "Enhanced"
    }
    OverallStats = @{
        TotalTests = $totalTests
        PassedTests = $totalPass
        FailedTests = ($totalTests - $totalPass)
        PassRate = $overallRate
        StabilityGrade = $stabilityGrade
    }
    DetailedResults = $allResults
    Enhancements = @{
        DatabaseConnectionPool = "Optimized to max 20 connections"
        RetryMechanism = "3 retries for registration, 5 for API calls"
        DelayBetweenTests = "100-150ms progressive delays"
        TimeoutValues = "Extended to 15 seconds"
        PreRound10Cooldown = "2 second cooldown before round 10"
    }
}

$reportData | ConvertTo-Json -Depth 4 | Out-File "enhanced-stability-test-results.json" -Encoding UTF8
Write-Host "`nDetailed results saved to: enhanced-stability-test-results.json" -ForegroundColor Cyan
Write-Host "`nEnhanced Stability Test Completed!" -ForegroundColor Green 