# MySQL 연동 가이드

## 1단계: 데이터베이스 생성

MySQL에 접속하여 데이터베이스를 생성하세요.

### 방법 1: MySQL 명령줄

```bash
mysql -u root -p
```

접속 후:
```sql
CREATE DATABASE always_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
SHOW DATABASES;  -- 확인
exit;
```

### 방법 2: MySQL Workbench

1. MySQL Workbench 실행
2. Root로 접속
3. 새 SQL 탭 열기
4. 다음 SQL 실행:
   ```sql
   CREATE DATABASE always_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

## 2단계: 비밀번호 확인

MySQL 설치 시 설정한 root 비밀번호를 확인하세요.

## 3단계: application-mysql.properties 설정

`always-svc/src/main/resources/application-mysql.properties` 파일을 열고:

```properties
spring.datasource.password=your_mysql_password  # 여기에 MySQL root 비밀번호 입력
```

기본 비밀번호가 `root`라면 그대로 사용하면 됩니다.

## 4단계: MySQL 프로파일로 서버 실행

### 방법 1: PowerShell 스크립트 사용 (추천)

```powershell
cd always-svc
.\run-mysql.ps1
```

### 방법 2: 환경 변수로 실행

```powershell
cd always-svc
$env:SPRING_PROFILES_ACTIVE = "mysql"
./mvnw.cmd spring-boot:run
```

### 방법 3: Maven 옵션 사용 (따옴표 필요)

```powershell
cd always-svc
./mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=mysql"
```

## 연결 확인

서버가 성공적으로 시작되면 MySQL에 연결된 것입니다!

콘솔 로그에서 다음과 같은 메시지가 보이면 성공:
- "HikariPool-1 - Starting..."
- "HikariPool-1 - Start completed"

## 문제 해결

### "Access denied" 오류
- 비밀번호가 올바른지 확인
- `application-mysql.properties`의 `spring.datasource.password` 확인

### "Unknown database" 오류
- 데이터베이스가 생성되었는지 확인: `SHOW DATABASES;`
- 데이터베이스 이름이 `always_db`인지 확인

### "Communications link failure" 오류
- MySQL 서버가 실행 중인지 확인
- 포트가 3306인지 확인
- Windows: 서비스 관리자에서 MySQL 서비스 확인


