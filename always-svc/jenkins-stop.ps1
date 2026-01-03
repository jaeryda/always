# Jenkins용 Spring Boot 종료 스크립트

Write-Host "=== Always 서버 종료 시작 ===" -ForegroundColor Cyan

$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
$port = 8089

# PID 파일이 있으면 사용
$pidFile = Join-Path $scriptPath "always-server.pid"
if (Test-Path $pidFile) {
    $pid = Get-Content $pidFile
    $process = Get-Process -Id $pid -ErrorAction SilentlyContinue
    if ($process) {
        Write-Host "프로세스 종료: PID $pid" -ForegroundColor Yellow
        Stop-Process -Id $pid -Force
        Remove-Item $pidFile -Force
        Write-Host "서버 종료 완료" -ForegroundColor Green
        exit 0
    }
}

# PID 파일이 없거나 프로세스가 없으면 포트로 찾기
Write-Host "포트 $port 사용 중인 프로세스 검색 중..." -ForegroundColor Yellow
$processes = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique

if ($processes) {
    foreach ($pid in $processes) {
        $process = Get-Process -Id $pid -ErrorAction SilentlyContinue
        if ($process -and $process.ProcessName -eq "java") {
            Write-Host "프로세스 종료: PID $pid ($($process.ProcessName))" -ForegroundColor Yellow
            Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
        }
    }
    Write-Host "서버 종료 완료" -ForegroundColor Green
} else {
    Write-Host "실행 중인 서버가 없습니다." -ForegroundColor Yellow
}

Write-Host "=== 종료 완료 ===" -ForegroundColor Green

