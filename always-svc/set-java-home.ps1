# JAVA_HOME 설정 스크립트
# 이 스크립트는 현재 세션에만 JAVA_HOME을 설정합니다 (임시)

Write-Host "Java 설치 경로 찾는 중..." -ForegroundColor Yellow

# Java 실행 파일 경로 찾기
try {
    $javaPath = (Get-Command java -ErrorAction Stop).Source
    Write-Host "Java 실행 파일: $javaPath" -ForegroundColor Green
    
    # JAVA_HOME 경로 찾기 (bin의 상위 디렉토리)
    $javaBinDir = Split-Path $javaPath -Parent
    $javaHome = Split-Path $javaBinDir -Parent
    
    Write-Host "`n찾은 JAVA_HOME 경로: $javaHome" -ForegroundColor Cyan
    
    # 현재 세션에만 설정 (임시)
    $env:JAVA_HOME = $javaHome
    
    Write-Host "`nJAVA_HOME이 현재 세션에 설정되었습니다!" -ForegroundColor Green
    Write-Host "JAVA_HOME = $env:JAVA_HOME" -ForegroundColor Yellow
    
    Write-Host "`n⚠️  이 설정은 현재 PowerShell 세션에만 유효합니다." -ForegroundColor Yellow
    Write-Host "영구적으로 설정하려면 아래 방법을 사용하세요:" -ForegroundColor Yellow
    Write-Host "1. 시스템 환경 변수에서 JAVA_HOME 설정" -ForegroundColor White
    Write-Host "2. 또는 아래 명령어를 관리자 권한으로 실행:" -ForegroundColor White
    Write-Host "   [Environment]::SetEnvironmentVariable('JAVA_HOME', '$javaHome', 'Machine')" -ForegroundColor Gray
    
} catch {
    Write-Host "❌ Java를 찾을 수 없습니다. Java가 설치되어 있는지 확인하세요." -ForegroundColor Red
    Write-Host "   https://www.microsoft.com/openjdk 에서 Java를 다운로드하세요." -ForegroundColor Yellow
}


