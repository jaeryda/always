# 프로젝트 실행 가이드

## 서버 실행 상태 확인

Spring Boot 서버가 실행되었다면 다음을 확인하세요:

1. **서버가 실행 중인지 확인**
   - 브라우저에서 `http://192.168.75.207:8089/api/hello` 접속
   - 또는 PowerShell에서: `curl http://192.168.75.207:8089/api/hello`

2. **예상 응답**
   ```json
   {
     "message": "Hello from Spring Boot!",
     "timestamp": "..."
   }
   ```

## Vue 프론트엔드 실행

새 터미널 창을 열고:

```bash
# 프로젝트 루트에서
npm run serve
```

Vue 개발 서버가 `http://192.168.75.207:8080`에서 실행됩니다.

## 전체 실행 순서

1. **Spring Boot 서버 실행** (이미 완료 ✅)
   ```bash
   cd always-svc
   .\run.ps1
   # 또는
   .\mvnw.cmd spring-boot:run
   ```
   - 서버 주소: `http://192.168.75.207:8089`

2. **Vue 프론트엔드 실행** (새 터미널)
   ```bash
   npm run serve
   ```
   - 프론트엔드 주소: `http://192.168.75.207:8080`

3. **브라우저에서 확인**
   - `http://192.168.75.207:8080` 접속
   - Home 페이지에서 "포스트 가져오기" 버튼 클릭
   - Spring Boot 서버에서 데이터를 가져오는지 확인

## API 엔드포인트 테스트

서버가 실행 중일 때 다음 엔드포인트들을 테스트할 수 있습니다:

- `GET http://192.168.75.207:8089/api/hello` - Hello 메시지
- `GET http://192.168.75.207:8089/api/posts` - 모든 포스트
- `GET http://192.168.75.207:8089/api/posts/1` - 특정 포스트
- `POST http://192.168.75.207:8089/api/posts` - 포스트 생성
- `PUT http://192.168.75.207:8089/api/posts/1` - 포스트 업데이트
- `DELETE http://192.168.75.207:8089/api/posts/1` - 포스트 삭제

## 문제 해결

### Vue에서 API 호출이 안 될 때

1. **서버가 실행 중인지 확인**
   - `http://192.168.75.207:8089/api/hello` 접속해서 확인

2. **CORS 오류가 발생하면**
   - `always-svc/src/main/java/com/always/config/WebConfig.java`에서 CORS 설정 확인
   - Vue 개발 서버 주소가 `http://192.168.75.207:8080`인지 확인

3. **API URL 확인**
   - `src/api/index.js`에서 `baseURL`이 `http://192.168.75.207:8089/api`로 설정되어 있는지 확인


