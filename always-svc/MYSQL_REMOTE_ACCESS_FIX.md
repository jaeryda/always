# MySQL 원격 접속 허용 가이드

## 문제: "Host 'XXX' is not allowed to connect to this MySQL server"

이 오류는 MySQL 서버가 해당 호스트(컴퓨터)에서의 접속을 허용하지 않을 때 발생합니다.

## 해결 방법 (모든 IP 허용)

1. **MySQL 서버(192.168.75.99)에서 MySQL에 접속**
   ```bash
   mysql -u root -p
   ```

2. **다음 SQL 실행 (모든 IP 허용 - 개발 환경용)**
   
   **MySQL 8.0 이상 (권장):**
   ```sql
   CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED BY '1234';
   GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
   FLUSH PRIVILEGES;
   ```
   
   **MySQL 5.7 이하 (레거시):**
   ```sql
   GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY '1234' WITH GRANT OPTION;
   FLUSH PRIVILEGES;
   ```
   
   > **참고**: MySQL 8.0 이상에서는 `CREATE USER`와 `GRANT`를 분리해야 합니다.

3. **DBeaver에서 다시 연결 시도**

## 권한 확인 (선택사항)

```sql
-- 모든 사용자 확인
SELECT user, host FROM mysql.user WHERE user = 'root';
```

## 추가 문제 해결

### "Public Key Retrieval is not allowed" 오류

이 오류는 MySQL 8.0 이상에서 발생할 수 있습니다.

**DBeaver 해결 방법:**
1. DBeaver에서 연결 설정 창 열기 (연결 편집)
2. **드라이버 속성** 탭 선택
3. **allowPublicKeyRetrieval** 속성을 찾아 값을 **true**로 설정
   - 속성이 없으면 **+** 버튼을 클릭하여 추가
   - 이름: `allowPublicKeyRetrieval`
   - 값: `true`
4. **확인** 클릭 후 다시 연결 시도

**또는 연결 URL에 직접 추가:**
- 연결 설정 → **일반** 탭
- URL 필드에 `?allowPublicKeyRetrieval=true` 추가
- 예: `jdbc:mysql://192.168.75.99:3306/always_db?allowPublicKeyRetrieval=true`

## 보안 참고 사항

⚠️ **프로덕션 환경에서는 `%` (모든 IP)를 사용하지 마세요!**

프로덕션에서는:
- 특정 IP 또는 IP 대역만 허용
- 전용 데이터베이스 사용자 생성 (root 사용 금지)
- 강력한 비밀번호 사용

개발 환경에서만 `%`를 사용하는 것을 권장합니다.
