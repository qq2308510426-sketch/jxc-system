$ProjectDir = "C:\claude\ja"
$BackendJar = "$ProjectDir\jxc-backend\target\jxc-backend-1.0.0.jar"
$NgrokPath = "$ProjectDir\temp\ngrok\ngrok.exe"
$Repo = "qq2308510426-sketch/jxc-system"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  JXC 进销存 - 一键启动" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Check JAR
if (-not (Test-Path $BackendJar)) {
    Write-Host "[!] 编译后端..." -ForegroundColor Yellow
    Set-Location "$ProjectDir\jxc-backend"
    & "$ProjectDir\apache-maven-3.9.6\bin\mvn.cmd" package -DskipTests -B
    if ($LASTEXITCODE -ne 0) { Write-Host "[X] 编译失败"; pause; exit 1 }
}

# 2. Kill old 8080
$existing = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue
if ($existing) {
    Write-Host "[*] 关闭旧进程..." -ForegroundColor Yellow
    Stop-Process -Id $existing[0].OwningProcess -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 2
}

# 3. Start backend
Write-Host "[*] 启动后端..." -ForegroundColor Yellow
$env:SPRING_PROFILES_ACTIVE = "prod"
$env:DB_URL = "jdbc:mysql://gateway01.eu-central-1.prod.aws.tidbcloud.com:4000/jxc_db?useSSL=true&serverTimezone=Asia/Shanghai&characterEncoding=utf8&allowPublicKeyRetrieval=true"
$env:DB_USERNAME = "3PpHnK6HsZBmz7C.root"
$env:DB_PASSWORD = "aGgfvbOqiyy4XdfC"
$env:JWT_SECRET = "jxc-secure-key-2026-production-use-at-least-32"
$env:CORS_ORIGINS = "*"
$env:SERVER_PORT = "8080"
Start-Process -FilePath "java" -ArgumentList "-Xms128m -Xmx256m -jar $BackendJar" -RedirectStandardOutput "$ProjectDir\backend.log" -RedirectStandardError "$ProjectDir\backend-err.log" -NoNewWindow

Write-Host "[*] 等待后端启动..." -ForegroundColor Yellow
for ($i = 0; $i -lt 30; $i += 2) {
    Start-Sleep -Seconds 2
    try { $h = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -TimeoutSec 2; if ($h.status -eq "UP") { Write-Host "[OK] 后端启动成功" -ForegroundColor Green; break } } catch {}
}

# 4. Start ngrok
Write-Host "[*] 启动内网穿透..." -ForegroundColor Yellow
$nr = Get-Process -Name "ngrok" -ErrorAction SilentlyContinue
if (-not $nr) {
    Start-Process -FilePath $NgrokPath -ArgumentList "http 8080 --log=stdout" -RedirectStandardOutput "$ProjectDir\ngrok.log" -NoNewWindow
    Start-Sleep -Seconds 5
}

# 5. Get ngrok URL
$ngrokUrl = ""
for ($i = 0; $i -lt 10; $i++) {
    try { $t = Invoke-RestMethod -Uri "http://127.0.0.1:4040/api/tunnels" -TimeoutSec 3; $ngrokUrl = $t.tunnels[0].public_url; if ($ngrokUrl) { break } } catch {}
    Start-Sleep -Seconds 2
}
if (-not $ngrokUrl) { Write-Host "[X] ngrok 失败"; pause; exit 1 }
$apiUrl = "$ngrokUrl/api"
Write-Host "[OK] 穿透地址: $ngrokUrl" -ForegroundColor Green

# 6. Update GitHub
Write-Host "[*] 更新前端..." -ForegroundColor Yellow
$env:GH_TOKEN = & gh auth token 2>$null
& gh variable set VITE_API_BASE_URL --body $apiUrl --repo $Repo 2>$null
Set-Content -Path "$ProjectDir\jxc-frontend\.deploy-trigger" -Value (Get-Date -Format "yyyy-MM-dd HH:mm:ss")
Set-Location $ProjectDir
git add jxc-frontend/.deploy-trigger 2>$null
git commit -m "chore: update API URL" 2>$null
git push origin master 2>$null

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  启动完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "前端: https://qq2308510426-sketch.github.io/jxc-system/" -ForegroundColor Cyan
Write-Host "后端: $ngrokUrl" -ForegroundColor Cyan
Write-Host "账号: admin / admin123" -ForegroundColor Cyan
Write-Host ""
Write-Host "关闭此窗口 = 关闭所有服务" -ForegroundColor Yellow
Write-Host ""
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")