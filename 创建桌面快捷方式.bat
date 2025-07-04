@echo off
title Create Desktop Shortcut
echo Creating desktop shortcut for CourtLink...

set "DESKTOP=%USERPROFILE%\Desktop"
set "CURRENT_DIR=%cd%"
set "LAUNCHER=%CURRENT_DIR%\CourtLink-启动器.bat"

echo Project Directory: %CURRENT_DIR%
echo Desktop Directory: %DESKTOP%
echo Launcher File: %LAUNCHER%

if not exist "%LAUNCHER%" (
    echo Error: Launcher file not found!
    pause
    exit /b 1
)

:: Create shortcut using PowerShell
powershell -ExecutionPolicy Bypass -Command "& {$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut('%DESKTOP%\CourtLink Tennis Court Booking.lnk'); $Shortcut.TargetPath = '%LAUNCHER%'; $Shortcut.WorkingDirectory = '%CURRENT_DIR%'; $Shortcut.Description = 'CourtLink Tennis Court Booking System'; $Shortcut.Save()}"

if exist "%DESKTOP%\CourtLink Tennis Court Booking.lnk" (
    echo Desktop shortcut created successfully!
    echo You can now double-click "CourtLink Tennis Court Booking" on your desktop to launch the system.
) else (
    echo Failed to create desktop shortcut.
)

echo.
echo Press any key to exit...
pause >nul 