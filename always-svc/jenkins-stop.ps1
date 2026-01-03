# Jenkins Spring Boot Stop Script

Write-Host "=== Always Server Stop Started ===" -ForegroundColor Cyan

$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
$port = 8089

# Use PID file if available
$pidFile = Join-Path $scriptPath "always-server.pid"
if (Test-Path $pidFile) {
    $processId = Get-Content $pidFile
    $process = Get-Process -Id $processId -ErrorAction SilentlyContinue
    if ($process) {
        Write-Host "Stopping process: PID $processId" -ForegroundColor Yellow
        Stop-Process -Id $processId -Force
        Remove-Item $pidFile -Force
        Write-Host "Server stopped" -ForegroundColor Green
        exit 0
    }
}

# If PID file not found or process not found, search by port
Write-Host "Searching for process using port $port..." -ForegroundColor Yellow
$processes = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique

if ($processes) {
    foreach ($processId in $processes) {
        $process = Get-Process -Id $processId -ErrorAction SilentlyContinue
        if ($process -and $process.ProcessName -eq "java") {
            Write-Host "Stopping process: PID $processId ($($process.ProcessName))" -ForegroundColor Yellow
            Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
        }
    }
    Write-Host "Server stopped" -ForegroundColor Green
} else {
    Write-Host "No running server found." -ForegroundColor Yellow
}

Write-Host "=== Stop Completed ===" -ForegroundColor Green
