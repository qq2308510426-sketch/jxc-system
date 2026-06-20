Write-Host ""
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "  JXC 停止服务" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow
Write-Host ""

# Stop Java backend
$javaProcs = Get-Process -Name "java" -ErrorAction SilentlyContinue
if ($javaProcs) {
    Write-Host "[*] 关闭后端..." -ForegroundColor Yellow
    $javaProcs | Stop-Process -Force
    Write-Host "[OK] 后端已关闭" -ForegroundColor Green
} else {
    Write-Host "[*] 后端未运行" -ForegroundColor Gray
}

# Stop ngrok
$ngrokProcs = Get-Process -Name "ngrok" -ErrorAction SilentlyContinue
if ($ngrokProcs) {
    Write-Host "[*] 关闭内网穿透..." -ForegroundColor Yellow
    $ngrokProcs | Stop-Process -Force
    Write-Host "[OK] 内网穿透已关闭" -ForegroundColor Green
} else {
    Write-Host "[*] 内网穿透未运行" -ForegroundColor Gray
}

Write-Host ""
Write-Host "所有服务已停止" -ForegroundColor Green
Write-Host ""
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
