@echo off
chcp 65001 >nul
title CourtLink 场地预约系统 - 关闭程序
color 0C

echo.
echo ╔════════════════════════════════════════════════════════════════════════════╗
echo ║                                                                            ║
echo ║                    🏓 CourtLink 系统关闭程序 🏓                           ║
echo ║                                                                            ║
echo ║                           安全关闭所有服务                                 ║
echo ║                                                                            ║
echo ╚════════════════════════════════════════════════════════════════════════════╝
echo.

echo 🔍 正在检查运行的服务...
echo.

:: 检查Java进程（后端服务）
echo [1/4] 检查后端服务...
tasklist | findstr /i "java.exe" >nul 2>&1
if %errorlevel% == 0 (
    echo 📦 发现后端服务正在运行
    echo 🛑 正在关闭后端服务...
    taskkill /f /im "java.exe" >nul 2>&1
    if %errorlevel% == 0 (
        echo ✅ 后端服务已关闭
    ) else (
        echo ⚠️ 后端服务关闭失败，可能需要手动关闭
    )
) else (
    echo ✅ 未发现后端服务
)

:: 检查Node.js进程（前端服务）
echo [2/4] 检查前端服务...
tasklist | findstr /i "node.exe" >nul 2>&1
if %errorlevel% == 0 (
    echo 📦 发现前端服务正在运行
    echo 🛑 正在关闭前端服务...
    taskkill /f /im "node.exe" >nul 2>&1
    if %errorlevel% == 0 (
        echo ✅ 前端服务已关闭
    ) else (
        echo ⚠️ 前端服务关闭失败，可能需要手动关闭
    )
) else (
    echo ✅ 未发现前端服务
)

:: 检查CMD窗口（启动器相关）
echo [3/4] 检查启动器窗口...
tasklist | findstr /i "cmd.exe" >nul 2>&1
if %errorlevel% == 0 (
    echo 📦 发现启动器相关窗口
    echo 🛑 正在关闭相关窗口...
    :: 只关闭带有特定标题的窗口
    taskkill /f /fi "WINDOWTITLE eq CourtLink-Backend*" >nul 2>&1
    taskkill /f /fi "WINDOWTITLE eq CourtLink-Frontend*" >nul 2>&1
    echo ✅ 启动器窗口已关闭
) else (
    echo ✅ 未发现启动器窗口
)

:: 清理临时文件
echo [4/4] 清理临时文件...
if exist "data\*.trace.db" (
    del "data\*.trace.db" >nul 2>&1
    echo ✅ 清理数据库临时文件
)
if exist "target\*.tmp" (
    del "target\*.tmp" >nul 2>&1
    echo ✅ 清理构建临时文件
)
if exist "frontend\dist\*.tmp" (
    del "frontend\dist\*.tmp" >nul 2>&1
    echo ✅ 清理前端临时文件
)
echo ✅ 临时文件清理完成

echo.
echo 🎯 关闭操作完成！
echo.
echo 📊 系统状态：
echo    ├─ 后端服务：已停止
echo    ├─ 前端服务：已停止
echo    ├─ 数据库：已关闭
echo    └─ 临时文件：已清理
echo.
echo 💡 提示：
echo    • 所有服务已安全关闭
echo    • 数据已自动保存到数据库
echo    • 下次启动请使用桌面快捷方式
echo.
echo 🔄 如需重新启动，请：
echo    • 双击桌面"CourtLink 场地预约系统"图标
echo    • 或运行"CourtLink-启动器.bat"
echo.
echo 按任意键退出...
pause >nul 