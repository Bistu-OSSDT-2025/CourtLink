#!/usr/bin/env pwsh

Write-Host "=== Starting CourtLink Enhanced Application ===" -ForegroundColor Green

# 检查Java环境
try {
    $javaVersion = java -version 2>&1 | Select-String "version" | ForEach-Object { $_.ToString() }
    Write-Host "Java Environment: $javaVersion" -ForegroundColor Cyan
} catch {
    Write-Host "Java not found! Please install Java 17 or later." -ForegroundColor Red
    exit 1
}

# 清理之前的构建产物
Write-Host "Cleaning previous builds..." -ForegroundColor Yellow
if (Test-Path "target") {
    Remove-Item -Recurse -Force "target" -ErrorAction SilentlyContinue
}

Write-Host "Starting application with enhanced configurations..." -ForegroundColor Yellow
Write-Host "Enhanced features enabled:" -ForegroundColor Cyan
Write-Host "- Database connection pool: max 20 connections" -ForegroundColor White
Write-Host "- User registration retry mechanism" -ForegroundColor White
Write-Host "- Optimized transaction settings" -ForegroundColor White
Write-Host "- Enhanced error handling" -ForegroundColor White
Write-Host ""
Write-Host "Press Ctrl+C to stop application" -ForegroundColor Red
Write-Host ""

try {
    # 使用 mvnw 启动应用
    .\mvnw.cmd clean spring-boot:run
} catch {
    Write-Host "Error starting application: $_" -ForegroundColor Red
    Write-Host "Please check if Maven Wrapper is available and Java is properly configured." -ForegroundColor Yellow
} 