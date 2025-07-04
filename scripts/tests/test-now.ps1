# CourtLink System Test
param([string]$BaseUrl = "http://localhost:8080")

$passed = 0
$failed = 0
$rounds = 10

Write-Host "=== CourtLink System Test ===" -ForegroundColor Cyan
Write-Host "Start Time $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Gray

# Wait for app
Write-Host "Waiting for application..." -ForegroundColor Yellow
$ready = $false
for ($w = 0; $w -lt 15; $w++) {
    try {
        $h = Invoke-RestMethod "$BaseUrl/actuator/health" -TimeoutSec 3
        if ($h.status -eq "UP") {
            $ready = $true
            Write-Host "Application is UP!" -ForegroundColor Green
            break
        }
    }
    catch {
        Start-Sleep 2
        Write-Host "." -NoNewline
    }
}

if (-not $ready) {
    Write-Host "App not ready, exiting" -ForegroundColor Red
    exit 1
}

# Basic tests
Write-Host "`nBasic Tests" -ForegroundColor Yellow

# Health check
try {
    $health = Invoke-RestMethod "$BaseUrl/actuator/health"
    if ($health.status -eq "UP") {
        Write-Host "[PASS] Health Check" -ForegroundColor Green
        $passed++
    }
}
catch {
    Write-Host "[FAIL] Health Check" -ForegroundColor Red
    $failed++
}

# User list
try {
    $users = Invoke-RestMethod "$BaseUrl/api/users"
    Write-Host "[PASS] User List" -ForegroundColor Green
    $passed++
}
catch {
    Write-Host "[FAIL] User List" -ForegroundColor Red
    $failed++
}

# CRUD Testing
Write-Host "`nCRUD Testing ($rounds rounds)" -ForegroundColor Yellow
$successRounds = 0

for ($i = 1; $i -le $rounds; $i++) {
    Write-Host "Round $i" -ForegroundColor Cyan
    $ok = $true
    
    $user = @{
        username = "test$(Get-Random)"
        email = "test$(Get-Random)@test.com"
        password = "password123"
        realName = "Test User"
        phoneNumber = "13800000001"
    }
    
    try {
        # Register
        $reg = Invoke-RestMethod "$BaseUrl/api/users/register" -Method POST -Body ($user | ConvertTo-Json) -ContentType "application/json"
        Write-Host "  Registration OK - ID $($reg.id)" -ForegroundColor Green
        
        # Query
        Start-Sleep -Milliseconds 100
        $query = Invoke-RestMethod "$BaseUrl/api/users/$($reg.id)"
        if ($query.username -eq $user.username) {
            Write-Host "  Query OK" -ForegroundColor Green
        } else {
            Write-Host "  Query FAIL" -ForegroundColor Red
            $ok = $false
        }
        
        # Delete
        Invoke-RestMethod "$BaseUrl/api/users/$($reg.id)" -Method DELETE | Out-Null
        Write-Host "  Cleanup OK" -ForegroundColor Green
    }
    catch {
        Write-Host "  Round FAILED - $($_.Exception.Message)" -ForegroundColor Red
        $ok = $false
    }
    
    if ($ok) {
        $successRounds++
        Write-Host "  Round $i SUCCESS" -ForegroundColor Green
    } else {
        Write-Host "  Round $i FAILED" -ForegroundColor Red
    }
    
    Start-Sleep -Milliseconds 200
}

# Results
$totalTests = $passed + $failed + $rounds
$totalPassed = $passed + $successRounds
$successRate = [math]::Round(($totalPassed / $totalTests) * 100, 1)
$roundRate = [math]::Round(($successRounds / $rounds) * 100, 1)

Write-Host "`n=== RESULTS ===" -ForegroundColor Cyan
Write-Host "Basic Tests: $passed passed, $failed failed" -ForegroundColor White
Write-Host "CRUD Rounds: $successRounds/$rounds successful ($roundRate%)" -ForegroundColor White
Write-Host "Overall Success Rate: $successRate%" -ForegroundColor White

# Grade
$grade = "F"
$rec = "Not recommended"
if ($successRate -ge 95 -and $roundRate -ge 90) {
    $grade = "A+"
    $rec = "Excellent - Deploy immediately"
} elseif ($successRate -ge 90 -and $roundRate -ge 80) {
    $grade = "A"
    $rec = "Very good - Deploy to production"
} elseif ($successRate -ge 85) {
    $grade = "B+"
    $rec = "Good - Minor issues to fix"
} elseif ($successRate -ge 75) {
    $grade = "B"
    $rec = "Acceptable - Needs improvement"
} elseif ($successRate -ge 60) {
    $grade = "C"
    $rec = "Poor - Major issues"
} else {
    $grade = "D"
    $rec = "Failed - Do not deploy"
}

Write-Host "`nSYSTEM GRADE: $grade" -ForegroundColor $(if($grade -match "A") {"Green"} elseif($grade -match "B") {"Yellow"} else {"Red"})
Write-Host "RECOMMENDATION: $rec" -ForegroundColor $(if($grade -match "A") {"Green"} elseif($grade -match "B") {"Yellow"} else {"Red"})

Write-Host "`nTest completed at $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Gray

# Save to file
$result = @{
    timestamp = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
    basicTests = @{ passed = $passed; failed = $failed }
    crudRounds = @{ total = $rounds; successful = $successRounds; rate = $roundRate }
    overall = @{ successRate = $successRate; grade = $grade; recommendation = $rec }
}

$result | ConvertTo-Json | Out-File "test-results-now.json"
Write-Host "Results saved to test-results-now.json" -ForegroundColor Gray 