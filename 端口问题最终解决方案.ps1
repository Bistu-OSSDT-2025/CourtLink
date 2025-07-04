# CourtLink应用8080端口问题最终解决方案
# 解决Spring Boot应用启动但端口无法访问的问题

param(
    [string]$Port = "8080",
    [switch]$Debug,
    [switch]$Force
)

$ErrorActionPreference = "Continue"

Write-Host "? CourtLink应用端口问题诊断与解决" -ForegroundColor Green
Write-Host "=" * 50 -ForegroundColor Cyan

# 设置Java环境
function Set-JavaEnvironment {
    Write-Host "第1步：配置Java环境" -ForegroundColor Yellow
    $env:JAVA_HOME = "C:\Users\ROG\.vscode\cursor1\Oracle_JDK-21"
    $env:PATH = "C:\Users\ROG\.vscode\cursor1\Oracle_JDK-21\bin;" + $env:PATH
    
    Write-Host "? JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Green
    
    try {
        $javaVersion = java -version 2>&1 | Select-String "version" | Select-Object -First 1
        Write-Host "? Java版本: $javaVersion" -ForegroundColor Green
    } catch {
        Write-Host "? Java环境配置失败" -ForegroundColor Red
        return $false
    }
    return $true
}

# 清理现有Java进程
function Stop-AllJavaProcesses {
    Write-Host "第2步：清理现有Java进程" -ForegroundColor Yellow
    
    $javaProcesses = Get-Process java -ErrorAction SilentlyContinue
    if ($javaProcesses) {
        Write-Host "发现 $($javaProcesses.Count) 个Java进程，正在停止..." -ForegroundColor Yellow
        $javaProcesses | Stop-Process -Force -ErrorAction SilentlyContinue
        Start-Sleep -Seconds 3
        Write-Host "? 已清理所有Java进程" -ForegroundColor Green
    } else {
        Write-Host "? 没有运行中的Java进程" -ForegroundColor Green
    }
}

# 检查端口占用
function Test-PortUsage {
    param([string]$TestPort)
    
    Write-Host "第3步：检查端口 $TestPort 占用情况" -ForegroundColor Yellow
    
    $portUsage = netstat -ano | findstr ":$TestPort"
    if ($portUsage) {
        Write-Host "?? 端口 $TestPort 被以下进程占用:" -ForegroundColor Yellow
        $portUsage | ForEach-Object { Write-Host "   $_" -ForegroundColor White }
        
        if ($Force) {
            Write-Host "强制模式：尝试释放端口..." -ForegroundColor Yellow
            # 这里可以添加释放端口的逻辑
        }
    } else {
        Write-Host "? 端口 $TestPort 可用" -ForegroundColor Green
    }
}

# 应用编译和打包
function Build-Application {
    Write-Host "第4步：重新编译和打包应用" -ForegroundColor Yellow
    
    try {
        Write-Host "执行: mvn clean package -DskipTests -q" -ForegroundColor Cyan
        mvn clean package -DskipTests -q
        
        $jarFile = Get-ChildItem target\*.jar | Where-Object {$_.Name -notlike "*sources*" -and $_.Name -notlike "*javadoc*"} | Select-Object -First 1
        if ($jarFile) {
            Write-Host "? 构建成功: $($jarFile.Name)" -ForegroundColor Green
            return $jarFile.FullName
        } else {
            Write-Host "? 未找到可执行JAR文件" -ForegroundColor Red
            return $null
        }
    } catch {
        Write-Host "? 构建失败: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

# 方案1：使用Maven插件启动
function Start-WithMaven {
    param([string]$StartPort)
    
    Write-Host "方案1：使用Maven插件启动" -ForegroundColor Magenta
    
    try {
        Write-Host "执行: mvn spring-boot:run -Dspring-boot.run.arguments='--server.port=$StartPort'" -ForegroundColor Cyan
        Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run", "-Dspring-boot.run.arguments=--server.port=$StartPort" -NoNewWindow
        return $true
    } catch {
        Write-Host "? Maven启动失败: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# 方案2：直接运行JAR
function Start-WithJar {
    param([string]$JarPath, [string]$StartPort)
    
    Write-Host "方案2：直接运行JAR文件" -ForegroundColor Magenta
    
    $javaArgs = @(
        "-Xmx1024m",
        "-Xms512m", 
        "-Dserver.port=$StartPort",
        "-Dspring.profiles.active=default",
        "-Dlogging.level.org.springframework.boot=INFO",
        "-jar",
        $JarPath
    )
    
    try {
        Write-Host "执行: java $($javaArgs -join ' ')" -ForegroundColor Cyan
        Start-Process -FilePath "java" -ArgumentList $javaArgs -NoNewWindow
        return $true
    } catch {
        Write-Host "? JAR启动失败: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# 方案3：使用不同配置启动
function Start-WithCustomConfig {
    param([string]$JarPath, [string]$StartPort)
    
    Write-Host "方案3：使用自定义配置启动" -ForegroundColor Magenta
    
    # 创建临时配置文件
    $tempConfig = @"
server:
  port: $StartPort
  address: 0.0.0.0
  tomcat:
    threads:
      max: 200
      min-spare: 10

spring:
  main:
    web-application-type: servlet

logging:
  level:
    org.springframework.boot: INFO
    org.apache.catalina: INFO
"@
    
    $configFile = "application-temp.yml"
    $tempConfig | Out-File -FilePath $configFile -Encoding UTF8
    
    $javaArgs = @(
        "-Xmx1024m",
        "-Xms512m",
        "-Dspring.config.additional-location=./$configFile",
        "-Dspring.profiles.active=temp",
        "-jar",
        $JarPath
    )
    
    try {
        Write-Host "执行: java $($javaArgs -join ' ')" -ForegroundColor Cyan
        Start-Process -FilePath "java" -ArgumentList $javaArgs -NoNewWindow
        return $true
    } catch {
        Write-Host "? 自定义配置启动失败: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# 测试应用访问
function Test-ApplicationAccess {
    param([string]$TestPort, [int]$MaxAttempts = 10)
    
    Write-Host "第5步：测试应用访问 (端口 $TestPort)" -ForegroundColor Yellow
    
    for ($i = 1; $i -le $MaxAttempts; $i++) {
        Write-Host "尝试 $i/$MaxAttempts : 等待应用启动..." -ForegroundColor Cyan
        Start-Sleep -Seconds 10
        
        # 检查端口监听
        $listening = netstat -ano | findstr "LISTENING" | findstr ":$TestPort"
        if ($listening) {
            Write-Host "? 端口 $TestPort 正在监听！" -ForegroundColor Green
            Write-Host "   $listening" -ForegroundColor White
        } else {
            Write-Host "?? 端口 $TestPort 尚未监听" -ForegroundColor Yellow
            continue
        }
        
        # 测试健康检查
        try {
            $health = Invoke-RestMethod -Uri "http://localhost:$TestPort/actuator/health" -TimeoutSec 5
            Write-Host "? 健康检查成功！状态: $($health.status)" -ForegroundColor Green
            
            # 测试用户API
            try {
                $users = Invoke-RestMethod -Uri "http://localhost:$TestPort/api/users" -TimeoutSec 5
                Write-Host "? 用户API访问成功！返回 $($users.Count) 条记录" -ForegroundColor Green
                
                Write-Host "=" * 50 -ForegroundColor Green
                Write-Host "? 应用启动成功！" -ForegroundColor Green
                Write-Host "? 应用地址: http://localhost:$TestPort" -ForegroundColor Cyan
                Write-Host "? 健康检查: http://localhost:$TestPort/actuator/health" -ForegroundColor Cyan
                Write-Host "? 用户API: http://localhost:$TestPort/api/users" -ForegroundColor Cyan
                Write-Host "? 场地API: http://localhost:$TestPort/api/courts" -ForegroundColor Cyan
                Write-Host "? API文档: http://localhost:$TestPort/swagger-ui/index.html" -ForegroundColor Cyan
                Write-Host "=" * 50 -ForegroundColor Green
                
                return $true
            } catch {
                Write-Host "?? 用户API测试失败: $($_.Exception.Message)" -ForegroundColor Yellow
            }
        } catch {
            Write-Host "? 健康检查失败: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
    
    Write-Host "? 应用访问测试失败" -ForegroundColor Red
    return $false
}

# 主执行流程
function Main {
    Write-Host "开始执行CourtLink应用启动解决方案..." -ForegroundColor Cyan
    
    # 第1步：配置Java环境
    if (-not (Set-JavaEnvironment)) {
        Write-Host "? Java环境配置失败，退出" -ForegroundColor Red
        return
    }
    
    # 第2步：清理现有进程
    Stop-AllJavaProcesses
    
    # 第3步：检查端口
    Test-PortUsage -TestPort $Port
    
    # 第4步：构建应用
    $jarPath = Build-Application
    if (-not $jarPath) {
        Write-Host "? 应用构建失败，退出" -ForegroundColor Red
        return
    }
    
    # 尝试不同的启动方案
    $success = $false
    
    Write-Host "? 尝试启动应用..." -ForegroundColor Green
    
    # 方案1：Maven插件
    if (Start-WithMaven -StartPort $Port) {
        if (Test-ApplicationAccess -TestPort $Port -MaxAttempts 6) {
            $success = $true
        } else {
            Write-Host "方案1失败，停止进程并尝试方案2" -ForegroundColor Yellow
            Stop-AllJavaProcesses
        }
    }
    
    # 方案2：直接运行JAR
    if (-not $success) {
        if (Start-WithJar -JarPath $jarPath -StartPort $Port) {
            if (Test-ApplicationAccess -TestPort $Port -MaxAttempts 6) {
                $success = $true
            } else {
                Write-Host "方案2失败，停止进程并尝试方案3" -ForegroundColor Yellow
                Stop-AllJavaProcesses
            }
        }
    }
    
    # 方案3：自定义配置
    if (-not $success) {
        if (Start-WithCustomConfig -JarPath $jarPath -StartPort $Port) {
            if (Test-ApplicationAccess -TestPort $Port -MaxAttempts 6) {
                $success = $true
            }
        }
    }
    
    if ($success) {
        Write-Host "? CourtLink应用成功启动并可访问！" -ForegroundColor Green
        
        # 现在可以运行分布测试
        Write-Host "? 下一步可以执行分布测试：" -ForegroundColor Cyan
        Write-Host "   .\health-check.ps1" -ForegroundColor White
        Write-Host "   .\quick-test.ps1 -Rounds 3" -ForegroundColor White
        Write-Host "   .\comprehensive-test.ps1 -TestRounds 3" -ForegroundColor White
        
    } else {
        Write-Host "? 所有启动方案都失败了" -ForegroundColor Red
        Write-Host "? 建议检查以下项目：" -ForegroundColor Yellow
        Write-Host "   1. 防火墙设置" -ForegroundColor White
        Write-Host "   2. 网络配置" -ForegroundColor White  
        Write-Host "   3. Spring Boot配置文件" -ForegroundColor White
        Write-Host "   4. H2数据库配置" -ForegroundColor White
    }
}

# 执行主流程
Main 