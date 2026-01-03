# JAVA_HOME 영구 설정 스크립트
# ⚠️ 관리자 권한으로 실행해야 합니다!

Write-Host "JAVA_HOME 영구 설정 스크립트" -ForegroundColor Cyan
Write-Host "⚠️  이 스크립트는 관리자 권한이 필요합니다!`n" -ForegroundColor Yellow

# 관리자 권한 확인
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if (-not $isAdmin) {
    Write-Host "❌ 관리자 권한이 필요합니다!" -ForegroundColor Red
    Write-Host "`n다음 중 하나를 선택하세요:" -ForegroundColor Yellow
    Write-Host "1. PowerShell을 관리자 권한으로 실행하고 이 스크립트를 다시 실행" -ForegroundColor White
    Write-Host "2. 아래 수동 설정 방법을 따라하세요`n" -ForegroundColor White
    Write-Host "수동 설정 방법:" -ForegroundColor Cyan
    Write-Host "1. Windows 키 + '환경 변수' 검색" -ForegroundColor White
    Write-Host "2. '시스템 환경 변수 편집' 선택" -ForegroundColor White
    Write-Host "3. '환경 변수' 버튼 클릭" -ForegroundColor White
    Write-Host "4. '시스템 변수'에서 '새로 만들기'" -ForegroundColor White
    Write-Host "5. 변수 이름: JAVA_HOME" -ForegroundColor White
    Write-Host "6. 변수 값: (아래에서 찾은 경로 입력)`n" -ForegroundColor White
    exit 1
}

Write-Host "Java 설치 경로 찾는 중..." -ForegroundColor Yellow

# Java 실행 파일 경로 찾기
try {
    $javaPath = (Get-Command java -ErrorAction Stop).Source
    Write-Host "Java 실행 파일: $javaPath" -ForegroundColor Green
    
    # JAVA_HOME 경로 찾기 (bin의 상위 디렉토리)
    $javaBinDir = Split-Path $javaPath -Parent
    $javaHome = Split-Path $javaBinDir -Parent
    
    Write-Host "`n찾은 JAVA_HOME 경로: $javaHome" -ForegroundColor Cyan
    
    # 시스템 환경 변수에 영구 설정
    [Environment]::SetEnvironmentVariable('JAVA_HOME', $javaHome, 'Machine')
    
    Write-Host "`n✅ JAVA_HOME이 시스템 환경 변수에 설정되었습니다!" -ForegroundColor Green
    Write-Host "   JAVA_HOME = $javaHome" -ForegroundColor Yellow
    
    Write-Host "`n⚠️  새로운 터미널을 열어야 변경사항이 적용됩니다." -ForegroundColor Yellow
    
} catch {
    Write-Host "❌ Java를 찾을 수 없습니다. Java가 설치되어 있는지 확인하세요." -ForegroundColor Red
    Write-Host "   https://www.microsoft.com/openjdk 에서 Java를 다운로드하세요." -ForegroundColor Yellow
}



