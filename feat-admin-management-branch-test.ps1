#!/usr/bin/env pwsh
# CourtLink feat/admin-management 分支功能测试脚本
# 测试所有管理员管理功能

param(
    [string]$BaseUrl = "http://localhost:8080",
    [int]$TestRounds = 1,
    [bool]$Verbose = $true
)

# 配置颜色输出
$Host.UI.RawUI.ForegroundColor = "White"

# 全局变量
$global:TestResults = @()
$global:AdminToken = ""
$global:CreatedAdminId = $null

# 工具函数
function Write-ColoredOutput($message, $color = "White") {
    $Host.UI.RawUI.ForegroundColor = $color
    Write-Host $message
    $Host.UI.RawUI.ForegroundColor = "White"
}

function Log-Test($testName, $success, $message, $details = "") {
    $result = @{
        TestName = $testName
        Success = $success
        Message = $message
        Details = $details
        Timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    }
    $global:TestResults += $result
    
    if ($success) {
        Write-ColoredOutput "? $testName - $message" "Green"
    } else {
        Write-ColoredOutput "? $testName - $message" "Red"
    }
    
    if ($Verbose -and $details) {
        Write-ColoredOutput "   详情: $details" "Gray"
    }
}

function Invoke-ApiRequest($method, $endpoint, $body = $null, $headers = @{}) {
    try {
        $uri = "$BaseUrl$endpoint"
        $defaultHeaders = @{
            "Content-Type" = "application/json"
            "Accept" = "application/json"
        }
        
        if ($global:AdminToken) {
            $defaultHeaders["Authorization"] = "Bearer $global:AdminToken"
        }
        
        $allHeaders = $defaultHeaders + $headers
        
        $params = @{
            Uri = $uri
            Method = $method
            Headers = $allHeaders
            UseBasicParsing = $true
        }
        
        if ($body) {
            $params.Body = ($body | ConvertTo-Json -Depth 10)
        }
        
        $response = Invoke-RestMethod @params
        return @{ Success = $true; Data = $response; StatusCode = 200 }
    }
    catch {
        $statusCode = if ($_.Exception.Response) { $_.Exception.Response.StatusCode.value__ } else { 0 }
        $errorMsg = $_.Exception.Message
        return @{ Success = $false; Error = $errorMsg; StatusCode = $statusCode }
    }
}

function Test-ApplicationStartup {
    Write-ColoredOutput "`n? 测试应用程序启动..." "Cyan"
    
    $maxRetries = 30
    $retryCount = 0
    
    while ($retryCount -lt $maxRetries) {
        try {
            $response = Invoke-RestMethod -Uri "$BaseUrl/api/health" -UseBasicParsing
            Log-Test "应用启动检查" $true "应用程序成功启动" "健康检查响应正常"
            return $true
        }
        catch {
            $retryCount++
            Write-ColoredOutput "等待应用启动... ($retryCount/$maxRetries)" "Yellow"
            Start-Sleep -Seconds 2
        }
    }
    
    Log-Test "应用启动检查" $false "应用程序启动失败" "超时等待健康检查响应"
    return $false
}

function Test-AdminInitialization {
    Write-ColoredOutput "`n??? 测试管理员初始化..." "Cyan"
    
    # 等待一下让初始化完成
    Start-Sleep -Seconds 3
    
    # 测试默认管理员登录
    $loginRequest = @{
        usernameOrEmail = "admin"
        password = "admin123"
    }
    
    $result = Invoke-ApiRequest "POST" "/api/admin/auth/login" $loginRequest
    
    if ($result.Success -and $result.Data.success) {
        $global:AdminToken = $result.Data.token
        Log-Test "默认管理员初始化" $true "默认管理员账户创建成功" "用户名: admin, 登录成功"
        return $true
    } else {
        Log-Test "默认管理员初始化" $false "默认管理员账户创建失败" $result.Error
        return $false
    }
}

function Test-AdminAuthentication {
    Write-ColoredOutput "`n? 测试管理员认证功能..." "Cyan"
    
    # 测试正确登录
    $loginRequest = @{
        usernameOrEmail = "admin"
        password = "admin123"
    }
    
    $result = Invoke-ApiRequest "POST" "/api/admin/auth/login" $loginRequest
    
    if ($result.Success -and $result.Data.success) {
        $global:AdminToken = $result.Data.token
        Log-Test "管理员登录" $true "正确凭据登录成功" "获取到令牌"
    } else {
        Log-Test "管理员登录" $false "正确凭据登录失败" $result.Error
        return $false
    }
    
    # 测试错误密码登录
    $wrongLoginRequest = @{
        usernameOrEmail = "admin"
        password = "wrongpassword"
    }
    
    $result = Invoke-ApiRequest "POST" "/api/admin/auth/login" $wrongLoginRequest
    
    if (-not $result.Success -or -not $result.Data.success) {
        Log-Test "错误密码登录" $true "错误密码被正确拒绝" "安全验证正常"
    } else {
        Log-Test "错误密码登录" $false "错误密码登录成功，安全漏洞" "应该拒绝错误密码"
    }
    
    # 测试登出
    $result = Invoke-ApiRequest "POST" "/api/admin/auth/logout" $null @{"Authorization" = "Bearer $global:AdminToken"}
    
    if ($result.Success -and $result.Data.success) {
        Log-Test "管理员登出" $true "登出成功" "会话正确终止"
    } else {
        Log-Test "管理员登出" $false "登出失败" $result.Error
    }
    
    return $true
}

function Test-AdminManagement {
    Write-ColoredOutput "`n? 测试管理员管理功能..." "Cyan"
    
    # 重新登录获取令牌
    $loginRequest = @{
        usernameOrEmail = "admin"
        password = "admin123"
    }
    $loginResult = Invoke-ApiRequest "POST" "/api/admin/auth/login" $loginRequest
    if ($loginResult.Success) {
        $global:AdminToken = $loginResult.Data.token
    }
    
    # 测试创建管理员
    $createAdminRequest = @{
        username = "testadmin"
        email = "testadmin@courtlink.com"
        password = "testpass123"
        fullName = "测试管理员"
        role = "ADMIN"
    }
    
    $result = Invoke-ApiRequest "POST" "/api/admin/admins" $createAdminRequest
    
    if ($result.Success -and $result.Data.success) {
        $global:CreatedAdminId = $result.Data.data.id
        Log-Test "创建管理员" $true "管理员创建成功" "ID: $global:CreatedAdminId, 用户名: testadmin"
    } else {
        Log-Test "创建管理员" $false "管理员创建失败" $result.Error
    }
    
    # 测试获取所有管理员
    $result = Invoke-ApiRequest "GET" "/api/admin/admins?page=0&size=10"
    
    if ($result.Success -and $result.Data.success) {
        $adminCount = $result.Data.data.Count
        Log-Test "获取管理员列表" $true "成功获取管理员列表" "共 $adminCount 个管理员"
    } else {
        Log-Test "获取管理员列表" $false "获取管理员列表失败" $result.Error
    }
    
    # 测试获取特定管理员
    if ($global:CreatedAdminId) {
        $result = Invoke-ApiRequest "GET" "/api/admin/admins/$global:CreatedAdminId"
        
        if ($result.Success -and $result.Data.success) {
            Log-Test "获取特定管理员" $true "成功获取管理员详情" "用户名: $($result.Data.data.username)"
        } else {
            Log-Test "获取特定管理员" $false "获取管理员详情失败" $result.Error
        }
    }
    
    # 测试更新管理员
    if ($global:CreatedAdminId) {
        $updateAdminRequest = @{
            username = "testadmin"
            email = "testadmin@courtlink.com"
            fullName = "更新后的测试管理员"
            role = "ADMIN"
        }
        
        $result = Invoke-ApiRequest "PUT" "/api/admin/admins/$global:CreatedAdminId" $updateAdminRequest
        
        if ($result.Success -and $result.Data.success) {
            Log-Test "更新管理员" $true "管理员更新成功" "姓名更新为: 更新后的测试管理员"
        } else {
            Log-Test "更新管理员" $false "管理员更新失败" $result.Error
        }
    }
    
    return $true
}

function Test-AdminStatusManagement {
    Write-ColoredOutput "`n? 测试管理员状态管理..." "Cyan"
    
    if (-not $global:CreatedAdminId) {
        Log-Test "管理员状态管理" $false "没有可用的测试管理员ID" "需要先创建管理员"
        return $false
    }
    
    # 测试停用管理员
    $result = Invoke-ApiRequest "PATCH" "/api/admin/admins/$global:CreatedAdminId/deactivate"
    
    if ($result.Success -and $result.Data.success) {
        Log-Test "停用管理员" $true "管理员停用成功" "ID: $global:CreatedAdminId"
    } else {
        Log-Test "停用管理员" $false "管理员停用失败" $result.Error
    }
    
    # 测试激活管理员
    $result = Invoke-ApiRequest "PATCH" "/api/admin/admins/$global:CreatedAdminId/activate"
    
    if ($result.Success -and $result.Data.success) {
        Log-Test "激活管理员" $true "管理员激活成功" "ID: $global:CreatedAdminId"
    } else {
        Log-Test "激活管理员" $false "管理员激活失败" $result.Error
    }
    
    return $true
}

function Test-AdminStatistics {
    Write-ColoredOutput "`n? 测试管理员统计功能..." "Cyan"
    
    # 测试获取管理员统计
    $result = Invoke-ApiRequest "GET" "/api/admin/statistics"
    
    if ($result.Success -and $result.Data.success) {
        $stats = $result.Data.data
        $details = "总数: $($stats.totalCount), 活跃: $($stats.activeCount), 锁定: $($stats.lockedCount)"
        Log-Test "获取管理员统计" $true "统计数据获取成功" $details
    } else {
        Log-Test "获取管理员统计" $false "统计数据获取失败" $result.Error
    }
    
    return $true
}

function Test-AdminSystemHealth {
    Write-ColoredOutput "`n? 测试管理员系统健康检查..." "Cyan"
    
    # 测试系统健康状态
    $result = Invoke-ApiRequest "GET" "/api/admin/system/health"
    
    if ($result.Success -and $result.Data.success) {
        $health = $result.Data.health
        $healthData = $result.Data.data
        $details = "状态: $health, 健康分数: $($healthData.healthScore)"
        Log-Test "系统健康检查" $true "系统健康状态正常" $details
    } else {
        Log-Test "系统健康检查" $false "系统健康检查失败" $result.Error
    }
    
    return $true
}

function Test-AdminDashboard {
    Write-ColoredOutput "`n? 测试管理员仪表板..." "Cyan"
    
    # 测试仪表板页面访问
    try {
        $response = Invoke-WebRequest -Uri "$BaseUrl/api/admin/dashboard" -UseBasicParsing
        
        if ($response.StatusCode -eq 200 -and $response.Content.Contains("CourtLink 管理后台")) {
            Log-Test "管理员仪表板" $true "仪表板页面访问成功" "HTML页面正确渲染"
        } else {
            Log-Test "管理员仪表板" $false "仪表板页面内容异常" "页面内容不包含预期标题"
        }
    }
    catch {
        Log-Test "管理员仪表板" $false "仪表板页面访问失败" $_.Exception.Message
    }
    
    return $true
}

function Test-SecurityConfiguration {
    Write-ColoredOutput "`n? 测试安全配置..." "Cyan"
    
    # 测试未授权访问管理员API
    $oldToken = $global:AdminToken
    $global:AdminToken = ""
    
    $result = Invoke-ApiRequest "GET" "/api/admin/admins"
    
    if (-not $result.Success -or $result.StatusCode -eq 401 -or $result.StatusCode -eq 403) {
        Log-Test "未授权访问保护" $true "未授权访问被正确拒绝" "安全防护正常"
    } else {
        Log-Test "未授权访问保护" $false "未授权访问未被拒绝" "存在安全漏洞"
    }
    
    $global:AdminToken = $oldToken
    
    # 测试H2控制台访问
    try {
        $response = Invoke-WebRequest -Uri "$BaseUrl/h2-console" -UseBasicParsing
        if ($response.StatusCode -eq 200) {
            Log-Test "H2控制台访问" $true "H2控制台可正常访问" "开发环境配置正确"
        }
    }
    catch {
        Log-Test "H2控制台访问" $false "H2控制台访问失败" $_.Exception.Message
    }
    
    return $true
}

function Cleanup-TestData {
    Write-ColoredOutput "`n? 清理测试数据..." "Cyan"
    
    # 删除测试创建的管理员
    if ($global:CreatedAdminId) {
        $result = Invoke-ApiRequest "DELETE" "/api/admin/admins/$global:CreatedAdminId"
        
        if ($result.Success -and $result.Data.success) {
            Log-Test "清理测试数据" $true "测试管理员删除成功" "ID: $global:CreatedAdminId"
        } else {
            Log-Test "清理测试数据" $false "测试管理员删除失败" $result.Error
        }
    }
}

function Generate-TestReport {
    Write-ColoredOutput "`n? 生成测试报告..." "Cyan"
    
    $totalTests = $global:TestResults.Count
    $passedTests = ($global:TestResults | Where-Object { $_.Success }).Count
    $failedTests = $totalTests - $passedTests
    $successRate = if ($totalTests -gt 0) { [math]::Round(($passedTests / $totalTests) * 100, 2) } else { 0 }
    
    $report = @{
        TestSummary = @{
            TotalTests = $totalTests
            PassedTests = $passedTests
            FailedTests = $failedTests
            SuccessRate = $successRate
        }
        TestResults = $global:TestResults
        TestConfiguration = @{
            BaseUrl = $BaseUrl
            TestRounds = $TestRounds
            Branch = "feat/admin-management"
            TestDate = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
        }
    }
    
    $reportFile = "feat-admin-management-test-results-$(Get-Date -Format 'yyyyMMdd-HHmmss').json"
    $report | ConvertTo-Json -Depth 10 | Out-File -FilePath $reportFile -Encoding UTF8
    
    Write-ColoredOutput "`n? 测试总结报告" "Yellow"
    Write-ColoredOutput "=================" "Yellow"
    Write-ColoredOutput "总测试数: $totalTests" "White"
    Write-ColoredOutput "通过测试: $passedTests" "Green"
    Write-ColoredOutput "失败测试: $failedTests" "Red"
    Write-ColoredOutput "成功率: $successRate%" $(if ($successRate -ge 90) { "Green" } elseif ($successRate -ge 70) { "Yellow" } else { "Red" })
    Write-ColoredOutput "报告文件: $reportFile" "Cyan"
    
    if ($failedTests -gt 0) {
        Write-ColoredOutput "`n? 失败的测试:" "Red"
        $global:TestResults | Where-Object { -not $_.Success } | ForEach-Object {
            Write-ColoredOutput "  - $($_.TestName): $($_.Message)" "Red"
        }
    }
    
    return $report
}

# 主测试执行函数
function Start-AdminManagementBranchTest {
    Write-ColoredOutput "? 开始 feat/admin-management 分支功能测试" "Magenta"
    Write-ColoredOutput "================================================" "Magenta"
    Write-ColoredOutput "基础URL: $BaseUrl" "White"
    Write-ColoredOutput "测试轮次: $TestRounds" "White"
    Write-ColoredOutput "详细模式: $Verbose" "White"
    Write-ColoredOutput "开始时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" "White"
    
    for ($round = 1; $round -le $TestRounds; $round++) {
        if ($TestRounds -gt 1) {
            Write-ColoredOutput "`n? 第 $round 轮测试" "Magenta"
        }
        
        # 执行所有测试
        if (-not (Test-ApplicationStartup)) {
            Write-ColoredOutput "? 应用程序启动失败，终止测试" "Red"
            break
        }
        
        Test-AdminInitialization
        Test-AdminAuthentication
        Test-AdminManagement
        Test-AdminStatusManagement
        Test-AdminStatistics
        Test-AdminSystemHealth
        Test-AdminDashboard
        Test-SecurityConfiguration
        Cleanup-TestData
    }
    
    # 生成测试报告
    $report = Generate-TestReport
    
    # 根据成功率确定退出代码
    if ($report.TestSummary.SuccessRate -ge 90) {
        Write-ColoredOutput "`n? 分支功能测试完成 - 测试通过" "Green"
        exit 0
    } elseif ($report.TestSummary.SuccessRate -ge 70) {
        Write-ColoredOutput "`n?? 分支功能测试完成 - 部分问题" "Yellow"
        exit 1
    } else {
        Write-ColoredOutput "`n? 分支功能测试完成 - 存在严重问题" "Red"
        exit 2
    }
}

# 启动测试
Start-AdminManagementBranchTest
