# CourtLink 全量测试脚本
# 
# 使用方法: .\scripts\tests\run-all-tests.ps1

param(
    [switch]$SkipIntegration,    # 跳过集成测试
    [switch]$GenerateReport,     # 生成测试报告
    [string]$TestProfile = "test" # 测试配置文件
)

Write-Host "🧪 开始运行 CourtLink 全量测试..." -ForegroundColor Green

# 设置测试环境变量
$env:SPRING_PROFILES_ACTIVE = $TestProfile

# 创建测试报告目录
$reportDir = "docs/tests/reports-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
if ($GenerateReport) {
    New-Item -ItemType Directory -Force -Path $reportDir
    Write-Host "测试报告将保存到: $reportDir" -ForegroundColor Cyan
}

# 1. 运行单元测试
Write-Host "`n📋 运行单元测试..." -ForegroundColor Yellow
$unitTestResult = mvn test -Dtest="*Test" -DfailIfNoTests=false
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 单元测试失败" -ForegroundColor Red
    exit 1
}
Write-Host "✅ 单元测试通过" -ForegroundColor Green

# 2. 运行集成测试（如果未跳过）
if (-not $SkipIntegration) {
    Write-Host "`n🔗 运行集成测试..." -ForegroundColor Yellow
    $integrationTestResult = mvn test -Dtest="*IntegrationTest" -DfailIfNoTests=false
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ 集成测试失败" -ForegroundColor Red
        exit 1
    }
    Write-Host "✅ 集成测试通过" -ForegroundColor Green
} else {
    Write-Host "⏭️  跳过集成测试" -ForegroundColor Yellow
}

# 3. 运行端到端测试
Write-Host "`n🌐 运行端到端测试..." -ForegroundColor Yellow
$e2eTestResult = mvn test -Dtest="*E2ETest" -DfailIfNoTests=false
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 端到端测试失败" -ForegroundColor Red
    exit 1
}
Write-Host "✅ 端到端测试通过" -ForegroundColor Green

# 4. 生成测试覆盖率报告
Write-Host "`n📊 生成测试覆盖率报告..." -ForegroundColor Yellow
mvn jacoco:report
Write-Host "✅ 覆盖率报告已生成" -ForegroundColor Green

# 5. 生成汇总报告
if ($GenerateReport) {
    Write-Host "`n📄 生成测试汇总报告..." -ForegroundColor Yellow
    
    $summary = @"
# CourtLink 测试报告
生成时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
测试配置: $TestProfile

## 测试结果
- ✅ 单元测试: 通过
- $(if ($SkipIntegration) { "⏭️  集成测试: 跳过" } else { "✅ 集成测试: 通过" })
- ✅ 端到端测试: 通过
- ✅ 覆盖率报告: 已生成

## 测试统计
详细统计信息请查看 target/site/jacoco/index.html

## 注意事项
- 所有核心功能测试通过
- 建议定期运行全量测试确保代码质量
- 新功能开发时请确保添加相应测试用例
"@
    
    $summary | Out-File -FilePath "$reportDir/test-summary.md" -Encoding UTF8
    Write-Host "✅ 测试报告已保存到: $reportDir/test-summary.md" -ForegroundColor Green
}

Write-Host "`n🎉 所有测试完成！" -ForegroundColor Green
Write-Host "📊 覆盖率报告: target/site/jacoco/index.html" -ForegroundColor Cyan

if ($GenerateReport) {
    Write-Host "📄 详细报告: $reportDir/test-summary.md" -ForegroundColor Cyan
} 