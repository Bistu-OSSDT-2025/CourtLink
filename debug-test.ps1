# Debug test for user registration
$BaseUrl = "http://localhost:8080"

Write-Host "=== Debug Test for User Registration ===" -ForegroundColor Cyan

# Test 1: Manual test with detailed error info
Write-Host "`nTest 1: Manual registration test..." -ForegroundColor Yellow
try {
    $timestamp = [int][double]::Parse((Get-Date -UFormat %s))
    $random = Get-Random -Min 1000 -Max 9999
    $userData = @{
        username = "debug_test_${timestamp}_${random}"
        email = "debug_test_${timestamp}_${random}@test.com"
        password = "password123"
        realName = "Debug Test User"
        phoneNumber = "13800000001"
    }
    
    Write-Host "User data: $($userData | ConvertTo-Json)" -ForegroundColor Gray
    
    $headers = @{"Content-Type" = "application/json"}
    $response = Invoke-RestMethod -Uri "$BaseUrl/api/users/register" -Method POST -Body ($userData | ConvertTo-Json) -Headers $headers
    
    Write-Host "SUCCESS: User registered with ID $($response.id)" -ForegroundColor Green
    Write-Host "Response: $($response | ConvertTo-Json)" -ForegroundColor Green
    
    # Test query
    Write-Host "`nTesting user query..." -ForegroundColor Yellow
    $queryResponse = Invoke-RestMethod -Uri "$BaseUrl/api/users/$($response.id)" -Method GET
    Write-Host "Query SUCCESS: $($queryResponse.username)" -ForegroundColor Green
    
    # Test cleanup
    Write-Host "`nTesting cleanup..." -ForegroundColor Yellow
    Invoke-RestMethod -Uri "$BaseUrl/api/users/$($response.id)" -Method DELETE
    Write-Host "Cleanup SUCCESS" -ForegroundColor Green
    
} catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Status Code: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    Write-Host "Full Error: $($_ | Out-String)" -ForegroundColor Red
}

# Test 2: Replication of the script logic
Write-Host "`n=== Test 2: Replicating script logic ===" -ForegroundColor Yellow

$SuccessCount = 0
$FailCount = 0

for ($i = 1; $i -le 3; $i++) {
    Write-Host "`n--- Test Round $i ---" -ForegroundColor Cyan
    
    try {
        $timestamp = [int][double]::Parse((Get-Date -UFormat %s))
        $random = Get-Random -Min 1000 -Max 9999
        $userData = @{
            username = "test_r${i}_${timestamp}_${random}"
            email = "test_r${i}_${timestamp}_${random}@test.com"
            password = "password123"
            realName = "Test User $i"
            phoneNumber = "13800000001"
        }
        
        Write-Host "Registering user: $($userData.username)" -ForegroundColor Gray
        
        $headers = @{"Content-Type" = "application/json"}
        $response = Invoke-RestMethod -Uri "$BaseUrl/api/users/register" -Method POST -Body ($userData | ConvertTo-Json) -Headers $headers
        
        if ($response.id) {
            Write-Host "SUCCESS: ID $($response.id)" -ForegroundColor Green
            $SuccessCount++
            
            # Cleanup
            try {
                Invoke-RestMethod -Uri "$BaseUrl/api/users/$($response.id)" -Method DELETE
                Write-Host "Cleanup OK" -ForegroundColor Green
            } catch {
                Write-Host "Cleanup failed: $($_.Exception.Message)" -ForegroundColor Yellow
            }
        } else {
            Write-Host "FAILED: No ID returned" -ForegroundColor Red
            $FailCount++
        }
    } catch {
        Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "Status Code: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
        $FailCount++
    }
    
    Start-Sleep -Milliseconds 200
}

Write-Host "`n=== Debug Results ===" -ForegroundColor Cyan
Write-Host "Success: $SuccessCount/3" -ForegroundColor Green
Write-Host "Failed: $FailCount/3" -ForegroundColor Red

# Test 3: Exception handling test
Write-Host "`n=== Test 3: Exception Tests ===" -ForegroundColor Yellow

# Test 404
Write-Host "Testing 404 response..." -NoNewline
try {
    Invoke-RestMethod -Uri "$BaseUrl/api/users/999999" -Method GET
    Write-Host " FAILED (should have thrown 404)" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode -eq 404) {
        Write-Host " OK (404)" -ForegroundColor Green
    } else {
        Write-Host " FAILED (wrong status: $($_.Exception.Response.StatusCode))" -ForegroundColor Red
    }
}

# Test 400
Write-Host "Testing 400 response..." -NoNewline
try {
    $invalidData = @{
        username = "ab"
        email = "invalid"
        password = "123"
    }
    $headers = @{"Content-Type" = "application/json"}
    Invoke-RestMethod -Uri "$BaseUrl/api/users/register" -Method POST -Body ($invalidData | ConvertTo-Json) -Headers $headers
    Write-Host " FAILED (should have thrown 400)" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode -eq 400) {
        Write-Host " OK (400)" -ForegroundColor Green
    } else {
        Write-Host " FAILED (wrong status: $($_.Exception.Response.StatusCode))" -ForegroundColor Red
    }
}

Write-Host "`n=== Debug Test Complete ===" -ForegroundColor Cyan 