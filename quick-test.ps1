# CourtLink Quick Stability Test
# Date: 2025-07-01
# Test available endpoints only

param(
    [int]$Rounds = 10,
    [string]$BaseUrl = "http://localhost:8080"
)

$Results = @{
    StartTime = Get-Date
    TestsPassed = 0
    TestsFailed = 0
    RoundsSuccessful = 0
    TestDetails = @()
    Failures = @()
}

function Write-Log($Message, $Color = "White") {
    $timestamp = Get-Date -Format 'HH:mm:ss'
    Write-Host "[$timestamp] $Message" -ForegroundColor $Color
}

function Test-Endpoint($Url, $Method = "GET", $Body = $null, $TestName) {
    try {
        $params = @{
            Uri = $Url
            Method = $Method
            TimeoutSec = 10
        }
        
        if ($Method -ne "GET" -and $Body) {
            $params.Headers = @{"Content-Type" = "application/json"}
            $params.Body = ($Body | ConvertTo-Json)
        }
        
        $response = Invoke-RestMethod @params
        
        Write-Log "$TestName - SUCCESS" "Green"
        $Results.TestsPassed++
        return @{ Success = $true; Data = $response }
    }
    catch {
        Write-Log "$TestName - FAILED: $($_.Exception.Message)" "Red"
        $Results.TestsFailed++
        $Results.Failures += "$TestName failed: $($_.Exception.Message)"
        return @{ Success = $false; Error = $_.Exception.Message }
    }
}

Write-Log "======================================" "Cyan"
Write-Log "CourtLink Quick Stability Test Started" "Cyan"
Write-Log "Test Time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" "Cyan"
Write-Log "Target: $BaseUrl" "Cyan"
Write-Log "Rounds: $Rounds" "Cyan"
Write-Log "======================================" "Cyan"

# Basic endpoint tests
Write-Log "Testing Basic Endpoints..." "Yellow"

# Test user list
Test-Endpoint "$BaseUrl/api/users" "GET" $null "User List Query"

# Test court list
Test-Endpoint "$BaseUrl/api/courts" "GET" $null "Court List Query"

# CRUD Testing Rounds
Write-Log "Starting $Rounds CRUD Test Rounds..." "Yellow"

for ($i = 1; $i -le $Rounds; $i++) {
    Write-Log "--- Round $i ---" "Cyan"
    $roundOK = $true
    
    # Generate test user
    $user = @{
        username = "testuser_$(Get-Random -Min 1000 -Max 9999)"
        email = "test$(Get-Random)@example.com"
        password = "password123"
        realName = "Test User $i"
        phoneNumber = "13800000001"
    }
    
    # Register user
    $registerResult = Test-Endpoint "$BaseUrl/api/users/register" "POST" $user "User Registration"
    
    if ($registerResult.Success -and $registerResult.Data.id) {
        $userId = $registerResult.Data.id
        Write-Log "  User created with ID: $userId" "Green"
        
        # Query user
        $queryResult = Test-Endpoint "$BaseUrl/api/users/$userId" "GET" $null "User Query"
        
        if ($queryResult.Success -and $queryResult.Data.username -eq $user.username) {
            Write-Log "  User query verified" "Green"
        } else {
            Write-Log "  User query verification failed" "Red"
            $roundOK = $false
        }
        
        # Delete user
        $deleteResult = Test-Endpoint "$BaseUrl/api/users/$userId" "DELETE" $null "User Deletion"
        
        if ($deleteResult.Success) {
            Write-Log "  User deleted successfully" "Green"
        } else {
            Write-Log "  User deletion failed" "Red"
            $roundOK = $false
        }
    } else {
        $roundOK = $false
    }
    
    if ($roundOK) {
        $Results.RoundsSuccessful++
        Write-Log "  Round ${i} SUCCESS" "Green"
    } else {
        Write-Log "  Round ${i} FAILED" "Red"
    }
    
    # Small delay between rounds
    Start-Sleep -Milliseconds 200
}

# Test Court Operations
Write-Log "Testing Court Operations..." "Yellow"

$court = @{
    name = "Test Court $(Get-Random)"
    location = "Test Location"
    description = "Test Description"
    pricePerHour = 100.0
    status = "AVAILABLE"
}

$courtResult = Test-Endpoint "$BaseUrl/api/courts" "POST" $court "Court Creation"

if ($courtResult.Success -and $courtResult.Data.id) {
    $courtId = $courtResult.Data.id
    Write-Log "Court created with ID: $courtId" "Green"
    
    # Query court
    Test-Endpoint "$BaseUrl/api/courts/$courtId" "GET" $null "Court Query"
    
    # Delete court
    Test-Endpoint "$BaseUrl/api/courts/$courtId" "DELETE" $null "Court Deletion"
}

# Exception Testing
Write-Log "Testing Exception Handling..." "Yellow"

# Test 404 error
$notFoundResult = Test-Endpoint "$BaseUrl/api/users/999999" "GET" $null "404 Error Test"
if (!$notFoundResult.Success) {
    Write-Log "404 Error handling works correctly" "Green"
    $Results.TestsPassed++
} else {
    Write-Log "404 Error handling test failed" "Red"
    $Results.TestsFailed++
}

# Test invalid registration
$invalidUser = @{
    username = "a"
    email = "invalid"
    password = "123"
}

$invalidResult = Test-Endpoint "$BaseUrl/api/users/register" "POST" $invalidUser "Invalid Data Test"
if (!$invalidResult.Success) {
    Write-Log "Invalid data validation works correctly" "Green"
    $Results.TestsPassed++
} else {
    Write-Log "Invalid data validation test failed" "Red"
    $Results.TestsFailed++
}

# Calculate results
$Results.EndTime = Get-Date
$Results.Duration = $Results.EndTime - $Results.StartTime

$totalTests = $Results.TestsPassed + $Results.TestsFailed + $Rounds
$totalPassed = $Results.TestsPassed + $Results.RoundsSuccessful
$successRate = if ($totalTests -gt 0) { [math]::Round(($totalPassed / $totalTests) * 100, 1) } else { 0 }
$roundRate = if ($Rounds -gt 0) { [math]::Round(($Results.RoundsSuccessful / $Rounds) * 100, 1) } else { 0 }

# Results Summary
Write-Log "======================================" "Cyan"
Write-Log "TEST RESULTS SUMMARY" "Cyan"
Write-Log "======================================" "Cyan"

Write-Log "Basic Tests: $($Results.TestsPassed) passed, $($Results.TestsFailed) failed" "White"
Write-Log "CRUD Rounds: $($Results.RoundsSuccessful)/$Rounds successful ($roundRate%)" "White"
Write-Log "Overall Success Rate: $successRate%" "White"
Write-Log "Test Duration: $([int]$Results.Duration.TotalSeconds) seconds" "White"

# System Grade
$grade = "F"
$rec = "Not recommended for deployment"
$gradeColor = "Red"

if ($successRate -ge 98 -and $roundRate -ge 95) {
    $grade = "A+"
    $rec = "Excellent - Highly recommended for immediate production deployment"
    $gradeColor = "Green"
} elseif ($successRate -ge 95 -and $roundRate -ge 90) {
    $grade = "A"
    $rec = "Very Good - Recommended for production deployment"
    $gradeColor = "Green"
} elseif ($successRate -ge 90 -and $roundRate -ge 85) {
    $grade = "B+"
    $rec = "Good - Recommended with minor fixes"
    $gradeColor = "Yellow"
} elseif ($successRate -ge 85) {
    $grade = "B"
    $rec = "Acceptable - Needs improvement"
    $gradeColor = "Yellow"
} elseif ($successRate -ge 70) {
    $grade = "C"
    $rec = "Poor - Significant issues to fix"
    $gradeColor = "Red"
} else {
    $grade = "D"
    $rec = "Failed - Major problems, do not deploy"
    $gradeColor = "Red"
}

Write-Log "======================================" "Cyan"
Write-Log "SYSTEM STABILITY GRADE: $grade" $gradeColor
Write-Log "DEPLOYMENT RECOMMENDATION: $rec" $gradeColor
Write-Log "======================================" "Cyan"

if ($Results.Failures.Count -gt 0) {
    Write-Log "Failure Details:" "Red"
    foreach ($failure in $Results.Failures) {
        Write-Log "  - $failure" "Red"
    }
}

# Save results
$finalResult = @{
    TestMetadata = @{
        TestName = "CourtLink Quick Stability Test"
        Date = (Get-Date).ToString("yyyy-MM-dd")
        Time = (Get-Date).ToString("HH:mm:ss")
        Duration = $Results.Duration.TotalSeconds
        BaseUrl = $BaseUrl
        TestRounds = $Rounds
    }
    Results = @{
        BasicTestsPassed = $Results.TestsPassed
        BasicTestsFailed = $Results.TestsFailed
        RoundsSuccessful = $Results.RoundsSuccessful
        TotalRounds = $Rounds
        OverallSuccessRate = $successRate
        RoundSuccessRate = $roundRate
        SystemGrade = $grade
        Recommendation = $rec
        FailureDetails = $Results.Failures
    }
} | ConvertTo-Json -Depth 5

$fileName = "quick-test-results-$(Get-Date -Format 'yyyyMMdd-HHmmss').json"
$finalResult | Out-File $fileName -Encoding UTF8

Write-Log "Results saved to: $fileName" "Gray"
Write-Log "Test completed at: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" "Gray" 