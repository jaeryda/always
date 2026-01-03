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
   - Repository URL: Git 저장소 URL (또는 로컬 경로)
   - Script Path: `Jenkinsfile` (기본값)

### 방법 2: Freestyle Project (GUI 설정)

1. **새 Item** 클릭
2. **Freestyle project** 선택
3. 이름 입력 (예: `always-deploy`)

#### 소스 코드 관리
- Git 선택
- Repository URL 입력

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

## 7. 포트 확인

서버가 정상적으로 시작되었는지 확인:
```powershell
netstat -ano | findstr :8089
```

또는 브라우저에서:
```
http://localhost:8089/api/hello
```

## 8. 자동 배포 설정 (선택사항)

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

### 빌드 실패
- Jenkins 콘솔 출력 확인
- Maven Wrapper 권한 확인
- 디스크 공간 확인

## 참고 사항

- 서버 재기동 시 약간의 다운타임이 발생할 수 있습니다
- 프로덕션 환경에서는 무중단 배포를 고려해야 합니다 (예: Blue-Green 배포)
- 로그 파일 관리 정책 설정 권장

