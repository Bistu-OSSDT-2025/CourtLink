@echo off
setlocal enabledelayedexpansion

echo 选择要运行的测试类型：
echo 1. 所有测试
echo 2. 单元测试
echo 3. 集成测试
echo 4. 性能测试
echo 5. 安全测试
echo 6. 端到端测试

set /p choice=请输入选项（1-6）：

if "%choice%"=="1" (
    powershell -ExecutionPolicy Bypass -File comprehensive-test.ps1 -TestType all
) else if "%choice%"=="2" (
    powershell -ExecutionPolicy Bypass -File comprehensive-test.ps1 -TestType unit
) else if "%choice%"=="3" (
    powershell -ExecutionPolicy Bypass -File comprehensive-test.ps1 -TestType integration
) else if "%choice%"=="4" (
    set /p users=请输入并发用户数（默认100）：
    set /p duration=请输入测试持续时间（格式：1h/30m，默认1h）：
    if "!users!"=="" set users=100
    if "!duration!"=="" set duration=1h
    powershell -ExecutionPolicy Bypass -File comprehensive-test.ps1 -TestType performance -ConcurrentUsers !users! -Duration !duration!
) else if "%choice%"=="5" (
    powershell -ExecutionPolicy Bypass -File comprehensive-test.ps1 -TestType security
) else if "%choice%"=="6" (
    powershell -ExecutionPolicy Bypass -File comprehensive-test.ps1 -TestType e2e
) else (
    echo 无效的选项！
    exit /b 1
)

echo 测试完成！
pause 