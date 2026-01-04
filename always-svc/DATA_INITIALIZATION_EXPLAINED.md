# 초기 데이터 생성 설명

## 📍 위치

초기 데이터는 `always-svc/src/main/java/com/always/config/DataInitializer.java` 파일에서 생성됩니다.

## ⏰ 언제 생성되는가?

**서버가 시작될 때 자동으로 생성됩니다.**

## 🔧 어떻게 동작하는가?

### 1. CommandLineRunner 인터페이스

```java
@Component
public class DataInitializer implements CommandLineRunner {
    // ...
}
```

- `@Component`: Spring이 이 클래스를 자동으로 관리
- `CommandLineRunner`: Spring Boot 애플리케이션이 완전히 시작된 후 실행되는 인터페이스

### 2. 실행 순서

1. Spring Boot 서버 시작
2. 데이터베이스 연결 (MySQL)
3. JPA 테이블 생성 (posts 테이블)
4. **DataInitializer.run() 메서드 자동 실행**
5. 데이터 확인 후 비어있으면 초기 데이터 삽입

### 3. 코드 동작

```java
@Override
public void run(String... args) {
    // 데이터베이스가 비어있으면 초기 데이터 추가
    if (postRepository.count() == 0) {
        // 포스트 3개 생성
        Post post1 = new Post();
        post1.setTitle("첫 번째 포스트");
        post1.setContent("안녕하세요! 이것은 첫 번째 포스트입니다.");
        // ... 2개 더
        
        // 데이터베이스에 저장
        postRepository.save(post1);
        postRepository.save(post2);
        postRepository.save(post3);
        
        System.out.println("초기 데이터가 생성되었습니다.");
    }
}
```

**중요**: `if (postRepository.count() == 0)` 조건 때문에:
- 데이터베이스가 비어있을 때만 실행됨
- 이미 데이터가 있으면 실행되지 않음
- 중복 생성 방지

## 📊 생성되는 데이터

1. **첫 번째 포스트**
   - 제목: "첫 번째 포스트"
   - 내용: "안녕하세요! 이것은 첫 번째 포스트입니다."

2. **두 번째 포스트**
   - 제목: "Vue와 Spring Boot 연동"
   - 내용: "Vue 프론트엔드와 Spring Boot 백엔드를 연동했습니다."

3. **세 번째 포스트**
   - 제목: "MySQL 데이터베이스 연동"
   - 내용: "Spring Boot JPA를 사용하여 MySQL 데이터베이스와 연동했습니다."

## 🔍 확인 방법

### 서버 콘솔에서 확인
서버가 시작될 때 다음과 같은 메시지가 표시됩니다:
```
초기 데이터가 생성되었습니다.
```

### DBeaver에서 확인
```sql
SELECT * FROM posts;
```

### API로 확인
```
GET http://192.168.75.85:8089/api/posts
```

## 🛠️ 수정하거나 제거하려면?

### 초기 데이터 수정
`DataInitializer.java` 파일의 내용을 수정하면 됩니다.

### 초기 데이터 생성 비활성화
1. `DataInitializer.java` 파일 삭제
2. 또는 `@Component` 어노테이션을 주석 처리
3. 또는 클래스 전체를 주석 처리

### 초기 데이터를 다시 생성하려면
데이터베이스에서 모든 데이터를 삭제한 후 서버를 재시작:
```sql
DELETE FROM posts;
-- 또는
TRUNCATE TABLE posts;
```

## 💡 왜 사용하는가?

- 개발/테스트 시 초기 데이터가 필요함
- 애플리케이션 시작 시 자동으로 데이터가 준비됨
- 수동으로 데이터를 입력할 필요가 없음
- 테스트 시 일관된 초기 상태 보장

