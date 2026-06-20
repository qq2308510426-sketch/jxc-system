@echo off
powershell -ExecutionPolicy Bypass -File "C:\claude\ja\start.ps1"
if %errorlevel% neq 0 pause
