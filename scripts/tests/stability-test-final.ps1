#!/usr/bin/env pwsh

Write-Host "=== CourtLink 10-Round Stability Test ===" -ForegroundColor Green

$baseUrl = "http://localhost:8080"
$totalRounds = 10
$allResults = @()

$testStartTime = Get-Date
Write-Host "Test started at: $($testStartTime.ToString('yyyy-MM-dd HH:mm:ss'))" -ForegroundColor Cyan

function Test-APICall {
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
    Write-Host "`n=== Round $round Testing ===" -ForegroundColor Yellow
    $roundTests = @()
    
    # Generate test user
    $testUser = @{
        username = "testuser_r${round}_$(Get-Random -Maximum 9999)"
        email = "test_r${round}_$(Get-Random -Maximum 9999)@example.com"
        password = "testpass123"
        fullName = "Test User $round"
        phoneNumber = "1380013800$round"
    }
    
    # 1. Health Check
    Write-Host "1. Health Check..." -NoNewline
    $result = Test-APICall "Health Check" "$baseUrl/actuator/health"
    if ($result.Success) {
        Write-Host " PASS" -ForegroundColor Green
        $roundTests += @{ Test = "HealthCheck"; Status = "PASS"; Round = $round }
    } else {
        Write-Host " FAIL" -ForegroundColor Red
        $roundTests += @{ Test = "HealthCheck"; Status = "FAIL"; Round = $round }
    }
    
    # 2. User Registration
    Write-Host "2. User Registration..." -NoNewline
    $registerResult = Test-APICall "User Registration" "$baseUrl/api/users/register" "POST" $testUser
    $userId = $null
    if ($registerResult.Success) {
        $userId = $registerResult.Response.id
        Write-Host " PASS (ID: $userId)" -ForegroundColor Green
        $roundTests += @{ Test = "UserRegistration"; Status = "PASS"; Round = $round }
    } else {
        Write-Host " FAIL" -ForegroundColor Red
        $roundTests += @{ Test = "UserRegistration"; Status = "FAIL"; Round = $round }
    }
    
    if ($userId) {
        # 3. User Login
        Write-Host "3. User Login..." -NoNewline
        $loginData = @{ username = $testUser.username; password = $testUser.password }
        $loginResult = Test-APICall "User Login" "$baseUrl/api/users/login" "POST" $loginData
        if ($loginResult.Success) {
            Write-Host " PASS" -ForegroundColor Green
            $roundTests += @{ Test = "UserLogin"; Status = "PASS"; Round = $round }
        } else {
            Write-Host " FAIL" -ForegroundColor Red
            $roundTests += @{ Test = "UserLogin"; Status = "FAIL"; Round = $round }
        }
        
        # 4. Get User Info
        Write-Host "4. Get User Info..." -NoNewline
        $getUserResult = Test-APICall "Get User Info" "$baseUrl/api/users/$userId"
        if ($getUserResult.Success) {
            Write-Host " PASS" -ForegroundColor Green
            $roundTests += @{ Test = "GetUserInfo"; Status = "PASS"; Round = $round }
        } else {
            Write-Host " FAIL" -ForegroundColor Red
            $roundTests += @{ Test = "GetUserInfo"; Status = "FAIL"; Round = $round }
        }
        
        # 5. Password Validation
        Write-Host "5. Password Validation..." -NoNewline
        $passwordCheck = @{ password = $testUser.password }
        $passwordResult = Test-APICall "Password Validation" "$baseUrl/api/users/$userId/validate-password" "POST" $passwordCheck
        if ($passwordResult.Success) {
            Write-Host " PASS" -ForegroundColor Green
            $roundTests += @{ Test = "PasswordValidation"; Status = "PASS"; Round = $round }
        } else {
            Write-Host " FAIL" -ForegroundColor Red
            $roundTests += @{ Test = "PasswordValidation"; Status = "FAIL"; Round = $round }
        }
        
        # 6. User Activation
        Write-Host "6. User Activation..." -NoNewline
        $activateResult = Test-APICall "User Activation" "$baseUrl/api/users/$userId/activate" "POST"
        if ($activateResult.Success) {
            Write-Host " PASS" -ForegroundColor Green
            $roundTests += @{ Test = "UserActivation"; Status = "PASS"; Round = $round }
        } else {
            Write-Host " FAIL" -ForegroundColor Red
            $roundTests += @{ Test = "UserActivation"; Status = "FAIL"; Round = $round }
        }
        
        # 7. User Deactivation
        Write-Host "7. User Deactivation..." -NoNewline
        $deactivateResult = Test-APICall "User Deactivation" "$baseUrl/api/users/$userId/deactivate" "POST"
        if ($deactivateResult.Success) {
            Write-Host " PASS" -ForegroundColor Green
            $roundTests += @{ Test = "UserDeactivation"; Status = "PASS"; Round = $round }
        } else {
            Write-Host " FAIL" -ForegroundColor Red
            $roundTests += @{ Test = "UserDeactivation"; Status = "FAIL"; Round = $round }
        }
        
        # Reactivate for cleanup
        Test-APICall "Reactivate User" "$baseUrl/api/users/$userId/activate" "POST" | Out-Null
        
        # 8. Delete User
        Write-Host "8. Delete User..." -NoNewline
        $deleteResult = Test-APICall "Delete User" "$baseUrl/api/users/$userId" "DELETE"
        if ($deleteResult.Success) {
            Write-Host " PASS" -ForegroundColor Green
            $roundTests += @{ Test = "DeleteUser"; Status = "PASS"; Round = $round }
        } else {
            Write-Host " FAIL" -ForegroundColor Red
            $roundTests += @{ Test = "DeleteUser"; Status = "FAIL"; Round = $round }
        }
    }
    
    # 9. Exception Test - Non-existent User
    Write-Host "9. Exception Test - Invalid User..." -NoNewline
    $invalidResult = Test-APICall "Invalid User Query" "$baseUrl/api/users/999999" "GET" $null 400
    if ($invalidResult.Success) {
        Write-Host " PASS" -ForegroundColor Green
        $roundTests += @{ Test = "ExceptionTest-InvalidUser"; Status = "PASS"; Round = $round }
    } else {
        Write-Host " FAIL" -ForegroundColor Red
        $roundTests += @{ Test = "ExceptionTest-InvalidUser"; Status = "FAIL"; Round = $round }
    }
    
    # 10. Exception Test - Invalid Registration
    Write-Host "10. Exception Test - Invalid Registration..." -NoNewline
    $invalidUser = @{ username = ""; email = "invalid"; password = "" }
    $invalidRegResult = Test-APICall "Invalid Registration" "$baseUrl/api/users/register" "POST" $invalidUser 400
    if ($invalidRegResult.Success) {
        Write-Host " PASS" -ForegroundColor Green
        $roundTests += @{ Test = "ExceptionTest-InvalidReg"; Status = "PASS"; Round = $round }
    } else {
        Write-Host " FAIL" -ForegroundColor Red
        $roundTests += @{ Test = "ExceptionTest-InvalidReg"; Status = "FAIL"; Round = $round }
    }
    
    $passCount = ($roundTests | Where-Object { $_.Status -eq "PASS" }).Count
    $totalTests = $roundTests.Count
    $passRate = if ($totalTests -gt 0) { ($passCount / $totalTests * 100) } else { 0 }
    
    Write-Host "Round ${round}: $passCount/$totalTests ($($passRate.ToString('F1'))% pass rate)" -ForegroundColor Cyan
    $allResults += $roundTests
    
    Start-Sleep -Seconds 1
}

$testEndTime = Get-Date
$totalDuration = ($testEndTime - $testStartTime).TotalSeconds

# Generate Report
Write-Host "`n" + "="*60 -ForegroundColor Green
Write-Host "10-Round Stability Test Report" -ForegroundColor Green
Write-Host "="*60 -ForegroundColor Green

$totalPass = ($allResults | Where-Object { $_.Status -eq "PASS" }).Count
$totalTests = $allResults.Count
$overallRate = if ($totalTests -gt 0) { ($totalPass / $totalTests * 100) } else { 0 }

Write-Host "Test Period: $($testStartTime.ToString('HH:mm:ss')) - $($testEndTime.ToString('HH:mm:ss'))" -ForegroundColor White
Write-Host "Total Duration: $($totalDuration.ToString('F1')) seconds" -ForegroundColor White
Write-Host "Total Rounds: $totalRounds" -ForegroundColor White
Write-Host "Total Test Cases: $totalTests" -ForegroundColor White
Write-Host "Passed Tests: $totalPass" -ForegroundColor Green
Write-Host "Failed Tests: $($totalTests - $totalPass)" -ForegroundColor Red
$passRateColor = if ($overallRate -eq 100) { "Green" } else { "Yellow" }
Write-Host "Overall Pass Rate: $($overallRate.ToString('F1'))%" -ForegroundColor $passRateColor

# Function Stability Analysis
Write-Host "`nFunction Stability Analysis:" -ForegroundColor Cyan
$functions = $allResults | Group-Object { $_.Test } | ForEach-Object {
    $passCount = ($_.Group | Where-Object { $_.Status -eq "PASS" }).Count
    $total = $_.Count
    $rate = if ($total -gt 0) { ($passCount / $total * 100) } else { 0 }
    "$($_.Name): $passCount/$total ($($rate.ToString('F1'))%)"
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
    $failures | ForEach-Object { Write-Host "Round $($_.Round) - $($_.Test)" -ForegroundColor Red }
} else {
    Write-Host "`nExcellent! All tests passed. System is highly stable!" -ForegroundColor Green
}

# Save results to JSON
$reportData = @{
    TestInfo = @{
        StartTime = $testStartTime
        EndTime = $testEndTime
        Duration = $totalDuration
        Rounds = $totalRounds
    }
    OverallStats = @{
        TotalTests = $totalTests
        PassedTests = $totalPass
        FailedTests = ($totalTests - $totalPass)
        PassRate = $overallRate
        StabilityGrade = $stabilityGrade
    }
    DetailedResults = $allResults
}

$reportData | ConvertTo-Json -Depth 3 | Out-File "stability-test-results.json" -Encoding UTF8
Write-Host "`nDetailed results saved to: stability-test-results.json" -ForegroundColor Cyan
Write-Host "`nStability Test Completed!" -ForegroundColor Green 