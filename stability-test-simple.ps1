#!/usr/bin/env pwsh
# 10-Round Stability Test for CourtLink User Management System

$BASE_URL = "http://localhost:8080"
$ROUNDS = 10
$testResults = @{
    totalTests = 0
    passedTests = 0
    failedTests = 0
    roundResults = @()
    errors = @()
}

function Write-Status($text, $color) {
    Write-Host $text -ForegroundColor $color
}

function Test-Api {
    param(
        [string]$TestName,
        [string]$Method = "GET",
        [string]$Endpoint,
        [hashtable]$Body = $null,
        [int]$ExpectedStatus = 200
    )
    
    $url = "$BASE_URL$Endpoint"
    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    
    try {
        $params = @{
            Method = $Method
            Uri = $url
            UseBasicParsing = $true
            TimeoutSec = 30
            Headers = @{"Content-Type" = "application/json"}
        }
        
        if ($Body) {
            $params.Body = ($Body | ConvertTo-Json -Depth 10)
        }
        
        $response = Invoke-WebRequest @params
        $stopwatch.Stop()
        
        $script:testResults.totalTests++
        
        if ($response.StatusCode -eq $ExpectedStatus) {
            $script:testResults.passedTests++
            Write-Status "  [PASS] $TestName - $($stopwatch.ElapsedMilliseconds)ms" "Green"
            return @{ Success = $true; StatusCode = $response.StatusCode; ResponseTime = $stopwatch.ElapsedMilliseconds }
        } else {
            $script:testResults.failedTests++
            Write-Status "  [FAIL] $TestName - Status: $($response.StatusCode)" "Red"
            return @{ Success = $false; StatusCode = $response.StatusCode; ResponseTime = $stopwatch.ElapsedMilliseconds }
        }
    }
    catch {
        $stopwatch.Stop()
        $script:testResults.totalTests++
        $script:testResults.failedTests++
        Write-Status "  [ERROR] $TestName - $($_.Exception.Message)" "Red"
        $script:testResults.errors += "$TestName - $($_.Exception.Message)"
        return @{ Success = $false; Error = $_.Exception.Message; ResponseTime = $stopwatch.ElapsedMilliseconds }
    }
}

function Clear-TestUsers {
    try {
        $users = Invoke-WebRequest -Uri "$BASE_URL/api/users/list" -UseBasicParsing | ConvertFrom-Json
        foreach ($user in $users) {
            if ($user.username -like "testuser*") {
                Invoke-WebRequest -Method DELETE -Uri "$BASE_URL/api/users/$($user.id)" -UseBasicParsing | Out-Null
            }
        }
    }
    catch {
        # Ignore cleanup errors
    }
}

function Run-TestRound($roundNum) {
    Write-Status "`n=== Round $roundNum Testing ===" "Cyan"
    
    $roundTests = @()
    
    # Health checks
    $roundTests += Test-Api "Health Status" "GET" "/api/health/status"
    $roundTests += Test-Api "Health Ping" "GET" "/api/health/ping"
    $roundTests += Test-Api "Health Info" "GET" "/api/health/info"
    
    # User registration
    $user1 = @{
        username = "testuser1"
        email = "testuser1@test.com"
        password = "password123"
        name = "Test User 1"
        phone = "13800138001"
    }
    
    $user2 = @{
        username = "testuser2"
        email = "testuser2@test.com"
        password = "password456"
        name = "Test User 2"
        phone = "13800138002"
    }
    
    $roundTests += Test-Api "User Registration 1" "POST" "/api/users/register" $user1
    $roundTests += Test-Api "User Registration 2" "POST" "/api/users/register" $user2
    
    # User login
    $login1 = @{ username = $user1.username; password = $user1.password }
    $login2 = @{ username = $user2.username; password = $user2.password }
    
    $roundTests += Test-Api "User Login 1" "POST" "/api/users/login" $login1
    $roundTests += Test-Api "User Login 2" "POST" "/api/users/login" $login2
    
    # User queries
    $roundTests += Test-Api "Get Users List" "GET" "/api/users/list"
    $roundTests += Test-Api "Check Username Exists" "GET" "/api/users/check-username?username=$($user1.username)"
    $roundTests += Test-Api "Check Username Not Exists" "GET" "/api/users/check-username?username=nonexistent"
    $roundTests += Test-Api "Check Email Exists" "GET" "/api/users/check-email?email=$($user1.email)"
    $roundTests += Test-Api "Check Email Not Exists" "GET" "/api/users/check-email?email=nonexistent@test.com"
    $roundTests += Test-Api "Get User by Username" "GET" "/api/users/username/$($user1.username)"
    $roundTests += Test-Api "Get User by Email" "GET" "/api/users/email/$($user1.email)"
    
    # Calculate round statistics
    $passed = ($roundTests | Where-Object { $_.Success }).Count
    $total = $roundTests.Count
    $avgTime = ($roundTests | Measure-Object -Property ResponseTime -Average).Average
    
    $roundResult = @{
        Round = $roundNum
        Total = $total
        Passed = $passed
        Failed = $total - $passed
        SuccessRate = [math]::Round(($passed / $total) * 100, 2)
        AvgResponseTime = [math]::Round($avgTime, 2)
    }
    
    $script:testResults.roundResults += $roundResult
    $successRateStr = $roundResult.SuccessRate.ToString() + "%"
    Write-Status "Round $roundNum: $passed/$total passed ($successRateStr)" "Yellow"
    
    # Cleanup
    Clear-TestUsers
    Start-Sleep -Seconds 1
    
    return $roundResult
}

# Main execution
Write-Status "Starting 10-Round Stability Test for CourtLink" "Magenta"
Write-Status "Target: $BASE_URL" "Yellow"

$startTime = Get-Date

# Initial cleanup
Clear-TestUsers

# Run all rounds
for ($i = 1; $i -le $ROUNDS; $i++) {
    try {
        Run-TestRound $i
        if ($i -lt $ROUNDS) {
            Start-Sleep -Seconds 2
        }
    }
    catch {
        Write-Status "Round $i failed with exception: $($_.Exception.Message)" "Red"
        $script:testResults.errors += "Round $i exception: $($_.Exception.Message)"
    }
}

# Generate final report
$totalTime = [math]::Round(((Get-Date) - $startTime).TotalSeconds, 2)
$overallSuccess = [math]::Round(($script:testResults.passedTests / $script:testResults.totalTests) * 100, 2)
$avgResponseTime = ($script:testResults.roundResults | Measure-Object -Property AvgResponseTime -Average).Average

Write-Status "`n=============== FINAL REPORT ===============" "White"
Write-Status "Total Tests: $($script:testResults.totalTests)" "White"
Write-Status "Passed: $($script:testResults.passedTests)" "Green"
Write-Status "Failed: $($script:testResults.failedTests)" "Red"
Write-Status "Overall Success Rate: $overallSuccess%" "Yellow"
Write-Status "Average Response Time: $([math]::Round($avgResponseTime, 2))ms" "Yellow"
Write-Status "Total Test Duration: ${totalTime}s" "Yellow"

Write-Status "`nRound Details:" "White"
foreach ($round in $script:testResults.roundResults) {
    Write-Status "  Round $($round.Round): $($round.Passed)/$($round.Total) ($($round.SuccessRate)%) - Avg: $($round.AvgResponseTime)ms" "Gray"
}

if ($script:testResults.errors.Count -gt 0) {
    Write-Status "`nErrors:" "Red"
    foreach ($error in $script:testResults.errors) {
        Write-Status "  - $error" "Red"
    }
}

# Stability assessment
Write-Status "`nStability Assessment:" "White"
if ($overallSuccess -ge 95) {
    Write-Status "EXCELLENT - System is highly stable" "Green"
} elseif ($overallSuccess -ge 85) {
    Write-Status "GOOD - System is mostly stable with minor issues" "Yellow"
} elseif ($overallSuccess -ge 70) {
    Write-Status "FAIR - System has some stability concerns" "Yellow"
} else {
    Write-Status "POOR - System has significant stability issues" "Red"
}

# Save detailed results
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$jsonResults = @{
    summary = @{
        totalTests = $script:testResults.totalTests
        passedTests = $script:testResults.passedTests
        failedTests = $script:testResults.failedTests
        successRate = $overallSuccess
        totalTime = $totalTime
        avgResponseTime = [math]::Round($avgResponseTime, 2)
        timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    }
    rounds = $script:testResults.roundResults
    errors = $script:testResults.errors
}

$jsonFile = "stability-test-results-$timestamp.json"
$jsonResults | ConvertTo-Json -Depth 10 | Out-File -FilePath $jsonFile -Encoding UTF8
Write-Status "`nResults saved to: $jsonFile" "Green"

Write-Status "============================================" "White"

# Exit with appropriate code
if ($script:testResults.failedTests -eq 0) {
    Write-Status "All tests passed! System is stable." "Green"
    exit 0
} else {
    Write-Status "Some tests failed. Please review system status." "Yellow"
    exit 1
} 