# Simple Stability Test for CourtLink System
Write-Host "=====================================================" -ForegroundColor Green
Write-Host "CourtLink System Stability Test - 5 Rounds" -ForegroundColor Green
Write-Host "=====================================================" -ForegroundColor Green

$baseUrl = "http://localhost:8080"
$headers = @{"Content-Type" = "application/json"}
$allRoundResults = @()

for ($round = 1; $round -le 5; $round++) {
    Write-Host ""
    Write-Host "Round $round Testing..." -ForegroundColor Cyan
    Write-Host "-----------------------------" -ForegroundColor Cyan
    
    $roundResults = @{
        Round = $round
        Tests = @()
        Pass = 0
        Fail = 0
    }
    
    $testUser = "testuser$round$(Get-Random -Minimum 1000 -Maximum 9999)"
    $testEmail = "$testUser@test.com"
    $testPassword = "TestPass$round"
    
    # Test 1: Health Check
    Write-Host "1. Health Check..." -NoNewline
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -Method GET
        if ($response.status -eq "UP") {
            Write-Host " PASS" -ForegroundColor Green
            $roundResults.Tests += "Health Check: PASS"
            $roundResults.Pass++
        } else {
            Write-Host " FAIL" -ForegroundColor Red
            $roundResults.Tests += "Health Check: FAIL"
            $roundResults.Fail++
        }
    } catch {
        Write-Host " FAIL" -ForegroundColor Red
        $roundResults.Tests += "Health Check: FAIL"
        $roundResults.Fail++
    }
    
    # Test 2: User Registration
    Write-Host "2. User Registration..." -NoNewline
    try {
        $registerData = @{
            username = $testUser
            password = $testPassword
            email = $testEmail
        } | ConvertTo-Json
        
        $response = Invoke-RestMethod -Uri "$baseUrl/api/users/register" -Method POST -Body $registerData -Headers $headers
        if ($response.id) {
            Write-Host " PASS" -ForegroundColor Green
            $roundResults.Tests += "User Registration: PASS"
            $roundResults.Pass++
            $userId = $response.id
        } else {
            Write-Host " FAIL" -ForegroundColor Red
            $roundResults.Tests += "User Registration: FAIL"
            $roundResults.Fail++
            continue
        }
    } catch {
        Write-Host " FAIL" -ForegroundColor Red
        $roundResults.Tests += "User Registration: FAIL"
        $roundResults.Fail++
        continue
    }
    
    # Test 3: User Login
    Write-Host "3. User Login..." -NoNewline
    try {
        $loginData = @{
            username = $testUser
            password = $testPassword
        } | ConvertTo-Json
        
        $response = Invoke-RestMethod -Uri "$baseUrl/api/users/login" -Method POST -Body $loginData -Headers $headers
        if ($response.token) {
            Write-Host " PASS" -ForegroundColor Green
            $roundResults.Tests += "User Login: PASS"
            $roundResults.Pass++
        } else {
            Write-Host " FAIL" -ForegroundColor Red
            $roundResults.Tests += "User Login: FAIL"
            $roundResults.Fail++
        }
    } catch {
        Write-Host " FAIL" -ForegroundColor Red
        $roundResults.Tests += "User Login: FAIL"
        $roundResults.Fail++
    }
    
    # Test 4: User Query
    Write-Host "4. User Query..." -NoNewline
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/api/users/$userId" -Method GET
        if ($response.username -eq $testUser) {
            Write-Host " PASS" -ForegroundColor Green
            $roundResults.Tests += "User Query: PASS"
            $roundResults.Pass++
        } else {
            Write-Host " FAIL" -ForegroundColor Red
            $roundResults.Tests += "User Query: FAIL"
            $roundResults.Fail++
        }
    } catch {
        Write-Host " FAIL" -ForegroundColor Red
        $roundResults.Tests += "User Query: FAIL"
        $roundResults.Fail++
    }
    
    # Test 5: Password Validation
    Write-Host "5. Password Validation..." -NoNewline
    try {
        $validateData = @{
            password = $testPassword
        } | ConvertTo-Json
        
        $response = Invoke-RestMethod -Uri "$baseUrl/api/users/$userId/validate-password" -Method POST -Body $validateData -Headers $headers
        if ($response.valid -eq $true) {
            Write-Host " PASS" -ForegroundColor Green
            $roundResults.Tests += "Password Validation: PASS"
            $roundResults.Pass++
        } else {
            Write-Host " FAIL" -ForegroundColor Red
            $roundResults.Tests += "Password Validation: FAIL"
            $roundResults.Fail++
        }
    } catch {
        Write-Host " FAIL" -ForegroundColor Red
        $roundResults.Tests += "Password Validation: FAIL"
        $roundResults.Fail++
    }
    
    # Test 6: User Update
    Write-Host "6. User Update..." -NoNewline
    try {
        $updateData = @{
            username = $testUser
            email = $testEmail
            realName = "Test User $round"
        } | ConvertTo-Json
        
        $response = Invoke-RestMethod -Uri "$baseUrl/api/users/$userId" -Method PUT -Body $updateData -Headers $headers
        if ($response.realName -eq "Test User $round") {
            Write-Host " PASS" -ForegroundColor Green
            $roundResults.Tests += "User Update: PASS"
            $roundResults.Pass++
        } else {
            Write-Host " FAIL" -ForegroundColor Red
            $roundResults.Tests += "User Update: FAIL"
            $roundResults.Fail++
        }
    } catch {
        Write-Host " FAIL" -ForegroundColor Red
        $roundResults.Tests += "User Update: FAIL"
        $roundResults.Fail++
    }
    
    # Test 7: User Activate
    Write-Host "7. User Activate..." -NoNewline
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/api/users/$userId/activate" -Method POST -Headers $headers
        if ($response.success -eq $true) {
            Write-Host " PASS" -ForegroundColor Green
            $roundResults.Tests += "User Activate: PASS"
            $roundResults.Pass++
        } else {
            Write-Host " FAIL" -ForegroundColor Red
            $roundResults.Tests += "User Activate: FAIL"
            $roundResults.Fail++
        }
    } catch {
        Write-Host " FAIL" -ForegroundColor Red
        $roundResults.Tests += "User Activate: FAIL"
        $roundResults.Fail++
    }
    
    # Test 8: User Delete
    Write-Host "8. User Delete..." -NoNewline
    try {
        Invoke-RestMethod -Uri "$baseUrl/api/users/$userId" -Method DELETE | Out-Null
        
        # Verify deletion
        try {
            Invoke-RestMethod -Uri "$baseUrl/api/users/$userId" -Method GET | Out-Null
            Write-Host " FAIL" -ForegroundColor Red
            $roundResults.Tests += "User Delete: FAIL"
            $roundResults.Fail++
        } catch {
            Write-Host " PASS" -ForegroundColor Green
            $roundResults.Tests += "User Delete: PASS"
            $roundResults.Pass++
        }
    } catch {
        Write-Host " FAIL" -ForegroundColor Red
        $roundResults.Tests += "User Delete: FAIL"
        $roundResults.Fail++
    }
    
    # Round Summary
    $total = $roundResults.Pass + $roundResults.Fail
    $successRate = if ($total -gt 0) { [Math]::Round($roundResults.Pass / $total * 100, 1) } else { 0 }
    Write-Host ""
    Write-Host "Round $round Summary: $($roundResults.Pass)/$total passed ($successRate%)" -ForegroundColor $(if($successRate -eq 100) {"Green"} elseif($successRate -ge 80) {"Yellow"} else {"Red"})
    
    $allRoundResults += $roundResults
    
    if ($round -lt 5) {
        Start-Sleep -Seconds 2
    }
}

# Overall Analysis
Write-Host ""
Write-Host "=====================================================" -ForegroundColor Green
Write-Host "Overall Stability Analysis" -ForegroundColor Green
Write-Host "=====================================================" -ForegroundColor Green

$totalTests = ($allRoundResults | ForEach-Object { $_.Pass + $_.Fail } | Measure-Object -Sum).Sum
$totalPass = ($allRoundResults | ForEach-Object { $_.Pass } | Measure-Object -Sum).Sum
$totalFail = ($allRoundResults | ForEach-Object { $_.Fail } | Measure-Object -Sum).Sum
$overallRate = if ($totalTests -gt 0) { [Math]::Round($totalPass / $totalTests * 100, 2) } else { 0 }

Write-Host "Total Tests: $totalTests" -ForegroundColor White
Write-Host "Total Pass: $totalPass" -ForegroundColor Green
Write-Host "Total Fail: $totalFail" -ForegroundColor Red
Write-Host "Overall Success Rate: $overallRate%" -ForegroundColor $(if($overallRate -eq 100) {"Green"} elseif($overallRate -ge 95) {"Yellow"} else {"Red"})

# Round-by-Round Results
Write-Host ""
Write-Host "Round-by-Round Results:" -ForegroundColor Yellow
foreach ($result in $allRoundResults) {
    $total = $result.Pass + $result.Fail
    $rate = if ($total -gt 0) { [Math]::Round($result.Pass / $total * 100, 1) } else { 0 }
    Write-Host "Round $($result.Round): $rate% ($($result.Pass)/$total)" -ForegroundColor $(if($rate -eq 100) {"Green"} elseif($rate -ge 80) {"Yellow"} else {"Red"})
}

# Stability Grade
Write-Host ""
Write-Host "Stability Grade:" -ForegroundColor Yellow
$grade = if ($overallRate -eq 100) {
    "A+ (Excellent)"
} elseif ($overallRate -ge 95) {
    "A (Good)"
} elseif ($overallRate -ge 90) {
    "B (Fair)"
} elseif ($overallRate -ge 80) {
    "C (Need Improvement)"
} else {
    "D (Unstable)"
}

$gradeColor = switch ($grade.Substring(0,1)) {
    "A" { "Green" }
    "B" { "Yellow" }
    "C" { "Red" }
    "D" { "DarkRed" }
}

Write-Host $grade -ForegroundColor $gradeColor

Write-Host ""
Write-Host "Test completed at: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Cyan
Write-Host "=====================================================" -ForegroundColor Green 