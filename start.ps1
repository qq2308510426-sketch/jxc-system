<#
.SYNOPSIS
    进销存管理系统一键启动脚本。
.DESCRIPTION
    默认只在本机启动后端(8080)与前端静态服务(4173)。
    机密（数据库地址、账号、JWT 密钥）一律从项目根目录的 .env.local 读取，
    该文件已被 .gitignore 忽略，不要把它提交到仓库。

    .\start.ps1              本机启动（不访问外网）
    .\start.ps1 -Tunnel      额外建立 cloudflared 快速隧道并输出外网地址
    .\start.ps1 -Build       强制重新打包后端 jar 与前端 dist
    .\start.ps1 -SkipWeb     只启动后端
.NOTES
    旧版本依赖 ngrok 且会把隧道地址自动 commit/push 到 GitHub，现已移除：
    启动脚本不再改写仓库，也不再内嵌任何凭据。
#>
param(
    [switch]$Tunnel,
    [switch]$Build,
    [switch]$SkipWeb,
    [int]$ApiPort = 8080,
    [int]$WebPort = 4173,
    [string]$CloudflaredPath = "C:\claude\out\bin\cloudflared.exe",
    [string]$MavenPath = ""
)

$ErrorActionPreference = "Stop"
$ProjectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$BackendDir = Join-Path $ProjectDir "jxc-backend"
$FrontendDir = Join-Path $ProjectDir "jxc-frontend"
$BackendJar = Join-Path $BackendDir "target\jxc-backend-1.0.0.jar"
$EnvFile = Join-Path $ProjectDir ".env.local"
$RunDir = Join-Path $ProjectDir "logs"
New-Item -ItemType Directory -Force -Path $RunDir | Out-Null

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  JXC 进销存系统 - 启动" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# ---------- 1. 读取 .env.local ----------
if (-not (Test-Path $EnvFile)) {
    Write-Host "[X] 缺少配置文件 $EnvFile" -ForegroundColor Red
    Write-Host "    请新建该文件（KEY=VALUE 每行一条）：DB_URL / DB_USERNAME / DB_PASSWORD / JWT_SECRET，可选 CORS_ORIGINS / SERVER_PORT" -ForegroundColor Yellow
    exit 1
}
foreach ($line in Get-Content $EnvFile) {
    if ($line -match '^\s*#') { continue }
    if ($line -match '^\s*([A-Z0-9_]+)\s*=\s*(.*)$') {
        Set-Item -Path ("env:" + $Matches[1]) -Value $Matches[2].Trim()
    }
}
foreach ($k in 'DB_URL', 'DB_USERNAME', 'DB_PASSWORD', 'JWT_SECRET') {
    if (-not (Get-Item ("env:$k")).Value) {
        Write-Host "[X] .env.local 缺少 $k" -ForegroundColor Red
        exit 1
    }
}
$env:SPRING_PROFILES_ACTIVE = "prod"
$env:SERVER_PORT = "$ApiPort"
Write-Host "[OK] 已载入 .env.local（凭据不回显）" -ForegroundColor Green

if (-not $MavenPath) {
    $MavenPath = (Get-ChildItem -Path $ProjectDir -Filter "mvn.cmd" -Recurse -Depth 3 -ErrorAction SilentlyContinue | Select-Object -First 1).FullName
}

# ---------- 2. 端口占用处理（只清理本服务自己的进程） ----------
function Stop-PortListener([int]$Port) {
    $conn = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    if ($conn) {
        Write-Host "[*] 释放端口 $Port ..." -ForegroundColor Yellow
        Stop-Process -Id $conn[0].OwningProcess -Force -ErrorAction SilentlyContinue
        Start-Sleep -Seconds 2
    }
}
Stop-PortListener $ApiPort
if (-not $SkipWeb) { Stop-PortListener $WebPort }

# ---------- 3. 外网隧道（可选，先建隧道以便后端一次配好 CORS） ----------
$ApiPublicUrl = ""
$WebPublicUrl = ""
if ($Tunnel) {
    if (-not (Test-Path $CloudflaredPath)) {
        Write-Host "[X] 未找到 cloudflared：$CloudflaredPath" -ForegroundColor Red
        Write-Host "    可用 -CloudflaredPath 指定路径，或不加 -Tunnel 只在本机运行" -ForegroundColor Yellow
        exit 1
    }
    Get-Process -Name "cloudflared" -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 1

    Write-Host "[*] 建立 cloudflared 快速隧道 ..." -ForegroundColor Yellow
    Start-Process -FilePath $CloudflaredPath `
        -ArgumentList "tunnel", "--url", "http://localhost:$ApiPort", "--no-autoupdate" `
        -RedirectStandardOutput (Join-Path $RunDir "tunnel-api.log") `
        -RedirectStandardError (Join-Path $RunDir "tunnel-api.err.log") -NoNewWindow
    Start-Process -FilePath $CloudflaredPath `
        -ArgumentList "tunnel", "--url", "http://localhost:$WebPort", "--no-autoupdate" `
        -RedirectStandardOutput (Join-Path $RunDir "tunnel-web.log") `
        -RedirectStandardError (Join-Path $RunDir "tunnel-web.err.log") -NoNewWindow

    for ($i = 0; $i -lt 20; $i++) {
        Start-Sleep -Seconds 2
        foreach ($pair in @(@("tunnel-api.log", "api"), @("tunnel-web.log", "web"))) {
            $txt = ""
            $logPath = Join-Path $RunDir $pair[0]
            if (Test-Path $logPath) { $txt = Get-Content $logPath -Raw -ErrorAction SilentlyContinue }
            if (-not $txt) { $txt = Get-Content (Join-Path $RunDir ($pair[0] -replace '\.log$', '.err.log')) -Raw -ErrorAction SilentlyContinue }
            if ($txt -match 'https://[a-z0-9-]+\.trycloudflare\.com') {
                if ($pair[1] -eq "api") { $ApiPublicUrl = $Matches[0] } else { $WebPublicUrl = $Matches[0] }
            }
        }
        if ($ApiPublicUrl -and $WebPublicUrl) { break }
    }
    if (-not ($ApiPublicUrl -and $WebPublicUrl)) {
        Write-Host "[X] 隧道地址获取失败，请查看 logs\tunnel-*.log" -ForegroundColor Red
        exit 1
    }
    Write-Host "[OK] 后端隧道: $ApiPublicUrl" -ForegroundColor Green
    Write-Host "[OK] 前端隧道: $WebPublicUrl" -ForegroundColor Green
}

# CORS 只放行本机前端与当次隧道来源
$origins = @("http://localhost:$WebPort", "http://localhost:5173")
if ($WebPublicUrl) { $origins += $WebPublicUrl }
$env:CORS_ORIGINS = ($origins -join ",")

# ---------- 4. 后端 ----------
if ($Build -or -not (Test-Path $BackendJar)) {
    if (-not $MavenPath) { Write-Host "[X] 未找到 mvn.cmd，请用 -MavenPath 指定" -ForegroundColor Red; exit 1 }
    Write-Host "[*] 打包后端 ..." -ForegroundColor Yellow
    Push-Location $BackendDir
    & $MavenPath package -DskipTests -B
    $code = $LASTEXITCODE
    Pop-Location
    if ($code -ne 0) { Write-Host "[X] 后端打包失败"; exit 1 }
}

Write-Host "[*] 启动后端 (:$ApiPort) ..." -ForegroundColor Yellow
Start-Process -FilePath "java" `
    -ArgumentList "-Xms128m", "-Xmx256m", "-jar", "`"$BackendJar`"" `
    -WorkingDirectory $BackendDir `
    -RedirectStandardOutput (Join-Path $RunDir "backend.log") `
    -RedirectStandardError (Join-Path $RunDir "backend-err.log") -NoNewWindow

$backendOk = $false
for ($i = 0; $i -lt 15; $i++) {
    Start-Sleep -Seconds 2
    try {
        $h = Invoke-RestMethod -Uri "http://localhost:$ApiPort/actuator/health" -TimeoutSec 2
        if ($h.status -eq "UP") { $backendOk = $true; break }
    } catch {}
}
if (-not $backendOk) {
    Write-Host "[X] 后端启动超时，日志尾部：" -ForegroundColor Red
    Get-Content (Join-Path $RunDir "backend-err.log") -Tail 15 -ErrorAction SilentlyContinue
    exit 1
}
Write-Host "[OK] 后端已就绪" -ForegroundColor Green

# ---------- 5. 前端 ----------
if (-not $SkipWeb) {
    Push-Location $FrontendDir
    $distIndex = Join-Path $FrontendDir "dist\index.html"
    if ($Tunnel) {
        # 隧道模式下 API 地址是运行时才知道的，必须按当次地址重新构建
        Write-Host "[*] 构建前端 (VITE_API_BASE_URL=$ApiPublicUrl/api) ..." -ForegroundColor Yellow
        $env:VITE_API_BASE_URL = "$ApiPublicUrl/api"
        & npx.cmd vite build
    } elseif (-not (Test-Path $distIndex)) {
        Write-Host "[*] 首次构建前端 ..." -ForegroundColor Yellow
        & npx.cmd vite build
    }
    Pop-Location

    Write-Host "[*] 启动前端静态服务 (:$WebPort) ..." -ForegroundColor Yellow
    Push-Location $FrontendDir
    Start-Process -FilePath "node" `
        -ArgumentList "node_modules\vite\bin\vite.js", "preview", "--host", "0.0.0.0", "--port", "$WebPort" `
        -WorkingDirectory $FrontendDir `
        -RedirectStandardOutput (Join-Path $RunDir "web.log") `
        -RedirectStandardError (Join-Path $RunDir "web-err.log") -NoNewWindow
    Pop-Location
    Start-Sleep -Seconds 3
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  启动完成" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host "本机前端: http://localhost:$WebPort/" -ForegroundColor Cyan
Write-Host "本机后端: http://localhost:$ApiPort/api" -ForegroundColor Cyan
if ($Tunnel) {
    Write-Host "外网前端: $WebPublicUrl" -ForegroundColor Cyan
    Write-Host "外网接口: $ApiPublicUrl/api" -ForegroundColor Cyan
    Write-Host "  快速隧道地址每次启动都会变化，属正常现象" -ForegroundColor Yellow
}
Write-Host "登录账号见交付说明《填写说明与待确认清单》，凭据不在本脚本中" -ForegroundColor Yellow
Write-Host "日志目录: $RunDir" -ForegroundColor DarkGray
Write-Host ""
