@echo off
setlocal
cd /d "%~dp0"
set "ROOT_DIR=%CD%"
title Lumira Loader & Website Synchronizer
cls
echo ========================================================
echo   LUMIRA CREATED - DELUXE CLIENT EDITION v2.0
echo   Запуск единого синхронизатора (Игра + Лаунчер + Сайт)
echo ========================================================

start /b python "%ROOT_DIR%\launcher\bridge.py" >nul 2>nul

set "LAUNCHER_URL=http://127.0.0.1:38241/launcher"
set "WEBSITE_URL=http://127.0.0.1:38241/"

echo [1/3] Запуск интерфейса Лаунчера...
start msedge.exe --app="%LAUNCHER_URL%" 2>nul || start chrome.exe --app="%LAUNCHER_URL%" 2>nul || start "" "%LAUNCHER_URL%"

echo [2/3] Запуск интерфейса Сайта...
start "" "%WEBSITE_URL%"

echo [3/3] Запуск игрового клиента Minecraft Fabric 1.21.4...
call "%ROOT_DIR%\launch_game.bat"
