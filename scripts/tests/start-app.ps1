#!/usr/bin/env pwsh

# CourtLink应用启动脚本
Write-Host "=== CourtLink 羽毛球场地预订系统启动 ===" -ForegroundColor Green

# 检查Java版本
Write-Host "检查Java环境..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1
    Write-Host "Java版本: $($javaVersion[0])" -ForegroundColor Cyan
} catch {
    Write-Error "Java未安装或未配置环境变量"
    exit 1
}

# 进入项目目录
Write-Host "进入项目目录..." -ForegroundColor Yellow
Set-Location "src"

# 清理之前的构建
Write-Host "清理项目..." -ForegroundColor Yellow
mvn clean

# 启动应用
Write-Host "启动SpringBoot应用..." -ForegroundColor Yellow
Write-Host "应用将在 http://localhost:8080 启动" -ForegroundColor Green
Write-Host "健康检查: http://localhost:8080/actuator/health" -ForegroundColor Green
Write-Host "API文档: http://localhost:8080/swagger-ui.html" -ForegroundColor Green
Write-Host "按 Ctrl+C 停止应用" -ForegroundColor Red

mvn spring-boot:run 