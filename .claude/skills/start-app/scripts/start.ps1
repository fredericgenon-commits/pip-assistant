# Start backend (:8080) + frontend (:4200) detached, then wait for health.
# Refuses to start a component whose port is already taken (use restart.ps1 to
# replace a running instance).
param([switch]$Force)   # -Force: skip the "already running" guard (used by restart.ps1)
. (Join-Path $PSScriptRoot '_common.ps1')

New-Item -ItemType Directory -Force -Path $LogDir | Out-Null

# --- Guard: don't double-start unless -Force ---------------------------------
if (-not $Force) {
    $busy = @()
    if ((Get-ListenerPids $BackendPort).Count  -gt 0) { $busy += "backend (:$BackendPort)" }
    if ((Get-ListenerPids $FrontendPort).Count -gt 0) { $busy += "frontend (:$FrontendPort)" }
    if ($busy.Count -gt 0) {
        Write-Host "Already running: $($busy -join ', '). Use restart.ps1 to replace it."
        exit 1
    }
}

# --- Backend -----------------------------------------------------------------
$jdk = Find-Jdk21
Write-Host "JAVA_HOME = $jdk"
$env:JAVA_HOME = $jdk

Write-Host "Starting backend (mvnw spring-boot:run) ..."
$beOut = Join-Path $LogDir 'backend.out'
$beErr = Join-Path $LogDir 'backend.err'
$be = Start-Process -FilePath (Join-Path $BackendDir 'mvnw.cmd') `
        -ArgumentList 'spring-boot:run' `
        -WorkingDirectory $BackendDir `
        -RedirectStandardOutput $beOut -RedirectStandardError $beErr `
        -WindowStyle Hidden -PassThru
$be.Id | Out-File (Join-Path $RunDir 'backend.pid') -Encoding ascii

# --- Frontend ----------------------------------------------------------------
if (-not (Test-Path (Join-Path $FrontendDir 'node_modules'))) {
    Write-Host "node_modules missing - running 'npm install' (first run, may take a while) ..."
    Push-Location $FrontendDir
    try { & npm.cmd install } finally { Pop-Location }
}

Write-Host "Starting frontend (npm start) ..."
$feOut = Join-Path $LogDir 'frontend.out'
$feErr = Join-Path $LogDir 'frontend.err'
$fe = Start-Process -FilePath 'npm.cmd' `
        -ArgumentList 'start' `
        -WorkingDirectory $FrontendDir `
        -RedirectStandardOutput $feOut -RedirectStandardError $feErr `
        -WindowStyle Hidden -PassThru
$fe.Id | Out-File (Join-Path $RunDir 'frontend.pid') -Encoding ascii

# --- Wait for health ---------------------------------------------------------
Write-Host "Waiting for servers to become ready ..."
$beOk = Wait-Healthy 'Backend ' { Test-BackendHealth } 120
$feOk = Wait-Healthy 'Frontend' { Test-FrontendUp }    120

Write-Host ""
Write-Host "Backend : $(if ($beOk) {'http://localhost:8080/api/health = UP'} else {'NOT READY - see .run/logs/backend.err'})"
Write-Host "Frontend: $(if ($feOk) {'http://localhost:4200'} else {'NOT READY - see .run/logs/frontend.err'})"

if (-not ($beOk -and $feOk)) { exit 1 }
