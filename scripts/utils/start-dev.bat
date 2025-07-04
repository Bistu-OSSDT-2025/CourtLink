@echo off
echo ========================================
echo 预约管理与支付集成系统 - 开发环境启动
echo ========================================
echo.

REM 检查Java环境
echo [1/5] 检查Java环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Java环境，请安装JDK 11+
    pause
    exit /b 1
)
echo Java环境检查通过

REM 检查Maven环境
echo [2/5] 检查Maven环境...
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Maven环境，请安装Maven 3.6+
    pause
    exit /b 1
)
echo Maven环境检查通过

REM 检查Git环境
echo [3/5] 检查Git环境...
git --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Git环境，请安装Git
    pause
    exit /b 1
)
echo Git环境检查通过

REM 清理并编译项目
echo [4/5] 清理并编译项目...
mvn clean compile
if %errorlevel% neq 0 (
    echo 错误: 项目编译失败
    pause
    exit /b 1
)
echo 项目编译成功

REM 启动应用
echo [5/5] 启动应用...
echo.
echo 应用将在以下地址启动:
echo - API文档: http://localhost:8080/api/swagger-ui.html
echo - 健康检查: http://localhost:8080/api/actuator/health
echo.
echo 按 Ctrl+C 停止应用
echo.

mvn spring-boot:run

pause 