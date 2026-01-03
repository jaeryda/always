# Jenkins용 간단한 재기동 스크립트 (한 파일로 모든 작업 수행)

param(
    [string]$Profile = "mysql"
)

$ErrorActionPreference = "Stop"

Write-Host "=== Always 서버 재기동 ===" -ForegroundColor Cyan

# 작업 디렉토리 설정
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptPath

# 1. 기존 프로세스 종료
Write-Host "기존 서버 종료 중..." -ForegroundColor Yellow
$port = 8089
$processes = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique
foreach ($pid in $processes) {
    $process = Get-Process -Id $pid -ErrorAction SilentlyContinue
    if ($process -and $process.ProcessName -eq "java") {
        Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
        Write-Host "프로세스 종료: PID $pid" -ForegroundColor Green
        Start-Sleep -Seconds 2
    }
}

# 2. 빌드
Write-Host "Maven 빌드 중..." -ForegroundColor Cyan
& .\mvnw.cmd clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "빌드 실패!" -ForegroundColor Red
    exit 1
}

# 3. JAR 파일 찾기
$jarFile = Get-ChildItem -Path "target" -Filter "*.jar" -Exclude "*-sources.jar","*-javadoc.jar" | Select-Object -First 1
if (-not $jarFile) {
    Write-Host "JAR 파일을 찾을 수 없습니다." -ForegroundColor Red
    exit 1
}

# 4. 서버 시작
Write-Host "서버 시작 중..." -ForegroundColor Cyan
$env:SPRING_PROFILES_ACTIVE = $Profile
Start-Process -FilePath "java" -ArgumentList "-jar", "`"$($jarFile.FullName)`"" -WorkingDirectory $scriptPath -WindowStyle Hidden

Start-Sleep -Seconds 5
Write-Host "서버 시작 완료! 포트: $port" -ForegroundColor Green

