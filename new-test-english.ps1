# CourtLink Main Branch Stability Test
# Test Date: 2025-07-01
# Version: 1.0 (Brand New Test)

param(
    [int]$TestRounds = 15,
    [string]$BaseUrl = "http://localhost:8080",
    [int]$TimeoutSeconds = 10
)

# Test Statistics
$TestStats = @{
    StartTime = Get-Date
    TestRounds = $TestRounds
    BasicTests = @{ Total = 0; Passed = 0; Failed = 0 }
    CrudTests = @{ Total = 0; Passed = 0; Failed = 0 }
    ExceptionTests = @{ Total = 0; Passed = 0; Failed = 0 }
    TestDetails = @()
    FailureDetails = @()
}

function Write-TestLog {
    param($Message, $Level = "INFO")
    $timestamp = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
    $color = switch($Level) {
        "INFO" { "White" }
        "SUCCESS" { "Green" }
        "WARNING" { "Yellow" }
        "ERROR" { "Red" }
        "HEADER" { "Cyan" }
        default { "Gray" }
    }
    Write-Host "[$timestamp] $Message" -ForegroundColor $color
}

function Invoke-ApiCall {
    param(
        [string]$Url,
        [string]$Method = "GET",
        [hashtable]$Body = $null,
        [int]$Timeout = 10
    )
    
    try {
        $params = @{
            Uri = $Url
            Method = $Method
            TimeoutSec = $Timeout
            Headers = @{ "Content-Type" = "application/json" }
        }
        
        if ($Body -and $Method -ne "GET") {
            $params.Body = ($Body | ConvertTo-Json -Depth 10)
        }
        
        $response = Invoke-RestMethod @params
        
        return @{
            Success = $true
            StatusCode = 200
            Data = $response
            ResponseTime = (Measure-Command { $response }).TotalMilliseconds
        }
    }
    catch {
        $statusCode = 0
        if ($_.Exception.Response) {
            $statusCode = [int]$_.Exception.Response.StatusCode
        }
        
        return @{
            Success = $false
            StatusCode = $statusCode
            Error = $_.Exception.Message
            ResponseTime = 0
        }
    }
}

function Test-ApplicationHealth {
    Write-TestLog "Starting Health Check Test..." "HEADER"
    
    $healthTest = Invoke-ApiCall "$BaseUrl/actuator/health"
    $TestStats.BasicTests.Total++
    
    if ($healthTest.Success -and $healthTest.Data.status -eq "UP") {
        Write-TestLog "Health Check PASSED - Status: UP" "SUCCESS"
        $TestStats.BasicTests.Passed++
        return $true
    } else {
        Write-TestLog "Health Check FAILED - Error: $($healthTest.Error)" "ERROR"
        $TestStats.BasicTests.Failed++
        $TestStats.FailureDetails += "Health check failed: $($healthTest.Error)"
        return $false
    }
}

function Test-BasicEndpoints {
    Write-TestLog "Starting Basic Endpoints Test..." "HEADER"
    
    # Test User List Endpoint
    $userListTest = Invoke-ApiCall "$BaseUrl/api/users"
    $TestStats.BasicTests.Total++
    
    if ($userListTest.Success) {
        Write-TestLog "User List Endpoint Test PASSED" "SUCCESS"
        $TestStats.BasicTests.Passed++
    } else {
        Write-TestLog "User List Endpoint Test FAILED - Error: $($userListTest.Error)" "ERROR"
        $TestStats.BasicTests.Failed++
        $TestStats.FailureDetails += "User list endpoint failed: $($userListTest.Error)"
    }
    
    # Test Court List Endpoint
    $courtListTest = Invoke-ApiCall "$BaseUrl/api/courts"
    $TestStats.BasicTests.Total++
    
    if ($courtListTest.Success) {
        Write-TestLog "Court List Endpoint Test PASSED" "SUCCESS"
        $TestStats.BasicTests.Passed++
    } else {
        Write-TestLog "Court List Endpoint Test FAILED - Error: $($courtListTest.Error)" "ERROR"
        $TestStats.BasicTests.Failed++
        $TestStats.FailureDetails += "Court list endpoint failed: $($courtListTest.Error)"
    }
    
    # Test Swagger Documentation
    $swaggerTest = Invoke-ApiCall "$BaseUrl/swagger-ui/index.html"
    $TestStats.BasicTests.Total++
    
    if ($swaggerTest.Success) {
        Write-TestLog "Swagger Documentation Test PASSED" "SUCCESS"
        $TestStats.BasicTests.Passed++
    } else {
        Write-TestLog "Swagger Documentation Test FAILED - Error: $($swaggerTest.Error)" "ERROR"
        $TestStats.BasicTests.Failed++
        $TestStats.FailureDetails += "Swagger documentation failed: $($swaggerTest.Error)"
    }
}

function Test-UserCrudOperations {
    param([int]$RoundNumber)
    
    Write-TestLog "Starting Round $RoundNumber User CRUD Test..." "HEADER"
    $roundSuccess = $true
    
    # Generate unique test data
    $timestamp = (Get-Date).Ticks
    $randomId = Get-Random -Min 1000 -Max 9999
    
    $testUser = @{
        username = "testuser_${RoundNumber}_${randomId}"
        email = "test_${RoundNumber}_${randomId}@example.com"
        password = "password123"
        realName = "Test User ${RoundNumber}"
        phoneNumber = "1380000000$RoundNumber"
    }
    
    # 1. User Registration Test
    Write-TestLog "  Executing User Registration..." "INFO"
    $registerTest = Invoke-ApiCall "$BaseUrl/api/users/register" "POST" $testUser
    $TestStats.CrudTests.Total++
    
    if ($registerTest.Success -and $registerTest.Data.id) {
        $userId = $registerTest.Data.id
        Write-TestLog "  User Registration SUCCESS - ID: $userId" "SUCCESS"
        $TestStats.CrudTests.Passed++
        
        # 2. User Query Test
        Write-TestLog "  Executing User Query..." "INFO"
        Start-Sleep -Milliseconds 100
        $queryTest = Invoke-ApiCall "$BaseUrl/api/users/$userId"
        $TestStats.CrudTests.Total++
        
        if ($queryTest.Success -and $queryTest.Data.username -eq $testUser.username) {
            Write-TestLog "  User Query SUCCESS - Data matches" "SUCCESS"
            $TestStats.CrudTests.Passed++
        } else {
            Write-TestLog "  User Query FAILED - Data mismatch or query failed" "ERROR"
            $TestStats.CrudTests.Failed++
            $TestStats.FailureDetails += "Round ${RoundNumber} user query failed"
            $roundSuccess = $false
        }
        
        # 3. Username Check Test
        Write-TestLog "  Executing Username Check..." "INFO"
        $usernameCheckTest = Invoke-ApiCall "$BaseUrl/api/users/check-username/$($testUser.username)"
        $TestStats.CrudTests.Total++
        
        if ($usernameCheckTest.Success) {
            Write-TestLog "  Username Check SUCCESS" "SUCCESS"
            $TestStats.CrudTests.Passed++
        } else {
            Write-TestLog "  Username Check FAILED" "ERROR"
            $TestStats.CrudTests.Failed++
            $TestStats.FailureDetails += "Round ${RoundNumber} username check failed"
            $roundSuccess = $false
        }
        
        # 4. User Deletion Test
        Write-TestLog "  Executing User Deletion..." "INFO"
        $deleteTest = Invoke-ApiCall "$BaseUrl/api/users/$userId" "DELETE"
        $TestStats.CrudTests.Total++
        
        if ($deleteTest.Success) {
            Write-TestLog "  User Deletion SUCCESS" "SUCCESS"
            $TestStats.CrudTests.Passed++
        } else {
            Write-TestLog "  User Deletion FAILED" "ERROR"
            $TestStats.CrudTests.Failed++
            $TestStats.FailureDetails += "Round ${RoundNumber} user deletion failed"
            $roundSuccess = $false
        }
        
    } else {
        Write-TestLog "  User Registration FAILED - Error: $($registerTest.Error)" "ERROR"
        $TestStats.CrudTests.Failed++
        $TestStats.FailureDetails += "Round ${RoundNumber} user registration failed: $($registerTest.Error)"
        $roundSuccess = $false
    }
    
    if ($roundSuccess) {
        Write-TestLog "Round $RoundNumber Test COMPLETED - SUCCESS" "SUCCESS"
    } else {
        Write-TestLog "Round $RoundNumber Test COMPLETED - FAILED" "ERROR"
    }
    
    $TestStats.TestDetails += @{
        Round = $RoundNumber
        Success = $roundSuccess
        Timestamp = Get-Date
    }
    
    return $roundSuccess
}

function Test-CourtOperations {
    Write-TestLog "Starting Court Management Test..." "HEADER"
    
    # Test Court Creation
    $testCourt = @{
        name = "Test Court $(Get-Random)"
        location = "Test Location"
        description = "Test Description"
        pricePerHour = 100.0
        status = "AVAILABLE"
    }
    
    $courtCreateTest = Invoke-ApiCall "$BaseUrl/api/courts" "POST" $testCourt
    $TestStats.CrudTests.Total++
    
    if ($courtCreateTest.Success -and $courtCreateTest.Data.id) {
        $courtId = $courtCreateTest.Data.id
        Write-TestLog "Court Creation SUCCESS - ID: $courtId" "SUCCESS"
        $TestStats.CrudTests.Passed++
        
        # Test Court Query
        $courtQueryTest = Invoke-ApiCall "$BaseUrl/api/courts/$courtId"
        $TestStats.CrudTests.Total++
        
        if ($courtQueryTest.Success) {
            Write-TestLog "Court Query SUCCESS" "SUCCESS"
            $TestStats.CrudTests.Passed++
        } else {
            Write-TestLog "Court Query FAILED" "ERROR"
            $TestStats.CrudTests.Failed++
            $TestStats.FailureDetails += "Court query failed"
        }
        
        # Test Court Deletion
        $courtDeleteTest = Invoke-ApiCall "$BaseUrl/api/courts/$courtId" "DELETE"
        $TestStats.CrudTests.Total++
        
        if ($courtDeleteTest.Success) {
            Write-TestLog "Court Deletion SUCCESS" "SUCCESS"
            $TestStats.CrudTests.Passed++
        } else {
            Write-TestLog "Court Deletion FAILED" "ERROR" 
            $TestStats.CrudTests.Failed++
            $TestStats.FailureDetails += "Court deletion failed"
        }
    } else {
        Write-TestLog "Court Creation FAILED - Error: $($courtCreateTest.Error)" "ERROR"
        $TestStats.CrudTests.Failed++
        $TestStats.FailureDetails += "Court creation failed: $($courtCreateTest.Error)"
    }
}

function Test-ExceptionHandling {
    Write-TestLog "Starting Exception Handling Test..." "HEADER"
    
    # Test 404 Error Handling
    $notFoundTest = Invoke-ApiCall "$BaseUrl/api/users/999999"
    $TestStats.ExceptionTests.Total++
    
    if (!$notFoundTest.Success -and $notFoundTest.StatusCode -eq 404) {
        Write-TestLog "404 Error Handling Test PASSED" "SUCCESS"
        $TestStats.ExceptionTests.Passed++
    } else {
        Write-TestLog "404 Error Handling Test FAILED" "ERROR"
        $TestStats.ExceptionTests.Failed++
        $TestStats.FailureDetails += "404 error handling test failed"
    }
    
    # Test 400 Error Handling (Invalid Data)
    $invalidUser = @{
        username = "a"  # Too short
        email = "invalid"  # Invalid email
        password = "123"  # Too short
    }
    
    $badRequestTest = Invoke-ApiCall "$BaseUrl/api/users/register" "POST" $invalidUser
    $TestStats.ExceptionTests.Total++
    
    if (!$badRequestTest.Success -and $badRequestTest.StatusCode -eq 400) {
        Write-TestLog "400 Error Handling Test PASSED" "SUCCESS"
        $TestStats.ExceptionTests.Passed++
    } else {
        Write-TestLog "400 Error Handling Test FAILED" "ERROR"
        $TestStats.ExceptionTests.Failed++
        $TestStats.FailureDetails += "400 error handling test failed"
    }
}

# Main Test Process Begins
Write-TestLog "========================================" "HEADER"
Write-TestLog "CourtLink Main Branch New Stability Test Started" "HEADER"  
Write-TestLog "Test Time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" "HEADER"
Write-TestLog "Test Rounds: $TestRounds" "HEADER"
Write-TestLog "Target URL: $BaseUrl" "HEADER"
Write-TestLog "========================================" "HEADER"

# Wait for Application Startup
Write-TestLog "Waiting for application startup..." "INFO"
$appReady = $false
$maxWaitTime = 60
$waitTime = 0

while (-not $appReady -and $waitTime -lt $maxWaitTime) {
    try {
        $healthCheck = Invoke-RestMethod "$BaseUrl/actuator/health" -TimeoutSec 5
        if ($healthCheck.status -eq "UP") {
            $appReady = $true
            Write-TestLog "Application is UP and READY!" "SUCCESS"
        }
    }
    catch {
        Start-Sleep 2
        $waitTime += 2
        Write-Host "." -NoNewline -ForegroundColor Gray
    }
}

if (-not $appReady) {
    Write-TestLog "Application startup timeout, test terminated" "ERROR"
    exit 1
}

# Execute Basic Tests
Test-ApplicationHealth
Test-BasicEndpoints

# Execute Multi-Round CRUD Tests
$successfulRounds = 0
for ($round = 1; $round -le $TestRounds; $round++) {
    if (Test-UserCrudOperations -RoundNumber $round) {
        $successfulRounds++
    }
    
    # Rest every 5 rounds
    if ($round % 5 -eq 0 -and $round -lt $TestRounds) {
        Write-TestLog "Round $round completed, brief rest..." "INFO"
        Start-Sleep -Seconds 1
    } else {
        Start-Sleep -Milliseconds 300
    }
}

# Execute Court Management Tests
Test-CourtOperations

# Execute Exception Handling Tests
Test-ExceptionHandling

# Generate Test Report
$TestStats.EndTime = Get-Date
$TestStats.Duration = $TestStats.EndTime - $TestStats.StartTime

$totalTests = $TestStats.BasicTests.Total + $TestStats.CrudTests.Total + $TestStats.ExceptionTests.Total
$totalPassed = $TestStats.BasicTests.Passed + $TestStats.CrudTests.Passed + $TestStats.ExceptionTests.Passed
$totalFailed = $TestStats.BasicTests.Failed + $TestStats.CrudTests.Failed + $TestStats.ExceptionTests.Failed

$overallSuccessRate = if ($totalTests -gt 0) { 
    [math]::Round(($totalPassed / $totalTests) * 100, 2) 
} else { 0 }

$roundSuccessRate = if ($TestRounds -gt 0) { 
    [math]::Round(($successfulRounds / $TestRounds) * 100, 2) 
} else { 0 }

# Output Test Results
Write-TestLog "========================================" "HEADER"
Write-TestLog "Test Results Summary" "HEADER"
Write-TestLog "========================================" "HEADER"

Write-TestLog "Basic Function Tests: $($TestStats.BasicTests.Passed)/$($TestStats.BasicTests.Total) passed" "INFO"
Write-TestLog "CRUD Function Tests: $($TestStats.CrudTests.Passed)/$($TestStats.CrudTests.Total) passed" "INFO"  
Write-TestLog "Exception Handling Tests: $($TestStats.ExceptionTests.Passed)/$($TestStats.ExceptionTests.Total) passed" "INFO"
Write-TestLog "CRUD Round Tests: $successfulRounds/$TestRounds rounds successful" "INFO"

Write-TestLog "Overall Statistics:" "HEADER"
Write-TestLog "  Total Tests: $totalTests" "INFO"
Write-TestLog "  Passed Tests: $totalPassed" "SUCCESS"
Write-TestLog "  Failed Tests: $totalFailed" $(if($totalFailed -gt 0) {"ERROR"} else {"SUCCESS"})
Write-TestLog "  Overall Success Rate: $overallSuccessRate%" $(if($overallSuccessRate -ge 95) {"SUCCESS"} elseif($overallSuccessRate -ge 80) {"WARNING"} else {"ERROR"})
Write-TestLog "  Round Success Rate: $roundSuccessRate%" $(if($roundSuccessRate -ge 95) {"SUCCESS"} elseif($roundSuccessRate -ge 80) {"WARNING"} else {"ERROR"})
Write-TestLog "  Test Duration: $([int]$TestStats.Duration.TotalSeconds) seconds" "INFO"

# System Grading
$systemGrade = "F"
$gradeColor = "ERROR"
$recommendation = "System unstable, deployment not recommended"

if ($overallSuccessRate -ge 98 -and $roundSuccessRate -ge 95) {
    $systemGrade = "A_PLUS"
    $gradeColor = "SUCCESS"
    $recommendation = "System extremely stable, highly recommended for immediate production deployment"
} elseif ($overallSuccessRate -ge 95 -and $roundSuccessRate -ge 90) {
    $systemGrade = "A"
    $gradeColor = "SUCCESS"
    $recommendation = "System stable, recommended for production deployment"
} elseif ($overallSuccessRate -ge 90 -and $roundSuccessRate -ge 85) {
    $systemGrade = "B_PLUS"
    $gradeColor = "WARNING"
    $recommendation = "System relatively stable, recommend small-scale testing before deployment"
} elseif ($overallSuccessRate -ge 85) {
    $systemGrade = "B"
    $gradeColor = "WARNING"
    $recommendation = "System basically stable, needs further optimization"
} elseif ($overallSuccessRate -ge 70) {
    $systemGrade = "C"
    $gradeColor = "ERROR"
    $recommendation = "System not stable enough, needs to fix issues"
} else {
    $systemGrade = "D"
    $gradeColor = "ERROR"
    $recommendation = "System has serious problems, deployment prohibited"
}

Write-TestLog "========================================" "HEADER"
Write-TestLog "System Stability Grade: $systemGrade" $gradeColor
Write-TestLog "Deployment Recommendation: $recommendation" $gradeColor
Write-TestLog "========================================" "HEADER"

# Output Failure Details
if ($TestStats.FailureDetails.Count -gt 0) {
    Write-TestLog "Failure Details:" "ERROR"
    foreach ($failure in $TestStats.FailureDetails) {
        Write-TestLog "  - $failure" "ERROR"
    }
}

# Save Detailed Results
$detailedResult = @{
    TestMetadata = @{
        TestName = "CourtLink Main Branch New Stability Test"
        Version = "1.0"
        Date = (Get-Date).ToString("yyyy-MM-dd")
        Time = (Get-Date).ToString("HH:mm:ss")
        Duration = $TestStats.Duration.TotalSeconds
        TestRounds = $TestRounds
        BaseUrl = $BaseUrl
    }
    Results = @{
        BasicTests = $TestStats.BasicTests
        CrudTests = $TestStats.CrudTests
        ExceptionTests = $TestStats.ExceptionTests
        OverallStats = @{
            TotalTests = $totalTests
            TotalPassed = $totalPassed
            TotalFailed = $totalFailed
            OverallSuccessRate = $overallSuccessRate
            RoundSuccessRate = $roundSuccessRate
            SuccessfulRounds = $successfulRounds
        }
        SystemGrade = @{
            Grade = $systemGrade
            Recommendation = $recommendation
        }
        FailureDetails = $TestStats.FailureDetails
        TestDetails = $TestStats.TestDetails
    }
} | ConvertTo-Json -Depth 10

$resultFileName = "new-stability-test-results-$(Get-Date -Format 'yyyyMMdd-HHmmss').json"
$detailedResult | Out-File $resultFileName -Encoding UTF8

Write-TestLog "Detailed test results saved to: $resultFileName" "INFO"
Write-TestLog "Test completed at: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" "INFO" 