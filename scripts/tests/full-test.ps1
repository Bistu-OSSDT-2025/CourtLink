# Full Functional Test Script for Main Branch
param([string]$BaseUrl = "http://localhost:8080", [int]$TestRounds = 3)

$ErrorActionPreference = "Continue"
$Results = @{ Total = 0; Passed = 0; Failed = 0; Details = @() }

function Test-API {
    param($Method, $Uri, $Body, $ExpectedStatus = 200, $TestName)
    $Results.Total++
    
    try {
        $params = @{ Uri = $Uri; Method = $Method; UseBasicParsing = $true; TimeoutSec = 30 }
        if ($Body) { $params.Body = ($Body | ConvertTo-Json); $params.ContentType = "application/json" }
        
        $response = Invoke-WebRequest @params
        $success = $response.StatusCode -eq $ExpectedStatus
        
        if ($success) {
            $Results.Passed++
            Write-Host "✅ $TestName" -ForegroundColor Green
        } else {
            $Results.Failed++
            Write-Host "❌ $TestName - Expected: $ExpectedStatus, Got: $($response.StatusCode)" -ForegroundColor Red
        }
        
        $Results.Details += @{ Name = $TestName; Success = $success; StatusCode = $response.StatusCode; Response = $response.Content }
        return @{ Success = $success; Response = $response.Content; StatusCode = $response.StatusCode }
        
    } catch {
        $Results.Failed++
        Write-Host "❌ $TestName - Error: $($_.Exception.Message)" -ForegroundColor Red
        $Results.Details += @{ Name = $TestName; Success = $false; Error = $_.Exception.Message }
        return @{ Success = $false; Error = $_.Exception.Message }
    }
}

function Get-JsonData { param($result); if ($result.Response) { try { return $result.Response | ConvertFrom-Json } catch { return $null } } }

Write-Host "🚀 Starting Full Functional Test for Main Branch" -ForegroundColor Cyan
Write-Host "Base URL: $BaseUrl | Test Rounds: $TestRounds" -ForegroundColor Cyan
Write-Host "=" * 60 -ForegroundColor Cyan

# Health Check Tests
Write-Host "📋 Health Check Tests" -ForegroundColor Magenta
Test-API "GET" "$BaseUrl/api/health" $null 200 "System Health Check"
Test-API "GET" "$BaseUrl/api/health/simple" $null 200 "Simple Health Check"
Test-API "GET" "$BaseUrl/api/health/ready" $null 200 "Readiness Check"
Test-API "GET" "$BaseUrl/api/health/live" $null 200 "Liveness Check"
Test-API "GET" "$BaseUrl/api/users/health" $null 200 "User Module Health Check"

# User Management Tests
Write-Host "👥 User Management Tests" -ForegroundColor Magenta
$CreatedUsers = @()

# User Registration
Write-Host "User Registration Tests" -ForegroundColor Yellow
for ($i = 1; $i -le $TestRounds; $i++) {
    $user = @{ username = "fulltest$i"; email = "fulltest$i@test.com"; password = "Test123!"; realName = "Full Test User $i" }
    $result = Test-API "POST" "$BaseUrl/api/users/register" $user 200 "Register User: fulltest$i"
    
    if ($result.Success) {
        $userData = Get-JsonData $result
        if ($userData) { $CreatedUsers += @{ id = $userData.id; username = $userData.username; email = $userData.email; password = $user.password } }
    }
}

Write-Host "Created Users: $($CreatedUsers.Count)" -ForegroundColor Green

# User Login Tests
Write-Host "User Login Tests" -ForegroundColor Yellow
foreach ($user in $CreatedUsers) {
    $loginData = @{ username = $user.username; password = $user.password }
    Test-API "POST" "$BaseUrl/api/users/login" $loginData 200 "Login User: $($user.username)"
}

# User Query Tests
Write-Host "User Query Tests" -ForegroundColor Yellow
if ($CreatedUsers.Count -gt 0) {
    $testUser = $CreatedUsers[0]
    Test-API "GET" "$BaseUrl/api/users/$($testUser.id)" $null 200 "Get User by ID"
    Test-API "GET" "$BaseUrl/api/users/username/$($testUser.username)" $null 200 "Get User by Username"
    Test-API "GET" "$BaseUrl/api/users/email/$($testUser.email)" $null 200 "Get User by Email"
    Test-API "GET" "$BaseUrl/api/users" $null 200 "Get Users List (Paginated)"
    Test-API "GET" "$BaseUrl/api/users/list" $null 200 "Get All Users List"
    Test-API "GET" "$BaseUrl/api/users/check-username?username=$($testUser.username)" $null 200 "Check Username Exists"
    Test-API "GET" "$BaseUrl/api/users/check-email?email=$($testUser.email)" $null 200 "Check Email Exists"
}

# Password Tests
Write-Host "Password Management Tests" -ForegroundColor Yellow
if ($CreatedUsers.Count -gt 0) {
    $testUser = $CreatedUsers[0]
    Test-API "POST" "$BaseUrl/api/users/$($testUser.id)/validate-password" @{ password = $testUser.password } 200 "Validate Correct Password"
    Test-API "POST" "$BaseUrl/api/users/$($testUser.id)/validate-password" @{ password = "wrong" } 200 "Validate Wrong Password"
    Test-API "POST" "$BaseUrl/api/users/$($testUser.id)/change-password" @{ oldPassword = $testUser.password; newPassword = "NewTest123!" } 200 "Change Password"
    Test-API "POST" "$BaseUrl/api/users/$($testUser.id)/reset-password" @{ newPassword = "ResetTest123!" } 200 "Reset Password"
}

# User Status Tests
Write-Host "User Status Management Tests" -ForegroundColor Yellow
if ($CreatedUsers.Count -gt 0) {
    $testUser = $CreatedUsers[0]
    Test-API "POST" "$BaseUrl/api/users/$($testUser.id)/deactivate" $null 200 "Deactivate User"
    Test-API "POST" "$BaseUrl/api/users/$($testUser.id)/activate" $null 200 "Activate User"
    Test-API "POST" "$BaseUrl/api/users/$($testUser.id)/toggle-status" $null 200 "Toggle User Status"
}

# User Update Tests
Write-Host "User Update Tests" -ForegroundColor Yellow
if ($CreatedUsers.Count -gt 0) {
    $testUser = $CreatedUsers[0]
    $updateData = @{ username = $testUser.username; email = "updated_$($testUser.email.Replace('@', '_at_'))@test.com"; realName = "Updated Name"; password = "Updated123!" }
    Test-API "PUT" "$BaseUrl/api/users/$($testUser.id)" $updateData 200 "Update User Information"
}

# Court Management Tests
Write-Host "🏟️ Court Management Tests" -ForegroundColor Magenta
$CreatedCourts = @()

# Court Creation
Write-Host "Court Creation Tests" -ForegroundColor Yellow
for ($i = 1; $i -le $TestRounds; $i++) {
    $court = @{ name = "Full Test Court $i"; location = "Test Location $i"; description = "Description for Full Test Court $i"; status = "AVAILABLE" }
    $result = Test-API "POST" "$BaseUrl/api/courts" $court 201 "Create Court: Full Test Court $i"
    
    if ($result.Success) {
        $courtData = Get-JsonData $result
        if ($courtData) { $CreatedCourts += @{ id = $courtData.id; name = $courtData.name; location = $courtData.location } }
    }
}

Write-Host "Created Courts: $($CreatedCourts.Count)" -ForegroundColor Green

# Court Query Tests
Write-Host "Court Query Tests" -ForegroundColor Yellow
Test-API "GET" "$BaseUrl/api/courts/all" $null 200 "Get All Courts"
Test-API "GET" "$BaseUrl/api/courts?keyword=Full" $null 200 "Search Courts by Keyword"
Test-API "GET" "$BaseUrl/api/courts?status=AVAILABLE" $null 200 "Search Courts by Status"

if ($CreatedCourts.Count -gt 0) {
    $testCourt = $CreatedCourts[0]
    Test-API "GET" "$BaseUrl/api/courts/$($testCourt.id)" $null 200 "Get Court Details"
}

# Court Update and Status Tests
Write-Host "Court Update and Status Tests" -ForegroundColor Yellow
if ($CreatedCourts.Count -gt 0) {
    $testCourt = $CreatedCourts[0]
    $updateData = @{ name = "Updated $($testCourt.name)"; location = "Updated $($testCourt.location)"; description = "Updated description"; status = "AVAILABLE" }
    Test-API "PUT" "$BaseUrl/api/courts/$($testCourt.id)" $updateData 200 "Update Court Information"
    Test-API "PATCH" "$BaseUrl/api/courts/$($testCourt.id)/status?status=MAINTENANCE" $null 200 "Change Court Status to Maintenance"
}

# Exception Handling Tests
Write-Host "⚠️ Exception Handling Tests" -ForegroundColor Magenta
Test-API "GET" "$BaseUrl/api/users/999999" $null 400 "Access Non-existent User"
Test-API "GET" "$BaseUrl/api/courts/999999" $null 404 "Access Non-existent Court"
Test-API "POST" "$BaseUrl/api/users/register" "invalid json" 400 "Send Invalid JSON"

# Duplicate Data Tests
if ($CreatedUsers.Count -gt 0) {
    $existingUser = $CreatedUsers[0]
    $duplicateUser = @{ username = $existingUser.username; email = "different@test.com"; password = "Test123!"; realName = "Duplicate Username" }
    Test-API "POST" "$BaseUrl/api/users/register" $duplicateUser 400 "Register Duplicate Username"
}

# Data Cleanup
Write-Host "🧹 Data Cleanup" -ForegroundColor Magenta
foreach ($court in $CreatedCourts) {
    Test-API "DELETE" "$BaseUrl/api/courts/$($court.id)" $null 204 "Delete Court: $($court.name)"
}

foreach ($user in $CreatedUsers) {
    Test-API "DELETE" "$BaseUrl/api/users/$($user.id)" $null 200 "Delete User: $($user.username)"
}

# Results Summary
$successRate = if ($Results.Total -gt 0) { [math]::Round(($Results.Passed / $Results.Total) * 100, 2) } else { 0 }

Write-Host "=" * 60 -ForegroundColor Cyan
Write-Host "📊 Full Functional Test Results" -ForegroundColor Cyan
Write-Host "=" * 60 -ForegroundColor Cyan
Write-Host "Total Tests: $($Results.Total)" -ForegroundColor White
Write-Host "Passed: $($Results.Passed)" -ForegroundColor Green
Write-Host "Failed: $($Results.Failed)" -ForegroundColor Red
Write-Host "Success Rate: $successRate%" -ForegroundColor $(if ($successRate -ge 95) { "Green" } elseif ($successRate -ge 80) { "Yellow" } else { "Red" })

# System Grade
$grade = if ($successRate -ge 98) { "A+" } elseif ($successRate -ge 95) { "A" } elseif ($successRate -ge 90) { "B+" } elseif ($successRate -ge 80) { "B" } elseif ($successRate -ge 70) { "C" } else { "D" }
$gradeColor = if ($grade -match "A") { "Green" } elseif ($grade -match "B") { "Yellow" } else { "Red" }

Write-Host "🏆 System Grade: $grade" -ForegroundColor $gradeColor

$recommendation = switch ($grade) {
    "A+" { "🟢 Excellent: Ready for production deployment" }
    "A"  { "🟢 Very Good: Suitable for production deployment" }
    "B+" { "🟡 Good: Minor issues, consider optimization" }
    "B"  { "🟡 Fair: Some issues need attention" }
    "C"  { "🟠 Poor: Significant issues need fixing" }
    "D"  { "🔴 Critical: Major problems, not ready for deployment" }
}

Write-Host "💡 Recommendation: $recommendation" -ForegroundColor Cyan

# Save Results
$reportFile = "full-test-results-$(Get-Date -Format 'yyyyMMdd-HHmmss').json"
$Results | ConvertTo-Json -Depth 10 | Out-File -FilePath $reportFile -Encoding UTF8
Write-Host "📄 Report saved: $reportFile" -ForegroundColor Cyan

if ($Results.Failed -gt 0) {
    Write-Host "❌ Failed Tests:" -ForegroundColor Red
    $Results.Details | Where-Object { -not $_.Success } | ForEach-Object { Write-Host "  • $($_.Name): $($_.Error -or 'Status code mismatch')" -ForegroundColor Red }
}

Write-Host "🎯 Full Functional Test Complete!" -ForegroundColor Cyan 