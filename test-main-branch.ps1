#!/usr/bin/env pwsh

# CourtLink Main Branch Test Script
$BaseUrl = "http://localhost:8095/api"
$TotalTests = 0
$PassedTests = 0
$FailedTests = 0

function Write-TestResult {
    param(
        [string]$TestName,
        [bool]$Success,
        [string]$Message = ""
    )
    
    $TotalTests++
    if ($Success) {
        $PassedTests++
        Write-Host "✅ $TestName - PASSED" -ForegroundColor Green
        if ($Message) {
            Write-Host "   $Message" -ForegroundColor Gray
        }
    } else {
        $FailedTests++
        Write-Host "❌ $TestName - FAILED" -ForegroundColor Red
        if ($Message) {
            Write-Host "   $Message" -ForegroundColor Red
        }
    }
}

function Test-Endpoint {
    param(
        [string]$Method,
        [string]$Path,
        [object]$Body = $null,
        [int]$ExpectedCode = 200,
        [string]$TestName
    )
    
    try {
        $params = @{
            Method = $Method
            Uri = "$BaseUrl$Path"
            Headers = @{
                "Content-Type" = "application/json"
                "Accept" = "application/json"
            }
        }
        
        if ($Body -and $Method -ne "GET") {
            $params.Body = ($Body | ConvertTo-Json)
        }
        
        $response = Invoke-WebRequest @params
        $success = $response.StatusCode -eq $ExpectedCode
        $message = if ($success) { "" } else { "Expected status code $ExpectedCode but got $($response.StatusCode)" }
        Write-TestResult -TestName $TestName -Success $success -Message $message
        return @{
            Success = $success
            Response = $response
        }
    }
    catch {
        Write-TestResult -TestName $TestName -Success $false -Message $_.Exception.Message
        return @{
            Success = $false
            Error = $_.Exception.Message
        }
    }
}

Write-Host "`n🚀 Starting CourtLink Main Branch Tests`n" -ForegroundColor Cyan

# Wait for application startup
Write-Host "⏳ Waiting for application to start..." -ForegroundColor Yellow
$ready = $false
for ($i = 0; $i -lt 30; $i++) {
    try {
        $health = Invoke-WebRequest "http://localhost:8095/api/actuator/health" -TimeoutSec 1
        if ($health.StatusCode -eq 200) {
            $ready = $true
            Write-Host "✅ Application is ready!" -ForegroundColor Green
            break
        }
    }
    catch {
        Start-Sleep -Seconds 1
        Write-Host "." -NoNewline
    }
}

if (-not $ready) {
    Write-Host "`n❌ Application failed to start within 30 seconds" -ForegroundColor Red
    exit 1
}

Write-Host "`n🏥 Testing Health Check Endpoints" -ForegroundColor Magenta
Test-Endpoint -Method "GET" -Path "/health/simple" -TestName "Simple Health Check"
Test-Endpoint -Method "GET" -Path "/health/live" -TestName "Liveness Check"
Test-Endpoint -Method "GET" -Path "/health/ready" -TestName "Readiness Check"

Write-Host "`n🏸 Testing Court Management" -ForegroundColor Magenta

# Create a new court
$court = @{
    name = "Test Court"
    location = "Test Location"
    description = "Test Description"
    status = "AVAILABLE"
    pricePerHour = 100.0
    facilities = "Lighting, Shower"
    openingHours = "09:00-22:00"
    maintenanceSchedule = "Monday 08:00-09:00"
    isActive = $true
}

$createResult = Test-Endpoint -Method "POST" -Path "/courts" -Body $court -TestName "Create Court"

if ($createResult.Success) {
    $courtData = $createResult.Response.Content | ConvertFrom-Json
    $courtId = $courtData.id
    
    # Get court by ID
    Test-Endpoint -Method "GET" -Path "/courts/$courtId" -TestName "Get Court by ID"
    
    # Update court
    $court.description = "Updated Description"
    Test-Endpoint -Method "PUT" -Path "/courts/$courtId" -Body $court -TestName "Update Court"
    
    # Search courts
    Test-Endpoint -Method "GET" -Path "/courts/search?keyword=Test&status=AVAILABLE" -TestName "Search Courts"
    
    # Update status
    Test-Endpoint -Method "PATCH" -Path "/courts/$courtId/status" -Body @{ status = "MAINTENANCE" } -TestName "Update Court Status"
    
    # Delete court
    Test-Endpoint -Method "DELETE" -Path "/courts/$courtId" -TestName "Delete Court"
}

Write-Host "`n📚 Testing API Documentation" -ForegroundColor Magenta
Test-Endpoint -Method "GET" -Path "/v3/api-docs" -TestName "OpenAPI Documentation"
Test-Endpoint -Method "GET" -Path "/swagger-ui.html" -TestName "Swagger UI"

# Print test summary
Write-Host "`n📊 Test Summary" -ForegroundColor Cyan
Write-Host "Total Tests: $TotalTests" -ForegroundColor White
Write-Host "Passed: $PassedTests" -ForegroundColor Green
Write-Host "Failed: $FailedTests" -ForegroundColor Red

if ($FailedTests -gt 0) {
    exit 1
} else {
    exit 0
} 