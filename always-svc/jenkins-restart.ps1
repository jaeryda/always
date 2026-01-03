# Jenkins Spring Boot Restart Script
# Stops existing process and starts a new one

param(
    [string]$Profile = "mysql"
)

Write-Host "=== Always Server Restart Started ===" -ForegroundColor Cyan

# 1. JAVA_HOME Setup
if (-not $env:JAVA_HOME) {
    Write-Host "Finding JAVA_HOME..." -ForegroundColor Yellow
    try {
        $javaPath = (Get-Command java -ErrorAction Stop).Source
        $javaBinDir = Split-Path $javaPath -Parent
        $javaHome = Split-Path $javaBinDir -Parent
        $env:JAVA_HOME = $javaHome
        Write-Host "JAVA_HOME set: $javaHome" -ForegroundColor Green
    } catch {
        Write-Host "ERROR: Java not found." -ForegroundColor Red
        exit 1
    }
}

# 2. Change to project directory
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptPath
Write-Host "Working directory: $scriptPath" -ForegroundColor Green

# 3. Stop existing process (Java process using port 8089)
Write-Host "`nStopping existing process..." -ForegroundColor Yellow
$port = 8089
$processes = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique

if ($processes) {
    foreach ($processId in $processes) {
        $process = Get-Process -Id $processId -ErrorAction SilentlyContinue
        if ($process -and $process.ProcessName -eq "java") {
            Write-Host "Stopping process: PID $processId ($($process.ProcessName))" -ForegroundColor Yellow
            Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
            Start-Sleep -Seconds 2
        }
    }
    Write-Host "Existing process stopped" -ForegroundColor Green
} else {
    Write-Host "No running process found." -ForegroundColor Green
}

# 4. Maven Build
Write-Host "`nStarting Maven build..." -ForegroundColor Cyan
Write-Host "Current directory: $(Get-Location)" -ForegroundColor Gray
& .\mvnw.cmd clean package -DskipTests
$buildExitCode = $LASTEXITCODE
if ($buildExitCode -ne 0) {
    Write-Host "ERROR: Build failed! Exit Code: $buildExitCode" -ForegroundColor Red
    exit 1
}
Write-Host "Build completed" -ForegroundColor Green

# 5. Find JAR file
Write-Host "`nSearching for JAR file..." -ForegroundColor Cyan
$targetPath = Join-Path $scriptPath "target"
Write-Host "target directory: $targetPath" -ForegroundColor Gray

if (-not (Test-Path $targetPath)) {
    Write-Host "ERROR: target directory does not exist." -ForegroundColor Red
    exit 1
}

$jarFiles = Get-ChildItem -Path $targetPath -Filter "*.jar" -ErrorAction SilentlyContinue
Write-Host "Found JAR files:" -ForegroundColor Gray
$jarFiles | ForEach-Object { Write-Host "  - $($_.Name)" -ForegroundColor Gray }

$jarFile = $jarFiles | Where-Object { $_.Name -notlike "*-sources.jar" -and $_.Name -notlike "*-javadoc.jar" } | Select-Object -First 1

if (-not $jarFile) {
    Write-Host "ERROR: Executable JAR file not found." -ForegroundColor Red
    Write-Host "target directory contents:" -ForegroundColor Yellow
    Get-ChildItem -Path $targetPath | ForEach-Object {
        $type = if ($_.PSIsContainer) { 'Directory' } else { 'File' }
        Write-Host "  - $($_.Name) ($type)" -ForegroundColor Yellow
    }
    exit 1
}

Write-Host "JAR file found: $($jarFile.Name)" -ForegroundColor Green

# 6. Start JAR file (background)
Write-Host "`nStarting server..." -ForegroundColor Cyan

$env:SPRING_PROFILES_ACTIVE = $Profile

# Check environment variables
if (-not $env:OPENAI_API_KEY) {
    Write-Host "WARNING: OPENAI_API_KEY environment variable not set." -ForegroundColor Yellow
    Write-Host "   Set environment variable in Jenkins Job to use OpenAI features." -ForegroundColor Yellow
}

if ($env:DATABASE_URL) {
    Write-Host "Using DATABASE_URL environment variable: $env:DATABASE_URL" -ForegroundColor Cyan
} else {
    Write-Host "Using default DATABASE_URL (application-mysql.properties)" -ForegroundColor Gray
}

# Create log directory
$logFile = Join-Path $scriptPath "logs\application.log"
$logDir = Split-Path $logFile -Parent
if (-not (Test-Path $logDir)) {
    New-Item -ItemType Directory -Path $logDir -Force | Out-Null
}

# Start in background (using Start-Process)
Write-Host "Executing JAR file: $($jarFile.FullName)" -ForegroundColor Gray

# Redirect stdout and stderr to separate files
$stdoutFile = Join-Path $logDir "stdout.log"
$stderrFile = Join-Path $logDir "stderr.log"

# Start-Process to run in background
# -WindowStyle Hidden: Hide window (background execution)
# -RedirectStandardOutput/RedirectStandardError: Redirect logs to files
# -PassThru: Return process object
# Note: Process runs as separate process to continue after Jenkins build ends
$process = Start-Process -FilePath "java" `
    -ArgumentList "-jar", "`"$($jarFile.FullName)`"" `
    -WorkingDirectory $scriptPath `
    -WindowStyle Hidden `
    -RedirectStandardOutput $stdoutFile `
    -RedirectStandardError $stderrFile `
    -PassThru

# Store process ID immediately to avoid file descriptor issues
$processId = $process.Id

# Wait for process start and check status
Write-Host "Waiting for server initialization..." -ForegroundColor Gray
$maxWait = 30  # Maximum 30 seconds wait
$waitInterval = 2  # Check every 2 seconds
$elapsed = 0
$isRunning = $false

while ($elapsed -lt $maxWait) {
    Start-Sleep -Seconds $waitInterval
    $elapsed += $waitInterval
    
    # Check if process is still running (using stored process ID)
    $processCheck = Get-Process -Id $processId -ErrorAction SilentlyContinue
    if (-not $processCheck) {
        Write-Host "ERROR: Server process terminated! (after ${elapsed}s)" -ForegroundColor Red
        Write-Host "`nStandard output log ($stdoutFile):" -ForegroundColor Yellow
        if (Test-Path $stdoutFile) {
            Get-Content $stdoutFile -Tail 50
        } else {
            Write-Host "  (Log file not found)" -ForegroundColor Gray
        }
        Write-Host "`nStandard error log ($stderrFile):" -ForegroundColor Yellow
        if (Test-Path $stderrFile) {
            Get-Content $stderrFile -Tail 50
        } else {
            Write-Host "  (Log file not found)" -ForegroundColor Gray
        }
        Write-Host "`nApplication log ($logFile):" -ForegroundColor Yellow
        if (Test-Path $logFile) {
            Get-Content $logFile -Tail 30
        } else {
            Write-Host "  (Log file not found)" -ForegroundColor Gray
        }
        exit 1
    }
    
    # Check if port is open (server is actually ready)
    $portCheck = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    if ($portCheck) {
        $isRunning = $true
        Write-Host "SUCCESS: Server started successfully! (took ${elapsed}s)" -ForegroundColor Green
        break
    }
    
    Write-Host "  Waiting... (${elapsed}/${maxWait}s)" -ForegroundColor Gray
}

if ($isRunning) {
    # Get process ID using the port
    $portProcess = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique
    if ($portProcess) {
        $actualProcessId = $portProcess[0]
        Write-Host "Server started. PID: $actualProcessId" -ForegroundColor Green
        Write-Host "Port: $port" -ForegroundColor Green
        Write-Host "Profile: $Profile" -ForegroundColor Green
        Write-Host "`nLog file: $logFile (created by Spring Boot logging configuration)" -ForegroundColor Yellow
        
        # Save process ID to file (for stopping later)
        $pidFile = Join-Path $scriptPath "always-server.pid"
        $actualProcessId | Out-File -FilePath $pidFile -Encoding ASCII
    } else {
        Write-Host "Server started (PID not available)" -ForegroundColor Green
        Write-Host "Port: $port" -ForegroundColor Green
        Write-Host "Profile: $Profile" -ForegroundColor Green
    }
} else {
    # Check process using port
    $portProcess = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique
    $processStillRunning = Get-Process -Id $processId -ErrorAction SilentlyContinue
    if ($portProcess -or $processStillRunning) {
        Write-Host "WARNING: Server process is running but port $port is not open." -ForegroundColor Yellow
        Write-Host "`nStandard output log ($stdoutFile):" -ForegroundColor Yellow
        if (Test-Path $stdoutFile) {
            Get-Content $stdoutFile -Tail 30
        }
        Write-Host "`nStandard error log ($stderrFile):" -ForegroundColor Yellow
        if (Test-Path $stderrFile) {
            Get-Content $stderrFile -Tail 30
        }
        Write-Host "`nApplication log ($logFile):" -ForegroundColor Yellow
        if (Test-Path $logFile) {
            Get-Content $logFile -Tail 30
        }
        # Process is running, treat as success (initialization may be slow)
        $pidFile = Join-Path $scriptPath "always-server.pid"
        if ($portProcess) {
            $portProcess[0] | Out-File -FilePath $pidFile -Encoding ASCII
        } elseif ($processStillRunning) {
            $processId | Out-File -FilePath $pidFile -Encoding ASCII
        }
    } else {
        Write-Host "ERROR: Server start failed!" -ForegroundColor Red
        exit 1
    }
}

# Clean up process object to prevent file descriptor leaks
if ($process) {
    try {
        $process.Dispose()
    } catch {
        # Ignore disposal errors
    }
}
$process = $null

Write-Host "`n=== Restart Completed ===" -ForegroundColor Green
