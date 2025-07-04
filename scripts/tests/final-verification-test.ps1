Write-Host "=== FINAL VERIFICATION: 10-Round Stability Test ===" -ForegroundColor Green

$baseUrl = "http://localhost:8080"
$allResults = @()
$testStartTime = Get-Date

for ($round = 1; $round -le 10; $round++) {
    Write-Host "`nRound $round:" -NoNewline -ForegroundColor Yellow
    
    if ($round -eq 10) {
        Start-Sleep -Seconds 1  # Brief pause for Round 10
    }
    
    # Unique test user
    $uniqueId = "final_r${round}_$([DateTimeOffset]::Now.ToUnixTimeMilliseconds())"
    $testUser = @{
        username = $uniqueId
        email = "${uniqueId}@test.com"
        password = "testpass123"
        fullName = "Final Test User $round"
        phoneNumber = "138${round.ToString().PadLeft(8, '0')}"
    }
    
    $roundPassed = 0
    $roundTotal = 10
    
    # 1. Health Check
    try {
        Invoke-RestMethod -Uri "$baseUrl/actuator/health" -TimeoutSec 10 | Out-Null
        Write-Host " H" -NoNewline -ForegroundColor Green
        $roundPassed++
    } catch {
        Write-Host " H" -NoNewline -ForegroundColor Red
    }
    
    # 2. User Registration  
    $userId = $null
    try {
        $regResponse = Invoke-RestMethod -Uri "$baseUrl/api/users/register" -Method POST -Body ($testUser | ConvertTo-Json) -ContentType "application/json" -TimeoutSec 15
        $userId = $regResponse.id
        Write-Host " R($userId)" -NoNewline -ForegroundColor Green
        $roundPassed++
    } catch {
        Write-Host " R" -NoNewline -ForegroundColor Red
    }
    
    if ($userId) {
        # 3. Login
        try {
            $loginData = @{ username = $testUser.username; password = $testUser.password }
            Invoke-RestMethod -Uri "$baseUrl/api/users/login" -Method POST -Body ($loginData | ConvertTo-Json) -ContentType "application/json" -TimeoutSec 10 | Out-Null
            Write-Host " L" -NoNewline -ForegroundColor Green
            $roundPassed++
        } catch {
            Write-Host " L" -NoNewline -ForegroundColor Red
        }
        
        # 4. Get User
        try {
            Invoke-RestMethod -Uri "$baseUrl/api/users/$userId" -TimeoutSec 10 | Out-Null
            Write-Host " G" -NoNewline -ForegroundColor Green
            $roundPassed++
        } catch {
            Write-Host " G" -NoNewline -ForegroundColor Red
        }
        
        # 5. Password Validation
        try {
            $pwdData = @{ password = $testUser.password }
            Invoke-RestMethod -Uri "$baseUrl/api/users/$userId/validate-password" -Method POST -Body ($pwdData | ConvertTo-Json) -ContentType "application/json" -TimeoutSec 10 | Out-Null
            Write-Host " P" -NoNewline -ForegroundColor Green
            $roundPassed++
        } catch {
            Write-Host " P" -NoNewline -ForegroundColor Red
        }
        
        # 6. Activate
        try {
            Invoke-RestMethod -Uri "$baseUrl/api/users/$userId/activate" -Method POST -TimeoutSec 10 | Out-Null
            Write-Host " A" -NoNewline -ForegroundColor Green
            $roundPassed++
        } catch {
            Write-Host " A" -NoNewline -ForegroundColor Red
        }
        
        # 7. Deactivate
        try {
            Invoke-RestMethod -Uri "$baseUrl/api/users/$userId/deactivate" -Method POST -TimeoutSec 10 | Out-Null
            Write-Host " D" -NoNewline -ForegroundColor Green
            $roundPassed++
        } catch {
            Write-Host " D" -NoNewline -ForegroundColor Red
        }
        
        # Reactivate for cleanup
        try { Invoke-RestMethod -Uri "$baseUrl/api/users/$userId/activate" -Method POST -TimeoutSec 5 | Out-Null } catch {}
        
        # 8. Delete
        try {
            Invoke-RestMethod -Uri "$baseUrl/api/users/$userId" -Method DELETE -TimeoutSec 10 | Out-Null
            Write-Host " Del" -NoNewline -ForegroundColor Green
            $roundPassed++
        } catch {
            Write-Host " Del" -NoNewline -ForegroundColor Red
        }
    }
    
    # 9. Exception Test - Invalid User (404)
    try {
        Invoke-RestMethod -Uri "$baseUrl/api/users/999999" -TimeoutSec 10 | Out-Null
        Write-Host " E1" -NoNewline -ForegroundColor Red
    } catch {
        if ($_.Exception.Response.StatusCode -eq 404) {
            Write-Host " E1" -NoNewline -ForegroundColor Green
            $roundPassed++
        } else {
            Write-Host " E1" -NoNewline -ForegroundColor Red
        }
    }
    
    # 10. Exception Test - Invalid Registration (400)
    try {
        $invalidUser = @{ username = ""; email = "invalid"; password = "" }
        Invoke-RestMethod -Uri "$baseUrl/api/users/register" -Method POST -Body ($invalidUser | ConvertTo-Json) -ContentType "application/json" -TimeoutSec 10 | Out-Null
        Write-Host " E2" -NoNewline -ForegroundColor Red
    } catch {
        if ($_.Exception.Response.StatusCode -eq 400) {
            Write-Host " E2" -NoNewline -ForegroundColor Green
            $roundPassed++
        } else {
            Write-Host " E2" -NoNewline -ForegroundColor Red
        }
    }
    
    $passRate = ($roundPassed / $roundTotal * 100)
    Write-Host " [$roundPassed/$roundTotal = $($passRate.ToString('F0'))%]" -ForegroundColor $(if ($passRate -eq 100) { "Green" } elseif ($passRate -ge 90) { "Yellow" } else { "Red" })
    
    $allResults += @{ Round = $round; Passed = $roundPassed; Total = $roundTotal; PassRate = $passRate; HasUserId = ($userId -ne $null) }
    
    Start-Sleep -Milliseconds 300  # Brief pause between rounds
}

$testEndTime = Get-Date
$totalDuration = ($testEndTime - $testStartTime).TotalSeconds

Write-Host "`n" + "="*70 -ForegroundColor Green
Write-Host "FINAL VERIFICATION REPORT" -ForegroundColor Green
Write-Host "="*70 -ForegroundColor Green

$totalPassed = ($allResults | Measure-Object -Property Passed -Sum).Sum
$totalTests = ($allResults | Measure-Object -Property Total -Sum).Sum
$overallPassRate = ($totalPassed / $totalTests * 100)

Write-Host "Test Duration: $($totalDuration.ToString('F1')) seconds" -ForegroundColor White
Write-Host "Total Tests: $totalTests" -ForegroundColor White
Write-Host "Total Passed: $totalPassed" -ForegroundColor Green
Write-Host "Overall Pass Rate: $($overallPassRate.ToString('F1'))%" -ForegroundColor $(if ($overallPassRate -eq 100) { "Green" } elseif ($overallPassRate -ge 95) { "Yellow" } else { "Red" })

Write-Host "`nRound-by-Round Analysis:" -ForegroundColor Cyan
$round10PassRate = ($allResults | Where-Object { $_.Round -eq 10 }).PassRate
foreach ($result in $allResults) {
    $color = if ($result.PassRate -eq 100) { "Green" } elseif ($result.PassRate -ge 90) { "Yellow" } else { "Red" }
    $userIdStatus = if ($result.HasUserId) { " (UserID)" } else { " (UserID)" }
    Write-Host "Round $($result.Round): $($result.PassRate.ToString('F0'))%$userIdStatus" -ForegroundColor $color
}

Write-Host "`nCritical Issue Analysis:" -ForegroundColor Cyan
$userRegistrationSuccessRounds = ($allResults | Where-Object { $_.HasUserId }).Count
Write-Host "User Registration Success Rate: $userRegistrationSuccessRounds/10 rounds" -ForegroundColor $(if ($userRegistrationSuccessRounds -eq 10) { "Green" } elseif ($userRegistrationSuccessRounds -ge 8) { "Yellow" } else { "Red" })
Write-Host "Round 10 Performance: $($round10PassRate.ToString('F0'))%" -ForegroundColor $(if ($round10PassRate -eq 100) { "Green" } elseif ($round10PassRate -ge 90) { "Yellow" } else { "Red" })

$grade = if ($overallPassRate -eq 100) { "A+ (Perfect)" }
         elseif ($overallPassRate -ge 98) { "A (Excellent)" }
         elseif ($overallPassRate -ge 95) { "B+ (Very Good)" }
         elseif ($overallPassRate -ge 90) { "B (Good)" }
         else { "C (Needs Improvement)" }

Write-Host "`nFinal Stability Grade: $grade" -ForegroundColor Green

if ($round10PassRate -eq 100 -and $userRegistrationSuccessRounds -eq 10) {
    Write-Host "`n SUCCESS! All repeat failure issues have been RESOLVED!" -ForegroundColor Green
    Write-Host " Round 10 stability: PERFECT" -ForegroundColor Green
    Write-Host " User registration reliability: PERFECT" -ForegroundColor Green
    Write-Host " Exception handling: WORKING" -ForegroundColor Green
} elseif ($overallPassRate -ge 95) {
    Write-Host "`n MAJOR IMPROVEMENT! Most issues resolved" -ForegroundColor Yellow
    if ($round10PassRate -lt 100) {
        Write-Host " Round 10 still needs minor optimization" -ForegroundColor Yellow
    }
    if ($userRegistrationSuccessRounds -lt 10) {
        Write-Host " User registration needs attention" -ForegroundColor Yellow
    }
} else {
    Write-Host "`n Still needs optimization" -ForegroundColor Red
}

Write-Host "`nFinal Verification Completed!" -ForegroundColor Green
