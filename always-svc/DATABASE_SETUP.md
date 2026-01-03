# 데이터베이스 설정 가이드

## 추천 데이터베이스

### 1. MySQL (가장 추천) ⭐
- **장점**: 널리 사용, 설치 간단, 커뮤니티 풍부
- **설치 URL**: https://dev.mysql.com/downloads/mysql/
- **또는**: https://dev.mysql.com/downloads/installer/ (Windows Installer)
- **용도**: 일반적인 웹 애플리케이션에 적합

### 2. MariaDB
- **장점**: MySQL 호환, 오픈소스
- **설치 URL**: https://mariadb.org/download/
- **용도**: MySQL 대안

## MySQL 설치 (Windows)

### 방법 1: MySQL Installer (추천)

1. **다운로드**: https://dev.mysql.com/downloads/installer/
   - "MySQL Installer for Windows" 선택
   - "mysql-installer-community-*.msi" 다운로드

2. **설치**
   - 설치 파일 실행
   - "Developer Default" 또는 "Server only" 선택
   - 설치 완료 후 MySQL Server 시작

3. **초기 설정**
   - Root 비밀번호 설정 (기억해두세요!)
   - 포트: 3306 (기본값)
   - Windows Service로 실행 설정 (권장)

### 방법 2: XAMPP (간단)

1. **다운로드**: https://www.apachefriends.org/
   - XAMPP 설치 (MySQL 포함)
   - Apache와 MySQL 서비스 시작

2. **설정**
   - 기본 포트: 3306
   - Root 비밀번호: 비어있음 (초기)

### 방법 3: Docker (개발용)

```bash
docker run --name mysql -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 -d mysql:8.0
```

## Spring Boot 연동 설정

### 1. MySQL Driver 추가

`pom.xml`에서 주석 해제:
```xml
<!-- MySQL Driver -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 2. application.properties 설정

```properties
# MySQL Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/always_db?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### 3. 데이터베이스 생성

MySQL에 접속하여 데이터베이스 생성:
```sql
CREATE DATABASE always_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

또는 MySQL Workbench나 명령줄에서:
```bash
mysql -u root -p
CREATE DATABASE always_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## 프로파일 설정 (환경별 설정)

개발/프로덕션 환경을 분리하려면:

### application-dev.properties (개발용 - H2)
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

### application-prod.properties (프로덕션용 - MySQL)
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/always_db
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
```

실행 시 프로파일 지정:

**방법 1: 환경 변수 사용 (PowerShell)**
```powershell
$env:SPRING_PROFILES_ACTIVE = "mysql"
./mvnw.cmd spring-boot:run
```

**방법 2: Maven 옵션 사용 (따옴표 필요)**
```powershell
./mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=mysql"
```

**방법 3: run-mysql.ps1 스크립트 사용**
```powershell
.\run-mysql.ps1
```

## 추천: MySQL 사용

프로젝트에서는 **MySQL**을 사용합니다:
- 가장 널리 사용됨
- 자료가 풍부함
- 설치가 간단함

