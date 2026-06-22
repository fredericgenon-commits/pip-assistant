# Shared helpers for the start-app skill scripts.
# Dot-sourced by status/stop/start/restart. Defines paths and small functions;
# performs no action on its own.

$ErrorActionPreference = 'Stop'

# Repo root = four levels up from this scripts/ dir:
#   <repo>\.claude\skills\start-app\scripts\_common.ps1
$Script:RepoRoot    = (Resolve-Path (Join-Path $PSScriptRoot '..\..\..\..')).Path
$Script:BackendDir  = Join-Path $RepoRoot 'pip-assistant-backend'
$Script:FrontendDir = Join-Path $RepoRoot 'pip-assistant-frontend'
$Script:RunDir      = Join-Path $RepoRoot '.run'
$Script:LogDir      = Join-Path $RunDir   'logs'

$Script:BackendPort  = 8080
$Script:FrontendPort = 4200

function Find-Jdk21 {
    # Pick the highest jdk21* under Amazon Corretto. Fail fast with a clear
    # message so the caller knows exactly what to fix.
    $base = 'C:\Program Files\Amazon Corretto'
    if (-not (Test-Path $base)) {
        throw "Amazon Corretto not found at '$base'. Install JDK 21 or update Find-Jdk21 in _common.ps1."
    }
    $jdk = Get-ChildItem $base -Directory -Filter 'jdk21*' -ErrorAction SilentlyContinue |
           Sort-Object Name -Descending | Select-Object -First 1
    if (-not $jdk) {
        throw "No 'jdk21*' folder under '$base'. Install Corretto 21 or update the detection glob."
    }
    return $jdk.FullName
}

function Get-ListenerPids([int]$Port) {
    # PIDs of processes LISTENing on $Port. Empty array if none.
    try {
        return @(Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction Stop |
                 Select-Object -ExpandProperty OwningProcess -Unique)
    } catch {
        return @()
    }
}

function Stop-Port([int]$Port, [string]$Label) {
    $pids = Get-ListenerPids $Port
    if ($pids.Count -eq 0) {
        Write-Host "  $Label (:$Port) - not running"
        return
    }
    foreach ($procId in $pids) {
        try {
            $name = (Get-Process -Id $procId -ErrorAction SilentlyContinue).ProcessName
            Stop-Process -Id $procId -Force -ErrorAction Stop
            Write-Host "  $Label (:$Port) - stopped PID $procId ($name)"
        } catch {
            Write-Host "  $Label (:$Port) - could not stop PID $procId : $($_.Exception.Message)"
        }
    }
}

function Wait-Healthy {
    param(
        [string]$Label,
        [scriptblock]$Probe,   # returns $true when ready
        [int]$TimeoutSec = 120
    )
    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    while ((Get-Date) -lt $deadline) {
        try { if (& $Probe) { Write-Host "  $Label - UP"; return $true } } catch { }
        Start-Sleep -Seconds 2
    }
    Write-Host "  $Label - NOT READY after ${TimeoutSec}s"
    return $false
}

function Test-BackendHealth {
    $r = Invoke-RestMethod -Uri "http://localhost:$BackendPort/api/health" -TimeoutSec 4
    return $r.status -eq 'UP'
}

function Test-FrontendUp {
    $r = Invoke-WebRequest -Uri "http://localhost:$FrontendPort" -TimeoutSec 4 -UseBasicParsing
    return $r.StatusCode -eq 200
}
