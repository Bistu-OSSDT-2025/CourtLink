@echo off
chcp 65001 >nul 2>&1
title CourtLink - Tennis Court Booking System
color 0A

:: Set window size
mode con: cols=80 lines=25

:: Display startup interface
echo.
echo ===============================================================================
echo.
echo                    CourtLink - Tennis Court Booking System                           
echo.
echo                        Professional Court Booking Management                              
echo.
echo ===============================================================================
echo.
echo System Information:
echo    Backend Service: Spring Boot (Port 8080)
echo    Frontend Service: Vue.js + Vite (Port 3007+)
echo    Database: H2 (Auto-initialized)
echo    System Version: v1.0.0
echo.
echo Default Accounts:
echo    User Login: testuser / test123
echo    User Login: testbooking / test123
echo    Admin Login: admin / admin123
echo.
echo Starting system...
echo.

:: Check Java environment
echo [1/4] Checking Java environment...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Java environment not found!
    echo Please install Java 8 or higher
    pause
    exit /b 1
)
echo Java environment check passed

:: Check Node.js environment
echo [2/4] Checking Node.js environment...
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Node.js environment not found!
    echo Please install Node.js 14 or higher
    pause
    exit /b 1
)
echo Node.js environment check passed

:: Check project files
echo [3/4] Checking project files...
if not exist "pom.xml" (
    echo Error: Backend project files not found!
    echo Please ensure running from project root directory
    pause
    exit /b 1
)
if not exist "frontend\package.json" (
    echo Error: Frontend project files not found!
    echo Please ensure frontend project is complete
    pause
    exit /b 1
)
echo Project files check passed

:: Start services
echo [4/4] Starting services...
echo.
echo Starting backend service...
start "CourtLink-Backend" cmd /k "echo CourtLink Backend Service && echo Port: 8080 && echo Console Log: && mvn spring-boot:run"

:: Wait for backend startup
echo Waiting for backend service to start...
timeout /t 10 /nobreak >nul

echo Starting frontend service...
start "CourtLink-Frontend" cmd /k "echo CourtLink Frontend Service && echo Port: 3007+ && echo Console Log: && cd frontend && npm run dev"

:: Wait for frontend startup
echo Waiting for frontend service to start...
timeout /t 8 /nobreak >nul

echo.
echo System startup completed!
echo.
echo Access URLs:
echo    User Portal: http://localhost:3007/
echo    Admin Panel: http://localhost:3007/admin
echo    API Health: http://localhost:8080/actuator/health
echo.
echo Usage Tips:
echo    * System will auto-open browser
echo    * Do not close this window, it will stop services
echo    * Press Ctrl+C to stop services
echo.

:: Auto-open browser
echo Opening browser...
start http://localhost:3007/

echo CourtLink Tennis Court Booking System has started successfully!
echo.
echo Press any key to exit launcher (services will continue running)...
pause >nul 