# 이미지 업로드 기능 가이드

## 📁 이미지 저장 경로

이미지 파일은 다음 경로에 저장됩니다:
```
C:\Users\jy_kim\Pictures\server_picture
```

이 디렉토리는 서버 시작 시 자동으로 생성됩니다.

## 🗄️ 데이터베이스

Post Entity에 `image_path` 컬럼이 추가되었습니다:
- 컬럼명: `image_path`
- 타입: VARCHAR(500)
- 저장 내용: UUID_파일명.확장자 (예: `a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg`)

## 🔌 API 엔드포인트

### 1. 이미지 업로드

**POST** `/api/posts/{id}/image`

**Request:**
- Content-Type: `multipart/form-data`
- Parameter: `file` (이미지 파일)

**Response:**
```json
{
  "message": "이미지가 업로드되었습니다.",
  "post": { ... },
  "imageUrl": "/images/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg",
  "timestamp": "2025-12-28T..."
}
```

**예제 (cURL):**
```bash
curl -X POST http://localhost:8081/api/posts/1/image \
  -F "file=@/path/to/image.jpg"
```

### 2. 이미지 조회

이미지는 정적 리소스로 제공됩니다:

**GET** `http://localhost:8081/images/{filename}`

브라우저에서 직접 접속 가능:
```
http://localhost:8081/images/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg
```

### 3. 포스트 조회 시 이미지 URL

포스트를 조회하면 이미지 경로가 포함됩니다:

**GET** `/api/posts/{id}`

**Response:**
```json
{
  "post": {
    "id": 1,
    "title": "...",
    "content": "...",
    "imagePath": "a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg",
    ...
  },
  "imageUrl": "http://localhost:8081/images/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg",
  "timestamp": "..."
}
```

## 📝 파일명 규칙

업로드된 파일은 UUID를 사용하여 고유한 이름으로 저장됩니다:
- 형식: `{UUID}.{원본확장자}`
- 예: `a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg`

이렇게 하면:
- 파일명 충돌 방지
- 보안 강화 (원본 파일명 노출 방지)

## ⚙️ 설정

### 파일 업로드 크기 제한

`application-mysql.properties`에 설정:
```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

### 정적 리소스 제공

`WebConfig.java`에서 설정:
```java
registry.addResourceHandler("/images/**")
    .addResourceLocations("file:C:/Users/jy_kim/Pictures/server_picture/");
```

## 🔄 동작 흐름

1. **이미지 업로드**:
   - 클라이언트 → `POST /api/posts/{id}/image` (multipart/form-data)
   - 서버 → 파일을 `C:\Users\jy_kim\Pictures\server_picture`에 저장
   - 서버 → DB의 `posts.image_path`에 파일명 저장
   - 서버 → 응답에 이미지 URL 포함

2. **이미지 조회**:
   - 클라이언트 → `GET /images/{filename}`
   - 서버 → 해당 경로의 파일 반환

3. **포스트 삭제 시**:
   - 연결된 이미지 파일도 자동으로 삭제됨

## 🧪 테스트

### Postman으로 테스트

1. **이미지 업로드**:
   - Method: POST
   - URL: `http://localhost:8081/api/posts/1/image`
   - Body: form-data
   - Key: `file` (type: File)
   - Value: 이미지 파일 선택

2. **이미지 확인**:
   - 브라우저에서: `http://localhost:8081/images/{filename}`

## ⚠️ 주의사항

1. **경로 확인**: `C:\Users\jy_kim\Pictures\server_picture` 디렉토리가 존재하는지 확인
2. **권한**: 서버가 해당 경로에 쓰기 권한이 있어야 함
3. **파일 크기**: 기본 최대 10MB (설정에서 변경 가능)
4. **파일 확장자**: 모든 이미지 형식 지원 (jpg, png, gif, webp 등)

