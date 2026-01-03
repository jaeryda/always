# Jenkins용 Spring Boot 재기동 스크립트
# 기존 프로세스를 종료하고 새로 시작합니다

param(
    [string]$Profile = "mysql"
)

Write-Host "=== Always 서버 재기동 시작 ===" -ForegroundColor Cyan

# 1. JAVA_HOME 설정
if (-not $env:JAVA_HOME) {
    Write-Host "JAVA_HOME을 찾는 중..." -ForegroundColor Yellow
    try {
        $javaPath = (Get-Command java -ErrorAction Stop).Source
        $javaBinDir = Split-Path $javaPath -Parent
        $javaHome = Split-Path $javaBinDir -Parent
        $env:JAVA_HOME = $javaHome
        Write-Host "JAVA_HOME 설정됨: $javaHome" -ForegroundColor Green
    } catch {
        Write-Host "❌ Java를 찾을 수 없습니다." -ForegroundColor Red
        exit 1
    }
}

# 2. 프로젝트 디렉토리로 이동
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptPath
Write-Host "작업 디렉토리: $scriptPath" -ForegroundColor Green

# 3. 기존 프로세스 종료 (포트 8089 사용 중인 Java 프로세스)
Write-Host "`n기존 프로세스 종료 중..." -ForegroundColor Yellow
$port = 8089
$processes = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique

if ($processes) {
    foreach ($processId in $processes) {
        $process = Get-Process -Id $processId -ErrorAction SilentlyContinue
        if ($process -and $process.ProcessName -eq "java") {
            Write-Host "프로세스 종료: PID $processId ($($process.ProcessName))" -ForegroundColor Yellow
            Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
            Start-Sleep -Seconds 2
        }
    }
    Write-Host "기존 프로세스 종료 완료" -ForegroundColor Green
} else {
    Write-Host "실행 중인 프로세스가 없습니다." -ForegroundColor Green
}

# 4. Maven 빌드
Write-Host "`nMaven 빌드 시작..." -ForegroundColor Cyan
Write-Host "현재 디렉토리: $(Get-Location)" -ForegroundColor Gray
& .\mvnw.cmd clean package -DskipTests
$buildExitCode = $LASTEXITCODE
if ($buildExitCode -ne 0) {
    Write-Host "❌ 빌드 실패! Exit Code: $buildExitCode" -ForegroundColor Red
    exit 1
}
Write-Host "빌드 완료" -ForegroundColor Green

# 5. JAR 파일 찾기
Write-Host "`nJAR 파일 검색 중..." -ForegroundColor Cyan
$targetPath = Join-Path $scriptPath "target"
Write-Host "target 디렉토리: $targetPath" -ForegroundColor Gray

if (-not (Test-Path $targetPath)) {
    Write-Host "❌ target 디렉토리가 존재하지 않습니다." -ForegroundColor Red
    exit 1
}

$jarFiles = Get-ChildItem -Path $targetPath -Filter "*.jar" -ErrorAction SilentlyContinue
Write-Host "발견된 JAR 파일:" -ForegroundColor Gray
$jarFiles | ForEach-Object { Write-Host "  - $($_.Name)" -ForegroundColor Gray }

$jarFile = $jarFiles | Where-Object { $_.Name -notlike "*-sources.jar" -and $_.Name -notlike "*-javadoc.jar" } | Select-Object -First 1

if (-not $jarFile) {
    Write-Host "❌ 실행 가능한 JAR 파일을 찾을 수 없습니다." -ForegroundColor Red
    Write-Host "target 디렉토리 내용:" -ForegroundColor Yellow
    Get-ChildItem -Path $targetPath | ForEach-Object {
        $type = if ($_.PSIsContainer) { 'Directory' } else { 'File' }
        Write-Host "  - $($_.Name) ($type)" -ForegroundColor Yellow
    }
    exit 1
}

Write-Host "JAR 파일 찾음: $($jarFile.Name)" -ForegroundColor Green

# 6. JAR 파일 실행 (백그라운드)
Write-Host "`n서버 시작 중..." -ForegroundColor Cyan

$env:SPRING_PROFILES_ACTIVE = $Profile

# 환경 변수 확인 및 안내
if (-not $env:OPENAI_API_KEY) {
    Write-Host "⚠️  OPENAI_API_KEY 환경 변수가 설정되지 않았습니다." -ForegroundColor Yellow
    Write-Host "   OpenAI 기능을 사용하려면 Jenkins Job에서 환경 변수를 설정하세요." -ForegroundColor Yellow
}

if ($env:DATABASE_URL) {
    Write-Host "📌 DATABASE_URL 환경 변수 사용: $env:DATABASE_URL" -ForegroundColor Cyan
} else {
    Write-Host "📌 기본 DATABASE_URL 사용 (application-mysql.properties)" -ForegroundColor Gray
}

# 로그 파일 디렉토리 생성
$logFile = Join-Path $scriptPath "logs\application.log"
$logDir = Split-Path $logFile -Parent
if (-not (Test-Path $logDir)) {
    New-Item -ItemType Directory -Path $logDir -Force | Out-Null
}

# 백그라운드에서 실행 (Start-Process 사용 - 리다이렉션 없이 실행)
Write-Host "JAR 파일 실행 중: $($jarFile.FullName)" -ForegroundColor Gray

# Start-Process를 사용하여 백그라운드로 실행 (로그는 Spring Boot의 파일 로깅 설정 사용)
$process = Start-Process -FilePath "java" `
    -ArgumentList "-jar", "`"$($jarFile.FullName)`"" `
    -WorkingDirectory $scriptPath `
    -WindowStyle Hidden `
    -PassThru

# 프로세스 시작 대기 및 상태 확인
Write-Host "서버 초기화 대기 중..." -ForegroundColor Gray
$maxWait = 30  # 최대 30초 대기
$waitInterval = 2  # 2초마다 확인
$elapsed = 0
$isRunning = $false

while ($elapsed -lt $maxWait) {
    Start-Sleep -Seconds $waitInterval
    $elapsed += $waitInterval
    
    # 프로세스가 여전히 실행 중인지 확인
    $processCheck = Get-Process -Id $process.Id -ErrorAction SilentlyContinue
    if (-not $processCheck) {
        Write-Host "❌ 서버 프로세스가 종료되었습니다! (${elapsed}초 후)" -ForegroundColor Red
        Write-Host "로그 파일을 확인하세요: $logFile" -ForegroundColor Yellow
        if (Test-Path $logFile) {
            Write-Host "`n최근 로그:" -ForegroundColor Yellow
            Get-Content $logFile -Tail 30
        }
        exit 1
    }
    
    # 포트가 열렸는지 확인 (서버가 실제로 준비되었는지)
    $portCheck = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    if ($portCheck) {
        $isRunning = $true
        Write-Host "✅ 서버가 정상적으로 시작되었습니다! (${elapsed}초 소요)" -ForegroundColor Green
        break
    }
    
    Write-Host "  대기 중... (${elapsed}/${maxWait}초)" -ForegroundColor Gray
}

if ($isRunning) {
    Write-Host "서버가 시작되었습니다. PID: $($process.Id)" -ForegroundColor Green
    Write-Host "포트: $port" -ForegroundColor Green
    Write-Host "프로파일: $Profile" -ForegroundColor Green
    Write-Host "`n로그 확인: $logFile (Spring Boot 로그 파일 설정에 따라 생성됨)" -ForegroundColor Yellow
    
    # 프로세스 ID를 파일에 저장 (나중에 종료할 때 사용)
    $pidFile = Join-Path $scriptPath "always-server.pid"
    $process.Id | Out-File -FilePath $pidFile -Encoding ASCII
} else {
    $processCheck = Get-Process -Id $process.Id -ErrorAction SilentlyContinue
    if ($processCheck) {
        Write-Host "⚠️  서버 프로세스는 실행 중이지만 포트 $port 가 열리지 않았습니다." -ForegroundColor Yellow
        Write-Host "로그 파일을 확인하세요: $logFile" -ForegroundColor Yellow
        if (Test-Path $logFile) {
            Write-Host "`n최근 로그:" -ForegroundColor Yellow
            Get-Content $logFile -Tail 30
        }
        # 프로세스는 실행 중이므로 성공으로 처리 (초기화가 늦을 수 있음)
        $pidFile = Join-Path $scriptPath "always-server.pid"
        $process.Id | Out-File -FilePath $pidFile -Encoding ASCII
    } else {
        Write-Host "❌ 서버 시작 실패!" -ForegroundColor Red
        exit 1
    }
}

Write-Host "`n=== 재기동 완료 ===" -ForegroundColor Green

