@echo off
chcp 65001 >nul 2>&1
title CourtLink - Desktop Installer
color 0B

echo.
echo ===============================================================================
echo.
echo                        CourtLink Desktop Installer                           
echo.
echo                           One-Click Install to Desktop                                   
echo.
echo ===============================================================================
echo.

:: Get current directory
set "CURRENT_DIR=%cd%"
set "DESKTOP_DIR=%USERPROFILE%\Desktop"
set "SHORTCUT_NAME=CourtLink Tennis Court Booking"
set "LAUNCHER_FILE=%CURRENT_DIR%\CourtLink-启动器.bat"

echo Installation Information:
echo    Project Directory: %CURRENT_DIR%
echo    Desktop Directory: %DESKTOP_DIR%
echo    Shortcut Name: %SHORTCUT_NAME%
echo.

:: Check project files
echo [1/3] Checking project files...
if not exist "%LAUNCHER_FILE%" (
    echo Error: Launcher file not found!
    echo Please ensure CourtLink-启动器.bat exists
    pause
    exit /b 1
)
echo Project files check passed

:: Create VBS script for desktop shortcut
echo [2/3] Creating desktop shortcut...
set "VBS_FILE=%TEMP%\create_shortcut.vbs"

(
echo Set WshShell = WScript.CreateObject^("WScript.Shell"^)
echo Set oShellLink = WshShell.CreateShortcut^("%DESKTOP_DIR%\%SHORTCUT_NAME%.lnk"^)
echo oShellLink.TargetPath = "%LAUNCHER_FILE%"
echo oShellLink.WorkingDirectory = "%CURRENT_DIR%"
echo oShellLink.Description = "CourtLink Tennis Court Booking System"
echo oShellLink.IconLocation = "shell32.dll,21"
echo oShellLink.Save
) > "%VBS_FILE%"

:: Execute VBS script
cscript //nologo "%VBS_FILE%"
if %errorlevel% equ 0 (
    echo Desktop shortcut created successfully
    del "%VBS_FILE%"
) else (
    echo Shortcut creation failed - trying alternative method
    del "%VBS_FILE%"
    
    :: Alternative method using PowerShell
    powershell -Command "$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut('%DESKTOP_DIR%\%SHORTCUT_NAME%.lnk'); $Shortcut.TargetPath = '%LAUNCHER_FILE%'; $Shortcut.WorkingDirectory = '%CURRENT_DIR%'; $Shortcut.Description = 'CourtLink Tennis Court Booking System'; $Shortcut.Save()"
    if exist "%DESKTOP_DIR%\%SHORTCUT_NAME%.lnk" (
        echo Desktop shortcut created successfully using PowerShell
    ) else (
        echo Desktop shortcut creation failed
        pause
        exit /b 1
    )
)

:: Create Start Menu shortcut
echo [3/3] Creating Start Menu shortcut...
set "START_MENU_DIR=%APPDATA%\Microsoft\Windows\Start Menu\Programs"
if not exist "%START_MENU_DIR%\CourtLink" mkdir "%START_MENU_DIR%\CourtLink"

powershell -Command "$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut('%START_MENU_DIR%\CourtLink\%SHORTCUT_NAME%.lnk'); $Shortcut.TargetPath = '%LAUNCHER_FILE%'; $Shortcut.WorkingDirectory = '%CURRENT_DIR%'; $Shortcut.Description = 'CourtLink Tennis Court Booking System'; $Shortcut.Save()"

:: Create uninstall shortcut in Start Menu
powershell -Command "$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut('%START_MENU_DIR%\CourtLink\Uninstall CourtLink.lnk'); $Shortcut.TargetPath = '%CURRENT_DIR%\卸载程序.bat'; $Shortcut.WorkingDirectory = '%CURRENT_DIR%'; $Shortcut.Description = 'Uninstall CourtLink Tennis Court Booking System'; $Shortcut.Save()"

echo Start Menu shortcuts created successfully

echo.
echo Installation completed!
echo.
echo Installation Contents:
echo    Desktop Shortcut: %SHORTCUT_NAME%
echo    Start Menu: All Programs - CourtLink - %SHORTCUT_NAME%
echo    Uninstall: All Programs - CourtLink - Uninstall CourtLink
echo.
echo Usage Instructions:
echo    * Double-click desktop shortcut to launch system
echo    * First launch requires dependency installation, please wait
echo    * System will auto-check environment and start services
echo    * Browser will open automatically after startup
echo.
echo System Requirements:
echo    * Java 8 or higher
echo    * Node.js 14 or higher
echo    * Windows 7 or higher
echo.
echo Press any key to exit installer...
pause >nul 