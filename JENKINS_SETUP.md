# Jenkins 설정 가이드

## 1. Jenkins 설치 및 기본 설정

Jenkins가 설치되어 있다고 가정합니다. (설치 가이드는 생략)

## 2. Freestyle Project 생성

1. Jenkins 메인 페이지에서 **"New Item"** 클릭
2. 프로젝트 이름 입력 (예: `always-deploy`)
3. **"Freestyle project"** 선택
4. **"OK"** 클릭

## 3. Git 저장소 연결

1. **Source Code Management** 섹션에서 **"Git"** 선택
2. **Repository URL** 입력: `https://github.com/jaeryda/always.git`
3. **Branch Specifier** 입력: `*/main` ⚠️ `master`가 아닌 `main`입니다!

## 4. 빌드 단계 설정

### 빌드 1: 환경 변수 설정 (새로 추가!)
- **Add build step** → **Execute Windows batch command**
- 가장 첫 번째 단계로 추가

**로컬 DB 사용 (권장 - 네트워크 오류 방지):**
```
set OPENAI_API_KEY=your-openai-api-key-here
set "DATABASE_URL=jdbc:mysql://localhost:3306/always_db?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8&allowPublicKeyRetrieval=true"
set DATABASE_USERNAME=root
set DATABASE_PASSWORD=your_password
```

**원격 DB 사용 (192.168.75.99 접근 가능한 경우):**
```
set OPENAI_API_KEY=your-openai-api-key-here
set "DATABASE_URL=jdbc:mysql://192.168.75.99:3306/always_db?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8&allowPublicKeyRetrieval=true"
set DATABASE_USERNAME=root
set DATABASE_PASSWORD=1234
```

⚠️ **중요**: `DATABASE_URL`은 따옴표(`"`)로 감싸야 합니다! `&` 문자가 배치 파일에서 명령어 구분자로 인식되는 것을 방지하기 위함입니다.

⚠️ **네트워크 오류 발생 시**: `localhost`를 사용하도록 변경하세요!

### 빌드 2: Maven 빌드
- **Add build step** → **Execute Windows batch command**
```
cd always-svc
call mvnw.cmd clean package -DskipTests
```

### 빌드 3: 서버 종료
- **Add build step** → **Execute Windows batch command**
```
cd always-svc
powershell -ExecutionPolicy Bypass -File jenkins-stop.ps1
```

### 빌드 4: 서버 시작
- **Add build step** → **Execute Windows batch command**
```
cd always-svc
powershell -ExecutionPolicy Bypass -File jenkins-restart.ps1 -Profile mysql
```

⚠️ **중요**: Jenkins 빌드가 완료되어도 서버가 계속 실행되도록 `Start-Process`로 백그라운드에서 실행합니다.
- 서버는 빌드 완료 후에도 계속 실행됩니다
- 서버를 종료하려면 `jenkins-stop.ps1`을 별도로 실행하세요

⚠️ **중요**: 환경 변수 설정 단계는 **가장 첫 번째** 빌드 단계여야 합니다!

## 4. 수동 실행 스크립트 방식 (가장 간단)

Jenkins에서 **Execute Windows batch command** 빌드 단계를 추가하고 다음 명령 실행:

```batch
cd always-svc
call mvnw.cmd clean package -DskipTests
powershell -ExecutionPolicy Bypass -File jenkins-restart.ps1 -Profile mysql
```

## 5. 재기동 스크립트 사용법

### jenkins-restart.ps1
- 기존 서버를 종료하고 새로 시작
- Maven 빌드 포함
- 사용법:
  ```powershell
  .\jenkins-restart.ps1 -Profile mysql
  ```

### jenkins-stop.ps1
- 실행 중인 서버를 종료
- 사용법:
  ```powershell
  .\jenkins-stop.ps1
  ```

## 6. 로그 확인

### Jenkins 빌드 실시간 로그 확인 ⭐

Jenkins에서 빌드를 실행하면 **실시간으로 로그를 확인**할 수 있습니다:

1. **빌드 실행 중 로그 보기**
   - Jenkins Job 페이지에서 **"Build Now"** 클릭
   - 빌드가 시작되면 **"Console Output"** 클릭
   - 또는 빌드 히스토리에서 최신 빌드 클릭 → **"Console Output"** 클릭

2. **이미 완료된 빌드 로그 보기**
   - 빌드 히스토리에서 원하는 빌드 번호 클릭
   - **"Console Output"** 클릭

**로그에서 확인할 내용:**
- ✅ 빌드 성공: `BUILD SUCCESS`
- ✅ 서버 시작: `서버가 정상적으로 시작되었습니다!`
- ✅ 포트 확인: `포트: 8089`
- ❌ 빌드 실패: `BUILD FAILURE` (오류 메시지 확인)
- ❌ 서버 시작 실패: `서버 시작 실패!` (로그 파일 확인)

### 서버가 시작 후 종료되는 문제

로그에서 서버가 시작되었다고 나오지만 실제로는 종료된 경우:

**증상:**
- Jenkins 빌드 로그: "서버가 정상적으로 시작되었습니다!"
- 하지만 `http://192.168.75.99:8089/api/hello` 호출 시 응답 없음
- `stdout.log`에서 `Shutdown initiated...` 확인됨

**원인:**
- Jenkins 빌드가 종료되면 PowerShell 스크립트도 종료되고, 그 과정에서 Java 프로세스도 종료될 수 있습니다.
- `Start-Process`로 시작한 프로세스는 일반적으로 부모 프로세스와 분리되어야 하지만, Jenkins 빌드 환경에서는 다르게 동작할 수 있습니다.

**해결 방법:**

1. **Jenkins Job 설정에서 "Execute Windows batch command" 옵션 확인:**
   - 빌드 단계에서 **"Advanced..."** 버튼 클릭
   - "Exit code to set build status" 옵션 확인
   - 기본값 그대로 사용 (빌드 실패 시에만 종료)

2. **서버 상태 확인:**
   ```powershell
   cd C:\Users\jy_kim\.jenkins\workspace\always-deploy\always-svc
   powershell -ExecutionPolicy Bypass -File check-server.ps1
   ```

3. **서버를 수동으로 시작 (테스트용):**
   Jenkins 워크스페이스에서 직접 실행:
   ```powershell
   cd C:\Users\jy_kim\.jenkins\workspace\always-deploy\always-svc
   powershell -ExecutionPolicy Bypass -File jenkins-restart.ps1 -Profile mysql
   ```
   
   서버가 계속 실행되는지 확인:
   ```powershell
   # 다른 PowerShell 창에서
   Get-Process java -ErrorAction SilentlyContinue
   netstat -ano | findstr :8089
   ```

4. **Windows 서비스로 실행 (권장 - 프로덕션 환경):**
   - NSSM (Non-Sucking Service Manager) 사용
   - 또는 Windows Task Scheduler 사용
   - 자세한 내용은 프로덕션 배포 가이드 참고

빌드가 성공했지만 DB 연결 여부를 확인하려면:

**방법 1: 애플리케이션 로그 파일 확인** ⭐

Jenkins 서버에서 로그 파일 확인:
```powershell
# 로그 파일 위치로 이동
cd C:\Users\jy_kim\.jenkins\workspace\always-deploy\always-svc\logs

# 로그 파일 내용 확인 (최근 50줄)
Get-Content application.log -Tail 50

# 또는 실시간 로그 확인 (Ctrl+C로 종료)
Get-Content application.log -Wait -Tail 20
```

로그에서 확인할 내용:
- ✅ DB 연결 성공: "HikariPool-1 - Starting..." 또는 "Started AlwaysApplication"
- ❌ DB 연결 실패: "Communications link failure" 또는 "Access denied"

**방법 2: 빌드 로그에서 확인**

- 빌드가 "SUCCESS"이고 서버가 시작되었다는 메시지가 있으면 DB 연결이 성공했을 가능성이 높습니다
- Spring Boot는 DB 연결 실패 시 애플리케이션이 시작되지 않거나 오류가 발생합니다
- 확실하게 확인하려면 방법 1(로그 파일 확인)을 권장합니다

## 7. 환경 변수 설정 (필수)

OpenAI API 기능을 사용하려면 Jenkins에서 환경 변수를 설정해야 합니다.

### 방법 1: Jenkins Credentials (권장 - 가장 안전)

1. **Jenkins 메인 페이지** → **"Manage Jenkins"** → **"Credentials"**
2. **"System"** → **"Global credentials"** → **"Add Credentials"**
3. 설정:
   - **Kind**: `Secret text`
   - **Secret**: OpenAI API 키 입력
   - **ID**: `openai-api-key` (또는 원하는 이름)
   - **Description**: `OpenAI API Key`
4. **"OK"** 클릭

5. **Pipeline Job인 경우**: `Jenkinsfile`의 `environment` 섹션에 추가:
   ```groovy
   environment {
       OPENAI_API_KEY = credentials('openai-api-key')
   }
   ```

6. **Freestyle Job인 경우**: 빌드 단계에서 환경 변수로 설정:
   ```batch
   set OPENAI_API_KEY=your-openai-api-key-here
   ```

### 방법 2: Jenkins Job 설정 (Freestyle Project)

1. Job 설정 페이지로 이동
2. **"Build Environment"** 섹션 찾기
3. **"Inject environment variables to the build process"** 체크
4. **"Properties Content"**에 추가:
   ```
   OPENAI_API_KEY=your-openai-api-key-here
   DATABASE_URL=jdbc:mysql://localhost:3306/always_db?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
   DATABASE_USERNAME=root
   DATABASE_PASSWORD=your_password
   ```

⚠️ **보안 참고사항:**
- API 키를 코드에 직접 하드코딩하지 마세요
- Jenkins Credentials를 사용하는 것이 가장 안전합니다
- 프로덕션 환경에서는 반드시 Credentials를 사용하세요

### 방법 3: 시스템 환경 변수 (Windows)

Jenkins 서버 시스템 환경 변수로 설정:
```powershell
[System.Environment]::SetEnvironmentVariable("OPENAI_API_KEY", "your-api-key-here", [System.EnvironmentVariableTarget]::Machine)
```

⚠️ **보안 참고사항:**
- API 키를 코드에 직접 하드코딩하지 마세요
- Jenkins Credentials를 사용하는 것이 가장 안전합니다
- 프로덕션 환경에서는 반드시 Credentials를 사용하세요

### 데이터베이스 연결 문제

**"DB 호출이 안 되는 경우":**

1. **네트워크 연결 확인**

   **방법 1: PowerShell Test-NetConnection (권장)** ⭐
   
   **Jenkins 서버에서 PowerShell 열기:**
   - Windows 키 누르기
   - "PowerShell" 또는 "Windows PowerShell" 검색
   - "Windows PowerShell" 클릭하여 실행
   - 또는 Windows 키 + X → "Windows PowerShell" 또는 "터미널" 선택
   
   PowerShell을 열고 다음 명령어 실행:
   ```powershell
   Test-NetConnection -ComputerName 192.168.75.99 -Port 3306
   ```
   
   결과 확인:
   - `TcpTestSucceeded: True` → 네트워크 연결 정상 ✅
   - `TcpTestSucceeded: False` → 네트워크 연결 불가 ❌ (방화벽 또는 네트워크 문제)
   
   **방법 2: telnet 사용**
   
   ```powershell
   telnet 192.168.75.99 3306
   ```
   - 연결 성공: 빈 화면이 나타나면 연결 성공 (Ctrl+C로 종료)
   - 연결 실패: "연결할 수 없습니다" 오류 메시지
   
   ⚠️ telnet이 설치되어 있지 않은 경우:
   - Windows 기능에서 "Telnet 클라이언트" 활성화 필요
   - 또는 방법 1 사용 권장
   
   **방법 3: MySQL 클라이언트 사용 (가장 정확)**
   
   ```powershell
   mysql -h 192.168.75.99 -u root -p
   ```
   - 연결 성공: MySQL 프롬프트 표시
   - 연결 실패: 오류 메시지 표시

2. **방화벽 확인**
   
   - Windows 방화벽에서 3306 포트가 열려있는지 확인
   - 또는 MySQL 서버의 방화벽 설정 확인

3. **Jenkins Job 설정에서 환경 변수 설정**

   **Freestyle Job인 경우:**
   - Job 설정 → **Build Environment** 섹션 → **"Inject environment variables to the build process"** 체크
   - **Properties Content**에 추가:
     ```
     DATABASE_URL=jdbc:mysql://localhost:3306/always_db?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
     DATABASE_USERNAME=root
     DATABASE_PASSWORD=your_password
     ```
   
   **Pipeline Job인 경우:**
   - `Jenkinsfile`의 `environment` 섹션에 추가:
     ```groovy
     environment {
         DATABASE_URL = 'jdbc:mysql://localhost:3306/always_db?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8&allowPublicKeyRetrieval=true'
         DATABASE_USERNAME = 'root'
         DATABASE_PASSWORD = 'your_password'
     }
     ```

3. **빌드 단계에서 환경 변수 설정 (대안)**
   
   Jenkins Job 설정 → **빌드(Build)** 섹션 → **빌드 단계 추가(Add build step)** → **Execute Windows batch command**
   
   ```batch
   set "DATABASE_URL=jdbc:mysql://localhost:3306/always_db?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8&allowPublicKeyRetrieval=true"
   set DATABASE_USERNAME=root
   set DATABASE_PASSWORD=your_password
   cd always-svc
   powershell -ExecutionPolicy Bypass -File jenkins-restart.ps1 -Profile mysql
   ```

4. **MySQL 서버 상태 확인**
   - MySQL 서버가 실행 중인지 확인
   - 방화벽에서 3306 포트가 열려있는지 확인

## 8. 포트 확인

서버가 실행 중인지 확인:

```powershell
netstat -ano | findstr :8089
```

포트가 LISTENING 상태이면 서버가 실행 중입니다.

## 9. 문제 해결

### 빌드 실패
- Jenkins 콘솔 출력 확인
- Maven Wrapper 권한 확인
- 디스크 공간 확인
- JAR 파일을 찾을 수 없는 경우: `target` 디렉토리가 생성되었는지 확인

## 참고 사항

- 서버 재기동 시 약간의 다운타임이 발생할 수 있습니다
- 프로덕션 환경에서는 무중단 배포를 고려해야 합니다 (예: Blue-Green 배포)
- 로그 파일 관리 정책 설정 권장

## 프론트엔드-백엔드 연결 문제 해결

### 웹에서 API 호출 시 네트워크 오류 발생

**증상**: 프론트엔드(Vue)에서 API 호출 시 네트워크 오류 또는 CORS 오류

**확인 사항:**

1. **프론트엔드 API URL 설정 확인**
   
   `src/config/index.ts` 파일 확인:
   ```typescript
   export const apiBaseURL = 'http://192.168.75.99:8089/api'
   export const imageBaseURL = 'http://192.168.75.99:8089'
   ```
   
   ⚠️ 포트 번호 확인:
   - 프론트엔드: `8088` (vue.config.js에서 설정)
   - 백엔드: `8089` (application.properties에서 설정)

2. **백엔드 서버 실행 확인**
   
   Jenkins 서버에서 백엔드가 실행 중인지 확인:
   ```powershell
   netstat -ano | findstr :8089
   ```
   
   또는 브라우저에서:
   ```
   http://192.168.75.99:8089/api/hello
   ```

3. **CORS 설정 확인**
   
   `WebConfig.java`에서 프론트엔드 포트(`8088`)가 허용되어 있는지 확인:
   ```java
   .allowedOrigins("http://localhost:8088", "http://192.168.75.99:8088")
   ```

4. **프론트엔드 개발 서버 재시작**
   
   API URL을 변경했다면 프론트엔드 서버를 재시작:
   ```powershell
   # 프론트엔드 디렉토리에서
   npm run serve
   ```

5. **브라우저 캐시 삭제**
   
   브라우저 개발자 도구(F12) → Network 탭에서 "Disable cache" 체크
   또는 Ctrl + Shift + R (강력 새로고침)

**서버 상태 확인:**

Jenkins 워크스페이스에서 서버 상태를 확인할 수 있습니다:

```powershell
cd always-svc
powershell -ExecutionPolicy Bypass -File check-server.ps1
```

이 스크립트는 다음을 확인합니다:
- 서버 프로세스 실행 여부
- 포트 8089 LISTENING 상태
- API 응답 테스트
- 로그 파일 내용 (stdout.log, stderr.log, application.log)

**일반적인 문제:**

- ❌ API URL이 `8081`로 설정되어 있음 → `8089`로 변경
- ❌ 백엔드 서버가 실행되지 않음 → Jenkins 빌드 확인
- ❌ CORS 오류 → `WebConfig.java`의 `allowedOrigins` 확인
- ❌ 프론트엔드 서버가 다른 포트에서 실행 중 → `vue.config.js` 확인
- ❌ 서버가 시작 후 종료됨 → `check-server.ps1`로 로그 확인
