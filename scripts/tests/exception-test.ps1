# Exception Handling Test Script
param([string]$BaseUrl = "http://localhost:8080")

$ErrorActionPreference = "Continue"
$Results = @{ Total = 0; Passed = 0; Failed = 0; Details = @() }

function Test-ExceptionAPI {
    param($Method, $Uri, $Body, $ExpectedStatus, $TestName)
    $Results.Total++
    
    try {
        $params = @{ 
            Uri = $Uri
            Method = $Method
            UseBasicParsing = $true
            TimeoutSec = 10
        }
        if ($Body) { 
            $params.Body = ($Body | ConvertTo-Json)
            $params.ContentType = "application/json" 
        }
        
        $response = Invoke-WebRequest @params
        $actualStatus = $response.StatusCode
        
    } catch {
        if ($_.Exception.Response) {
            $actualStatus = [int]$_.Exception.Response.StatusCode
        } else {
            $Results.Failed++
            Write-Host "ERROR $TestName - Exception: $($_.Exception.Message)" -ForegroundColor Red
            return
        }
    }
    
    $success = $actualStatus -eq $ExpectedStatus
    
    if ($success) {
        $Results.Passed++
        Write-Host "PASS $TestName - Status: $actualStatus" -ForegroundColor Green
    } else {
        $Results.Failed++
        Write-Host "FAIL $TestName - Expected: $ExpectedStatus, Got: $actualStatus" -ForegroundColor Red
    }
    
    $Results.Details += @{ 
        Name = $TestName
        Success = $success
        ExpectedStatus = $ExpectedStatus
        ActualStatus = $actualStatus
    }
}

Write-Host "=== Exception Handling Test ===" -ForegroundColor Cyan
Write-Host "Testing HTTP error status code consistency" -ForegroundColor Cyan

# Create a test user first
Write-Host "Setting up test data..." -ForegroundColor Yellow
$testUser = @{ username = "exceptiontest"; email = "exceptiontest@test.com"; password = "Test123!"; realName = "Exception Test User" }
try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/api/users/register" -Method POST -Body ($testUser | ConvertTo-Json) -ContentType "application/json" -UseBasicParsing
    $userData = $response.Content | ConvertFrom-Json
    $testUserId = $userData.id
    Write-Host "Created test user ID: $testUserId" -ForegroundColor Green
} catch {
    Write-Host "Failed to create test user - continuing with generic tests" -ForegroundColor Yellow
    $testUserId = 999999
}

# Test Exception Handling
Write-Host "Running exception handling tests..." -ForegroundColor Yellow

# Test 1: Non-existent User (Should be 404 after our fix)
Test-ExceptionAPI "GET" "$BaseUrl/api/users/999999" $null 404 "Access Non-existent User"

# Test 2: Non-existent Court (Should be 404)
Test-ExceptionAPI "GET" "$BaseUrl/api/courts/999999" $null 404 "Access Non-existent Court"

# Test 3: Invalid JSON (Should be 400)
Test-ExceptionAPI "POST" "$BaseUrl/api/users/register" "invalid json" 400 "Send Invalid JSON"

# Test 4: Duplicate Username (Should be 400)
if ($testUserId -ne 999999) {
    $duplicateUser = @{ username = "exceptiontest"; email = "different@test.com"; password = "Test123!"; realName = "Duplicate" }
    Test-ExceptionAPI "POST" "$BaseUrl/api/users/register" $duplicateUser 400 "Register Duplicate Username"
}

# Test 5: Missing Required Fields (Should be 400)
$incompleteUser = @{ username = "incomplete" }
Test-ExceptionAPI "POST" "$BaseUrl/api/users/register" $incompleteUser 400 "Missing Required Fields"

# Test 6: Non-existent Endpoint (Should be 404)
Test-ExceptionAPI "GET" "$BaseUrl/api/nonexistent" $null 404 "Non-existent Endpoint"

# Cleanup
if ($testUserId -ne 999999) {
    Write-Host "Cleaning up test data..." -ForegroundColor Yellow
    try {
        Invoke-WebRequest -Uri "$BaseUrl/api/users/$testUserId" -Method DELETE -UseBasicParsing | Out-Null
        Write-Host "Deleted test user" -ForegroundColor Green
    } catch {
        Write-Host "Failed to delete test user" -ForegroundColor Yellow
    }
}

# Results
$successRate = if ($Results.Total -gt 0) { [math]::Round(($Results.Passed / $Results.Total) * 100, 2) } else { 0 }

Write-Host "=== Exception Handling Test Results ===" -ForegroundColor Cyan
Write-Host "Total Tests: $($Results.Total)" -ForegroundColor White
Write-Host "Passed: $($Results.Passed)" -ForegroundColor Green
Write-Host "Failed: $($Results.Failed)" -ForegroundColor Red
Write-Host "Success Rate: $successRate%" -ForegroundColor $(if ($successRate -eq 100) { "Green" } else { "Yellow" })

# Analysis
Write-Host "=== Analysis ===" -ForegroundColor Cyan
$userTest = $Results.Details | Where-Object { $_.Name -eq "Access Non-existent User" }
$courtTest = $Results.Details | Where-Object { $_.Name -eq "Access Non-existent Court" }

if ($userTest -and $courtTest) {
    if ($userTest.ActualStatus -eq $courtTest.ActualStatus) {
        Write-Host "CONSISTENT: User and Court modules return same status code ($($userTest.ActualStatus)) for non-existent resources" -ForegroundColor Green
    } else {
        Write-Host "INCONSISTENT: User module returns $($userTest.ActualStatus), Court module returns $($courtTest.ActualStatus)" -ForegroundColor Red
        Write-Host "Recommendation: Both should return 404 for non-existent resources" -ForegroundColor Yellow
    }
}

# Save results
$reportFile = "exception-test-results-$(Get-Date -Format 'yyyyMMdd-HHmmss').json"
$Results | ConvertTo-Json -Depth 5 | Out-File -FilePath $reportFile -Encoding UTF8
Write-Host "Results saved to: $reportFile" -ForegroundColor Cyan

Write-Host "Exception handling test complete!" -ForegroundColor Cyan 