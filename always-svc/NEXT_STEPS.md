# 서버 실행 후 확인 사항

## ✅ 완료된 작업
- Spring Boot 서버 실행 (MySQL 프로파일)
- MySQL 데이터베이스 연동
- JPA Entity 및 Repository 설정

## 🔍 확인할 사항

### 1. 데이터베이스 테이블 생성 확인

DBeaver에서 확인:
1. `always_db` 데이터베이스 확장
2. `Tables` 폴더 확인
3. `posts` 테이블이 생성되었는지 확인
4. 테이블 더블클릭하여 데이터 확인 (초기 데이터 3개)

또는 SQL 쿼리로 확인:
```sql
USE always_db;
SHOW TABLES;
SELECT * FROM posts;
```

### 2. API 엔드포인트 테스트

서버가 실행 중일 때 다음 URL을 브라우저에서 테스트:

- `http://192.168.75.207:8089/api/hello` - Hello 메시지
- `http://192.168.75.207:8089/api/posts` - 모든 포스트 조회 (MySQL에서)

### 3. Vue 프론트엔드 연동 확인

Vue 프론트엔드를 실행하고:
```bash
npm run serve
```

브라우저에서 `http://192.168.75.207:8080` 접속 후:
- Home 페이지에서 "포스트 가져오기" 버튼 클릭
- MySQL에서 가져온 데이터가 표시되는지 확인

### 4. 서버 콘솔 로그 확인

서버 콘솔에서 다음 메시지들을 확인:
- `HikariPool-1 - Start completed` - 데이터베이스 연결 성공
- `Initializing Spring Data JPA repositories` - JPA 초기화
- `초기 데이터가 생성되었습니다.` - 초기 데이터 삽입 완료
- API 요청 로그 (LoggingInterceptor)

## 📝 다음 단계

### 1. API 테스트 (Postman 또는 브라우저)

**GET - 모든 포스트 조회**
```
GET http://192.168.75.207:8089/api/posts
```

**GET - 특정 포스트 조회**
```
GET http://192.168.75.207:8089/api/posts/1
```

**POST - 포스트 생성**
```
POST http://192.168.75.207:8089/api/posts
Content-Type: application/json

{
  "title": "새로운 포스트",
  "content": "포스트 내용입니다."
}
```

**PUT - 포스트 수정**
```
PUT http://192.168.75.207:8089/api/posts/1
Content-Type: application/json

{
  "title": "수정된 제목",
  "content": "수정된 내용"
}
```

**DELETE - 포스트 삭제**
```
DELETE http://192.168.75.207:8089/api/posts/1
```

### 2. DBeaver에서 데이터 확인

- 포스트 생성/수정/삭제 후 DBeaver에서 데이터 변화 확인
- `SELECT * FROM posts;` 쿼리로 데이터 확인

### 3. Vue 프론트엔드 기능 추가

현재 Vue 프론트엔드에서:
- ✅ 포스트 목록 조회
- ⏳ 포스트 생성 기능 추가 가능
- ⏳ 포스트 수정 기능 추가 가능
- ⏳ 포스트 삭제 기능 추가 가능

## 🎉 축하합니다!

이제 완전한 CRUD 기능을 가진 REST API 서버가 MySQL 데이터베이스와 연동되어 실행 중입니다!

