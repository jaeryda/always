# Always REST API Server

Spring Boot 기반 REST API 서버입니다.

## 기술 스택

- Java 17 이상
- Spring Boot 3.2.0
- Spring Data JPA
- H2 Database (개발용)
- Maven

## Java 설치가 필요하다면?

Java가 설치되어 있지 않다면 다음 중 하나를 설치하세요:

1. **Microsoft Build of OpenJDK** (가장 추천 - Windows용)
   - https://www.microsoft.com/openjdk
   - MSI 설치 파일로 간편하게 설치 가능

2. **Eclipse Temurin (Adoptium)**
   - https://adoptium.net/
   - "Latest LTS Release" 또는 "17" 다운로드

3. **Amazon Corretto**
   - https://aws.amazon.com/corretto/
   - "Corretto 17" 다운로드

자세한 설치 가이드는 `JAVA_INSTALL_GUIDE.md`를 참고하세요.

## JAVA_HOME 설정 (필요시)

Maven Wrapper가 JAVA_HOME을 찾지 못하면 다음 중 하나를 선택하세요:

### 빠른 해결 (현재 세션만)

PowerShell에서:
```powershell
# Java 경로 자동 찾아서 설정
$javaPath = (Get-Command java).Source
$javaHome = Split-Path (Split-Path $javaPath -Parent) -Parent
$env:JAVA_HOME = $javaHome

# 또는 수동으로 설정 (일반적인 경로)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17.0.17"
```

### 영구 설정 (시스템 전체)

1. **Windows 키 + "환경 변수" 검색** → "시스템 환경 변수 편집"
2. **"환경 변수" 버튼 클릭**
3. **"시스템 변수"에서 "새로 만들기"**
   - 변수 이름: `JAVA_HOME`
   - 변수 값: `C:\Program Files\Java\jdk-17.0.17` (실제 설치 경로)
4. **새 터미널 열기** (변경사항 적용)

📖 자세한 가이드: `JAVA_HOME_SETUP.md` 참고

## 실행 방법

### 1. Maven Wrapper 사용 (추천 - Maven 설치 불필요)

```bash
# 프로젝트 루트에서
cd always-svc

# Windows에서 실행 (PowerShell)
./mvnw.cmd spring-boot:run

# 또는 빌드 후 실행
./mvnw.cmd clean install
./mvnw.cmd spring-boot:run
```

### 2. Maven이 설치되어 있다면

```bash
# 프로젝트 루트에서
cd always-svc

# 빌드
mvn clean install

# 실행
mvn spring-boot:run
```

### 3. IDE에서 실행

`AlwaysApplication.java`를 실행하면 됩니다.

### 참고: Maven Wrapper란?

Maven Wrapper(`mvnw`)를 사용하면 Maven을 별도로 설치하지 않아도 프로젝트를 실행할 수 있습니다.
첫 실행 시 필요한 Maven 바이너리를 자동으로 다운로드합니다.

## API 엔드포인트

서버가 실행되면 기본적으로 `http://localhost:8081`에서 실행됩니다.

### 예제 엔드포인트

- `GET /api/hello` - Hello 메시지
- `GET /api/posts` - 모든 포스트 조회
- `GET /api/posts/{id}` - 특정 포스트 조회
- `POST /api/posts` - 포스트 생성
- `PUT /api/posts/{id}` - 포스트 업데이트
- `DELETE /api/posts/{id}` - 포스트 삭제

## Vue 프론트엔드 연동

Vue 프론트엔드에서 이 서버를 사용하려면 `src/api/index.js`의 `baseURL`을 다음과 같이 변경하세요:

```javascript
const api = axios.create({
  baseURL: 'http://localhost:8081/api',
  // ...
})
```

또는 환경 변수로 설정:

```env
VUE_APP_API_BASE_URL=http://localhost:8081/api
```

## 데이터베이스

### 개발 환경 (기본 - H2)
현재 설정은 H2 인메모리 데이터베이스를 사용합니다.
- H2 콘솔: `http://localhost:8081/h2-console`
- 데이터는 서버 재시작 시 초기화됩니다

### 프로덕션 환경 (MySQL)

#### MySQL 설정

1. **MySQL 설치**
   - 다운로드: https://dev.mysql.com/downloads/installer/
   - 상세 가이드: `DATABASE_SETUP.md` 참고

2. **데이터베이스 생성**
   ```sql
   CREATE DATABASE always_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

3. **프로파일로 실행**
   
   **방법 1: PowerShell 스크립트 사용 (추천)**
   ```powershell
   .\run-mysql.ps1
   ```
   
   **방법 2: 환경 변수 사용**
   ```powershell
   $env:SPRING_PROFILES_ACTIVE = "mysql"
   ./mvnw.cmd spring-boot:run
   ```
   
   **방법 3: Maven 옵션 사용**
   ```powershell
   ./mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=mysql"
   ```
   
   또는 `application.properties`에서 직접 MySQL 설정 사용

4. **설정 파일**: `application-mysql.properties` 참고

📖 자세한 설정 가이드: `DATABASE_SETUP.md` 참고

