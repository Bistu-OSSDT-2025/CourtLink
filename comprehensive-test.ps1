# 主分支全功能综合测试脚本
# 测试所有功能模块：用户管理、场地管理、健康检查、异常处理

param(
    [string]$BaseUrl = "http://localhost:8080",
    [int]$TestRounds = 3,
    [switch]$Verbose
)

$ErrorActionPreference = "Continue"
$ProgressPreference = "SilentlyContinue"

# 测试统计
$TestResults = @{
    StartTime = Get-Date
    TotalTests = 0
    PassedTests = 0
    FailedTests = 0
    TestDetails = @()
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
Write-ColorOutput "=" * 60 "Cyan"

# 第一部分：系统健康检查测试
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

# 第二部分：用户管理功能测试
Write-ColorOutput "👥 第二部分：用户管理功能测试" "Magenta"

# 测试数据
$TestUsers = @()
for ($i = 1; $i -le $TestRounds; $i++) {
    $TestUsers += @{
        username = "fulltest_user_$i"
        email = "fulltest$i@test.com"
        password = "TestPass123!"
        realName = "全功能测试用户$i"
    }
}

# 用户注册测试
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

# 用户登录测试
Write-ColorOutput "2.2 用户登录测试" "Yellow"
foreach ($User in $CreatedUsers) {
    $LoginData = @{
        username = $User.username
        password = $User.password
    }
    $Result = Invoke-TestRequest -Method "POST" -Uri "$BaseUrl/api/users/login" -Body $LoginData -TestName "用户登录: $($User.username)"
}

# 用户查询测试
Write-ColorOutput "2.3 用户查询测试" "Yellow"
if ($CreatedUsers.Count -gt 0) {
    $TestUser = $CreatedUsers[0]
    
    # 各种查询方式
    $Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/users/$($TestUser.id)" -TestName "根据ID查询用户"
    $Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/users/username/$($TestUser.username)" -TestName "根据用户名查询用户"
    $Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/users/email/$($TestUser.email)" -TestName "根据邮箱查询用户"
    $Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/users" -TestName "分页查询用户列表"
    $Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/users/list" -TestName "查询所有用户"
    
    # 检查存在性
    $Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/users/check-username?username=$($TestUser.username)" -TestName "检查用户名存在"
    $Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/users/check-email?email=$($TestUser.email)" -TestName "检查邮箱存在"
}

# 密码相关测试
Write-ColorOutput "2.4 密码相关测试" "Yellow"
if ($CreatedUsers.Count -gt 0) {
    $TestUser = $CreatedUsers[0]
    
    # 密码验证
    $PasswordData = @{ password = $TestUser.password }
    $Result = Invoke-TestRequest -Method "POST" -Uri "$BaseUrl/api/users/$($TestUser.id)/validate-password" -Body $PasswordData -TestName "验证正确密码"
    
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

# 用户状态管理测试
Write-ColorOutput "2.5 用户状态管理测试" "Yellow"
if ($CreatedUsers.Count -gt 0) {
    $TestUser = $CreatedUsers[0]
    
    $Result = Invoke-TestRequest -Method "POST" -Uri "$BaseUrl/api/users/$($TestUser.id)/deactivate" -TestName "停用用户"
    $Result = Invoke-TestRequest -Method "POST" -Uri "$BaseUrl/api/users/$($TestUser.id)/activate" -TestName "激活用户"
    $Result = Invoke-TestRequest -Method "POST" -Uri "$BaseUrl/api/users/$($TestUser.id)/toggle-status" -TestName "切换用户状态"
}

# 用户信息更新测试
Write-ColorOutput "2.6 用户信息更新测试" "Yellow"
if ($CreatedUsers.Count -gt 0) {
    $TestUser = $CreatedUsers[0]
    $UpdateData = @{
        username = $TestUser.username
        email = "updated_$($TestUser.email)"
        realName = "更新的用户名"
        password = "UpdatedPass123!"
    }
    $Result = Invoke-TestRequest -Method "PUT" -Uri "$BaseUrl/api/users/$($TestUser.id)" -Body $UpdateData -TestName "更新用户信息"
}

# 第三部分：场地管理功能测试
Write-ColorOutput "🏟️ 第三部分：场地管理功能测试" "Magenta"

# 场地创建测试
Write-ColorOutput "3.1 场地创建测试" "Yellow"
$TestCourts = @()
for ($i = 1; $i -le $TestRounds; $i++) {
    $CourtData = @{
        name = "全功能测试场地$i"
        location = "测试位置$i"
        description = "全功能测试场地$i的描述"
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

# 场地查询测试
Write-ColorOutput "3.2 场地查询测试" "Yellow"
$Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/courts/all" -TestName "查询所有场地"
$Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/courts?keyword=全功能" -TestName "按关键词搜索场地"
$Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/courts?status=AVAILABLE" -TestName "按状态搜索场地"

if ($TestCourts.Count -gt 0) {
    $TestCourt = $TestCourts[0]
    $Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/courts/$($TestCourt.id)" -TestName "获取场地详情"
}

# 场地更新和状态管理测试
Write-ColorOutput "3.3 场地更新和状态管理测试" "Yellow"
if ($TestCourts.Count -gt 0) {
    $TestCourt = $TestCourts[0]
    
    # 更新场地信息
    $UpdateData = @{
        name = "更新的$($TestCourt.name)"
        location = "更新的$($TestCourt.location)"
        description = "更新后的场地描述"
        status = "AVAILABLE"
    }
    $Result = Invoke-TestRequest -Method "PUT" -Uri "$BaseUrl/api/courts/$($TestCourt.id)" -Body $UpdateData -TestName "更新场地信息"
    
    # 更改场地状态
    $Result = Invoke-TestRequest -Method "PATCH" -Uri "$BaseUrl/api/courts/$($TestCourt.id)/status?status=MAINTENANCE" -TestName "设置场地为维护状态"
}

# 第四部分：异常处理测试
Write-ColorOutput "⚠️ 第四部分：异常处理测试" "Magenta"

# 访问不存在的资源
$Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/users/999999" -TestName "访问不存在用户" -ExpectedStatusCode 400
$Result = Invoke-TestRequest -Method "GET" -Uri "$BaseUrl/api/courts/999999" -TestName "访问不存在场地" -ExpectedStatusCode 404

# 无效请求
$Result = Invoke-TestRequest -Method "POST" -Uri "$BaseUrl/api/users/register" -Body "invalid json" -TestName "发送无效JSON" -ExpectedStatusCode 400

# 重复数据
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

# 第五部分：数据清理
Write-ColorOutput "🧹 第五部分：数据清理" "Magenta"

# 清理场地
foreach ($Court in $TestCourts) {
    $Result = Invoke-TestRequest -Method "DELETE" -Uri "$BaseUrl/api/courts/$($Court.id)" -TestName "删除场地: $($Court.name)" -ExpectedStatusCode 204
}

# 清理用户
foreach ($User in $CreatedUsers) {
    $Result = Invoke-TestRequest -Method "DELETE" -Uri "$BaseUrl/api/users/$($User.id)" -TestName "删除用户: $($User.username)"
}

# 测试结果统计
$TestResults.EndTime = Get-Date
$TestResults.Duration = $TestResults.EndTime - $TestResults.StartTime
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

# 系统评级
$Grade = ""
$GradeColor = ""
$Score = 0

if ($SuccessRate -ge 98) {
    $Grade = "A+"
    $GradeColor = "Green"
    $Score = 99
} elseif ($SuccessRate -ge 95) {
    $Grade = "A"
    $GradeColor = "Green"
    $Score = 95
} elseif ($SuccessRate -ge 90) {
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

# 保存测试结果
$ReportFile = "comprehensive-test-results-$(Get-Date -Format 'yyyyMMdd-HHmmss').json"
$TestResults | ConvertTo-Json -Depth 10 | Out-File -FilePath $ReportFile -Encoding UTF8
Write-ColorOutput "📄 详细测试报告已保存: $ReportFile" "Cyan"

# 显示失败测试详情
if ($TestResults.FailedTests -gt 0) {
    Write-ColorOutput "❌ 失败测试详情:" "Red"
    $FailedTests = $TestResults.TestDetails | Where-Object { -not $_.Success }
    foreach ($Test in $FailedTests) {
        Write-ColorOutput "  • $($Test.TestName): $($Test.Error -or "状态码不匹配")" "Red"
    }
}

Write-ColorOutput "=" * 60 "Cyan"
Write-ColorOutput "🎯 全功能测试脚本执行完成！" "Cyan" 