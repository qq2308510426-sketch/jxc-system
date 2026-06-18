$ErrorActionPreference = "SilentlyContinue"

Write-Host ""
Write-Host "============================================"
Write-Host "   JXC Management System - Stop Script"
Write-Host "============================================"
Write-Host ""

Write-Host "Stopping services..." -ForegroundColor Cyan

foreach ($port in @(8080, 5173)) {
    $conns = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    foreach ($c in $conns) {
        $procId = $c.OwningProcess
        if ($procId -gt 0) {
            $name = (Get-Process -Id $procId -ErrorAction SilentlyContinue).ProcessName
            Write-Host "  Stopping PID $procId ($name) on port $port"
            Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
        }
    }
}

Start-Sleep -Seconds 2

$remaining = @()
foreach ($port in @(8080, 5173)) {
    if (Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue) {
        $remaining += $port
    }
}

if ($remaining.Count -eq 0) {
    Write-Host ""
    Write-Host "  All services stopped!" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "  Warning: ports still in use: $($remaining -join ', ')" -ForegroundColor Yellow
}

Write-Host ""
Read-Host "Press Enter to close"