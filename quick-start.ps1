# CourtLink 场地预约系统 - 快速启动脚本
# 使用方法：在项目根目录下运行 .\quick-start.ps1

Write-Host "🏟️ CourtLink 场地预约系统 - 快速启动" -ForegroundColor Green
Write-Host "===========================================" -ForegroundColor Yellow

# 检查当前目录是否为项目根目录
if (!(Test-Path "pom.xml") -or !(Test-Path "frontend")) {
    Write-Host "❌ 错误：请在项目根目录下运行此脚本！" -ForegroundColor Red
    exit 1
}

Write-Host "📋 启动信息：" -ForegroundColor Cyan
Write-Host "   后端服务：Spring Boot (端口 8080)" -ForegroundColor White
Write-Host "   前端服务：Vue.js + Vite (端口 3007+)" -ForegroundColor White
Write-Host "   数据库：H2 (自动初始化)" -ForegroundColor White
Write-Host ""

Write-Host "🔑 测试账号：" -ForegroundColor Cyan
Write-Host "   用户登录：testuser / test123" -ForegroundColor White
Write-Host "   用户登录：testbooking / test123" -ForegroundColor White
Write-Host "   管理员登录：admin / admin123" -ForegroundColor White
Write-Host ""

# 启动后端服务
Write-Host "🚀 正在启动后端服务..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Write-Host '🔧 后端服务 (Spring Boot)' -ForegroundColor Green; mvn spring-boot:run"

# 等待后端启动
Write-Host "⏳ 等待后端服务启动 (10秒)..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# 启动前端服务
Write-Host "🎨 正在启动前端服务..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Write-Host '🖥️ 前端服务 (Vue.js)' -ForegroundColor Green; cd frontend; npm run dev"

Write-Host ""
Write-Host "✅ 启动完成！" -ForegroundColor Green
Write-Host "📱 前端访问地址：http://localhost:3007/" -ForegroundColor Cyan
Write-Host "🔧 后端API地址：http://localhost:8080/" -ForegroundColor Cyan
Write-Host ""
Write-Host "💡 提示：" -ForegroundColor Yellow
Write-Host "   - 如果前端端口被占用，Vite会自动选择其他端口" -ForegroundColor White
Write-Host "   - 关闭服务请在对应的终端窗口中按 Ctrl+C" -ForegroundColor White
Write-Host "   - 数据库文件位于 data/ 目录下" -ForegroundColor White

# 等待用户按键
Write-Host ""
Write-Host "按任意键关闭此窗口..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown") 