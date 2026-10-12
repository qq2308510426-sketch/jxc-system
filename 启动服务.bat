@echo off
REM ============================================================
REM  JXC one-click launcher (double-click)
REM  Default: local backend + frontend + two public tunnels
REM  Extra flags pass through, e.g.
REM     启动服务.bat -Build        force rebuild jar first
REM     启动服务.bat -SkipWeb      backend only
REM  For local-only run use:  powershell -File start.ps1   (no -Tunnel)
REM ============================================================
title JXC Launcher
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0start.ps1" -Tunnel %*
echo.
echo Press any key to close this window. The public URLs above also stay in logs\public-urls.txt
pause >nul
