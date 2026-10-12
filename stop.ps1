<#
.SYNOPSIS
    停止进销存管理系统的本地服务与外网隧道。
.DESCRIPTION
    只停本服务自己的进程：按端口找占用者（8080 后端 / 4173 前端），
    再停 cloudflared 隧道，以及命令行里明确属于本项目的 vite preview node 进程。
    不会 indiscriminately 杀掉机器上其它 java 或 node 程序。
#>
param(
    [int]$ApiPort = 8080,
    [int]$WebPort = 4173
)

function Stop-PortOwner([int]$Port, [string]$Label) {
    $conn = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    if (-not $conn) { Write-Host "  $Label (:$Port) 未运行" -ForegroundColor DarkGray; return }
    foreach ($procId in ($conn | Select-Object -ExpandProperty OwningProcess -Unique)) {
        $p = Get-Process -Id $procId -ErrorAction SilentlyContinue
        if ($p) {
            Stop-Process -Id $p.Id -Force -ErrorAction SilentlyContinue
            Write-Host "  [OK] 已停止 $Label (PID $($p.Id) $($p.ProcessName))" -ForegroundColor Green
        }
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "  JXC 进销存系统 - 停止服务" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow

Stop-PortOwner $ApiPort "后端"
Stop-PortOwner $WebPort "前端"

# 隧道：cloudflared 是本项目的启动脚本拉起来的，直接按进程名停
$cf = @(Get-Process -Name "cloudflared" -ErrorAction SilentlyContinue)
if ($cf.Count -gt 0) {
    $cf | Stop-Process -Force -ErrorAction SilentlyContinue
    Write-Host "  [OK] 已停止 $($cf.Count) 条 cloudflared 隧道（外网地址随之失效）" -ForegroundColor Green
} else {
    Write-Host "  cloudflared 未运行" -ForegroundColor DarkGray
}

# 兜底：命令行里明确带本项目 vite preview 的 node 进程
$node = @(Get-CimInstance Win32_Process -Filter "Name='node.exe'" -ErrorAction SilentlyContinue |
    Where-Object { $_.CommandLine -match 'jxc-frontend' -and $_.CommandLine -match 'vite' })
foreach ($n in $node) {
    Stop-Process -Id $n.ProcessId -Force -ErrorAction SilentlyContinue
    Write-Host "  [OK] 已清理 vite preview (PID $($n.ProcessId))" -ForegroundColor Green
}

Write-Host ""
Write-Host "  完成。日志目录：$PSScriptRoot\logs" -ForegroundColor Cyan
Write-Host ""
