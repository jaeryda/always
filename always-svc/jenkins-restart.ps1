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
$buildResult = & .\mvnw.cmd clean package -DskipTests 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 빌드 실패!" -ForegroundColor Red
    Write-Host $buildResult
    exit 1
}
Write-Host "빌드 완료" -ForegroundColor Green

# 5. JAR 파일 실행 (백그라운드)
Write-Host "`n서버 시작 중..." -ForegroundColor Cyan
$jarFile = Get-ChildItem -Path "target" -Filter "*.jar" -Exclude "*-sources.jar","*-javadoc.jar" | Select-Object -First 1

if (-not $jarFile) {
    Write-Host "❌ JAR 파일을 찾을 수 없습니다." -ForegroundColor Red
    exit 1
}

$env:SPRING_PROFILES_ACTIVE = $Profile

# OpenAI API 키 환경 변수 확인 (Jenkins에서 설정된 경우 사용)
if (-not $env:OPENAI_API_KEY) {
    Write-Host "⚠️  OPENAI_API_KEY 환경 변수가 설정되지 않았습니다." -ForegroundColor Yellow
    Write-Host "   OpenAI 기능을 사용하려면 Jenkins Job에서 환경 변수를 설정하세요." -ForegroundColor Yellow
}

# 백그라운드에서 실행
$processStartInfo = New-Object System.Diagnostics.ProcessStartInfo
$processStartInfo.FileName = "java"
$processStartInfo.Arguments = "-jar `"$($jarFile.FullName)`""
$processStartInfo.WorkingDirectory = $scriptPath
$processStartInfo.UseShellExecute = $false
$processStartInfo.RedirectStandardOutput = $true
$processStartInfo.RedirectStandardError = $true

$process = [System.Diagnostics.Process]::Start($processStartInfo)

# 로그 파일에 출력 저장 (선택사항)
$logFile = Join-Path $scriptPath "logs\application.log"
$logDir = Split-Path $logFile -Parent
if (-not (Test-Path $logDir)) {
    New-Item -ItemType Directory -Path $logDir -Force | Out-Null
}

Write-Host "서버가 시작되었습니다. PID: $($process.Id)" -ForegroundColor Green
Write-Host "포트: $port" -ForegroundColor Green
Write-Host "프로파일: $Profile" -ForegroundColor Green
Write-Host "`n로그 확인: $logFile" -ForegroundColor Yellow

# 프로세스 ID를 파일에 저장 (나중에 종료할 때 사용)
$pidFile = Join-Path $scriptPath "always-server.pid"
$process.Id | Out-File -FilePath $pidFile -Encoding ASCII

Write-Host "`n=== 재기동 완료 ===" -ForegroundColor Green

