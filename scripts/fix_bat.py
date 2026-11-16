# Convert/write clean CRLF bat files

launcher_launch_bat = """@echo off
setlocal
cd /d "%~dp0.."
set "PROJECT_DIR=%CD%"
title Lumira Client - Fabric Game Process
cls
echo ========================================================
echo   LUMIRA CLIENT EDITION - FABRIC 1.21.4
echo   Starting game client via Gradle runClient...
echo   Directory: %PROJECT_DIR%
echo ========================================================

if not exist "%PROJECT_DIR%\\gradlew.bat" (
    echo [ERROR] gradlew.bat not found in "%PROJECT_DIR%"!
    pause
    exit /b 1
)

call "%PROJECT_DIR%\\gradlew.bat" runClient
if %ERRORLEVEL% neq 0 (
    echo.
    echo [WARNING] Game process finished with code %ERRORLEVEL%.
)
pause
"""

root_launch_bat = """@echo off
setlocal
cd /d "%~dp0"
set "PROJECT_DIR=%CD%"
title Lumira Client - Fabric Game Process
cls
echo ========================================================
echo   LUMIRA CLIENT EDITION - FABRIC 1.21.4
echo   Starting game client via Gradle runClient...
echo   Directory: %PROJECT_DIR%
echo ========================================================

if not exist "%PROJECT_DIR%\\gradlew.bat" (
    echo [ERROR] gradlew.bat not found in "%PROJECT_DIR%"!
    pause
    exit /b 1
)

call "%PROJECT_DIR%\\gradlew.bat" runClient
if %ERRORLEVEL% neq 0 (
    echo.
    echo [WARNING] Game process finished with code %ERRORLEVEL%.
)
pause
"""

run_launcher_bat = """@echo off
setlocal
cd /d "%~dp0"
title Lumira Loader Launcher
cls
echo ========================================================
echo   LUMIRA CREATED - DELUXE CLIENT EDITION v2.0
echo   Launching Launcher Interface...
echo ========================================================

set "LAUNCHER_PATH=%~dp0index.html"

start msedge.exe --app="file:///%LAUNCHER_PATH%" 2>nul || start chrome.exe --app="file:///%LAUNCHER_PATH%" 2>nul || start "" "%LAUNCHER_PATH%"
exit
"""

with open('launcher/launch_game.bat', 'wb') as f:
    f.write(launcher_launch_bat.replace('\r\n', '\n').replace('\n', '\r\n').encode('utf-8'))

with open('launch_game.bat', 'wb') as f:
    f.write(root_launch_bat.replace('\r\n', '\n').replace('\n', '\r\n').encode('utf-8'))

with open('launcher/run_launcher.bat', 'wb') as f:
    f.write(run_launcher_bat.replace('\r\n', '\n').replace('\n', '\r\n').encode('utf-8'))

print("All batch files written with valid Windows CRLF!")
