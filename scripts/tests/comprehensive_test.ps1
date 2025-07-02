# ===========================
# 主分支全功能综合测试脚本
# 测试范围：用户管理、场地管理、健康检查、异常处理
# ===========================

param(
    [string]$BaseUrl = "http://localhost:8080",
    [int]$TestRounds = 5,
    [switch]$Verbose
)

# 初始化
$ErrorActionPreference = "Continue"
$ProgressPreference = "SilentlyContinue"

# 测试统计
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

# 颜色输出函数
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

# HTTP请求函数
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
        
        # 更新性能统计
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
                Write-ColorOutput "  ✅ $TestName - 响应时间: ${ResponseTimeMs}ms" "Green"
            }
        } else {
            $TestResults.FailedTests++
            Write-ColorOutput "  ❌ $TestName - 期望状态码: $ExpectedStatusCode, 实际: $($Response.StatusCode)" "Red"
        }
        
        # 记录测试详情
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
        Write-ColorOutput "  ❌ $TestName - 错误: $($_.Exception.Message)" "Red"
        
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

# 解析JSON响应
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

Write-ColorOutput "🚀 开始主分支全功能综合测试" "Cyan"
Write-ColorOutput "基础URL: $BaseUrl" "Cyan"
Write-ColorOutput "测试轮次: $TestRounds" "Cyan"
Write-ColorOutput "详细输出: $($Verbose.IsPresent)" "Cyan"
Write-ColorOutput "=" * 60 "Cyan"

# ===========================
# 第一部分：系统健康检查测试
# ===========================
Write-ColorOutput "📋 第一部分：系统健康检查测试" "Magenta"

$HealthTests = @(
    @{ Uri = "$BaseUrl/api/health"; Name = "系统健康检查" },
    @{ Uri = "$BaseUrl/api/health/simple"; Name = "简单健康检查" },
    @{ Uri = "$BaseUrl/api/health/ready"; Name = "就绪检查" },
    @{ Uri = "$BaseUrl/api/health/live"; Name = "存活检查" },
    @{ Uri = "$BaseUrl/api/users/health"; Name = "用户模块健康检查" }
)

foreach ($Test in $HealthTests) {
    $Result = Invoke-TestRequest -Method "GET" -Uri $Test.Uri -TestName $Test.Name
}

# ===========================
# 第二部分：用户管理功能测试
# ===========================
Write-ColorOutput "👥 第二部分：用户管理功能测试" "Magenta"

# 测试数据
$TestUsers = @()
for ($i = 1; $i -le $TestRounds; $i++) {
    $TestUsers += @{
        username = "testuser_$i"
        email = "testuser$i@test.com"
        password = "TestPass123!"
        realName = "测试用户$i"
    }
}

# 2.1 用户注册测试
Write-ColorOutput "2.1 用户注册测试" "Yellow"
$CreatedUsers = @()

foreach ($User in $TestUsers) {
    $Result = Invoke-TestRequest -Method "POST" -Uri "$BaseUrl/api/users/register" -Body $User -TestName "注册用户: $($User.username)"
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

Write-ColorOutput "已创建用户数量: $($CreatedUsers.Count)" "Green"

# 2.2 用户名/邮箱存在性检查测试
Write-ColorOutput "2.2 用户名/邮箱存在性检查测试" "Yellow"

if ($CreatedUsers.Count -gt 0) {
    $TestUser = $CreatedUsers[0]
    
    # 检查已存在的用户名
    $Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/users/check-username?username=$($TestUser.username)" -TestName "检查已存在用户名"
    
    # 检查已存在的邮箱
    $Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/users/check-email?email=$($TestUser.email)" -TestName "检查已存在邮箱"
    
    # 检查不存在的用户名
    $Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/users/check-username?username=nonexistent_user" -TestName "检查不存在用户名"
    
    # 检查不存在的邮箱
    $Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/users/check-email?email=nonexistent@test.com" -TestName "检查不存在邮箱"
}

# 2.3 用户登录测试
Write-ColorOutput "2.3 用户登录测试" "Yellow"

$LoginTokens = @()
foreach ($User in $CreatedUsers) {
    $LoginData = @{
        username = $User.username
        password = $User.password
    }
    
    $Result = Invoke-TestRequest -Method "POST" -Uri "$BaseUrl/api/users/login" -Body $LoginData -TestName "用户登录: $($User.username)"
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

# 2.4 用户信息查询测试
Write-ColorOutput "2.4 用户信息查询测试" "Yellow"

if ($CreatedUsers.Count -gt 0) {
    $TestUser = $CreatedUsers[0]
    
    # 根据ID查询用户
    $Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/users/$($TestUser.id)" -TestName "根据ID查询用户"
    
    # 根据用户名查询用户
    $Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/users/username/$($TestUser.username)" -TestName "根据用户名查询用户"
    
    # 根据邮箱查询用户
    $Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/users/email/$($TestUser.email)" -TestName "根据邮箱查询用户"
    
    # 查询不存在的用户
    $Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/users/999999" -TestName "查询不存在用户" -ExpectedStatusCode 400
}

# 2.5 用户列表查询测试
Write-ColorOutput "2.5 用户列表查询测试" "Yellow"

# 分页查询用户列表
$Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/users?page=0&size=10" -TestName "分页查询用户列表"

# 查询所有用户
$Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/users/list" -TestName "查询所有用户列表"

# 2.6 密码相关测试
Write-ColorOutput "2.6 密码相关测试" "Yellow"

if ($CreatedUsers.Count -gt 0) {
    $TestUser = $CreatedUsers[0]
    
    # 验证正确密码
    $PasswordData = @{ password = $TestUser.password }
    $Result = Invoke-TestRequest -Method "POST" -Uri "$BaseUrl/api/users/$($TestUser.id)/validate-password" -Body $PasswordData -TestName "验证正确密码"
    
    # 验证错误密码
    $WrongPasswordData = @{ password = "wrong_password" }
    $Result = Invoke-TestRequest -Method "POST" -Uri "$BaseUrl/api/users/$($TestUser.id)/validate-password" -Body $WrongPasswordData -TestName "验证错误密码"
    
    # 修改密码
    $ChangePasswordData = @{
        oldPassword = $TestUser.password
        newPassword = "NewTestPass123!"
    }
    $Result = Invoke-TestRequest -Method "POST" -Uri "$BaseUrl/api/users/$($TestUser.id)/change-password" -Body $ChangePasswordData -TestName "修改用户密码"
    
    # 重置密码
    $ResetPasswordData = @{ newPassword = "ResetPass123!" }
    $Result = Invoke-TestRequest -Method "POST" -Uri "$BaseUrl/api/users/$($TestUser.id)/reset-password" -Body $ResetPasswordData -TestName "重置用户密码"
}

# 2.7 用户状态管理测试
Write-ColorOutput "2.7 用户状态管理测试" "Yellow"

if ($CreatedUsers.Count -gt 1) {
    $TestUser1 = $CreatedUsers[0]
    $TestUser2 = $CreatedUsers[1]
    
    # 停用用户
    $Result = Invoke-TestRequest -Method "POST" -Uri "$BaseUrl/api/users/$($TestUser1.id)/deactivate" -TestName "停用用户"
    
    # 激活用户
    $Result = Invoke-TestRequest -Method "POST" -Uri "$BaseUrl/api/users/$($TestUser1.id)/activate" -TestName "激活用户"
    
    # 切换用户状态
    $Result = Invoke-TestRequest -Method "POST" -Uri "$BaseUrl/api/users/$($TestUser2.id)/toggle-status" -TestName "切换用户状态"
}

# 2.8 用户信息更新测试
Write-ColorOutput "2.8 用户信息更新测试" "Yellow"

if ($CreatedUsers.Count -gt 0) {
    $TestUser = $CreatedUsers[0]
    $UpdateData = @{
        username = $TestUser.username
        email = "updated_" + $TestUser.email
        realName = "更新的用户名"
        password = "UpdatedPass123!"
    }
    
    $Result = Invoke-TestRequest -Method "PUT" -Uri "$BaseUrl/api/users/$($TestUser.id)" -Body $UpdateData -TestName "更新用户信息"
}

# ===========================
# 第三部分：场地管理功能测试
# ===========================
Write-ColorOutput "🏟️ 第三部分：场地管理功能测试" "Magenta"

# 3.1 场地创建测试
Write-ColorOutput "3.1 场地创建测试" "Yellow"

$TestCourts = @()
for ($i = 1; $i -le $TestRounds; $i++) {
    $CourtData = @{
        name = "测试场地$i"
        location = "测试位置$i"
        description = "这是测试场地$i的描述"
        status = "AVAILABLE"
    }
    
    $Result = Invoke-TestRequest -Method "POST" -Uri "$BaseUrl/api/courts" -Body $CourtData -TestName "创建场地: $($CourtData.name)" -ExpectedStatusCode 201
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

Write-ColorOutput "已创建场地数量: $($TestCourts.Count)" "Green"

# 3.2 场地查询测试
Write-ColorOutput "3.2 场地查询测试" "Yellow"

# 查询所有场地
$Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/courts/all" -TestName "查询所有场地"

# 搜索场地（按关键词）
$Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/courts?keyword=测试" -TestName "按关键词搜索场地"

# 搜索场地（按状态）
$Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/courts?status=AVAILABLE" -TestName "按状态搜索场地"

# 获取单个场地详情
if ($TestCourts.Count -gt 0) {
    $TestCourt = $TestCourts[0]
    $Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/courts/$($TestCourt.id)" -TestName "获取场地详情"
    
    # 查询不存在的场地
    $Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/courts/999999" -TestName "查询不存在场地" -ExpectedStatusCode 404
}

# 3.3 场地更新测试
Write-ColorOutput "3.3 场地更新测试" "Yellow"

if ($TestCourts.Count -gt 0) {
    $TestCourt = $TestCourts[0]
    $UpdateData = @{
        name = "更新的$($TestCourt.name)"
        location = "更新的$($TestCourt.location)"
        description = "更新后的场地描述"
        status = "AVAILABLE"
    }
    
    $Result = Invoke-TestRequest -Method "PUT" -Uri "$BaseUrl/api/courts/$($TestCourt.id)" -Body $UpdateData -TestName "更新场地信息"
}

# 3.4 场地状态管理测试
Write-ColorOutput "3.4 场地状态管理测试" "Yellow"

if ($TestCourts.Count -gt 1) {
    $TestCourt1 = $TestCourts[0]
    $TestCourt2 = $TestCourts[1]
    
    # 更改场地状态为维护中
    $Result = Invoke-TestRequest -Method "PATCH" -Uri "$BaseUrl/api/courts/$($TestCourt1.id)/status?status=MAINTENANCE" -TestName "设置场地为维护状态"
    
    # 更改场地状态为不可用
    $Result = Invoke-TestRequest -Method "PATCH" -Uri "$BaseUrl/api/courts/$($TestCourt2.id)/status?status=UNAVAILABLE" -TestName "设置场地为不可用状态"
}

# ===========================
# 第四部分：异常处理和边界条件测试
# ===========================
Write-ColorOutput "⚠️ 第四部分：异常处理和边界条件测试" "Magenta"

# 4.1 无效请求测试
Write-ColorOutput "4.1 无效请求测试" "Yellow"

# 无效JSON格式
$Result = Invoke-TestRequest -Method "POST" -Uri "$BaseUrl/api/users/register" -Body "invalid json" -TestName "发送无效JSON格式" -ExpectedStatusCode 400

# 缺少必需字段
$IncompleteUser = @{ username = "incomplete" }
$Result = Invoke-TestRequest -Method "POST" -Uri "$BaseUrl/api/users/register" -Body $IncompleteUser -TestName "注册缺少必需字段用户" -ExpectedStatusCode 400

# 4.2 重复数据测试
Write-ColorOutput "4.2 重复数据测试" "Yellow"

if ($CreatedUsers.Count -gt 0) {
    $ExistingUser = $CreatedUsers[0]
    $DuplicateUser = @{
        username = $ExistingUser.username
        email = "different@email.com" 
        password = "TestPass123!"
        realName = "重复用户名"
    }
    
    $Result = Invoke-TestRequest -Method "POST" -Uri "$BaseUrl/api/users/register" -Body $DuplicateUser -TestName "注册重复用户名" -ExpectedStatusCode 400
}

# 4.3 不存在资源访问测试
Write-ColorOutput "4.3 不存在资源访问测试" "Yellow"

# 访问不存在的用户
$Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/users/999999" -TestName "访问不存在用户" -ExpectedStatusCode 400

# 访问不存在的场地
$Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/courts/999999" -TestName "访问不存在场地" -ExpectedStatusCode 404

# 访问不存在的端点
$Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/nonexistent" -TestName "访问不存在端点" -ExpectedStatusCode 404

# ===========================
# 第五部分：压力和性能测试
# ===========================
Write-ColorOutput "⚡ 第五部分：压力和性能测试" "Magenta"

Write-ColorOutput "5.1 并发请求测试" "Yellow"

# 并发查询用户列表
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
Write-ColorOutput "并发请求成功: $ConcurrentSuccess/10" "Green"

# ===========================
# 第六部分：数据清理
# ===========================
Write-ColorOutput "🧹 第六部分：数据清理" "Magenta"

# 清理创建的场地
Write-ColorOutput "6.1 清理测试场地" "Yellow"
foreach ($Court in $TestCourts) {
    $Result = Invoke-TestRequest -Method "DELETE" -Uri "$BaseUrl/api/courts/$($Court.id)" -TestName "删除场地: $($Court.name)" -ExpectedStatusCode 204
}

# 清理创建的用户
Write-ColorOutput "6.2 清理测试用户" "Yellow"
foreach ($User in $CreatedUsers) {
    $Result = Invoke-TestRequest -Method "DELETE" -Uri "$BaseUrl/api/users/$($User.id)" -TestName "删除用户: $($User.username)"
}

# ===========================
# 测试结果统计和报告
# ===========================
$TestResults.EndTime = Get-Date
$TestResults.Duration = $TestResults.EndTime - $TestResults.StartTime
$TestResults.Performance.AverageResponse = if ($TestResults.TotalTests -gt 0) { 
    [math]::Round($TestResults.Performance.TotalResponseTime / $TestResults.TotalTests, 2) 
} else { 0 }

$SuccessRate = if ($TestResults.TotalTests -gt 0) { 
    [math]::Round(($TestResults.PassedTests / $TestResults.TotalTests) * 100, 2) 
} else { 0 }

Write-ColorOutput "=" * 60 "Cyan"
Write-ColorOutput "📊 全功能测试完成报告" "Cyan"
Write-ColorOutput "=" * 60 "Cyan"

Write-ColorOutput "⏱️  测试时间: $($TestResults.StartTime.ToString('yyyy-MM-dd HH:mm:ss')) - $($TestResults.EndTime.ToString('yyyy-MM-dd HH:mm:ss'))" "White"
Write-ColorOutput "⏳ 总耗时: $([math]::Round($TestResults.Duration.TotalSeconds, 2)) 秒" "White"
Write-ColorOutput "🎯 总测试数: $($TestResults.TotalTests)" "White"
Write-ColorOutput "✅ 通过测试: $($TestResults.PassedTests)" "Green"
Write-ColorOutput "❌ 失败测试: $($TestResults.FailedTests)" "Red"
Write-ColorOutput "📈 成功率: $SuccessRate%" $(if ($SuccessRate -ge 95) { "Green" } elseif ($SuccessRate -ge 80) { "Yellow" } else { "Red" })

Write-ColorOutput "🚀 性能统计:" "Cyan"
Write-ColorOutput "  • 最快响应: $($TestResults.Performance.FastestResponse)ms" "Green"
Write-ColorOutput "  • 最慢响应: $($TestResults.Performance.SlowestResponse)ms" "Yellow"
Write-ColorOutput "  • 平均响应: $($TestResults.Performance.AverageResponse)ms" "White"

# 系统评级
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

Write-ColorOutput "🏆 系统评级: $Grade 级 ($Score 分)" $GradeColor

$Recommendation = switch ($Grade) {
    "A+" { "🟢 强烈推荐：系统完美稳定，可立即部署到生产环境" }
    "A"  { "🟢 推荐：系统高度稳定，适合生产环境部署" }
    "B+" { "🟡 谨慎推荐：系统基本稳定，建议优化后部署" }
    "B"  { "🟡 需要改进：存在一些问题，建议修复后部署" }
    "C"  { "🟠 不推荐：问题较多，需要大幅改进" }
    "D"  { "🔴 严重问题：系统不稳定，不适合部署" }
}

Write-ColorOutput "💡 部署建议: $Recommendation" "Cyan"

# 保存详细测试结果
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
}

$ReportFile = "comprehensive-test-results-$(Get-Date -Format 'yyyyMMdd-HHmmss').json"
$ReportData | ConvertTo-Json -Depth 10 | Out-File -FilePath $ReportFile -Encoding UTF8

Write-ColorOutput "📄 详细测试报告已保存: $ReportFile" "Cyan"

# 如果有失败的测试，显示详情
if ($TestResults.FailedTests -gt 0) {
    Write-ColorOutput "❌ 失败测试详情:" "Red"
    $FailedTests = $TestResults.TestDetails | Where-Object { -not $_.Success }
    foreach ($Test in $FailedTests) {
        Write-ColorOutput "  • $($Test.TestName): $($Test.Error -or "状态码不匹配")" "Red"
    }
}

Write-ColorOutput "=" * 60 "Cyan"
Write-ColorOutput "🎯 全功能测试脚本执行完成！" "Cyan" 