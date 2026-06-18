# ============================================
#   JXC Management System - Start Script
# ============================================

$ErrorActionPreference = "SilentlyContinue"
$backendPort = 8080
$frontendPort = 5173

Write-Host ""
Write-Host "============================================"
Write-Host "   JXC Management System"
Write-Host "============================================"
Write-Host ""

# --- Step 1: Clean up old processes ---
Write-Host "[1/3] Cleaning up old processes..." -ForegroundColor Cyan
foreach ($port in @($backendPort, $frontendPort)) {
    $conns = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    foreach ($c in $conns) {
        $procId = $c.OwningProcess
        if ($procId -gt 0) {
            $procName = (Get-Process -Id $procId -ErrorAction SilentlyContinue).ProcessName
            Write-Host "  Killing PID $procId ($procName) on port $port"
            Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
        }
    }
}

# Wait for ports to be free
$waitCount = 0
while ($waitCount -lt 15) {
    $busy = (Get-NetTCPConnection -LocalPort $backendPort -State Listen -ErrorAction SilentlyContinue) -or
            (Get-NetTCPConnection -LocalPort $frontendPort -State Listen -ErrorAction SilentlyContinue)
    if (-not $busy) { break }
    Start-Sleep -Seconds 1
    $waitCount++
}
Write-Host "  Done" -ForegroundColor Green
Write-Host ""

# --- Step 2: Start backend ---
Write-Host "[2/3] Starting backend..." -ForegroundColor Cyan
$backendProc = Start-Process -FilePath "cmd" -ArgumentList "/c", "`"C:\claude\ja\apache-maven-3.9.6\bin\mvn.cmd`" spring-boot:run > C:\claude\ja\backend-output.log 2>&1" -WorkingDirectory "C:\claude\ja\jxc-backend" -WindowStyle Hidden -PassThru
Write-Host "  Waiting for backend (max 90s)..."

$ready = $false
for ($i = 1; $i -le 30; $i++) {
    Start-Sleep -Seconds 3
    try {
        $r = Invoke-RestMethod -Uri "http://localhost:$backendPort/api/auth/login" -Method POST -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}' -TimeoutSec 3 -ErrorAction Stop
        if ($r.code -eq 200) {
            $ready = $true
            Write-Host "  Backend ready! ($($i * 3)s)" -ForegroundColor Green
            break
        }
    } catch {}
    Write-Host "  Starting... ($i/30)" -ForegroundColor DarkGray
}

if (-not $ready) {
    Write-Host "  Backend TIMEOUT! Check backend-output.log" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}
Write-Host ""

# --- Step 3: Start frontend ---
Write-Host "[3/3] Starting frontend..." -ForegroundColor Cyan
Remove-Item "C:\claude\ja\frontend-output.log" -Force -ErrorAction SilentlyContinue
$frontendProc = Start-Process -FilePath "cmd" -ArgumentList "/c", "npm run dev -- --port $frontendPort > C:\claude\ja\frontend-output.log 2>&1" -WorkingDirectory "C:\claude\ja\jxc-frontend" -WindowStyle Hidden -PassThru
Write-Host "  Waiting for frontend (max 30s)..."

$ready = $false
for ($i = 1; $i -le 15; $i++) {
    Start-Sleep -Seconds 2
    $conn = Get-NetTCPConnection -LocalPort $frontendPort -State Listen -ErrorAction SilentlyContinue
    if ($conn) {
        $ready = $true
        Write-Host "  Frontend ready! ($($i * 2)s)" -ForegroundColor Green
        break
    }
    Write-Host "  Starting... ($i/15)" -ForegroundColor DarkGray
}

if (-not $ready) {
    Write-Host "  Frontend TIMEOUT! Check frontend-output.log" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}
Write-Host ""

# --- Done ---
Write-Host "============================================" -ForegroundColor Green
Write-Host "  All services started!" -ForegroundColor Green
Write-Host "  Frontend: http://localhost:$frontendPort"
Write-Host "  Backend:  http://localhost:$backendPort"
Write-Host "  Username: admin"
Write-Host "  Password: admin123"
Write-Host "============================================" -ForegroundColor Green
Write-Host ""

Start-Process "http://localhost:$frontendPort"

Write-Host "Press Enter to close this window..."
Read-Host