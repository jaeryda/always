# DBeaver로 MySQL 접속 가이드

## 1단계: DBeaver에서 새 연결 만들기

1. **DBeaver 실행**
2. **새 데이터베이스 연결 생성**
   - 상단 메뉴: `데이터베이스` → `새 데이터베이스 연결`
   - 또는 왼쪽 상단 `새 연결` 버튼 클릭
   - 또는 `Ctrl + Shift + N` 단축키

## 2단계: MySQL 선택

1. 데이터베이스 목록에서 **MySQL** 선택
2. **다음** 클릭

## 3단계: 연결 정보 입력

### 기본 정보
- **서버 호스트**: `192.168.75.80` (또는 `localhost` / `127.0.0.1` - 로컬인 경우)
  - ⚠️ **중요**: `http://` 프로토콜은 사용하지 않습니다. IP 주소만 입력하세요.
- **포트**: `3306` (기본값)
- **데이터베이스**: `always_db` (생성한 데이터베이스 이름)
- **사용자 이름**: `root`
- **비밀번호**: MySQL 설치 시 설정한 root 비밀번호

### 연결 확인
- **연결 테스트** 버튼 클릭
- "연결 성공" 메시지가 나오면 **완료** 클릭

### ⚠️ "Public Key Retrieval is not allowed" 오류 발생 시

MySQL 8.0 이상에서 이 오류가 발생하면:

1. 연결 설정 창에서 **드라이버 속성** 탭 선택
2. **allowPublicKeyRetrieval** 속성을 찾아 값 **true**로 설정
   - 속성이 없으면 **+** 버튼으로 추가
3. **확인** 후 다시 연결 시도

**또는 연결 URL에 직접 추가:**
- 연결 설정 → **일반** 탭 → URL 필드에 `?allowPublicKeyRetrieval=true` 추가

## 4단계: 데이터베이스 생성 (없는 경우)

만약 `always_db` 데이터베이스가 아직 생성되지 않았다면:

1. **root 연결로 접속** (데이터베이스 없이 연결)
2. **SQL 스크립트 실행**
   ```sql
   CREATE DATABASE always_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. 새로고침 후 `always_db` 선택

## 5단계: 데이터베이스 사용

연결 후:
- 왼쪽 사이드바에서 `always_db` 데이터베이스 확장
- 테이블, 뷰 등을 확인할 수 있습니다
- SQL 편집기를 열어 쿼리 실행 가능

## 연결 정보 요약

```
서버 호스트: 192.168.75.80 (또는 localhost - 로컬인 경우)
포트: 3306
데이터베이스: always_db
사용자: root
비밀번호: (설치 시 설정한 비밀번호)
```

**참고**: 
- MySQL 연결에는 `http://` 프로토콜이 필요 없습니다. IP 주소만 입력하세요.
- 예: `192.168.75.80` (O) / `http://192.168.75.80` (X)

## 문제 해결

### "Access denied" 오류
- 비밀번호가 올바른지 확인
- MySQL 서버가 실행 중인지 확인

### "Public Key Retrieval is not allowed" 오류
이 오류는 MySQL 8.0 이상에서 발생할 수 있습니다.

**해결 방법:**
1. 연결 설정 창에서 **드라이버 속성** 탭 선택
2. **allowPublicKeyRetrieval** 속성을 찾아 값 **true**로 설정
   - 속성이 없으면 **+** 버튼으로 추가
3. **확인** 후 다시 연결 시도

**또는 연결 URL에 직접 추가:**
- 연결 설정 → **일반** 탭 → URL 필드에 `?allowPublicKeyRetrieval=true` 추가

### "Host 'XXX' is not allowed to connect to this MySQL server" 오류
이 오류는 MySQL 서버가 해당 호스트에서의 접속을 허용하지 않을 때 발생합니다.

**해결 방법:**
1. MySQL 서버(192.168.75.80)에서 MySQL에 접속
2. 다음 SQL 실행 (모든 IP 허용 - 개발 환경용):
   
   **MySQL 8.0 이상:**
   ```sql
   CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED BY '1234';
   GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
   FLUSH PRIVILEGES;
   ```
   
   **MySQL 5.7 이하:**
   ```sql
   GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY '1234' WITH GRANT OPTION;
   FLUSH PRIVILEGES;
   ```
3. DBeaver에서 다시 연결 시도

자세한 내용은 `MYSQL_REMOTE_ACCESS_FIX.md` 파일을 참고하세요.

### "Unknown database" 오류
- 데이터베이스 이름을 비워두고 연결 시도
- 또는 데이터베이스를 먼저 생성

### "Communications link failure" 오류
- MySQL 서버가 실행 중인지 확인
- Windows: 서비스 관리자에서 MySQL 서비스 확인
- 포트 3306이 사용 중인지 확인
- MySQL 서버의 `bind-address` 설정 확인 (원격 접속 허용)

## MySQL 드라이버 설치 (필요시)

DBeaver가 MySQL 드라이버를 자동으로 다운로드하지만, 문제가 있으면:

1. 연결 설정 창에서 **편집** 클릭
2. **드라이버** 탭 선택
3. **다운로드/업데이트** 클릭하여 드라이버 설치

