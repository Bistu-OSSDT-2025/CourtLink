#!/usr/bin/env pwsh
# CourtLink IntelliJ IDEA 缓存刷新脚本
# 
# 使用方法: .\scripts\utils\refresh-idea.ps1

param(
    [switch]$DeepClean,      # 执行深度清理
    [switch]$SkipMaven       # 跳过Maven构建
)

Write-Host "🔄 开始刷新IntelliJ IDEA Java环境..." -ForegroundColor Green
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

# Step 2: 清理IDEA项目配置
Write-Host "`n2️⃣ 清理IDEA项目配置..." -ForegroundColor Yellow

# 删除IDEA配置文件
if (Test-Path ".idea") {
    Write-Host "   删除 .idea 目录..." -ForegroundColor Gray
    try {
        Remove-Item ".idea" -Recurse -Force
        Write-Host "   ✅ .idea目录已删除" -ForegroundColor Green
    } catch {
        Write-Host "   ⚠️ .idea目录删除失败（可能被IDEA占用）" -ForegroundColor Yellow
        Write-Host "   ℹ️ 请先关闭IDEA，然后重新运行此脚本" -ForegroundColor Cyan
    }
} else {
    Write-Host "   ℹ️ 未找到 .idea 目录" -ForegroundColor Cyan
}

# 删除IDEA模块文件
$imlFiles = Get-ChildItem "*.iml" -ErrorAction SilentlyContinue
if ($imlFiles) {
    Write-Host "   删除 .iml 模块文件..." -ForegroundColor Gray
    $imlFiles | Remove-Item -Force
    Write-Host "   ✅ 模块文件已删除: $($imlFiles.Count)个" -ForegroundColor Green
} else {
    Write-Host "   ℹ️ 未找到 .iml 模块文件" -ForegroundColor Cyan
}

# Step 3: 清理IDEA系统缓存
Write-Host "`n3️⃣ 清理IDEA系统缓存..." -ForegroundColor Yellow

# 查找IDEA安装版本
$ideaVersions = @()
$possiblePaths = @(
    "$env:APPDATA\JetBrains\IntelliJIdea*",
    "$env:LOCALAPPDATA\JetBrains\IntelliJIdea*"
)

foreach ($pattern in $possiblePaths) {
    $found = Get-ChildItem $pattern -Directory -ErrorAction SilentlyContinue
    $ideaVersions += $found
}

if ($ideaVersions.Count -gt 0) {
    Write-Host "   找到IDEA版本: $($ideaVersions.Count)个" -ForegroundColor Gray
    
    foreach ($ideaPath in $ideaVersions) {
        $ideaVersion = $ideaPath.Name
        Write-Host "   处理版本: $ideaVersion" -ForegroundColor Gray
        
        # 清理编译缓存
        $compilePath = Join-Path $ideaPath.FullName "system\compile-server"
        if (Test-Path $compilePath) {
            Remove-Item $compilePath -Recurse -Force -ErrorAction SilentlyContinue
            Write-Host "     ✅ 编译缓存已清理" -ForegroundColor Green
        }
        
        # 清理索引缓存
        $indexPath = Join-Path $ideaPath.FullName "system\index"
        if (Test-Path $indexPath) {
            Remove-Item $indexPath -Recurse -Force -ErrorAction SilentlyContinue
            Write-Host "     ✅ 索引缓存已清理" -ForegroundColor Green
        }
        
        # 清理Maven缓存
        $mavenPath = Join-Path $ideaPath.FullName "system\Maven"
        if (Test-Path $mavenPath) {
            Remove-Item $mavenPath -Recurse -Force -ErrorAction SilentlyContinue
            Write-Host "     ✅ Maven缓存已清理" -ForegroundColor Green
        }
        
        # 深度清理（可选）
        if ($DeepClean) {
            $systemPath = Join-Path $ideaPath.FullName "system"
            if (Test-Path $systemPath) {
                Write-Host "     执行深度清理..." -ForegroundColor Gray
                Remove-Item $systemPath -Recurse -Force -ErrorAction SilentlyContinue
                Write-Host "     ✅ 深度清理完成" -ForegroundColor Green
            }
        }
    }
} else {
    Write-Host "   ℹ️ 未找到IDEA安装目录" -ForegroundColor Cyan
    Write-Host "   💡 请确认IDEA已正确安装" -ForegroundColor Yellow
}

# Step 4: 项目特定清理
Write-Host "`n4️⃣ 清理项目特定缓存..." -ForegroundColor Yellow

# 删除target目录
if (Test-Path "target") {
    Write-Host "   清理Maven target目录..." -ForegroundColor Gray
    Remove-Item "target" -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "   ✅ Target目录已清理" -ForegroundColor Green
}

# 清理out目录（IDEA编译输出）
if (Test-Path "out") {
    Write-Host "   清理IDEA out目录..." -ForegroundColor Gray
    Remove-Item "out" -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "   ✅ Out目录已清理" -ForegroundColor Green
}

# 清理本地Maven仓库中的项目artifacts（深度清理）
if ($DeepClean -and (-not $SkipMaven)) {
    Write-Host "   清理本地Maven仓库中的项目artifacts..." -ForegroundColor Gray
    mvn dependency:purge-local-repository -DmanualInclude="com.courtlink:courtlink" -q
    Write-Host "   ✅ 项目artifacts已清理" -ForegroundColor Green
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
Write-Host "`n🎉 IntelliJ IDEA缓存刷新完成！" -ForegroundColor Green

Write-Host "`n📝 后续操作指南:" -ForegroundColor Cyan
Write-Host "1. 启动IntelliJ IDEA" -ForegroundColor White
Write-Host "2. 选择 File → Open → 选择项目根目录的 pom.xml" -ForegroundColor White
Write-Host "3. 在导入对话框中：" -ForegroundColor White
Write-Host "   • 选择 'Import Maven project automatically'" -ForegroundColor Gray
Write-Host "   • 勾选 'Download sources and documentation'" -ForegroundColor Gray
Write-Host "   • 选择正确的JDK版本 (Java 21)" -ForegroundColor Gray
Write-Host "4. 等待项目索引完成（可能需要3-5分钟）" -ForegroundColor White
Write-Host "5. 如需要：File → Invalidate Caches → Invalidate and Restart" -ForegroundColor White

Write-Host "`n💡 如果问题仍然存在:" -ForegroundColor Yellow
Write-Host "• 运行深度清理: .\scripts\utils\refresh-idea.ps1 -DeepClean" -ForegroundColor Gray
Write-Host "• 检查IDEA的JDK配置：File → Project Structure → Project" -ForegroundColor Gray
Write-Host "• 确认Maven配置：File → Settings → Build → Build Tools → Maven" -ForegroundColor Gray
Write-Host "• 重新启动IDEA并重新导入项目" -ForegroundColor Gray

Write-Host "`n🔍 故障排除: 查看 docs/reports/IDE_CACHE_REFRESH_GUIDE.md" -ForegroundColor Cyan

# 创建临时启动脚本
$startScript = @"
#!/usr/bin/env pwsh
# IDEA项目启动快捷脚本
Write-Host "🚀 正在启动CourtLink项目..." -ForegroundColor Green
if (Get-Command idea -ErrorAction SilentlyContinue) {
    idea .
} elseif (Get-Command idea.cmd -ErrorAction SilentlyContinue) {
    idea.cmd .
} else {
    Write-Host "💡 请手动启动IDEA并打开此目录" -ForegroundColor Yellow
    explorer .
}
"@

$startScript | Out-File -FilePath "start-idea.ps1" -Encoding UTF8
Write-Host "`n🎯 已创建启动脚本: start-idea.ps1" -ForegroundColor Cyan 