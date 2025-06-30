# CourtLink 15 Round Final Stability Test
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

Write-Host "`n=== CourtLink 15 Round Final Stability Test ===" -ForegroundColor Cyan
Write-Host "Start Time: $(Get-Date)" -ForegroundColor Gray
Write-Host "Base URL: $BaseUrl" -ForegroundColor Gray
Write-Host "Total Rounds: $Rounds" -ForegroundColor Gray
Write-Host "=============================================" -ForegroundColor Cyan

$StartTime = Get-Date

# Main test loop
for ($round = 1; $round -le $Rounds; $round++) {
    $RoundStart = Get-Date
    $RoundSuccess = $true
    
    Write-Host "`n--- Round $round ---" -ForegroundColor Yellow
    
    # 1. Health Check
    Write-Host "  [1/5] Health Check..." -NoNewline
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
    
    # 2. User Registration
    Write-Host "  [2/5] User Registration..." -NoNewline
    $UserId = $null
    try {
        $timestamp = [int][double]::Parse((Get-Date -UFormat %s))
        $random = Get-Random -Min 1000 -Max 9999
        $userData = @{
            username = "test_r${round}_${timestamp}_${random}"
            email = "test_r${round}_${timestamp}_${random}@test.com"
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
            Write-Host " FAIL" -ForegroundColor Red
            $RoundSuccess = $false
        }
    } catch {
        Write-Host " FAIL" -ForegroundColor Red
        $RoundSuccess = $false
    }
    
    Start-Sleep -Milliseconds 100
    
    # 3. User Query
    if ($UserId) {
        Write-Host "  [3/5] User Query..." -NoNewline
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
        Write-Host "  [3/5] User Query... SKIP" -ForegroundColor Yellow
    }
    
    # 4. Exception Test
    Write-Host "  [4/5] Exception Test..." -NoNewline
    $ExceptionTestPass = $true
    
    # Test 404 for non-existent user
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
    
    # Test 400 for invalid registration
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
        Write-Host "  [5/5] Cleanup..." -NoNewline
        try {
            Invoke-RestMethod -Uri "$BaseUrl/api/users/$UserId" -Method DELETE
            Write-Host " OK" -ForegroundColor Green
            $CleanupPass++
        } catch {
            Write-Host " FAIL" -ForegroundColor Red
            $RoundSuccess = $false
        }
    } else {
        Write-Host "  [5/5] Cleanup... SKIP" -ForegroundColor Yellow
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
Write-Host "=== 15 Round Final Test Report ===" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan

Write-Host "`nOverall Results:" -ForegroundColor White
Write-Host "  Total Rounds: $Rounds" -ForegroundColor Gray
Write-Host "  Successful: $SuccessRounds" -ForegroundColor Green
Write-Host "  Failed: $FailRounds" -ForegroundColor Red
if ($SuccessRate -ge 95) {
    Write-Host "  Success Rate: $SuccessRate%" -ForegroundColor Green
} elseif ($SuccessRate -ge 85) {
    Write-Host "  Success Rate: $SuccessRate%" -ForegroundColor Yellow
} else {
    Write-Host "  Success Rate: $SuccessRate%" -ForegroundColor Red
}
Write-Host "  Total Duration: $([int]$TotalDuration.TotalSeconds) seconds" -ForegroundColor Gray

Write-Host "`nDetailed Statistics:" -ForegroundColor White
Write-Host "  Health Check: $HealthPass/$Rounds ($(($HealthPass/$Rounds*100).ToString("F1"))%)" -ForegroundColor $(if ($HealthPass -eq $Rounds) { "Green" } else { "Red" })
Write-Host "  User Registration: $RegisterPass/$Rounds ($(($RegisterPass/$Rounds*100).ToString("F1"))%)" -ForegroundColor $(if ($RegisterPass -eq $Rounds) { "Green" } else { "Red" })
Write-Host "  User Query: $QueryPass/$Rounds ($(($QueryPass/$Rounds*100).ToString("F1"))%)" -ForegroundColor $(if ($QueryPass -eq $Rounds) { "Green" } else { "Red" })
Write-Host "  Exception Test: $ExceptionPass/$Rounds ($(($ExceptionPass/$Rounds*100).ToString("F1"))%)" -ForegroundColor $(if ($ExceptionPass -eq $Rounds) { "Green" } else { "Red" })
Write-Host "  Cleanup: $CleanupPass/$Rounds ($(($CleanupPass/$Rounds*100).ToString("F1"))%)" -ForegroundColor $(if ($CleanupPass -eq $Rounds) { "Green" } else { "Red" })

Write-Host "`nSystem Stability Grade:" -ForegroundColor White
if ($SuccessRate -eq 100) {
    Write-Host "  Grade: A+ (Perfect)" -ForegroundColor Green
    Write-Host "  Recommendation: Deploy to production immediately" -ForegroundColor Green
} elseif ($SuccessRate -ge 95) {
    Write-Host "  Grade: A (Excellent)" -ForegroundColor Green
    Write-Host "  Recommendation: Recommended for production deployment" -ForegroundColor Green
} elseif ($SuccessRate -ge 90) {
    Write-Host "  Grade: A- (Good)" -ForegroundColor Yellow
    Write-Host "  Recommendation: Can deploy with monitoring" -ForegroundColor Yellow
} elseif ($SuccessRate -ge 80) {
    Write-Host "  Grade: B (Fair)" -ForegroundColor Yellow
    Write-Host "  Recommendation: Fix issues before deployment" -ForegroundColor Yellow
} else {
    Write-Host "  Grade: C (Needs Improvement)" -ForegroundColor Red
    Write-Host "  Recommendation: Not recommended for deployment" -ForegroundColor Red
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
        HealthCheck = $HealthPass
        UserRegistration = $RegisterPass
        UserQuery = $QueryPass
        ExceptionTest = $ExceptionPass
        Cleanup = $CleanupPass
    }
}

$ResultFile = "final-15round-test-results.json"
$Results | ConvertTo-Json -Depth 3 | Out-File $ResultFile -Encoding UTF8

Write-Host "`nResults saved to: $ResultFile" -ForegroundColor Cyan
Write-Host "`n=== 15 Round Test Complete ===" -ForegroundColor Cyan 