# Spring Boot 실행 스크립트 (JAVA_HOME 자동 설정)

Write-Host "Spring Boot 서버 시작 중..." -ForegroundColor Cyan

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

# Maven Wrapper 실행
Write-Host "`nMaven Wrapper로 Spring Boot 시작..." -ForegroundColor Cyan

# 프로파일이 지정되었는지 확인
if ($args -contains "-Dspring-boot.run.profiles=mysql") {
    .\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mysql
} else {
    .\mvnw.cmd spring-boot:run
}



