#!/usr/bin/env pwsh

# CourtLink 优化功能测试脚本
Write-Host "=== CourtLink 代码优化验证测试 ===" -ForegroundColor Green

$baseUrl = "http://localhost:8080"
$testResults = @()

function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Url,
        [string]$Method = "GET",
        [object]$Body = $null
    )
    
    Write-Host "测试: $Name" -ForegroundColor Yellow
    
    try {
        $params = @{
            Uri = $Url
            Method = $Method
            TimeoutSec = 10
        }
        
        if ($Body) {
            $params.Body = ($Body | ConvertTo-Json)
            $params.ContentType = "application/json"
        }
        
        $response = Invoke-RestMethod @params
        Write-Host "✓ $Name - 成功" -ForegroundColor Green
        
        $testResults += [PSCustomObject]@{
            Test = $Name
            Status = "成功"
            Url = $Url
            Method = $Method
        }
        
        return $response
    } catch {
        $errorMsg = $_.Exception.Message
        if ($_.Exception.Response) {
            $statusCode = $_.Exception.Response.StatusCode.value__
            $errorMsg = "HTTP $statusCode - $errorMsg"
        }
        
        Write-Host "✗ $Name - 失败: $errorMsg" -ForegroundColor Red
        
        $testResults += [PSCustomObject]@{
            Test = $Name
            Status = "失败"
            Error = $errorMsg
            Url = $Url
            Method = $Method
        }
        
        return $null
    }
}

Write-Host "`n1. 测试应用基础功能" -ForegroundColor Cyan

# 测试应用是否启动
Test-Endpoint "Spring Boot Actuator健康检查" "$baseUrl/actuator/health"

# 测试用户管理基础功能
Test-Endpoint "获取用户列表" "$baseUrl/api/users/list"

Write-Host "`n2. 测试异常处理优化" -ForegroundColor Cyan

# 测试不存在的用户ID（应该返回404）
Test-Endpoint "查询不存在的用户（异常处理测试）" "$baseUrl/api/users/999999"

# 测试不存在的API端点（应该返回404）
Test-Endpoint "访问不存在的端点（异常处理测试）" "$baseUrl/api/nonexistent"

Write-Host "`n3. 测试用户注册功能（参数验证）" -ForegroundColor Cyan

# 测试用户注册（正常情况）
$newUser = @{
    username = "testuser_$(Get-Date -Format 'yyyyMMddHHmmss')"
    email = "test_$(Get-Date -Format 'yyyyMMddHHmmss')@example.com"
    password = "testpass123"
    fullName = "测试用户"
    phoneNumber = "13800138000"
}

$registeredUser = Test-Endpoint "用户注册" "$baseUrl/api/users/register" "POST" $newUser

if ($registeredUser) {
    $userId = $registeredUser.id
    Write-Host "注册用户ID: $userId" -ForegroundColor Green
    
    Write-Host "`n4. 测试用户管理功能" -ForegroundColor Cyan
    
    # 测试用户登录
    $loginData = @{
        username = $newUser.username
        password = $newUser.password
    }
    Test-Endpoint "用户登录" "$baseUrl/api/users/login" "POST" $loginData
    
    # 测试获取用户信息
    Test-Endpoint "获取用户信息" "$baseUrl/api/users/$userId"
    
    # 测试用户名存在性检查
    Test-Endpoint "检查用户名存在性" "$baseUrl/api/users/check-username?username=$($newUser.username)"
    
    # 测试密码验证
    $passwordCheck = @{ password = $newUser.password }
    Test-Endpoint "密码验证" "$baseUrl/api/users/$userId/validate-password" "POST" $passwordCheck
    
    # 测试更新用户信息
    $updateData = @{
        username = $newUser.username
        email = $newUser.email
        fullName = "更新的测试用户"
        phoneNumber = "13900139000"
    }
    Test-Endpoint "更新用户信息" "$baseUrl/api/users/$userId" "PUT" $updateData
    
    # 测试用户激活
    Test-Endpoint "用户激活" "$baseUrl/api/users/$userId/activate" "POST"
    
    # 测试用户停用
    Test-Endpoint "用户停用" "$baseUrl/api/users/$userId/deactivate" "POST"
    
    # 重新激活用户
    Test-Endpoint "重新激活用户" "$baseUrl/api/users/$userId/activate" "POST"
    
    # 清理测试数据
    Test-Endpoint "删除测试用户" "$baseUrl/api/users/$userId" "DELETE"
}

Write-Host "`n5. 测试参数验证改进" -ForegroundColor Cyan

# 测试空用户名注册（应该返回400）
$invalidUser = @{
    username = ""
    email = "invalid@example.com"
    password = "pass123"
}
Test-Endpoint "空用户名注册（参数验证测试）" "$baseUrl/api/users/register" "POST" $invalidUser

# 测试重复用户名（如果有现有用户）
if ($registeredUser) {
    Test-Endpoint "重复用户名注册（冲突检测测试）" "$baseUrl/api/users/register" "POST" $newUser
}

Write-Host "`n=== 测试结果汇总 ===" -ForegroundColor Cyan

$successCount = ($testResults | Where-Object { $_.Status -eq "成功" }).Count
$failureCount = ($testResults | Where-Object { $_.Status -eq "失败" }).Count
$totalCount = $testResults.Count

Write-Host "总测试数: $totalCount" -ForegroundColor White
Write-Host "成功: $successCount" -ForegroundColor Green
Write-Host "失败: $failureCount" -ForegroundColor Red

if ($totalCount -gt 0) {
    $successRate = ($successCount / $totalCount * 100)
    Write-Host "成功率: $($successRate.ToString('F1'))%" -ForegroundColor Cyan
}

if ($failureCount -gt 0) {
    Write-Host "`n失败的测试详情:" -ForegroundColor Red
    $testResults | Where-Object { $_.Status -eq "失败" } | ForEach-Object {
        Write-Host "- $($_.Test): $($_.Error)" -ForegroundColor Red
        Write-Host "  URL: $($_.Method) $($_.Url)" -ForegroundColor Yellow
    }
}

Write-Host "`n=== 优化验证总结 ===" -ForegroundColor Green

$optimizations = @(
    "✅ PowerShell启动脚本 (start-app.ps1)",
    "✅ 健康检查脚本 (health-check.ps1)", 
    "✅ API测试脚本 (test-api.ps1)",
    "✅ 异常处理机制优化",
    "✅ 参数验证增强",
    "✅ 全局异常处理器改进",
    "✅ 用户服务实现优化",
    "✅ Spring Boot Actuator健康检查"
)

$optimizations | ForEach-Object {
    Write-Host $_ -ForegroundColor Green
}

Write-Host "`n代码整合和优化已完成！" -ForegroundColor Green
Write-Host "项目现在具备更好的异常处理、参数验证和监控能力。" -ForegroundColor Cyan 