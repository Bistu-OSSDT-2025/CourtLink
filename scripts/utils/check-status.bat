@echo off
echo ========================================
echo 预约管理与支付集成系统 - 项目状态检查
echo ========================================
echo.

REM 检查Git状态
echo [1/4] 检查Git状态...
git status --porcelain
if %errorlevel% equ 0 (
    echo Git工作区状态正常
) else (
    echo 警告: Git工作区有未提交的更改
)
echo.

REM 检查当前分支
echo [2/4] 检查当前分支...
git branch --show-current
echo.

REM 检查远程仓库连接
echo [3/4] 检查远程仓库连接...
git remote -v
echo.

REM 检查项目依赖
echo [4/4] 检查项目依赖...
mvn dependency:tree -DoutputFile=dependency-tree.txt >nul 2>&1
if %errorlevel% equ 0 (
    echo 项目依赖检查完成，详情请查看 dependency-tree.txt
) else (
    echo 警告: 项目依赖检查失败
)
echo.

echo ========================================
echo 检查完成！
echo ========================================
echo.
echo 下一步操作建议:
echo 1. 如果Git工作区有更改，请提交代码
echo 2. 确保在正确的分支上开发
echo 3. 运行 start-dev.bat 启动开发环境
echo 4. 查看 QUICK_START.md 了解详细使用说明
echo.

pause 