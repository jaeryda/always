# JAVA_HOME 설정 가이드

Java가 설치되어 있지만 JAVA_HOME 환경 변수가 설정되지 않은 경우의 해결 방법입니다.

## 방법 1: PowerShell 스크립트 사용 (권장)

### 임시 설정 (현재 세션만)

```powershell
cd always-svc
.\set-java-home.ps1
```

이 설정은 현재 PowerShell 창에서만 유효합니다.

### 영구 설정 (시스템 전체)

1. **PowerShell을 관리자 권한으로 실행**
   - Windows 키 누르기
   - "PowerShell" 검색
   - "Windows PowerShell"에서 우클릭 → "관리자 권한으로 실행"

2. **스크립트 실행**
   ```powershell
   cd C:\workspace\always\always-svc
   .\set-java-home-permanent.ps1
   ```

3. **새 터미널 열기** (변경사항 적용을 위해)

## 방법 2: 수동 설정 (Windows)

1. **Java 설치 경로 확인**
   
   PowerShell에서 다음 명령어 실행:
   ```powershell
   $javaPath = (Get-Command java).Source
   $javaBinDir = Split-Path $javaPath -Parent
   $javaHome = Split-Path $javaBinDir -Parent
   Write-Host "JAVA_HOME = $javaHome"
   ```

2. **환경 변수 설정**
   - Windows 키 + "환경 변수" 검색
   - "시스템 환경 변수 편집" 선택
   - "환경 변수" 버튼 클릭
   - "시스템 변수" 섹션에서 "새로 만들기" 클릭
   - 변수 이름: `JAVA_HOME`
   - 변수 값: 위에서 찾은 경로 (예: `C:\Program Files\Java\jdk-17.0.17`)
   - "확인" 클릭

3. **Path에 추가 (선택사항)**
   - "시스템 변수"의 "Path" 선택 → "편집"
   - "새로 만들기" 클릭
   - `%JAVA_HOME%\bin` 추가
   - "확인" 클릭

4. **새 터미널 열기** (변경사항 적용)

## 방법 3: PowerShell에서 현재 세션에만 설정

```powershell
# Java 경로 찾기
$javaPath = (Get-Command java).Source
$javaBinDir = Split-Path $javaPath -Parent
$javaHome = Split-Path $javaBinDir -Parent

# 현재 세션에 설정
$env:JAVA_HOME = $javaHome

# 확인
echo $env:JAVA_HOME
```

## 설정 확인

새 터미널에서 확인:

```powershell
echo $env:JAVA_HOME
java -version
```

## 일반적인 Java 설치 경로

- Oracle JDK: `C:\Program Files\Java\jdk-17.x.x`
- Microsoft OpenJDK: `C:\Program Files\Microsoft\jdk-17.x.x`
- Eclipse Temurin: `C:\Program Files\Eclipse Adoptium\jdk-17.x.x`

## 문제 해결

### "Java를 찾을 수 없습니다" 오류
- Java가 제대로 설치되어 있는지 확인: `java -version`
- 위의 스크립트로 Java 경로를 다시 확인

### "관리자 권한이 필요합니다" 오류
- PowerShell을 관리자 권한으로 실행
- 또는 방법 2(수동 설정) 사용

### 변경사항이 적용되지 않음
- 모든 터미널 창을 닫고 새로 열기
- 컴퓨터 재시작 (가장 확실한 방법)



