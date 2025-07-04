# System Stability Rating Script
param([string]$BaseUrl = "http://localhost:8080")

$ErrorActionPreference = "Continue"
Write-Host "🏆 CourtLink System Stability Rating" -ForegroundColor Cyan
Write-Host "Target: $BaseUrl" -ForegroundColor Cyan
Write-Host "Rating Time: $(Get-Date)" -ForegroundColor Cyan
Write-Host "=" * 50 -ForegroundColor Cyan

# Test Categories
$Results = @{
    CoreFunctionality = @{ Total = 0; Passed = 0; Tests = @() }
    ExceptionHandling = @{ Total = 0; Passed = 0; Tests = @() }
    Performance = @{ Total = 0; Passed = 0; Tests = @() }
    DataConsistency = @{ Total = 0; Passed = 0; Tests = @() }
}

function Test-Endpoint {
    param($Method, $Uri, $Body, $ExpectedStatus = 200, $TestName, $Category)
    $Results.$Category.Total++
    $start = Get-Date
    
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
            $actualStatus = 0
        }
    }
    
    $duration = (Get-Date) - $start
    $success = $actualStatus -eq $ExpectedStatus
    
    if ($success) {
        $Results.$Category.Passed++
        Write-Host "✅ $TestName ($($duration.TotalMilliseconds)ms)" -ForegroundColor Green
    } else {
        Write-Host "❌ $TestName - Expected: $ExpectedStatus, Got: $actualStatus" -ForegroundColor Red
    }
    
    $Results.$Category.Tests += @{ 
        Name = $TestName
        Success = $success
        Duration = $duration.TotalMilliseconds
        Status = $actualStatus
    }
    
    return @{ Success = $success; Response = $response; Duration = $duration.TotalMilliseconds }
}

# Core Functionality Tests
Write-Host "🔧 Core Functionality Tests" -ForegroundColor Yellow

# Basic Health Checks
Test-Endpoint "GET" "$BaseUrl/api/health" $null 200 "System Health Check" "CoreFunctionality"
Test-Endpoint "GET" "$BaseUrl/api/users/health" $null 200 "User Module Health" "CoreFunctionality"

# Basic CRUD Operations
$testUser = @{ username = "ratingtest"; email = "ratingtest@test.com"; password = "Test123!"; realName = "Rating Test User" }
$registerResult = Test-Endpoint "POST" "$BaseUrl/api/users/register" $testUser 200 "User Registration" "CoreFunctionality"

if ($registerResult.Success) {
    $userData = $registerResult.Response.Content | ConvertFrom-Json
    $userId = $userData.id
    
    Test-Endpoint "GET" "$BaseUrl/api/users/$userId" $null 200 "User Query by ID" "CoreFunctionality"
    Test-Endpoint "GET" "$BaseUrl/api/users" $null 200 "User List Query" "CoreFunctionality"
    
    # Court Operations
    $testCourt = @{ name = "Rating Test Court"; location = "Test Location"; description = "Rating test court"; status = "AVAILABLE" }
    $courtResult = Test-Endpoint "POST" "$BaseUrl/api/courts" $testCourt 201 "Court Creation" "CoreFunctionality"
    
    if ($courtResult.Success) {
        $courtData = $courtResult.Response.Content | ConvertFrom-Json
        $courtId = $courtData.id
        
        Test-Endpoint "GET" "$BaseUrl/api/courts/$courtId" $null 200 "Court Query by ID" "CoreFunctionality"
        Test-Endpoint "GET" "$BaseUrl/api/courts/all" $null 200 "Court List Query" "CoreFunctionality"
        Test-Endpoint "DELETE" "$BaseUrl/api/courts/$courtId" $null 204 "Court Deletion" "CoreFunctionality"
    }
    
    Test-Endpoint "DELETE" "$BaseUrl/api/users/$userId" $null 200 "User Deletion" "CoreFunctionality"
}

# Exception Handling Tests
Write-Host "⚠️ Exception Handling Tests" -ForegroundColor Yellow

Test-Endpoint "GET" "$BaseUrl/api/users/999999" $null 404 "Non-existent User (404)" "ExceptionHandling"
Test-Endpoint "GET" "$BaseUrl/api/courts/999999" $null 404 "Non-existent Court (404)" "ExceptionHandling"
Test-Endpoint "POST" "$BaseUrl/api/users/register" "invalid json" 400 "Invalid JSON (400)" "ExceptionHandling"
Test-Endpoint "GET" "$BaseUrl/api/nonexistent" $null 404 "Non-existent Endpoint (404)" "ExceptionHandling"

# Performance Tests
Write-Host "🚀 Performance Tests" -ForegroundColor Yellow

$perfStart = Get-Date
Test-Endpoint "GET" "$BaseUrl/api/users" $null 200 "Response Time - User List" "Performance"
Test-Endpoint "GET" "$BaseUrl/api/courts/all" $null 200 "Response Time - Court List" "Performance"
$perfEnd = Get-Date
$totalPerfTime = ($perfEnd - $perfStart).TotalMilliseconds

# Data Consistency Tests
Write-Host "📊 Data Consistency Tests" -ForegroundColor Yellow

# Multi-operation consistency test
$consistencyUser = @{ username = "consistency"; email = "consistency@test.com"; password = "Test123!"; realName = "Consistency Test" }
$createResult = Test-Endpoint "POST" "$BaseUrl/api/users/register" $consistencyUser 200 "Consistency - Create User" "DataConsistency"

if ($createResult.Success) {
    $userData = $createResult.Response.Content | ConvertFrom-Json
    $userId = $userData.id
    
    # Verify immediate availability
    Test-Endpoint "GET" "$BaseUrl/api/users/$userId" $null 200 "Consistency - Immediate Query" "DataConsistency"
    
    # Cleanup
    Test-Endpoint "DELETE" "$BaseUrl/api/users/$userId" $null 200 "Consistency - Cleanup" "DataConsistency"
    
    # Verify deletion
    Test-Endpoint "GET" "$BaseUrl/api/users/$userId" $null 404 "Consistency - Verify Deletion" "DataConsistency"
}

# Calculate Ratings
Write-Host "=" * 50 -ForegroundColor Cyan
Write-Host "📊 System Stability Rating Analysis" -ForegroundColor Cyan
Write-Host "=" * 50 -ForegroundColor Cyan

$categories = @("CoreFunctionality", "ExceptionHandling", "Performance", "DataConsistency")
$totalScore = 0
$maxScore = 0

foreach ($category in $categories) {
    $result = $Results.$category
    $successRate = if ($result.Total -gt 0) { [math]::Round(($result.Passed / $result.Total) * 100, 1) } else { 0 }
    $avgDuration = if ($result.Tests.Count -gt 0) { [math]::Round(($result.Tests | Measure-Object Duration -Average).Average, 1) } else { 0 }
    
    Write-Host "$category Results:" -ForegroundColor White
    Write-Host "  Tests: $($result.Passed)/$($result.Total) (${successRate}%)" -ForegroundColor $(if ($successRate -ge 95) { "Green" } elseif ($successRate -ge 90) { "Yellow" } else { "Red" })
    Write-Host "  Avg Response: ${avgDuration}ms" -ForegroundColor $(if ($avgDuration -le 100) { "Green" } elseif ($avgDuration -le 500) { "Yellow" } else { "Red" })
    
    # Scoring weights
    $weight = switch ($category) {
        "CoreFunctionality" { 40 }
        "ExceptionHandling" { 25 }
        "Performance" { 20 }
        "DataConsistency" { 15 }
    }
    
    $categoryScore = ($successRate / 100) * $weight
    $totalScore += $categoryScore
    $maxScore += $weight
    
    Write-Host "  Category Score: $([math]::Round($categoryScore, 1))/$weight" -ForegroundColor Cyan
    Write-Host ""
}

# Overall Performance Metrics
$overallSuccessRate = [math]::Round($totalScore / $maxScore * 100, 1)
$overallPerformance = if ($Results.Performance.Tests.Count -gt 0) { 
    [math]::Round(($Results.Performance.Tests | Measure-Object Duration -Average).Average, 1) 
} else { 0 }

# System Grade Calculation
$grade = if ($overallSuccessRate -ge 97) { 
    @{ Letter = "A+"; Color = "Green"; Description = "Excellent - Production Ready" }
} elseif ($overallSuccessRate -ge 93) {
    @{ Letter = "A"; Color = "Green"; Description = "Very Good - Recommended for Production" }
} elseif ($overallSuccessRate -ge 90) {
    @{ Letter = "A-"; Color = "Green"; Description = "Good - Suitable for Production" }
} elseif ($overallSuccessRate -ge 87) {
    @{ Letter = "B+"; Color = "Yellow"; Description = "Above Average - Minor Issues" }
} elseif ($overallSuccessRate -ge 83) {
    @{ Letter = "B"; Color = "Yellow"; Description = "Average - Needs Attention" }
} else {
    @{ Letter = "C"; Color = "Red"; Description = "Below Average - Major Issues" }
}

# Performance Rating
$performanceRating = if ($overallPerformance -le 50) { "Excellent" }
elseif ($overallPerformance -le 100) { "Very Good" }
elseif ($overallPerformance -le 200) { "Good" }
elseif ($overallPerformance -le 500) { "Fair" }
else { "Poor" }

# Final Report
Write-Host "🏆 FINAL SYSTEM STABILITY RATING" -ForegroundColor Cyan
Write-Host "=" * 50 -ForegroundColor Cyan
Write-Host "Overall Score: $overallSuccessRate/100" -ForegroundColor White
Write-Host "System Grade: $($grade.Letter)" -ForegroundColor $grade.Color
Write-Host "Status: $($grade.Description)" -ForegroundColor $grade.Color
Write-Host "Average Response Time: ${overallPerformance}ms ($performanceRating)" -ForegroundColor $(if ($overallPerformance -le 100) { "Green" } elseif ($overallPerformance -le 200) { "Yellow" } else { "Red" })

# Deployment Recommendation
$deploymentStatus = if ($overallSuccessRate -ge 95) {
    @{ Symbol = "🟢"; Status = "APPROVED"; Message = "Strongly recommended for immediate production deployment" }
} elseif ($overallSuccessRate -ge 90) {
    @{ Symbol = "🟡"; Status = "CONDITIONAL"; Message = "Recommended for production with monitoring" }
} elseif ($overallSuccessRate -ge 85) {
    @{ Symbol = "🟠"; Status = "CAUTION"; Message = "Consider addressing issues before production deployment" }
} else {
    @{ Symbol = "🔴"; Status = "NOT APPROVED"; Message = "Significant issues must be resolved before deployment" }
}

Write-Host ""
Write-Host "Deployment Status: $($deploymentStatus.Symbol) $($deploymentStatus.Status)" -ForegroundColor $(if ($deploymentStatus.Status -eq "APPROVED") { "Green" } elseif ($deploymentStatus.Status -eq "CONDITIONAL") { "Yellow" } else { "Red" })
Write-Host "Recommendation: $($deploymentStatus.Message)" -ForegroundColor White

# Save detailed report
$reportFile = "system-stability-rating-$(Get-Date -Format 'yyyyMMdd-HHmmss').json"
$detailedReport = @{
    Timestamp = Get-Date
    OverallScore = $overallSuccessRate
    Grade = $grade.Letter
    Status = $grade.Description
    PerformanceRating = $performanceRating
    AverageResponseTime = $overallPerformance
    DeploymentStatus = $deploymentStatus.Status
    CategoryResults = $Results
}

$detailedReport | ConvertTo-Json -Depth 10 | Out-File -FilePath $reportFile -Encoding UTF8
Write-Host ""
Write-Host "Detailed rating report saved: $reportFile" -ForegroundColor Cyan
Write-Host "System stability rating complete!" -ForegroundColor Green 