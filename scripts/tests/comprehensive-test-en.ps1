# Main Branch Comprehensive Functional Test Script
# Test Coverage: User Management, Court Management, Health Check, Exception Handling

param(
    [string]$BaseUrl = "http://localhost:8080",
    [int]$TestRounds = 3,
    [switch]$Verbose
)

$ErrorActionPreference = "Continue"
$ProgressPreference = "SilentlyContinue"

# Test Statistics
$TestResults = @{
    StartTime = Get-Date
    TotalTests = 0
    PassedTests = 0
    FailedTests = 0
    TestDetails = @()
    Performance = @{
        FastestResponse = [int]::MaxValue
        SlowestResponse = 0
        AverageResponse = 0
        TotalResponseTime = 0
    }
}

# Color Output Function
function Write-ColorOutput {
    param([string]$Message, [string]$Color = "White")
    switch ($Color) {
        "Green" { Write-Host $Message -ForegroundColor Green }
        "Red" { Write-Host $Message -ForegroundColor Red }
        "Yellow" { Write-Host $Message -ForegroundColor Yellow }
        "Cyan" { Write-Host $Message -ForegroundColor Cyan }
        "Magenta" { Write-Host $Message -ForegroundColor Magenta }
        default { Write-Host $Message }
    }
}

# HTTP Request Function
function Invoke-TestRequest {
    param(
        [string]$Method,
        [string]$Uri,
        [object]$Body,
        [string]$ContentType = "application/json",
        [int]$ExpectedStatusCode = 200,
        [string]$TestName
    )
    
    $TestResults.TotalTests++
    $StartTime = Get-Date
    
    try {
        $RequestParams = @{
            Uri = $Uri
            Method = $Method
            ContentType = $ContentType
            UseBasicParsing = $true
            TimeoutSec = 30
        }
        
        if ($Body) {
            if ($Body -is [string]) {
                $RequestParams.Body = $Body
            } else {
                $RequestParams.Body = $Body | ConvertTo-Json -Depth 10
            }
        }
        
        $Response = Invoke-WebRequest @RequestParams
        $ResponseTime = (Get-Date) - $StartTime
        $ResponseTimeMs = [int]$ResponseTime.TotalMilliseconds
        
        # Update Performance Statistics
        $TestResults.Performance.TotalResponseTime += $ResponseTimeMs
        if ($ResponseTimeMs -lt $TestResults.Performance.FastestResponse) {
            $TestResults.Performance.FastestResponse = $ResponseTimeMs
        }
        if ($ResponseTimeMs -gt $TestResults.Performance.SlowestResponse) {
            $TestResults.Performance.SlowestResponse = $ResponseTimeMs
        }
        
        $Success = $Response.StatusCode -eq $ExpectedStatusCode
        
        if ($Success) {
            $TestResults.PassedTests++
            if ($Verbose) {
                Write-ColorOutput "  ✅ $TestName - Response Time: ${ResponseTimeMs}ms" "Green"
            }
        } else {
            $TestResults.FailedTests++
            Write-ColorOutput "  ❌ $TestName - Expected: $ExpectedStatusCode, Actual: $($Response.StatusCode)" "Red"
        }
        
        $TestDetail = @{
            TestName = $TestName
            Method = $Method
            Uri = $Uri
            ExpectedStatusCode = $ExpectedStatusCode
            ActualStatusCode = $Response.StatusCode
            ResponseTime = $ResponseTimeMs
            Success = $Success
            Response = $Response.Content
            Timestamp = Get-Date
        }
        
        $TestResults.TestDetails += $TestDetail
        return $TestDetail
        
    } catch {
        $ResponseTime = (Get-Date) - $StartTime
        $ResponseTimeMs = [int]$ResponseTime.TotalMilliseconds
        
        $TestResults.FailedTests++
        Write-ColorOutput "  ❌ $TestName - Error: $($_.Exception.Message)" "Red"
        
        $TestDetail = @{
            TestName = $TestName
            Method = $Method
            Uri = $Uri
            ExpectedStatusCode = $ExpectedStatusCode
            ActualStatusCode = $null
            ResponseTime = $ResponseTimeMs
            Success = $false
            Error = $_.Exception.Message
            Timestamp = Get-Date
        }
        
        $TestResults.TestDetails += $TestDetail
        return $TestDetail
    }
}

# Parse JSON Response
function Get-JsonResponse {
    param([object]$TestDetail)
    if ($TestDetail.Response) {
        try {
            return $TestDetail.Response | ConvertFrom-Json
        } catch {
            return $null
        }
    }
    return $null
}

Write-ColorOutput "🚀 Starting Main Branch Comprehensive Functional Test" "Cyan"
Write-ColorOutput "Base URL: $BaseUrl" "Cyan"
Write-ColorOutput "Test Rounds: $TestRounds" "Cyan"
Write-ColorOutput "Verbose Mode: $($Verbose.IsPresent)" "Cyan"
Write-ColorOutput "=" * 70 "Cyan"

# Part 1: System Health Check Tests
Write-ColorOutput "📋 Part 1: System Health Check Tests" "Magenta"

$HealthTests = @(
    @{ Uri = "$BaseUrl/api/health"; Name = "System Health Check" },
    @{ Uri = "$BaseUrl/api/health/simple"; Name = "Simple Health Check" },
    @{ Uri = "$BaseUrl/api/health/ready"; Name = "Readiness Check" },
    @{ Uri = "$BaseUrl/api/health/live"; Name = "Liveness Check" },
    @{ Uri = "$BaseUrl/api/users/health"; Name = "User Module Health Check" }
)

foreach ($Test in $HealthTests) {
    $Result = Invoke-TestRequest -Method "GET" -Uri $Test.Uri -TestName $Test.Name
}

# Part 2: User Management Functional Tests
Write-ColorOutput "👥 Part 2: User Management Functional Tests" "Magenta"

# Test Data
$TestUsers = @()
for ($i = 1; $i -le $TestRounds; $i++) {
    $TestUsers += @{
        username = "comptest_user_$i"
        email = "comptest$i@test.com"
        password = "CompTest123!"
        realName = "Comprehensive Test User $i"
    }
}

# User Registration Tests
Write-ColorOutput "2.1 User Registration Tests" "Yellow"
$CreatedUsers = @()

foreach ($User in $TestUsers) {
    $Result = Invoke-TestRequest -Method "POST" -Uri "$BaseUrl/api/users/register" -Body $User -TestName "Register User: $($User.username)"
    if ($Result.Success) {
        $UserData = Get-JsonResponse $Result
        if ($UserData) {
            $CreatedUsers += @{
                id = $UserData.id
                username = $UserData.username
                email = $UserData.email
                password = $User.password
            }
        }
    }
}

Write-ColorOutput "Created Users Count: $($CreatedUsers.Count)" "Green"

# User Login Tests
Write-ColorOutput "2.2 User Login Tests" "Yellow"
$LoginTokens = @()
foreach ($User in $CreatedUsers) {
    $LoginData = @{
        username = $User.username
        password = $User.password
    }
    $Result = Invoke-TestRequest -Method "POST" -Uri "$BaseUrl/api/users/login" -Body $LoginData -TestName "User Login: $($User.username)"
    if ($Result.Success) {
        $LoginResponse = Get-JsonResponse $Result
        if ($LoginResponse -and $LoginResponse.token) {
            $LoginTokens += @{
                userId = $User.id
                token = $LoginResponse.token
            }
        }
    }
}

# User Query Tests
Write-ColorOutput "2.3 User Query Tests" "Yellow"
if ($CreatedUsers.Count -gt 0) {
    $TestUser = $CreatedUsers[0]
    
    # Various Query Methods
    $Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/users/$($TestUser.id)" -TestName "Query User by ID"
    $Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/users/username/$($TestUser.username)" -TestName "Query User by Username"
    $Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/users/email/$($TestUser.email)" -TestName "Query User by Email"
    $Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/users" -TestName "Paginated User List Query"
    $Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/users/list" -TestName "All Users List Query"
    
    # Existence Checks
    $Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/users/check-username?username=$($TestUser.username)" -TestName "Check Username Exists"
    $Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/users/check-email?email=$($TestUser.email)" -TestName "Check Email Exists"
    
    # Non-existent Checks
    $Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/users/check-username?username=nonexistent_user" -TestName "Check Non-existent Username"
    $Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/users/check-email?email=nonexistent@test.com" -TestName "Check Non-existent Email"
}

# Password Related Tests
Write-ColorOutput "2.4 Password Related Tests" "Yellow"
if ($CreatedUsers.Count -gt 0) {
    $TestUser = $CreatedUsers[0]
    
    # Password Validation
    $PasswordData = @{ password = $TestUser.password }
    $Result = Invoke-TestRequest -Method "POST" -Uri "$BaseUrl/api/users/$($TestUser.id)/validate-password" -Body $PasswordData -TestName "Validate Correct Password"
    
    # Wrong Password Validation
    $WrongPasswordData = @{ password = "wrong_password" }
    $Result = Invoke-TestRequest -Method "POST" -Uri "$BaseUrl/api/users/$($TestUser.id)/validate-password" -Body $WrongPasswordData -TestName "Validate Wrong Password"
    
    # Change Password
    $ChangePasswordData = @{
        oldPassword = $TestUser.password
        newPassword = "NewCompTest123!"
    }
    $Result = Invoke-TestRequest -Method "POST" -Uri "$BaseUrl/api/users/$($TestUser.id)/change-password" -Body $ChangePasswordData -TestName "Change User Password"
    
    # Reset Password
    $ResetPasswordData = @{ newPassword = "ResetCompTest123!" }
    $Result = Invoke-TestRequest -Method "POST" -Uri "$BaseUrl/api/users/$($TestUser.id)/reset-password" -Body $ResetPasswordData -TestName "Reset User Password"
}

# User Status Management Tests
Write-ColorOutput "2.5 User Status Management Tests" "Yellow"
if ($CreatedUsers.Count -gt 1) {
    $TestUser1 = $CreatedUsers[0]
    $TestUser2 = $CreatedUsers[1]
    
    $Result = Invoke-TestRequest -Method "POST" -Uri "$BaseUrl/api/users/$($TestUser1.id)/deactivate" -TestName "Deactivate User"
    $Result = Invoke-TestRequest -Method "POST" -Uri "$BaseUrl/api/users/$($TestUser1.id)/activate" -TestName "Activate User"
    $Result = Invoke-TestRequest -Method "POST" -Uri "$BaseUrl/api/users/$($TestUser2.id)/toggle-status" -TestName "Toggle User Status"
}

# User Information Update Tests
Write-ColorOutput "2.6 User Information Update Tests" "Yellow"
if ($CreatedUsers.Count -gt 0) {
    $TestUser = $CreatedUsers[0]
    $UpdateData = @{
        username = $TestUser.username
        email = "updated_$($TestUser.email)"
        realName = "Updated User Name"
        password = "UpdatedCompTest123!"
    }
    $Result = Invoke-TestRequest -Method "PUT" -Uri "$BaseUrl/api/users/$($TestUser.id)" -Body $UpdateData -TestName "Update User Information"
}

# Part 3: Court Management Functional Tests
Write-ColorOutput "🏟️ Part 3: Court Management Functional Tests" "Magenta"

# Court Creation Tests
Write-ColorOutput "3.1 Court Creation Tests" "Yellow"
$TestCourts = @()
for ($i = 1; $i -le $TestRounds; $i++) {
    $CourtData = @{
        name = "Comprehensive Test Court $i"
        location = "Test Location $i"
        description = "Description for Comprehensive Test Court $i"
        status = "AVAILABLE"
    }
    
    $Result = Invoke-TestRequest -Method "POST" -Uri "$BaseUrl/api/courts" -Body $CourtData -TestName "Create Court: $($CourtData.name)" -ExpectedStatusCode 201
    if ($Result.Success) {
        $CourtResponse = Get-JsonResponse $Result
        if ($CourtResponse) {
            $TestCourts += @{
                id = $CourtResponse.id
                name = $CourtResponse.name
                location = $CourtResponse.location
            }
        }
    }
}

Write-ColorOutput "Created Courts Count: $($TestCourts.Count)" "Green"

# Court Query Tests
Write-ColorOutput "3.2 Court Query Tests" "Yellow"
$Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/courts/all" -TestName "Query All Courts"
$Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/courts?keyword=Comprehensive" -TestName "Search Courts by Keyword"
$Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/courts?status=AVAILABLE" -TestName "Search Courts by Status"

if ($TestCourts.Count -gt 0) {
    $TestCourt = $TestCourts[0]
    $Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/courts/$($TestCourt.id)" -TestName "Get Court Details"
}

# Court Update and Status Management Tests
Write-ColorOutput "3.3 Court Update and Status Management Tests" "Yellow"
if ($TestCourts.Count -gt 1) {
    $TestCourt1 = $TestCourts[0]
    $TestCourt2 = $TestCourts[1]
    
    # Update Court Information
    $UpdateData = @{
        name = "Updated $($TestCourt1.name)"
        location = "Updated $($TestCourt1.location)"
        description = "Updated court description"
        status = "AVAILABLE"
    }
    $Result = Invoke-TestRequest -Method "PUT" -Uri "$BaseUrl/api/courts/$($TestCourt1.id)" -Body $UpdateData -TestName "Update Court Information"
    
    # Change Court Status
    $Result = Invoke-TestRequest -Method "PATCH" -Uri "$BaseUrl/api/courts/$($TestCourt1.id)/status?status=MAINTENANCE" -TestName "Set Court to Maintenance Status"
    $Result = Invoke-TestRequest -Method "PATCH" -Uri "$BaseUrl/api/courts/$($TestCourt2.id)/status?status=UNAVAILABLE" -TestName "Set Court to Unavailable Status"
}

# Part 4: Exception Handling and Edge Case Tests
Write-ColorOutput "⚠️ Part 4: Exception Handling and Edge Case Tests" "Magenta"

# Invalid Request Tests
Write-ColorOutput "4.1 Invalid Request Tests" "Yellow"

# Invalid JSON Format
$Result = Invoke-TestRequest -Method "POST" -Uri "$BaseUrl/api/users/register" -Body "invalid json" -TestName "Send Invalid JSON Format" -ExpectedStatusCode 400

# Missing Required Fields
$IncompleteUser = @{ username = "incomplete" }
$Result = Invoke-TestRequest -Method "POST" -Uri "$BaseUrl/api/users/register" -Body $IncompleteUser -TestName "Register User with Missing Required Fields" -ExpectedStatusCode 400

# Duplicate Data Tests
Write-ColorOutput "4.2 Duplicate Data Tests" "Yellow"

if ($CreatedUsers.Count -gt 0) {
    $ExistingUser = $CreatedUsers[0]
    $DuplicateUser = @{
        username = $ExistingUser.username
        email = "different@email.com"
        password = "CompTest123!"
        realName = "Duplicate Username"
    }
    
    $Result = Invoke-TestRequest -Method "POST" -Uri "$BaseUrl/api/users/register" -Body $DuplicateUser -TestName "Register Duplicate Username" -ExpectedStatusCode 400
    
    $DuplicateEmailUser = @{
        username = "different_username"
        email = $ExistingUser.email
        password = "CompTest123!"
        realName = "Duplicate Email"
    }
    
    $Result = Invoke-TestRequest -Method "POST" -Uri "$BaseUrl/api/users/register" -Body $DuplicateEmailUser -TestName "Register Duplicate Email" -ExpectedStatusCode 400
}

# Non-existent Resource Access Tests
Write-ColorOutput "4.3 Non-existent Resource Access Tests" "Yellow"

# Access Non-existent User
$Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/users/999999" -TestName "Access Non-existent User" -ExpectedStatusCode 400

# Access Non-existent Court
$Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/courts/999999" -TestName "Access Non-existent Court" -ExpectedStatusCode 404

# Access Non-existent Endpoint
$Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/nonexistent" -TestName "Access Non-existent Endpoint" -ExpectedStatusCode 404

# Part 5: Performance and Stress Tests
Write-ColorOutput "⚡ Part 5: Performance and Stress Tests" "Magenta"

Write-ColorOutput "5.1 Concurrent Request Tests" "Yellow"

# Concurrent User List Queries
$Jobs = @()
for ($i = 1; $i -le 10; $i++) {
    $Job = Start-Job -ScriptBlock {
        param($BaseUrl, $i)
        try {
            $Response = Invoke-WebRequest -Uri "$BaseUrl/api/users" -Method GET -UseBasicParsing -TimeoutSec 10
            return @{ Index = $i; StatusCode = $Response.StatusCode; Success = $true }
        } catch {
            return @{ Index = $i; StatusCode = $null; Success = $false; Error = $_.Exception.Message }
        }
    } -ArgumentList $BaseUrl, $i
    $Jobs += $Job
}

$ConcurrentResults = $Jobs | Wait-Job | Receive-Job
$Jobs | Remove-Job

$ConcurrentSuccess = ($ConcurrentResults | Where-Object { $_.Success }).Count
Write-ColorOutput "Concurrent Requests Success: $ConcurrentSuccess/10" "Green"

# Part 6: Data Cleanup
Write-ColorOutput "🧹 Part 6: Data Cleanup" "Magenta"

# Cleanup Test Courts
Write-ColorOutput "6.1 Cleanup Test Courts" "Yellow"
foreach ($Court in $TestCourts) {
    $Result = Invoke-TestRequest -Method "DELETE" -Uri "$BaseUrl/api/courts/$($Court.id)" -TestName "Delete Court: $($Court.name)" -ExpectedStatusCode 204
}

# Cleanup Test Users
Write-ColorOutput "6.2 Cleanup Test Users" "Yellow"
foreach ($User in $CreatedUsers) {
    $Result = Invoke-TestRequest -Method "DELETE" -Uri "$BaseUrl/api/users/$($User.id)" -TestName "Delete User: $($User.username)"
}

# Test Results Statistics and Report
$TestResults.EndTime = Get-Date
$TestResults.Duration = $TestResults.EndTime - $TestResults.StartTime
$TestResults.Performance.AverageResponse = if ($TestResults.TotalTests -gt 0) { 
    [math]::Round($TestResults.Performance.TotalResponseTime / $TestResults.TotalTests, 2) 
} else { 0 }

$SuccessRate = if ($TestResults.TotalTests -gt 0) { 
    [math]::Round(($TestResults.PassedTests / $TestResults.TotalTests) * 100, 2) 
} else { 0 }

Write-ColorOutput "=" * 70 "Cyan"
Write-ColorOutput "📊 Comprehensive Functional Test Complete Report" "Cyan"
Write-ColorOutput "=" * 70 "Cyan"

Write-ColorOutput "⏱️  Test Period: $($TestResults.StartTime.ToString('yyyy-MM-dd HH:mm:ss')) - $($TestResults.EndTime.ToString('yyyy-MM-dd HH:mm:ss'))" "White"
Write-ColorOutput "⏳ Total Duration: $([math]::Round($TestResults.Duration.TotalSeconds, 2)) seconds" "White"
Write-ColorOutput "🎯 Total Tests: $($TestResults.TotalTests)" "White"
Write-ColorOutput "✅ Passed Tests: $($TestResults.PassedTests)" "Green"
Write-ColorOutput "❌ Failed Tests: $($TestResults.FailedTests)" "Red"
Write-ColorOutput "📈 Success Rate: $SuccessRate%" $(if ($SuccessRate -ge 95) { "Green" } elseif ($SuccessRate -ge 80) { "Yellow" } else { "Red" })

Write-ColorOutput "🚀 Performance Statistics:" "Cyan"
Write-ColorOutput "  • Fastest Response: $($TestResults.Performance.FastestResponse)ms" "Green"
Write-ColorOutput "  • Slowest Response: $($TestResults.Performance.SlowestResponse)ms" "Yellow"
Write-ColorOutput "  • Average Response: $($TestResults.Performance.AverageResponse)ms" "White"

# System Grading
$Grade = ""
$GradeColor = ""
$Score = 0

if ($SuccessRate -ge 98 -and $TestResults.Performance.AverageResponse -lt 100) {
    $Grade = "A+"
    $GradeColor = "Green"
    $Score = 99
} elseif ($SuccessRate -ge 95 -and $TestResults.Performance.AverageResponse -lt 200) {
    $Grade = "A"
    $GradeColor = "Green"
    $Score = 95
} elseif ($SuccessRate -ge 90 -and $TestResults.Performance.AverageResponse -lt 500) {
    $Grade = "B+"
    $GradeColor = "Yellow"
    $Score = 90
} elseif ($SuccessRate -ge 80) {
    $Grade = "B"
    $GradeColor = "Yellow"
    $Score = 80
} elseif ($SuccessRate -ge 70) {
    $Grade = "C"
    $GradeColor = "Red"
    $Score = 70
} else {
    $Grade = "D"
    $GradeColor = "Red"
    $Score = 60
}

Write-ColorOutput "🏆 System Grade: $Grade Level ($Score points)" $GradeColor

$Recommendation = switch ($Grade) {
    "A+" { "🟢 Highly Recommended: Perfect system stability, ready for immediate production deployment" }
    "A"  { "🟢 Recommended: High system stability, suitable for production deployment" }
    "B+" { "🟡 Cautiously Recommended: System is basically stable, suggest optimization before deployment" }
    "B"  { "🟡 Needs Improvement: Some issues exist, suggest fixes before deployment" }
    "C"  { "🟠 Not Recommended: Many issues, needs significant improvement" }
    "D"  { "🔴 Serious Issues: System is unstable, not suitable for deployment" }
}

Write-ColorOutput "💡 Deployment Recommendation: $Recommendation" "Cyan"

# Save Detailed Test Results
$ReportData = @{
    TestSummary = $TestResults
    TestConfiguration = @{
        BaseUrl = $BaseUrl
        TestRounds = $TestRounds
        VerboseMode = $Verbose.IsPresent
    }
    SystemGrade = @{
        Grade = $Grade
        Score = $Score
        Recommendation = $Recommendation
    }
    ConcurrentTestResults = $ConcurrentResults
}

$ReportFile = "comprehensive-test-results-$(Get-Date -Format 'yyyyMMdd-HHmmss').json"
$ReportData | ConvertTo-Json -Depth 10 | Out-File -FilePath $ReportFile -Encoding UTF8

Write-ColorOutput "📄 Detailed test report saved: $ReportFile" "Cyan"

# Display Failed Test Details if any
if ($TestResults.FailedTests -gt 0) {
    Write-ColorOutput "❌ Failed Test Details:" "Red"
    $FailedTests = $TestResults.TestDetails | Where-Object { -not $_.Success }
    foreach ($Test in $FailedTests) {
        Write-ColorOutput "  • $($Test.TestName): $($Test.Error -or "Status code mismatch")" "Red"
    }
}

Write-ColorOutput "=" * 70 "Cyan"
Write-ColorOutput "🎯 Comprehensive Functional Test Script Execution Complete!" "Cyan" 