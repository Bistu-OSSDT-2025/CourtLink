@echo off
echo ========================================
echo CourtLink 运动场地预约系统 - 测试运行脚本
echo ========================================
echo.

echo [1/5] 清理之前的测试结果...
if exist target\surefire-reports rmdir /s /q target\surefire-reports
if exist target\test-classes rmdir /s /q target\test-classes
echo 清理完成
echo.

echo [2/5] 编译项目...
call mvn clean compile test-compile
if %errorlevel% neq 0 (
    echo 编译失败，请检查代码错误
    pause
    exit /b 1
)
echo 编译成功
echo.

echo [3/5] 运行单元测试...
call mvn test -Dtest=*Test -DfailIfNoTests=false
if %errorlevel% neq 0 (
    echo 单元测试失败
    pause
    exit /b 1
)
echo 单元测试完成
echo.

echo [4/5] 运行集成测试...
call mvn test -Dtest=*IntegrationTest -DfailIfNoTests=false
if %errorlevel% neq 0 (
    echo 集成测试失败
    pause
    exit /b 1
)
echo 集成测试完成
echo.

echo [5/5] 运行性能测试...
call mvn test -Dtest=*PerformanceTest -DfailIfNoTests=false
if %errorlevel% neq 0 (
    echo 性能测试失败
    pause
    exit /b 1
)
echo 性能测试完成
echo.

echo ========================================
echo 所有测试完成！
echo ========================================
echo.
echo 测试报告位置：
echo - 单元测试: target\surefire-reports\
echo - 覆盖率报告: target\site\jacoco\
echo.

echo 是否打开测试报告？(Y/N)
set /p choice=
if /i "%choice%"=="Y" (
    start target\surefire-reports\
)

pause 