# CourtLink System Stability Test
param(
    [int]$Rounds = 10,
    [string]$BaseUrl = "http://localhost:8080"
)

$Results = @{
    StartTime = Get-Date
    TotalTests = 0
    PassedTests = 0
    FailedTests = 0
    SuccessfulRounds = 0
}

function Test-Api {
    param($Uri, $Method = "GET", $Body = $null)
    
    try {
        $headers = @{"Content-Type" = "application/json"}
        if ($Body -and $Method -ne "GET") {
            $response = Invoke-RestMethod -Uri $Uri -Method $Method -Body ($Body | ConvertTo-Json) -Headers $headers -TimeoutSec 10
        } else {
            $response = Invoke-RestMethod -Uri $Uri -Method $Method -Headers $headers -TimeoutSec 10
        }
        return @{ Success = $true; Data = $response }
    }
    catch {
        return @{ Success = $false; Error = $_.Exception.Message }
    }
}

Write-Host "`n=== CourtLink System Stability Test ===" -ForegroundColor Cyan
Write-Host "Start Time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Gray
Write-Host "Test Rounds: $Rounds" -ForegroundColor Gray
Write-Host "================================================" -ForegroundColor Cyan

# Wait for application startup
Write-Host "`nWaiting for application startup..." -ForegroundColor Yellow
$maxWait = 30
$waited = 0
$appReady = $false

while ($waited -lt $maxWait -and -not $appReady) {
    try {
        $health = Invoke-RestMethod "$BaseUrl/actuator/health" -TimeoutSec 5
        if ($health.status -eq "UP") {
            $appReady = $true
            Write-Host "Application is ready!" -ForegroundColor Green
        }
    }
    catch {
        Start-Sleep -Seconds 2
        $waited += 2
        Write-Host "." -NoNewline -ForegroundColor Gray
    }
}

if (-not $appReady) {
    Write-Host "`nApplication startup timeout. Cannot proceed with tests." -ForegroundColor Red
    exit 1
}

# Basic functionality tests
Write-Host "`n=== Basic Functionality Tests ===" -ForegroundColor Yellow

# Health Check
$health = Test-Api "$BaseUrl/actuator/health"
$Results.TotalTests++
if ($health.Success -and $health.Data.status -eq "UP") {
    Write-Host "[PASS] Health Check - Status: UP" -ForegroundColor Green
    $Results.PassedTests++
} else {
    Write-Host "[FAIL] Health Check" -ForegroundColor Red
    $Results.FailedTests++
}

# User List
$userList = Test-Api "$BaseUrl/api/users"
$Results.TotalTests++
if ($userList.Success) {
    Write-Host "[PASS] User List Query" -ForegroundColor Green
    $Results.PassedTests++
} else {
    Write-Host "[FAIL] User List Query" -ForegroundColor Red
    $Results.FailedTests++
}

# CRUD Rounds Testing
Write-Host "`n=== $Rounds Rounds CRUD Stability Test ===" -ForegroundColor Yellow

for ($i = 1; $i -le $Rounds; $i++) {
    Write-Host "`n--- Round $i ---" -ForegroundColor Cyan
    $roundOK = $true
    
    # Generate test user
    $testUser = @{
        username = "test_$(Get-Random -Min 1000 -Max 9999)"
        email = "test$(Get-Random)@example.com"
        password = "password123"
        realName = "Test User $i"
        phoneNumber = "13800000001"
    }
    
    # User Registration
    $register = Test-Api "$BaseUrl/api/users/register" "POST" $testUser
    if ($register.Success -and $register.Data.id) {
        Write-Host "  [PASS] User Registration - ID: $($register.Data.id)" -ForegroundColor Green
        $userId = $register.Data.id
        
        # User Query
        Start-Sleep -Milliseconds 100
        $query = Test-Api "$BaseUrl/api/users/$userId"
        if ($query.Success -and $query.Data.username -eq $testUser.username) {
            Write-Host "  [PASS] User Query - Username matches" -ForegroundColor Green
        } else {
            Write-Host "  [FAIL] User Query - Data mismatch" -ForegroundColor Red
            $roundOK = $false
        }
        
        # Cleanup
        $delete = Test-Api "$BaseUrl/api/users/$userId" "DELETE"
        if ($delete.Success) {
            Write-Host "  [PASS] Data Cleanup" -ForegroundColor Green
        } else {
            Write-Host "  [FAIL] Data Cleanup" -ForegroundColor Red
            $roundOK = $false
        }
        
    } else {
        Write-Host "  [FAIL] User Registration" -ForegroundColor Red
        $roundOK = $false
    }
    
    if ($roundOK) {
        $Results.SuccessfulRounds++
        Write-Host "  Round $i: SUCCESS" -ForegroundColor Green
    } else {
        Write-Host "  Round $i: FAILED" -ForegroundColor Red
    }
    
    if ($i -lt $Rounds) {
        Start-Sleep -Milliseconds 200
    }
}

# Exception Handling Tests
Write-Host "`n=== Exception Handling Tests ===" -ForegroundColor Yellow

# 404 Test
$notFound = Test-Api "$BaseUrl/api/users/999999"
$Results.TotalTests++
if (!$notFound.Success) {
    Write-Host "[PASS] 404 Error Handling" -ForegroundColor Green
    $Results.PassedTests++
} else {
    Write-Host "[FAIL] 404 Error Handling" -ForegroundColor Red
    $Results.FailedTests++
}

# Generate Final Report
$Results.EndTime = Get-Date
$Results.Duration = $Results.EndTime - $Results.StartTime

$totalFunctionalTests = $Results.TotalTests
$totalRoundTests = $Rounds
$totalTests = $totalFunctionalTests + $totalRoundTests

$functionalSuccessRate = if ($totalFunctionalTests -gt 0) { 
    [math]::Round(($Results.PassedTests / $totalFunctionalTests) * 100, 1) 
} else { 0 }

$roundSuccessRate = if ($totalRoundTests -gt 0) { 
    [math]::Round(($Results.SuccessfulRounds / $totalRoundTests) * 100, 1) 
} else { 0 }

$overallSuccessRate = [math]::Round((($Results.PassedTests + $Results.SuccessfulRounds) / ($totalFunctionalTests + $totalRoundTests)) * 100, 1)

Write-Host "`n" + "=" * 50 -ForegroundColor Cyan
Write-Host "=== TEST RESULTS SUMMARY ===" -ForegroundColor Cyan
Write-Host "=" * 50 -ForegroundColor Cyan

Write-Host "`nFunctional Tests:" -ForegroundColor Yellow
Write-Host "  Total: $totalFunctionalTests" -ForegroundColor White
Write-Host "  Passed: $($Results.PassedTests)" -ForegroundColor Green
Write-Host "  Failed: $($Results.FailedTests)" -ForegroundColor Red
Write-Host "  Success Rate: $functionalSuccessRate%" -ForegroundColor Green

Write-Host "`nCRUD Round Tests:" -ForegroundColor Yellow
Write-Host "  Total Rounds: $totalRoundTests" -ForegroundColor White
Write-Host "  Successful Rounds: $($Results.SuccessfulRounds)" -ForegroundColor Green
Write-Host "  Failed Rounds: $($totalRoundTests - $Results.SuccessfulRounds)" -ForegroundColor Red
Write-Host "  Round Success Rate: $roundSuccessRate%" -ForegroundColor Green

Write-Host "`nOverall Statistics:" -ForegroundColor Yellow
Write-Host "  Overall Success Rate: $overallSuccessRate%" -ForegroundColor Green
Write-Host "  Test Duration: $([int]$Results.Duration.TotalSeconds) seconds" -ForegroundColor White

# System Grading
$grade = "F"
$gradeColor = "Red"
$recommendation = "Not recommended for deployment"

if ($overallSuccessRate -ge 95 -and $roundSuccessRate -ge 90) {
    $grade = "A+"
    $gradeColor = "Green"
    $recommendation = "Highly recommended for immediate production deployment"
} elseif ($overallSuccessRate -ge 90 -and $roundSuccessRate -ge 80) {
    $grade = "A"
    $gradeColor = "Green"
    $recommendation = "Recommended for production deployment"
} elseif ($overallSuccessRate -ge 85 -and $roundSuccessRate -ge 70) {
    $grade = "B+"
    $gradeColor = "Yellow"
    $recommendation = "Deployment recommended after fixing issues"
} elseif ($overallSuccessRate -ge 75) {
    $grade = "B"
    $gradeColor = "Yellow"
    $recommendation = "Requires further testing and optimization"
} elseif ($overallSuccessRate -ge 60) {
    $grade = "C"
    $gradeColor = "Red"
    $recommendation = "Multiple issues exist, requires fixes"
} else {
    $grade = "D"
    $gradeColor = "Red"
    $recommendation = "System unstable, deployment prohibited"
}

Write-Host "`nSYSTEM STABILITY GRADE: $grade" -ForegroundColor $gradeColor
Write-Host "DEPLOYMENT RECOMMENDATION: $recommendation" -ForegroundColor $gradeColor

Write-Host "`nTest Completed: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Gray

# Save results to JSON
$jsonResult = @{
    TestSummary = @{
        StartTime = $Results.StartTime.ToString("yyyy-MM-dd HH:mm:ss")
        EndTime = $Results.EndTime.ToString("yyyy-MM-dd HH:mm:ss")
        Duration = $Results.Duration.TotalSeconds
        FunctionalTests = @{
            Total = $totalFunctionalTests
            Passed = $Results.PassedTests
            Failed = $Results.FailedTests
            SuccessRate = $functionalSuccessRate
        }
        RoundTests = @{
            TotalRounds = $totalRoundTests
            SuccessfulRounds = $Results.SuccessfulRounds
            FailedRounds = $totalRoundTests - $Results.SuccessfulRounds
            RoundSuccessRate = $roundSuccessRate
        }
        OverallSuccessRate = $overallSuccessRate
        SystemGrade = $grade
        Recommendation = $recommendation
    }
} | ConvertTo-Json -Depth 3

$jsonResult | Out-File "stability-test-results-current.json" -Encoding UTF8
Write-Host "`nDetailed results saved to: stability-test-results-current.json" -ForegroundColor Gray 