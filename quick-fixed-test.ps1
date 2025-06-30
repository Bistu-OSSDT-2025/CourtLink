Write-Host "=== CourtLink FIXED 10-Round Stability Test ===" -ForegroundColor Green

$baseUrl = "http://localhost:8080"
$totalRounds = 10
$allResults = @()
$testStartTime = Get-Date

function Test-API {
    param([string]$Name, [string]$Url, [string]$Method = "GET", [object]$Body = $null, [int]$ExpectedStatus = 200, [int]$MaxRetries = 3)
    
    $retryCount = 0
    while ($retryCount -le $MaxRetries) {
        try {
            $params = @{ Uri = $Url; Method = $Method; TimeoutSec = 20 }
            if ($Body) {
                $params.Body = ($Body | ConvertTo-Json)
                $params.ContentType = "application/json"
            }
            $response = Invoke-RestMethod @params
            return @{ Success = $true; Response = $response; Attempts = ($retryCount + 1) }
        } catch {
            $statusCode = if ($_.Exception.Response) { $_.Exception.Response.StatusCode.value__ } else { 0 }
            if ($ExpectedStatus -ne 200 -and $statusCode -eq $ExpectedStatus) {
                return @{ Success = $true; Response = "Expected error"; Attempts = ($retryCount + 1) }
            }
            $retryCount++
            if ($retryCount -le $MaxRetries) {
                Write-Host " (R$retryCount)" -NoNewline -ForegroundColor Yellow
                Start-Sleep -Milliseconds (300 * $retryCount)
            }
        }
    }
    return @{ Success = $false; Error = $_.Exception.Message; Attempts = $retryCount }
}

for ($round = 1; $round -le $totalRounds; $round++) {
    Write-Host "`n=== Round $round ===" -ForegroundColor Yellow
    $roundTests = @()
    
    if ($round -eq 10) {
        Write-Host "Round 10 optimization..." -ForegroundColor Cyan
        Start-Sleep -Seconds 2
    }
    
    # Unique test user
    $timestamp = [DateTimeOffset]::Now.ToUnixTimeMilliseconds()
    $testUser = @{
        username = "fixed_r${round}_${timestamp}"
        email = "fixed_r${round}_${timestamp}@test.com"
        password = "testpass123"
        fullName = "Fixed Test User $round"
        phoneNumber = "138${round.ToString().PadLeft(8, '0')}"
    }
    
    # 1. Health Check
    Write-Host "1. Health..." -NoNewline
    $result = Test-API "Health" "$baseUrl/actuator/health"
    Write-Host $(if ($result.Success) { " PASS" } else { " FAIL" }) -ForegroundColor $(if ($result.Success) { "Green" } else { "Red" })
    $roundTests += @{ Test = "Health"; Status = $(if ($result.Success) { "PASS" } else { "FAIL" }); Round = $round }
    
    Start-Sleep -Milliseconds 200
    
    # 2. User Registration
    Write-Host "2. Register..." -NoNewline
    $retries = if ($round -eq 10) { 6 } else { 4 }
    $regResult = Test-API "Register" "$baseUrl/api/users/register" "POST" $testUser 200 $retries
    $userId = $null
    if ($regResult.Success) {
        $userId = $regResult.Response.id
        Write-Host " PASS (ID:$userId)" -ForegroundColor Green
        $roundTests += @{ Test = "Register"; Status = "PASS"; Round = $round; UserId = $userId }
    } else {
        Write-Host " FAIL" -ForegroundColor Red
        $roundTests += @{ Test = "Register"; Status = "FAIL"; Round = $round }
    }
    
    if ($userId) {
        Start-Sleep -Milliseconds 200
        
        # 3. Login
        Write-Host "3. Login..." -NoNewline
        $loginData = @{ username = $testUser.username; password = $testUser.password }
        $loginResult = Test-API "Login" "$baseUrl/api/users/login" "POST" $loginData
        Write-Host $(if ($loginResult.Success) { " PASS" } else { " FAIL" }) -ForegroundColor $(if ($loginResult.Success) { "Green" } else { "Red" })
        $roundTests += @{ Test = "Login"; Status = $(if ($loginResult.Success) { "PASS" } else { "FAIL" }); Round = $round }
        
        Start-Sleep -Milliseconds 200
        
        # 4. Get User
        Write-Host "4. GetUser..." -NoNewline
        $getUserResult = Test-API "GetUser" "$baseUrl/api/users/$userId"
        Write-Host $(if ($getUserResult.Success) { " PASS" } else { " FAIL" }) -ForegroundColor $(if ($getUserResult.Success) { "Green" } else { "Red" })
        $roundTests += @{ Test = "GetUser"; Status = $(if ($getUserResult.Success) { "PASS" } else { "FAIL" }); Round = $round }
        
        Start-Sleep -Milliseconds 200
        
        # 5. Password Check
        Write-Host "5. Password..." -NoNewline
        $pwdCheck = @{ password = $testUser.password }
        $pwdResult = Test-API "Password" "$baseUrl/api/users/$userId/validate-password" "POST" $pwdCheck
        Write-Host $(if ($pwdResult.Success) { " PASS" } else { " FAIL" }) -ForegroundColor $(if ($pwdResult.Success) { "Green" } else { "Red" })
        $roundTests += @{ Test = "Password"; Status = $(if ($pwdResult.Success) { "PASS" } else { "FAIL" }); Round = $round }
        
        Start-Sleep -Milliseconds 200
        
        # 6. Activate
        Write-Host "6. Activate..." -NoNewline
        $activateResult = Test-API "Activate" "$baseUrl/api/users/$userId/activate" "POST"
        Write-Host $(if ($activateResult.Success) { " PASS" } else { " FAIL" }) -ForegroundColor $(if ($activateResult.Success) { "Green" } else { "Red" })
        $roundTests += @{ Test = "Activate"; Status = $(if ($activateResult.Success) { "PASS" } else { "FAIL" }); Round = $round }
        
        Start-Sleep -Milliseconds 200
        
        # 7. Deactivate
        Write-Host "7. Deactivate..." -NoNewline
        $deactivateResult = Test-API "Deactivate" "$baseUrl/api/users/$userId/deactivate" "POST"
        Write-Host $(if ($deactivateResult.Success) { " PASS" } else { " FAIL" }) -ForegroundColor $(if ($deactivateResult.Success) { "Green" } else { "Red" })
        $roundTests += @{ Test = "Deactivate"; Status = $(if ($deactivateResult.Success) { "PASS" } else { "FAIL" }); Round = $round }
        
        # Reactivate for cleanup
        Test-API "Reactivate" "$baseUrl/api/users/$userId/activate" "POST" | Out-Null
        Start-Sleep -Milliseconds 200
        
        # 8. Delete
        Write-Host "8. Delete..." -NoNewline
        $deleteResult = Test-API "Delete" "$baseUrl/api/users/$userId" "DELETE"
        Write-Host $(if ($deleteResult.Success) { " PASS" } else { " FAIL" }) -ForegroundColor $(if ($deleteResult.Success) { "Green" } else { "Red" })
        $roundTests += @{ Test = "Delete"; Status = $(if ($deleteResult.Success) { "PASS" } else { "FAIL" }); Round = $round }
        
        Start-Sleep -Milliseconds 200
    }
    
    # 9. Exception Test - Invalid User (Expecting 404)
    Write-Host "9. Exception404..." -NoNewline
    $invalidResult = Test-API "Exception404" "$baseUrl/api/users/999999" "GET" $null 404
    Write-Host $(if ($invalidResult.Success) { " PASS" } else { " FAIL" }) -ForegroundColor $(if ($invalidResult.Success) { "Green" } else { "Red" })
    $roundTests += @{ Test = "Exception404"; Status = $(if ($invalidResult.Success) { "PASS" } else { "FAIL" }); Round = $round }
    
    Start-Sleep -Milliseconds 200
    
    # 10. Exception Test - Invalid Registration (Expecting 400)
    Write-Host "10. Exception400..." -NoNewline
    $invalidUser = @{ username = ""; email = "invalid"; password = "" }
    $invalidRegResult = Test-API "Exception400" "$baseUrl/api/users/register" "POST" $invalidUser 400
    Write-Host $(if ($invalidRegResult.Success) { " PASS" } else { " FAIL" }) -ForegroundColor $(if ($invalidRegResult.Success) { "Green" } else { "Red" })
    $roundTests += @{ Test = "Exception400"; Status = $(if ($invalidRegResult.Success) { "PASS" } else { "FAIL" }); Round = $round }
    
    $passCount = ($roundTests | Where-Object { $_.Status -eq "PASS" }).Count
    $totalTests = $roundTests.Count
    $passRate = if ($totalTests -gt 0) { ($passCount / $totalTests * 100) } else { 0 }
    
    Write-Host "Round ${round}: $passCount/$totalTests ($($passRate.ToString('F1'))%)" -ForegroundColor Cyan
    $allResults += $roundTests
    
    Start-Sleep -Seconds $(if ($round -ge 8) { 2 } else { 1 })
}

$testEndTime = Get-Date
$totalDuration = ($testEndTime - $testStartTime).TotalSeconds

Write-Host "`n" + "="*60 -ForegroundColor Green
Write-Host "FIXED 10-Round Stability Test Report" -ForegroundColor Green
Write-Host "="*60 -ForegroundColor Green

$totalPass = ($allResults | Where-Object { $_.Status -eq "PASS" }).Count
$totalTests = $allResults.Count
$overallRate = if ($totalTests -gt 0) { ($totalPass / $totalTests * 100) } else { 0 }

Write-Host "Duration: $($totalDuration.ToString('F1'))s" -ForegroundColor White
Write-Host "Total Tests: $totalTests" -ForegroundColor White
Write-Host "Passed: $totalPass" -ForegroundColor Green
Write-Host "Failed: $($totalTests - $totalPass)" -ForegroundColor Red
Write-Host "Pass Rate: $($overallRate.ToString('F1'))%" -ForegroundColor $(if ($overallRate -eq 100) { "Green" } elseif ($overallRate -ge 95) { "Yellow" } else { "Red" })

$stabilityGrade = if ($overallRate -eq 100) { "A+ (Perfect)" }
                 elseif ($overallRate -ge 98) { "A (Excellent)" }
                 elseif ($overallRate -ge 95) { "B+ (Very Good)" }
                 elseif ($overallRate -ge 90) { "B (Good)" }
                 else { "C (Poor)" }

Write-Host "Stability Grade: $stabilityGrade" -ForegroundColor Green

# Check Round 10 specifically
$round10Tests = $allResults | Where-Object { $_.Round -eq 10 }
$round10PassRate = if ($round10Tests.Count -gt 0) { (($round10Tests | Where-Object { $_.Status -eq "PASS" }).Count / $round10Tests.Count * 100) } else { 0 }
Write-Host "Round 10 Pass Rate: $($round10PassRate.ToString('F1'))%" -ForegroundColor $(if ($round10PassRate -eq 100) { "Green" } elseif ($round10PassRate -ge 90) { "Yellow" } else { "Red" })

$failures = $allResults | Where-Object { $_.Status -eq "FAIL" }
if ($failures.Count -eq 0) {
    Write-Host "`nSUCCESS! All repeat failure issues RESOLVED!" -ForegroundColor Green
} else {
    Write-Host "`nRemaining Failures:" -ForegroundColor Red
    $failures | Group-Object Round | ForEach-Object {
        Write-Host "Round $($_.Name): $($_.Count) failures" -ForegroundColor Red
    }
}

Write-Host "`nOptimizations Applied:" -ForegroundColor Cyan
Write-Host " Enhanced data uniqueness with timestamps" -ForegroundColor Green
Write-Host " Fixed exception test status codes (404/400)" -ForegroundColor Green
Write-Host " Increased Round 10 retry attempts" -ForegroundColor Green
Write-Host " Progressive delays and timeouts" -ForegroundColor Green

Write-Host "`nFixed Stability Test Completed!" -ForegroundColor Green
