# 综合测试脚本
param (
    [string]$TestType = "all",
    [int]$ConcurrentUsers = 100,
    [string]$Duration = "1h",
    [string]$Module = "all"
)

# 设置错误操作
$ErrorActionPreference = "Stop"

# 定义颜色输出函数
function Write-ColorOutput($ForegroundColor) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    if ($args) {
        Write-Output $args
    }
    $host.UI.RawUI.ForegroundColor = $fc
}

# 定义测试结果目录
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$resultsDir = "test-results-$timestamp"
New-Item -ItemType Directory -Force -Path $resultsDir | Out-Null

# 运行单元测试
function Run-UnitTests {
    Write-ColorOutput Green "开始运行单元测试..."
    try {
        mvn test -Dtest=*Test -DfailIfNoTests=false
        if ($LASTEXITCODE -eq 0) {
            Write-ColorOutput Green "单元测试完成"
        } else {
            Write-ColorOutput Red "单元测试失败"
            exit 1
        }
    } catch {
        Write-ColorOutput Red "单元测试执行出错: $_"
        exit 1
    }
}

# 运行集成测试
function Run-IntegrationTests {
    Write-ColorOutput Green "开始运行集成测试..."
    try {
        mvn verify -P integration-test -DskipUnitTests=true
        if ($LASTEXITCODE -eq 0) {
            Write-ColorOutput Green "集成测试完成"
        } else {
            Write-ColorOutput Red "集成测试失败"
            exit 1
        }
    } catch {
        Write-ColorOutput Red "集成测试执行出错: $_"
        exit 1
    }
}

# 运行性能测试
function Run-PerformanceTests {
    param (
        [int]$Users,
        [string]$TestDuration
    )
    Write-ColorOutput Green "开始运行性能测试 (用户数: $Users, 持续时间: $TestDuration)..."
    try {
        # 启动应用
        $springBootApp = Start-Process mvn "spring-boot:run" -PassThru
        Start-Sleep -Seconds 30

        # 运行 JMeter 测试
        jmeter -n -t performance-tests.jmx `
            -Jusers=$Users `
            -Jduration=$TestDuration `
            -l "$resultsDir\performance-test-results.jtl" `
            -e -o "$resultsDir\performance-report"

        # 停止应用
        Stop-Process -Id $springBootApp.Id
        Write-ColorOutput Green "性能测试完成"
    } catch {
        Write-ColorOutput Red "性能测试执行出错: $_"
        exit 1
    }
}

# 运行安全测试
function Run-SecurityTests {
    Write-ColorOutput Green "开始运行安全测试..."
    try {
        # 运行 OWASP 依赖检查
        mvn org.owasp:dependency-check-maven:check
        
        # 运行安全单元测试
        mvn test -Dtest=*SecurityTest -DfailIfNoTests=false
        
        Write-ColorOutput Green "安全测试完成"
    } catch {
        Write-ColorOutput Red "安全测试执行出错: $_"
        exit 1
    }
}

# 运行端到端测试
function Run-E2ETests {
    Write-ColorOutput Green "开始运行端到端测试..."
    try {
        # 启动应用
        $springBootApp = Start-Process mvn "spring-boot:run" -PassThru
        Start-Sleep -Seconds 30

        # 运行 Selenium 测试
        mvn test -Dtest=*E2ETest -DfailIfNoTests=false

        # 停止应用
        Stop-Process -Id $springBootApp.Id
        Write-ColorOutput Green "端到端测试完成"
    } catch {
        Write-ColorOutput Red "端到端测试执行出错: $_"
        exit 1
    }
}

# 生成测试报告
function Generate-TestReport {
    Write-ColorOutput Green "生成测试报告..."
    try {
        # 合并测试结果
        Get-ChildItem -Path $resultsDir -Filter *.jtl | 
        ForEach-Object {
            $reportFile = "$resultsDir\combined-report.html"
            Add-Content -Path $reportFile -Value (Get-Content $_.FullName)
        }
        
        # 生成覆盖率报告
        mvn jacoco:report
        
        Write-ColorOutput Green "测试报告生成完成"
    } catch {
        Write-ColorOutput Red "报告生成出错: $_"
        exit 1
    }
}

# 主测试流程
Write-ColorOutput Cyan "开始综合测试流程..."

switch ($TestType) {
    "unit" { 
        Run-UnitTests 
    }
    "integration" { 
        Run-IntegrationTests 
    }
    "performance" { 
        Run-PerformanceTests -Users $ConcurrentUsers -TestDuration $Duration 
    }
    "security" { 
        Run-SecurityTests 
    }
    "e2e" { 
        Run-E2ETests 
    }
    "all" {
        Run-UnitTests
        Run-IntegrationTests
        Run-SecurityTests
        Run-PerformanceTests -Users $ConcurrentUsers -TestDuration $Duration
        Run-E2ETests
    }
    default { 
        Write-ColorOutput Red "未知的测试类型: $TestType"
        exit 1
    }
}

Generate-TestReport

Write-ColorOutput Green "所有测试完成！"
Write-ColorOutput Yellow "测试报告位置: $resultsDir" 