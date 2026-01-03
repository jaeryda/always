# 서버 상태 확인 스크립트

Write-Host "=== Always 서버 상태 확인 ===" -ForegroundColor Cyan

$port = 8089
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
$logDir = Join-Path $scriptPath "logs"
$stdoutFile = Join-Path $logDir "stdout.log"
$stderrFile = Join-Path $logDir "stderr.log"
$logFile = Join-Path $logDir "application.log"
$pidFile = Join-Path $scriptPath "always-server.pid"

Write-Host "`n1. 프로세스 확인:" -ForegroundColor Yellow
if (Test-Path $pidFile) {
    $pid = Get-Content $pidFile -ErrorAction SilentlyContinue
    if ($pid) {
        Write-Host "   PID 파일에서 읽은 PID: $pid" -ForegroundColor Gray
        $process = Get-Process -Id $pid -ErrorAction SilentlyContinue
        if ($process) {
            Write-Host "   ✅ 프로세스 실행 중 (PID: $pid, 이름: $($process.ProcessName))" -ForegroundColor Green
        } else {
            Write-Host "   ❌ 프로세스가 실행 중이지 않습니다 (PID: $pid)" -ForegroundColor Red
        }
    }
} else {
    Write-Host "   PID 파일이 없습니다: $pidFile" -ForegroundColor Gray
}

# Java 프로세스 확인
Write-Host "`n2. Java 프로세스 확인:" -ForegroundColor Yellow
$javaProcesses = Get-Process java -ErrorAction SilentlyContinue
if ($javaProcesses) {
    Write-Host "   실행 중인 Java 프로세스:" -ForegroundColor Gray
    $javaProcesses | ForEach-Object {
        Write-Host "   - PID: $($_.Id), 메모리: $([math]::Round($_.WorkingSet64 / 1MB, 2)) MB" -ForegroundColor Gray
    }
} else {
    Write-Host "   ❌ 실행 중인 Java 프로세스가 없습니다" -ForegroundColor Red
}

Write-Host "`n3. 포트 $port 확인:" -ForegroundColor Yellow
try {
    $connection = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    if ($connection) {
        Write-Host "   ✅ 포트 $port 가 LISTENING 상태입니다" -ForegroundColor Green
        Write-Host "   프로세스 ID: $($connection.OwningProcess)" -ForegroundColor Gray
    } else {
        Write-Host "   ❌ 포트 $port 가 열려있지 않습니다" -ForegroundColor Red
    }
} catch {
    Write-Host "   ⚠️  포트 확인 실패: $_" -ForegroundColor Yellow
}

Write-Host "`n4. API 테스트:" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:$port/api/hello" -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
    Write-Host "   ✅ API 응답 성공 (HTTP $($response.StatusCode))" -ForegroundColor Green
    Write-Host "   응답 내용:" -ForegroundColor Gray
    $response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10 | Write-Host -ForegroundColor Gray
} catch {
    Write-Host "   ❌ API 응답 실패: $_" -ForegroundColor Red
}

Write-Host "`n5. 로그 파일 확인:" -ForegroundColor Yellow

if (Test-Path $stdoutFile) {
    Write-Host "`n   표준 출력 로그 (stdout.log) - 마지막 20줄:" -ForegroundColor Cyan
    Get-Content $stdoutFile -Tail 20 -ErrorAction SilentlyContinue | ForEach-Object {
        Write-Host "   $_" -ForegroundColor Gray
    }
} else {
    Write-Host "   stdout.log 파일이 없습니다: $stdoutFile" -ForegroundColor Gray
}

if (Test-Path $stderrFile) {
    Write-Host "`n   표준 오류 로그 (stderr.log) - 마지막 20줄:" -ForegroundColor Cyan
    Get-Content $stderrFile -Tail 20 -ErrorAction SilentlyContinue | ForEach-Object {
        Write-Host "   $_" -ForegroundColor Red
    }
} else {
    Write-Host "   stderr.log 파일이 없습니다: $stderrFile" -ForegroundColor Gray
}

if (Test-Path $logFile) {
    Write-Host "`n   애플리케이션 로그 (application.log) - 마지막 20줄:" -ForegroundColor Cyan
    Get-Content $logFile -Tail 20 -ErrorAction SilentlyContinue | ForEach-Object {
        Write-Host "   $_" -ForegroundColor Gray
    }
} else {
    Write-Host "   application.log 파일이 없습니다: $logFile" -ForegroundColor Gray
}

Write-Host "`n=== 확인 완료 ===" -ForegroundColor Cyan

