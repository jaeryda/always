# MySQL 프로파일로 Spring Boot 실행 스크립트

Write-Host "Spring Boot 서버 시작 중 (MySQL 프로파일)..." -ForegroundColor Cyan

# JAVA_HOME이 설정되어 있지 않으면 자동으로 찾아서 설정
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
        Write-Host "Java가 설치되어 있는지 확인하세요: java -version" -ForegroundColor Yellow
        exit 1
    }
} else {
    Write-Host "JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Green
}

# 프로파일을 환경 변수로 설정
$env:SPRING_PROFILES_ACTIVE = "mysql"

# Maven Wrapper 실행
Write-Host "`nMaven Wrapper로 Spring Boot 시작 (MySQL 프로파일)..." -ForegroundColor Cyan
.\mvnw.cmd spring-boot:run

