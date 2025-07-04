@echo off
chcp 65001 >nul
title CourtLink 场地预约系统 - 项目打包工具
color 0D

echo.
echo ╔════════════════════════════════════════════════════════════════════════════╗
echo ║                                                                            ║
echo ║                    🏓 CourtLink 项目打包工具 🏓                           ║
echo ║                                                                            ║
echo ║                           创建分发版本                                     ║
echo ║                                                                            ║
echo ╚════════════════════════════════════════════════════════════════════════════╝
echo.

:: 设置变量
set "PROJECT_NAME=CourtLink-场地预约系统"
set "VERSION=v1.0.0"
set "DATE=%date:~0,4%-%date:~5,2%-%date:~8,2%"
set "PACKAGE_NAME=%PROJECT_NAME%-%VERSION%-%DATE%"
set "DIST_DIR=dist"
set "TEMP_DIR=temp_package"

echo 📦 打包信息：
echo    ├─ 项目名称：%PROJECT_NAME%
echo    ├─ 版本号：%VERSION%
echo    ├─ 打包日期：%DATE%
echo    └─ 输出目录：%DIST_DIR%
echo.

:: 检查项目文件
echo [1/8] 检查项目文件...
if not exist "pom.xml" (
    echo ❌ 错误：找不到后端项目文件！
    pause
    exit /b 1
)
if not exist "frontend\package.json" (
    echo ❌ 错误：找不到前端项目文件！
    pause
    exit /b 1
)
if not exist "CourtLink-启动器.bat" (
    echo ❌ 错误：找不到启动器文件！
    pause
    exit /b 1
)
echo ✅ 项目文件检查完成

:: 创建临时目录
echo [2/8] 创建临时目录...
if exist "%TEMP_DIR%" rmdir /s /q "%TEMP_DIR%"
mkdir "%TEMP_DIR%"
mkdir "%TEMP_DIR%\%PACKAGE_NAME%"
echo ✅ 临时目录创建完成

:: 复制项目文件
echo [3/8] 复制项目文件...
echo 📁 复制源代码...
xcopy /s /e /y "src" "%TEMP_DIR%\%PACKAGE_NAME%\src\" >nul
xcopy /s /e /y "frontend" "%TEMP_DIR%\%PACKAGE_NAME%\frontend\" >nul
xcopy /y "pom.xml" "%TEMP_DIR%\%PACKAGE_NAME%\" >nul

echo 📁 复制资源文件...
if exist "data" xcopy /s /e /y "data" "%TEMP_DIR%\%PACKAGE_NAME%\data\" >nul
if exist "logs" xcopy /s /e /y "logs" "%TEMP_DIR%\%PACKAGE_NAME%\logs\" >nul

echo 📁 复制启动脚本...
copy /y "CourtLink-启动器.bat" "%TEMP_DIR%\%PACKAGE_NAME%\" >nul
copy /y "桌面安装.bat" "%TEMP_DIR%\%PACKAGE_NAME%\" >nul
copy /y "关闭系统.bat" "%TEMP_DIR%\%PACKAGE_NAME%\" >nul
copy /y "卸载程序.bat" "%TEMP_DIR%\%PACKAGE_NAME%\" >nul

echo 📁 复制配置文件...
copy /y "系统配置.ini" "%TEMP_DIR%\%PACKAGE_NAME%\" >nul
copy /y "系统说明.md" "%TEMP_DIR%\%PACKAGE_NAME%\" >nul

echo 📁 复制其他文件...
if exist "*.md" copy /y "*.md" "%TEMP_DIR%\%PACKAGE_NAME%\" >nul
if exist "*.txt" copy /y "*.txt" "%TEMP_DIR%\%PACKAGE_NAME%\" >nul
echo ✅ 项目文件复制完成

:: 清理不必要的文件
echo [4/8] 清理不必要的文件...
echo 🧹 清理临时文件...
if exist "%TEMP_DIR%\%PACKAGE_NAME%\target" rmdir /s /q "%TEMP_DIR%\%PACKAGE_NAME%\target"
if exist "%TEMP_DIR%\%PACKAGE_NAME%\frontend\node_modules" rmdir /s /q "%TEMP_DIR%\%PACKAGE_NAME%\frontend\node_modules"
if exist "%TEMP_DIR%\%PACKAGE_NAME%\frontend\dist" rmdir /s /q "%TEMP_DIR%\%PACKAGE_NAME%\frontend\dist"
if exist "%TEMP_DIR%\%PACKAGE_NAME%\data\*.trace.db" del /q "%TEMP_DIR%\%PACKAGE_NAME%\data\*.trace.db"
if exist "%TEMP_DIR%\%PACKAGE_NAME%\data\*.lock" del /q "%TEMP_DIR%\%PACKAGE_NAME%\data\*.lock"

echo 🧹 清理版本控制文件...
if exist "%TEMP_DIR%\%PACKAGE_NAME%\.git" rmdir /s /q "%TEMP_DIR%\%PACKAGE_NAME%\.git"
if exist "%TEMP_DIR%\%PACKAGE_NAME%\.gitignore" del /q "%TEMP_DIR%\%PACKAGE_NAME%\.gitignore"
if exist "%TEMP_DIR%\%PACKAGE_NAME%\.gitattributes" del /q "%TEMP_DIR%\%PACKAGE_NAME%\.gitattributes"

echo 🧹 清理IDE文件...
if exist "%TEMP_DIR%\%PACKAGE_NAME%\.vscode" rmdir /s /q "%TEMP_DIR%\%PACKAGE_NAME%\.vscode"
if exist "%TEMP_DIR%\%PACKAGE_NAME%\.idea" rmdir /s /q "%TEMP_DIR%\%PACKAGE_NAME%\.idea"
if exist "%TEMP_DIR%\%PACKAGE_NAME%\*.iml" del /q "%TEMP_DIR%\%PACKAGE_NAME%\*.iml"
echo ✅ 清理完成

:: 创建安装说明文件
echo [5/8] 创建安装说明文件...
echo 🏓 CourtLink 场地预约系统 %VERSION% > "%TEMP_DIR%\%PACKAGE_NAME%\安装说明.txt"
echo. >> "%TEMP_DIR%\%PACKAGE_NAME%\安装说明.txt"
echo 🚀 快速安装： >> "%TEMP_DIR%\%PACKAGE_NAME%\安装说明.txt"
echo 1. 双击"桌面安装.bat"进行安装 >> "%TEMP_DIR%\%PACKAGE_NAME%\安装说明.txt"
echo 2. 双击桌面快捷方式启动系统 >> "%TEMP_DIR%\%PACKAGE_NAME%\安装说明.txt"
echo 3. 首次启动会自动安装依赖，请耐心等待 >> "%TEMP_DIR%\%PACKAGE_NAME%\安装说明.txt"
echo. >> "%TEMP_DIR%\%PACKAGE_NAME%\安装说明.txt"
echo 📋 系统要求： >> "%TEMP_DIR%\%PACKAGE_NAME%\安装说明.txt"
echo • Windows 7 及以上版本 >> "%TEMP_DIR%\%PACKAGE_NAME%\安装说明.txt"
echo • Java 8 或以上版本 >> "%TEMP_DIR%\%PACKAGE_NAME%\安装说明.txt"
echo • Node.js 14 或以上版本 >> "%TEMP_DIR%\%PACKAGE_NAME%\安装说明.txt"
echo • 4GB 内存，2GB 硬盘空间 >> "%TEMP_DIR%\%PACKAGE_NAME%\安装说明.txt"
echo. >> "%TEMP_DIR%\%PACKAGE_NAME%\安装说明.txt"
echo 🔑 默认账户： >> "%TEMP_DIR%\%PACKAGE_NAME%\安装说明.txt"
echo • 用户：testuser / test123 >> "%TEMP_DIR%\%PACKAGE_NAME%\安装说明.txt"
echo • 管理员：admin / admin123 >> "%TEMP_DIR%\%PACKAGE_NAME%\安装说明.txt"
echo. >> "%TEMP_DIR%\%PACKAGE_NAME%\安装说明.txt"
echo 📞 技术支持：详见"系统说明.md"文件 >> "%TEMP_DIR%\%PACKAGE_NAME%\安装说明.txt"
echo. >> "%TEMP_DIR%\%PACKAGE_NAME%\安装说明.txt"
echo 打包时间：%DATE% %TIME% >> "%TEMP_DIR%\%PACKAGE_NAME%\安装说明.txt"
echo ✅ 安装说明文件创建完成

:: 创建版本信息文件
echo [6/8] 创建版本信息文件...
echo {> "%TEMP_DIR%\%PACKAGE_NAME%\version.json"
echo   "name": "%PROJECT_NAME%",>> "%TEMP_DIR%\%PACKAGE_NAME%\version.json"
echo   "version": "%VERSION%",>> "%TEMP_DIR%\%PACKAGE_NAME%\version.json"
echo   "buildDate": "%DATE%",>> "%TEMP_DIR%\%PACKAGE_NAME%\version.json"
echo   "buildTime": "%TIME%",>> "%TEMP_DIR%\%PACKAGE_NAME%\version.json"
echo   "description": "专业场地预约管理系统",>> "%TEMP_DIR%\%PACKAGE_NAME%\version.json"
echo   "author": "CourtLink Team",>> "%TEMP_DIR%\%PACKAGE_NAME%\version.json"
echo   "homepage": "https://github.com/courtlink/courtlink",>> "%TEMP_DIR%\%PACKAGE_NAME%\version.json"
echo   "license": "MIT">> "%TEMP_DIR%\%PACKAGE_NAME%\version.json"
echo }>> "%TEMP_DIR%\%PACKAGE_NAME%\version.json"
echo ✅ 版本信息文件创建完成

:: 创建分发目录
echo [7/8] 创建分发包...
if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"

echo 📦 创建ZIP压缩包...
powershell -command "Compress-Archive -Path '%TEMP_DIR%\%PACKAGE_NAME%' -DestinationPath '%DIST_DIR%\%PACKAGE_NAME%.zip' -Force"

echo 📦 创建文件夹分发版...
if exist "%DIST_DIR%\%PACKAGE_NAME%" rmdir /s /q "%DIST_DIR%\%PACKAGE_NAME%"
xcopy /s /e /y "%TEMP_DIR%\%PACKAGE_NAME%" "%DIST_DIR%\%PACKAGE_NAME%\" >nul

echo ✅ 分发包创建完成

:: 清理临时文件
echo [8/8] 清理临时文件...
rmdir /s /q "%TEMP_DIR%"
echo ✅ 临时文件清理完成

echo.
echo 🎉 打包完成！
echo.
echo 📊 打包结果：
echo    ├─ 压缩包：%DIST_DIR%\%PACKAGE_NAME%.zip
echo    ├─ 文件夹：%DIST_DIR%\%PACKAGE_NAME%\
echo    └─ 大小：
dir "%DIST_DIR%\%PACKAGE_NAME%.zip" | findstr /C:"%PACKAGE_NAME%.zip"
echo.
echo 📋 分发内容：
echo    ├─ 完整源代码
echo    ├─ 所有启动脚本
echo    ├─ 系统配置文件
echo    ├─ 安装说明文档
echo    └─ 版本信息文件
echo.
echo 💡 使用说明：
echo    • 将ZIP文件发送给用户
echo    • 用户解压后运行"桌面安装.bat"
echo    • 系统会自动安装到桌面
echo    • 双击桌面快捷方式即可启动
echo.
echo 🔄 测试建议：
echo    • 在另一台电脑上测试安装
echo    • 验证所有功能正常运行
echo    • 确认文档说明准确无误
echo.
echo 按任意键打开分发目录...
pause >nul
start "" "%DIST_DIR%" 