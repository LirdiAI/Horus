import os
import shutil

ROOT_DIR = os.path.abspath(os.path.dirname(os.path.dirname(__file__)))

root_launch_bat = """@echo off
setlocal
cd /d "%~dp0"
set "ROOT_DIR=%CD%"
if not exist "%ROOT_DIR%\\gradlew.bat" (
    cd /d "%~dp0.."
    set "ROOT_DIR=%CD%"
)

title Horus Client 1.21.11 - Game Process
cls
echo ========================================================
echo   HORUS CLIENT - FABRIC 1.21.11
echo   Starting game client via Gradle runClient...
echo   Directory: %ROOT_DIR%
echo ========================================================

if not exist "%ROOT_DIR%\\gradlew.bat" (
    echo [ERROR] gradlew.bat not found in %ROOT_DIR%!
    pause
    exit /b 1
)

call "%ROOT_DIR%\\gradlew.bat" runClient
if %ERRORLEVEL% neq 0 (
    echo.
    echo [WARNING] Game process finished with code %ERRORLEVEL%.
)
pause
"""

launcher_launch_bat = """@echo off
setlocal
cd /d "%~dp0.."
set "ROOT_DIR=%CD%"
if not exist "%ROOT_DIR%\\gradlew.bat" (
    cd /d "%~dp0"
    set "ROOT_DIR=%CD%"
)

title Horus Client 1.21.11 - Game Process
cls
echo ========================================================
echo   HORUS CLIENT - FABRIC 1.21.11
echo   Starting game client via Gradle runClient...
echo   Directory: %ROOT_DIR%
echo ========================================================

if not exist "%ROOT_DIR%\\gradlew.bat" (
    echo [ERROR] gradlew.bat not found in %ROOT_DIR%!
    pause
    exit /b 1
)

call "%ROOT_DIR%\\gradlew.bat" runClient
if %ERRORLEVEL% neq 0 (
    echo.
    echo [WARNING] Game process finished with code %ERRORLEVEL%.
)
pause
"""

run_launcher_bat = """@echo off
setlocal
cd /d "%~dp0.."
set "ROOT_DIR=%CD%"
if not exist "%ROOT_DIR%\\launcher" (
    cd /d "%~dp0"
    set "ROOT_DIR=%CD%"
)

title Horus Loader Launcher
cls
echo ========================================================
echo   HORUS CLIENT - DELUXE EDITION v2.0
echo   Starting Launcher Bridge and Interface...
echo ========================================================

start /b python "%ROOT_DIR%\\launcher\\bridge.py" >nul 2>nul

set "LAUNCHER_URL=http://127.0.0.1:38241/launcher"
set "FALLBACK_PATH=%ROOT_DIR%\\launcher\\index.html"

start msedge.exe --app="%LAUNCHER_URL%" 2>nul || start chrome.exe --app="%LAUNCHER_URL%" 2>nul || start "" "%LAUNCHER_URL%" 2>nul || start "" "%FALLBACK_PATH%"
exit
"""

def write_crlf(file_path, content):
    full_path = os.path.join(ROOT_DIR, file_path)
    with open(full_path, "wb") as f:
        f.write(content.replace("\r\n", "\n").replace("\n", "\r\n").encode("utf-8"))

write_crlf("launch_game.bat", root_launch_bat)
write_crlf("launcher/launch_game.bat", launcher_launch_bat)
write_crlf("launcher/run_launcher.bat", run_launcher_bat)
print("Updated all batch scripts successfully!")
