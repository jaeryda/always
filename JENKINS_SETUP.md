# Jenkins 설정 가이드

이 문서는 Always 프로젝트를 Jenkins로 재기동하는 방법을 설명합니다.

## 1. Jenkins 플러그인 설치 (필수)

Jenkins 관리 → 플러그인 관리에서 다음 플러그인을 설치하세요:

- **Git Plugin** (이미 설치되어 있을 수 있음)
- **Pipeline Plugin** (이미 설치되어 있을 수 있음)
- **Maven Integration Plugin** (선택사항, Maven 도구 설정용)

## 2. JDK 도구 설정

1. **Jenkins 관리** → **도구 설정 (Global Tool Configuration)**
2. **JDK** 섹션에서:
   - **JDK installations...** 클릭
   - **Add JDK** 클릭
   - Name: `JDK-17` (Jenkinsfile과 동일하게)
   - JAVA_HOME: Java 17 설치 경로 (예: `C:\Program Files\Java\jdk-17`)
   - 또는 **Install automatically** 선택

## 3. Jenkins Pipeline Job 생성

### 방법 1: Pipeline Job (Jenkinsfile 사용) - 추천

1. **새 Item** 클릭
2. **Pipeline** 선택
3. 이름 입력 (예: `always-deploy`)
4. **Pipeline** 섹션에서:
   - Definition: **Pipeline script from SCM**
   - SCM: **Git**
   - Repository URL: `https://github.com/jaeryda/always.git` (GitHub 저장소 URL)
   - **Branches to build**: `*/main` 또는 `main` (기본값이 `*/master`인 경우 변경 필요)
   - Script Path: `Jenkinsfile` (기본값)

### 방법 2: Freestyle Project (GUI 설정)

1. **새 Item** 클릭
2. **Freestyle project** 선택
3. 이름 입력 (예: `always-deploy`)

#### 소스 코드 관리
- Git 선택
- Repository URL: `https://github.com/jaeryda/always.git`
- **Branch Specifier**: `*/main` 또는 `main` (⚠️ 기본값 `*/master`에서 변경 필수!)

#### 빌드 단계 추가

**빌드 1: Maven 빌드**
- **Add build step** → **Execute Windows batch command**
```
cd always-svc
call mvnw.cmd clean package -DskipTests
```

**빌드 2: 서버 종료**
- **Add build step** → **Execute Windows batch command**
```
cd always-svc
powershell -ExecutionPolicy Bypass -File jenkins-stop.ps1
```

**빌드 3: 서버 시작**
- **Add build step** → **Execute Windows batch command**
```
cd always-svc
powershell -ExecutionPolicy Bypass -File jenkins-restart.ps1 -Profile mysql
```

## 4. 수동 실행 스크립트 방식 (가장 간단)

Jenkins에서 **Execute Windows batch command** 빌드 단계를 추가하고 다음 명령 실행:

```batch
cd always-svc
call mvnw.cmd clean package -DskipTests
powershell -ExecutionPolicy Bypass -File jenkins-restart.ps1 -Profile mysql
```

## 5. 재기동 스크립트 사용법

### jenkins-restart.ps1
- 기존 프로세스를 종료하고 새로 빌드 후 시작
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

서버 실행 후 로그는 다음 위치에서 확인할 수 있습니다:
- 콘솔: Jenkins 빌드 콘솔 출력
- 파일: `always-svc/logs/application.log` (생성되는 경우)

## 7. 환경 변수 설정 (필수)

OpenAI API 기능을 사용하려면 Jenkins에서 환경 변수를 설정해야 합니다.

### 방법 1: Jenkins Credentials 사용 (권장)

1. **Jenkins 관리** → **Credentials** → **System** → **Global credentials**
2. **Add Credentials** 클릭
3. 설정:
   - **Kind**: `Secret text`
   - **Secret**: OpenAI API 키 입력
   - **ID**: `openai-api-key` (Jenkinsfile과 동일하게)
   - **Description**: `OpenAI API Key`
4. **OK** 클릭

### 방법 2: Jenkins Job 환경 변수 설정

**Pipeline Job:**
- Jenkinsfile의 `environment` 섹션에 직접 추가:
  ```groovy
  environment {
      OPENAI_API_KEY = 'your-api-key-here'  // 또는 credentials('openai-api-key')
  }
  ```

**Freestyle Project:**
1. Job 설정 → **Build Environment**
2. **Use secret text(s) or file(s)** 체크 (Credentials Binding 플러그인 필요)
3. **Bindings** → **Add** → **Secret text**
4. **Variable**: `OPENAI_API_KEY`
5. **Credentials**: 생성한 Credentials 선택

**또는 빌드 단계에서 직접 설정:**
```batch
set OPENAI_API_KEY=your-api-key-here
powershell -ExecutionPolicy Bypass -File jenkins-restart.ps1 -Profile mysql
```

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
   - Jenkins 서버에서 DB 서버로 접근 가능한지 확인:
     ```powershell
     Test-NetConnection -ComputerName 192.168.75.207 -Port 3306
     ```
   - 또는:
     ```powershell
     telnet 192.168.75.207 3306
     ```

2. **DB URL 환경 변수로 변경 (Freestyle Project)**

   **단계별 가이드:**
   
   1. Jenkins 대시보드에서 **`always-deploy`** Job 클릭
   2. 왼쪽 메뉴에서 **"구성(Configure)"** 클릭
   3. 아래로 스크롤하여 **"빌드 환경(Build Environment)"** 섹션 찾기
   4. **"빌드 프로세스에 환경 변수 삽입(Inject environment variables to the build process)"** 체크박스 체크
   5. **"Properties Content"** 텍스트 영역에 다음 내용 입력:
      ```properties
      DATABASE_URL=jdbc:mysql://localhost:3306/always_db?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
      DATABASE_USERNAME=root
      DATABASE_PASSWORD=your_password
      ```
   - 또는 원격 DB를 사용하는 경우 (접근 가능한 경우):
     ```properties
     DATABASE_URL=jdbc:mysql://192.168.75.207:3306/always_db?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
     DATABASE_USERNAME=root
     DATABASE_PASSWORD=1234
     ```
   6. 맨 아래 **"저장(Save)"** 버튼 클릭

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
   set DATABASE_URL=jdbc:mysql://localhost:3306/always_db?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
   set DATABASE_USERNAME=root
   set DATABASE_PASSWORD=your_password
   cd always-svc
   powershell -ExecutionPolicy Bypass -File jenkins-restart.ps1 -Profile mysql
   ```

4. **MySQL 서버 상태 확인**
   - MySQL 서버가 실행 중인지 확인
   - 방화벽에서 3306 포트가 열려있는지 확인

## 8. 포트 확인

서버가 정상적으로 시작되었는지 확인:
```powershell
netstat -ano | findstr :8089
```

또는 브라우저에서:
```
http://localhost:8089/api/hello
```

## 9. 자동 배포 설정 (선택사항)

### Git Webhook 설정
1. Git 저장소에서 Webhook 추가
2. Payload URL: `http://jenkins-server:포트/github-webhook/`
3. Content type: `application/json`
4. Push 이벤트 선택

### Jenkins 빌드 트리거
1. Job 설정 → **Build Triggers**
2. **GitHub hook trigger for GITScm polling** 선택 (GitHub 플러그인 설치 필요)
3. 또는 **Poll SCM** 선택하여 주기적으로 체크

## 문제 해결

### "Couldn't find any revision to build" 오류

Jenkins가 `master` 브랜치를 찾지 못하는 경우:

**원인**: Jenkins Job 설정에서 브랜치가 `master`로 설정되어 있는데, 실제 Git 저장소는 `main` 브랜치를 사용하고 있습니다.

**해결 방법**:
1. Jenkins Job 설정 페이지로 이동 (`always-deploy` Job → **Configure**)
2. **Pipeline Job**인 경우:
   - **Pipeline** 섹션 → **Branches to build** → `*/main` 또는 `main`으로 변경 (기본값: `*/master`)
3. **Freestyle Project**인 경우:
   - **소스 코드 관리** → **Branch Specifier** → `*/main` 또는 `main`으로 변경 (기본값: `*/master`)
4. **저장** 클릭
5. 다시 빌드 실행

⚠️ **주의**: GitHub의 기본 브랜치가 `main`으로 변경되었으므로, Jenkins Job도 `main` 브랜치를 사용하도록 설정해야 합니다.

### PowerShell 실행 정책 오류
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### 포트가 이미 사용 중
```powershell
# 프로세스 확인
netstat -ano | findstr :8089

# 프로세스 강제 종료
taskkill /PID <PID번호> /F
```

### Java를 찾을 수 없음
- 시스템 환경 변수에 JAVA_HOME 설정 확인
- 또는 Jenkins 도구 설정에서 JDK 경로 확인

### "PID 변수는 읽기 전용이거나 상수이므로 덮어쓸 수 없습니다" 오류

**원인**: PowerShell의 `$pid`는 현재 프로세스 ID를 나타내는 읽기 전용 자동 변수입니다.

**해결**: 최신 버전의 스크립트를 사용하세요. `$pid` 변수가 `$processId`로 변경되었습니다.

### 빌드 실패
- Jenkins 콘솔 출력 확인
- Maven Wrapper 권한 확인
- 디스크 공간 확인
- JAR 파일을 찾을 수 없는 경우: `target` 디렉토리가 생성되었는지 확인

## 참고 사항

- 서버 재기동 시 약간의 다운타임이 발생할 수 있습니다
- 프로덕션 환경에서는 무중단 배포를 고려해야 합니다 (예: Blue-Green 배포)
- 로그 파일 관리 정책 설정 권장

