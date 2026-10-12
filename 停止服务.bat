@echo off
REM Stops the local backend/frontend and any cloudflared tunnels started by 启动服务.bat
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0stop.ps1" %*
echo.
pause
