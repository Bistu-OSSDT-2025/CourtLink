# CourtLink 开发环境启动脚本
# 
# 使用方法: .\scripts\utils\start-dev.ps1

Write-Host "🚀 启动 CourtLink 开发环境..." -ForegroundColor Green

# 检查Java环境
Write-Host "检查Java环境..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "Java版本: $javaVersion" -ForegroundColor Cyan
} catch {
    Write-Host "❌ 未找到Java环境，请安装Java 21或更高版本" -ForegroundColor Red
    exit 1
}

# 检查Maven环境
Write-Host "检查Maven环境..." -ForegroundColor Yellow
try {
    $mavenVersion = mvn -version | Select-String "Apache Maven"
    Write-Host "Maven版本: $mavenVersion" -ForegroundColor Cyan
} catch {
    Write-Host "❌ 未找到Maven环境，请安装Maven" -ForegroundColor Red
    exit 1
}

# 检查数据库连接
Write-Host "检查数据库连接..." -ForegroundColor Yellow
# 这里可以添加数据库连接检查逻辑

# 清理target目录
Write-Host "清理构建缓存..." -ForegroundColor Yellow
if (Test-Path "target") {
    Remove-Item -Recurse -Force "target"
}

# 创建logs目录
Write-Host "创建日志目录..." -ForegroundColor Yellow
if (!(Test-Path "logs")) {
    New-Item -ItemType Directory -Path "logs"
}

# 启动应用
Write-Host "启动应用..." -ForegroundColor Green
Write-Host "应用将在 http://localhost:8080/api 启动" -ForegroundColor Cyan
Write-Host "Swagger文档: http://localhost:8080/api/swagger-ui.html" -ForegroundColor Cyan
Write-Host "按 Ctrl+C 停止应用" -ForegroundColor Yellow

mvn spring-boot:run -Dspring-boot.run.profiles=dev

Write-Host "✅ 应用已停止" -ForegroundColor Green 