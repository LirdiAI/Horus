@echo off
setlocal
cd /d "%~dp0.."
set "ROOT_DIR=%CD%"
if not exist "%ROOT_DIR%\launcher" (
    cd /d "%~dp0"
    set "ROOT_DIR=%CD%"
)

title Horus Loader Launcher
cls
echo ========================================================
echo   HORUS CLIENT - HORUS EDITION v2.0
echo   Starting Launcher Bridge and Interface...
echo ========================================================

start /b python "%ROOT_DIR%\launcher\bridge.py" >nul 2>nul

set "LAUNCHER_URL=http://127.0.0.1:38241/launcher"
set "FALLBACK_PATH=%ROOT_DIR%\launcher\index.html"

start msedge.exe --app="%LAUNCHER_URL%" 2>nul || start chrome.exe --app="%LAUNCHER_URL%" 2>nul || start "" "%LAUNCHER_URL%" 2>nul || start "" "%FALLBACK_PATH%"
exit
