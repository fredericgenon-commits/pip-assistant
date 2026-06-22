# Stop the backend and frontend by killing whatever listens on their ports.
. (Join-Path $PSScriptRoot '_common.ps1')

Write-Host "PIP Assistant - stopping"
Stop-Port $BackendPort  'Backend '
Stop-Port $FrontendPort 'Frontend'

# Clean up PID files if present.
foreach ($f in @('backend.pid', 'frontend.pid')) {
    $path = Join-Path $RunDir $f
    if (Test-Path $path) { Remove-Item $path -Force -ErrorAction SilentlyContinue }
}
Write-Host "Done."
