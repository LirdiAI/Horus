@echo off
setlocal
cd /d "%~dp0"
set "ROOT_DIR=%CD%"
if not exist "%ROOT_DIR%\gradlew.bat" (
    cd /d "%~dp0.."
    set "ROOT_DIR=%CD%"
)
if not exist "%ROOT_DIR%\gradlew.bat" (
    cd /d "C:\Users\Никита\Downloads\HorusClient"
    set "ROOT_DIR=%CD%"
)

if "%1"=="/silent" goto :run_direct
if "%1"=="/headless" goto :run_direct

:: If double-clicked directly by user, switch to hidden VBS launch so console window does not stay open
if exist "%ROOT_DIR%\launch_game_silent.vbs" (
    start "" wscript.exe "%ROOT_DIR%\launch_game_silent.vbs"
    exit /b 0
)
if exist "%~dp0launch_game_silent.vbs" (
    start "" wscript.exe "%~dp0launch_game_silent.vbs"
    exit /b 0
)

:run_direct
if not exist "%ROOT_DIR%\gradlew.bat" (
    exit /b 1
)

if exist "%ROOT_DIR%\horus_session.txt" (
    set /p HORUS_USER=<"%ROOT_DIR%\horus_session.txt"
)
if "%HORUS_USER%"=="" (
    set "HORUS_USER=Lumira"
)

if exist "%ROOT_DIR%\horus_ram.txt" (
    set /p HORUS_RAM=<"%ROOT_DIR%\horus_ram.txt"
)
if "%HORUS_RAM%"=="" (
    set "HORUS_RAM=6"
)

call "%ROOT_DIR%\gradlew.bat" runClient -PhorusRam=%HORUS_RAM% -Dhorus.user=%HORUS_USER% > "%ROOT_DIR%\minecraft_game.log" 2>&1
exit /b 0
