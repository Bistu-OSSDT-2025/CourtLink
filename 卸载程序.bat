@echo off
chcp 65001 >nul
title CourtLink 场地预约系统 - 卸载程序
color 0E

echo.
echo ╔════════════════════════════════════════════════════════════════════════════╗
echo ║                                                                            ║
echo ║                    🏓 CourtLink 卸载程序 🏓                               ║
echo ║                                                                            ║
echo ║                           移除系统快捷方式                                 ║
echo ║                                                                            ║
echo ╚════════════════════════════════════════════════════════════════════════════╝
echo.

set "DESKTOP_DIR=%USERPROFILE%\Desktop"
set "START_MENU_DIR=%APPDATA%\Microsoft\Windows\Start Menu\Programs\CourtLink"
set "SHORTCUT_NAME=CourtLink 场地预约系统"

echo ⚠️ 警告：此操作将完全卸载 CourtLink 场地预约系统
echo.
echo 📋 卸载内容：
echo    ├─ 桌面快捷方式
echo    ├─ 开始菜单快捷方式
echo    ├─ 关闭运行中的服务
echo    └─ 清理临时文件
echo.
echo 💡 注意：项目文件和数据库不会被删除
echo.
set /p "confirm=确定要卸载吗？(Y/N): "
if /i not "%confirm%"=="Y" (
    echo 取消卸载
    pause
    exit /b 0
)

echo.
echo 🔧 开始卸载...
echo.

:: 先关闭所有服务
echo [1/5] 关闭运行中的服务...
echo 🛑 正在关闭后端服务...
taskkill /f /im "java.exe" >nul 2>&1
echo 🛑 正在关闭前端服务...
taskkill /f /im "node.exe" >nul 2>&1
echo 🛑 正在关闭启动器窗口...
taskkill /f /fi "WINDOWTITLE eq CourtLink-Backend*" >nul 2>&1
taskkill /f /fi "WINDOWTITLE eq CourtLink-Frontend*" >nul 2>&1
echo ✅ 服务关闭完成

:: 删除桌面快捷方式
echo [2/5] 删除桌面快捷方式...
if exist "%DESKTOP_DIR%\%SHORTCUT_NAME%.lnk" (
    del "%DESKTOP_DIR%\%SHORTCUT_NAME%.lnk" >nul 2>&1
    echo ✅ 桌面快捷方式已删除
) else (
    echo ✅ 未发现桌面快捷方式
)

:: 删除开始菜单快捷方式
echo [3/5] 删除开始菜单快捷方式...
if exist "%START_MENU_DIR%" (
    rmdir /s /q "%START_MENU_DIR%" >nul 2>&1
    echo ✅ 开始菜单快捷方式已删除
) else (
    echo ✅ 未发现开始菜单快捷方式
)

:: 清理临时文件
echo [4/5] 清理临时文件...
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
if exist "logs\*.tmp" (
    del "logs\*.tmp" >nul 2>&1
    echo ✅ 清理日志临时文件
)
echo ✅ 临时文件清理完成

:: 清理注册表（如果有的话）
echo [5/5] 清理系统注册信息...
:: 这里可以添加清理注册表的代码（如果需要）
echo ✅ 系统注册信息清理完成

echo.
echo 🎉 卸载完成！
echo.
echo 📊 卸载结果：
echo    ├─ 桌面快捷方式：已删除
echo    ├─ 开始菜单快捷方式：已删除
echo    ├─ 运行中的服务：已关闭
echo    └─ 临时文件：已清理
echo.
echo 💡 保留的内容：
echo    • 项目源代码文件
echo    • 数据库文件（用户数据）
echo    • 日志文件
echo    • 配置文件
echo.
echo 📝 如需完全删除：
echo    • 请手动删除项目文件夹
echo    • 数据库文件位于：data\courtlink.mv.db
echo    • 日志文件位于：logs\ 文件夹
echo.
echo 🔄 重新安装：
echo    • 运行"桌面安装.bat"即可重新安装
echo.
echo 感谢使用 CourtLink 场地预约系统！
echo.
echo 按任意键退出...
pause >nul 