# Stop any running instance, then start fresh. The usual entry point.
. (Join-Path $PSScriptRoot '_common.ps1')

Write-Host "PIP Assistant - restart"
& (Join-Path $PSScriptRoot 'stop.ps1')

# Give the OS a moment to release the listening sockets before rebinding.
Start-Sleep -Seconds 2

& (Join-Path $PSScriptRoot 'start.ps1') -Force
exit $LASTEXITCODE
