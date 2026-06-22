# Show what is currently listening on the backend / frontend ports.
. (Join-Path $PSScriptRoot '_common.ps1')

function Show-Port([int]$Port, [string]$Label) {
    $pids = Get-ListenerPids $Port
    if ($pids.Count -eq 0) {
        Write-Host "  $Label (:$Port) - not running"
    } else {
        foreach ($procId in $pids) {
            $p = Get-Process -Id $procId -ErrorAction SilentlyContinue
            Write-Host "  $Label (:$Port) - PID $procId ($($p.ProcessName))"
        }
    }
}

Write-Host "PIP Assistant - status"
Show-Port $BackendPort  'Backend '
Show-Port $FrontendPort 'Frontend'
