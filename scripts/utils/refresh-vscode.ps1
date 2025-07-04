#!/usr/bin/env pwsh
# CourtLink VS Code + Java 缓存刷新脚本
# 
# 使用方法: .\scripts\utils\refresh-vscode.ps1

param(
    [switch]$DeepClean,      # 执行深度清理
    [switch]$SkipMaven       # 跳过Maven构建
)

Write-Host "🔄 开始刷新VS Code Java环境..." -ForegroundColor Green
Write-Host "项目: CourtLink 羽毛球场预订系统" -ForegroundColor Cyan

# Step 1: Maven清理（除非跳过）
if (-not $SkipMaven) {
    Write-Host "`n1️⃣ Maven清理和依赖更新..." -ForegroundColor Yellow
    
    # 基础清理
    Write-Host "   清理项目构建缓存..." -ForegroundColor Gray
    mvn clean -q
    
    # 强制更新依赖
    Write-Host "   强制更新所有依赖..." -ForegroundColor Gray
    mvn install -U -q
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Maven构建失败，请检查网络连接和配置" -ForegroundColor Red
        exit 1
    }
    
    # 验证JWT依赖
    Write-Host "   验证JWT依赖..." -ForegroundColor Gray
    $jwtCheck = mvn dependency:tree | Select-String "jsonwebtoken"
    if ($jwtCheck) {
        Write-Host "   ✅ JWT依赖正常: $($jwtCheck -join ', ')" -ForegroundColor Green
    } else {
        Write-Host "   ⚠️ 未检测到JWT依赖" -ForegroundColor Yellow
    }
} else {
    Write-Host "⏭️ 跳过Maven构建步骤" -ForegroundColor Yellow
}

# Step 2: 清理VS Code缓存
Write-Host "`n2️⃣ 清理VS Code缓存..." -ForegroundColor Yellow

# 工作空间缓存
$vsCodeCache = "$env:APPDATA\Code\User\workspaceStorage"
if (Test-Path $vsCodeCache) {
    Write-Host "   清理工作空间缓存..." -ForegroundColor Gray
    try {
        Get-ChildItem $vsCodeCache | Remove-Item -Recurse -Force -ErrorAction SilentlyContinue
        Write-Host "   ✅ 工作空间缓存已清理" -ForegroundColor Green
    } catch {
        Write-Host "   ⚠️ 部分工作空间缓存清理失败（可能被占用）" -ForegroundColor Yellow
    }
} else {
    Write-Host "   ℹ️ 未找到工作空间缓存目录" -ForegroundColor Cyan
}

# Java扩展缓存
Write-Host "   清理Java扩展缓存..." -ForegroundColor Gray
$javaCache = "$env:APPDATA\Code\CachedExtensions"
if (Test-Path $javaCache) {
    try {
        Get-ChildItem $javaCache -Filter "*java*" | Remove-Item -Recurse -Force -ErrorAction SilentlyContinue
        Get-ChildItem $javaCache -Filter "*redhat*" | Remove-Item -Recurse -Force -ErrorAction SilentlyContinue
        Write-Host "   ✅ Java扩展缓存已清理" -ForegroundColor Green
    } catch {
        Write-Host "   ⚠️ 部分Java扩展缓存清理失败" -ForegroundColor Yellow
    }
} else {
    Write-Host "   ℹ️ 未找到Java扩展缓存目录" -ForegroundColor Cyan
}

# Step 3: 深度清理（可选）
if ($DeepClean) {
    Write-Host "`n3️⃣ 执行深度清理..." -ForegroundColor Yellow
    
    # 清理用户数据缓存
    $userDataCache = "$env:APPDATA\Code\CachedData"
    if (Test-Path $userDataCache) {
        Write-Host "   清理用户数据缓存..." -ForegroundColor Gray
        Get-ChildItem $userDataCache | Remove-Item -Recurse -Force -ErrorAction SilentlyContinue
    }
    
    # 清理日志文件
    $logsPath = "$env:APPDATA\Code\logs"
    if (Test-Path $logsPath) {
        Write-Host "   清理旧日志文件..." -ForegroundColor Gray
        Get-ChildItem $logsPath -Filter "*.log" | Where-Object LastWriteTime -lt (Get-Date).AddDays(-7) | Remove-Item -Force
    }
    
    # 清理临时文件
    Write-Host "   清理系统临时文件..." -ForegroundColor Gray
    $tempPath = "$env:TEMP\vscode-*"
    Get-ChildItem $tempPath -ErrorAction SilentlyContinue | Remove-Item -Recurse -Force -ErrorAction SilentlyContinue
    
    Write-Host "   ✅ 深度清理完成" -ForegroundColor Green
}

# Step 4: 项目特定清理
Write-Host "`n4️⃣ 清理项目特定缓存..." -ForegroundColor Yellow

# 删除target目录
if (Test-Path "target") {
    Write-Host "   清理Maven target目录..." -ForegroundColor Gray
    Remove-Item "target" -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "   ✅ Target目录已清理" -ForegroundColor Green
}

# 清理VS Code项目缓存
if (Test-Path ".vscode/.java") {
    Write-Host "   清理VS Code Java项目缓存..." -ForegroundColor Gray
    Remove-Item ".vscode/.java" -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "   ✅ VS Code Java缓存已清理" -ForegroundColor Green
}

# Step 5: 验证环境
Write-Host "`n5️⃣ 验证开发环境..." -ForegroundColor Yellow

# Java环境检查
Write-Host "   检查Java环境..." -ForegroundColor Gray
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "   ✅ Java版本: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "   ❌ Java环境异常" -ForegroundColor Red
}

# Maven环境检查
Write-Host "   检查Maven环境..." -ForegroundColor Gray
try {
    $mavenVersion = mvn -version 2>&1 | Select-String "Apache Maven"
    Write-Host "   ✅ Maven版本: $mavenVersion" -ForegroundColor Green
} catch {
    Write-Host "   ❌ Maven环境异常" -ForegroundColor Red
}

# 项目编译验证
if (-not $SkipMaven) {
    Write-Host "   验证项目编译..." -ForegroundColor Gray
    mvn compile -q
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   ✅ 项目编译成功" -ForegroundColor Green
    } else {
        Write-Host "   ❌ 项目编译失败" -ForegroundColor Red
    }
}

# 完成
Write-Host "`n🎉 VS Code缓存刷新完成！" -ForegroundColor Green

Write-Host "`n📝 后续操作指南:" -ForegroundColor Cyan
Write-Host "1. 重新启动VS Code" -ForegroundColor White
Write-Host "2. 打开CourtLink项目" -ForegroundColor White
Write-Host "3. 等待Java扩展自动加载（可能需要1-2分钟）" -ForegroundColor White
Write-Host "4. 执行命令: Ctrl+Shift+P → 'Java: Clean Workspace'" -ForegroundColor White
Write-Host "5. 执行命令: Ctrl+Shift+P → 'Java: Rebuild Projects'" -ForegroundColor White

Write-Host "`n💡 如果问题仍然存在:" -ForegroundColor Yellow
Write-Host "• 运行深度清理: .\scripts\utils\refresh-vscode.ps1 -DeepClean" -ForegroundColor Gray
Write-Host "• 检查网络连接和代理设置" -ForegroundColor Gray
Write-Host "• 确认Java 21和Maven版本兼容性" -ForegroundColor Gray

Write-Host "`n🔍 故障排除: 查看 docs/reports/IDE_CACHE_REFRESH_GUIDE.md" -ForegroundColor Cyan 