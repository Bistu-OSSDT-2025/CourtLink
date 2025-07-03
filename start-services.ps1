# 启动羽毛球场地管理系统
Write-Host "=== 启动羽毛球场地管理系统 ===" -ForegroundColor Green

# 检查端口是否被占用
$backendPort = 8082
$frontendPort = 3008

Write-Host "检查端口占用情况..." -ForegroundColor Yellow

# 检查后端端口
$backendProcess = Get-NetTCPConnection -LocalPort $backendPort -ErrorAction SilentlyContinue
if ($backendProcess) {
    Write-Host "后端端口 $backendPort 已被占用" -ForegroundColor Red
    Write-Host "请先关闭占用端口的进程，或者等待现有服务启动完成" -ForegroundColor Yellow
} else {
    Write-Host "后端端口 $backendPort 可用" -ForegroundColor Green
}

# 检查前端端口
$frontendProcess = Get-NetTCPConnection -LocalPort $frontendPort -ErrorAction SilentlyContinue
if ($frontendProcess) {
    Write-Host "前端端口 $frontendPort 已被占用" -ForegroundColor Red
    Write-Host "请先关闭占用端口的进程，或者等待现有服务启动完成" -ForegroundColor Yellow
} else {
    Write-Host "前端端口 $frontendPort 可用" -ForegroundColor Green
}

# 启动后端服务
Write-Host "`n=== 启动后端服务 ===" -ForegroundColor Green
Write-Host "正在启动Spring Boot应用..." -ForegroundColor Yellow

# 使用新的PowerShell窗口启动后端
$backendJob = Start-Job -ScriptBlock {
    Set-Location $using:PWD
    mvn spring-boot:run -q
}

Write-Host "后端服务已在后台启动 (Job ID: $($backendJob.Id))" -ForegroundColor Green

# 等待后端启动
Write-Host "等待后端服务启动..." -ForegroundColor Yellow
$maxWait = 60
$waited = 0

do {
    Start-Sleep -Seconds 2
    $waited += 2
    
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$backendPort/actuator/health" -Method GET -TimeoutSec 5
        if ($response.StatusCode -eq 200) {
            Write-Host "✅ 后端服务已启动成功" -ForegroundColor Green
            break
        }
    } catch {
        # 继续等待
    }
    
    if ($waited -ge $maxWait) {
        Write-Host "❌ 后端服务启动超时" -ForegroundColor Red
        break
    }
    
    Write-Host "等待中... ($waited/$maxWait 秒)" -ForegroundColor Yellow
} while ($true)

# 启动前端服务
Write-Host "`n=== 启动前端服务 ===" -ForegroundColor Green
Write-Host "正在启动Vue开发服务器..." -ForegroundColor Yellow

# 切换到前端目录
Push-Location frontend

# 检查依赖
if (-not (Test-Path "node_modules")) {
    Write-Host "安装前端依赖..." -ForegroundColor Yellow
    npm install
}

# 使用新的PowerShell窗口启动前端
$frontendJob = Start-Job -ScriptBlock {
    Set-Location $using:PWD
    Set-Location frontend
    npm run dev
}

Write-Host "前端服务已在后台启动 (Job ID: $($frontendJob.Id))" -ForegroundColor Green

# 等待前端启动
Write-Host "等待前端服务启动..." -ForegroundColor Yellow
$maxWait = 30
$waited = 0

do {
    Start-Sleep -Seconds 2
    $waited += 2
    
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$frontendPort" -Method GET -TimeoutSec 5
        if ($response.StatusCode -eq 200) {
            Write-Host "✅ 前端服务已启动成功" -ForegroundColor Green
            break
        }
    } catch {
        # 继续等待
    }
    
    if ($waited -ge $maxWait) {
        Write-Host "❌ 前端服务启动超时" -ForegroundColor Red
        break
    }
    
    Write-Host "等待中... ($waited/$maxWait 秒)" -ForegroundColor Yellow
} while ($true)

Pop-Location

Write-Host "`n=== 服务启动完成 ===" -ForegroundColor Green
Write-Host "🌐 前端地址: http://localhost:$frontendPort" -ForegroundColor Cyan
Write-Host "🔧 后端地址: http://localhost:$backendPort" -ForegroundColor Cyan
Write-Host "👤 管理员账号: admin / admin123" -ForegroundColor Cyan
Write-Host "📊 H2数据库控制台: http://localhost:$backendPort/h2-console" -ForegroundColor Cyan

Write-Host "`n=== 使用说明 ===" -ForegroundColor Yellow
Write-Host "1. 访问前端地址，点击右上角'管理员登录'" -ForegroundColor White
Write-Host "2. 使用管理员账号登录" -ForegroundColor White
Write-Host "3. 在管理界面中可以管理场地和时间段" -ForegroundColor White
Write-Host "4. 单个时段操作：点击时段可切换开放/关闭状态" -ForegroundColor White
Write-Host "5. 批量操作：使用'批量开放'按钮开放所有关闭的时段" -ForegroundColor White

Write-Host "`n⚠️  注意：关闭此窗口会停止所有服务" -ForegroundColor Red
Write-Host "按 Ctrl+C 停止服务，或直接关闭窗口" -ForegroundColor Yellow

# 等待用户输入
Write-Host "`n按任意键打开浏览器..." -ForegroundColor Green
$null = Read-Host

# 打开浏览器
Start-Process "http://localhost:$frontendPort"

# 保持脚本运行
Write-Host "服务正在运行中，按 Ctrl+C 停止服务..." -ForegroundColor Green
try {
    while ($true) {
        Start-Sleep -Seconds 5
        
        # 检查服务状态
        $backendRunning = $false
        $frontendRunning = $false
        
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:$backendPort/actuator/health" -Method GET -TimeoutSec 2
            $backendRunning = $response.StatusCode -eq 200
        } catch { }
        
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:$frontendPort" -Method GET -TimeoutSec 2
            $frontendRunning = $response.StatusCode -eq 200
        } catch { }
        
        $status = "后端: $(if($backendRunning){'✅'}else{'❌'}) | 前端: $(if($frontendRunning){'✅'}else{'❌'})"
        Write-Host "服务状态: $status" -ForegroundColor Cyan
    }
} finally {
    Write-Host "`n=== 停止服务 ===" -ForegroundColor Red
    
    # 停止后台作业
    if ($backendJob) {
        Stop-Job -Job $backendJob -ErrorAction SilentlyContinue
        Remove-Job -Job $backendJob -ErrorAction SilentlyContinue
    }
    
    if ($frontendJob) {
        Stop-Job -Job $frontendJob -ErrorAction SilentlyContinue
        Remove-Job -Job $frontendJob -ErrorAction SilentlyContinue
    }
    
    Write-Host "所有服务已停止" -ForegroundColor Green
} 