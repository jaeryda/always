# 추천 라이브러리 목록

프로젝트에 추가하면 유용한 라이브러리들을 우선순위별로 정리했습니다.

## 🎯 프론트엔드 (Vue 3)

### 1. **@vueuse/core** ⭐⭐⭐ (강력 추천)
Vue Composition API 유틸리티 라이브러리
```bash
npm install @vueuse/core
```

**주요 기능:**
- `useWindowSize()` - 반응형 화면 크기 감지 (현재 수동 구현한 `isMobile` 로직 대체)
- `useLocalStorage()` - 로컬 스토리지 자동 관리
- `useDebounceFn()` - 디바운스 (검색 기능에 유용)
- `useThrottleFn()` - 스로틀
- `useClipboard()` - 클립보드 복사
- `useFetch()` - HTTP 요청 (axios 대체 가능)

**사용 예시:**
```typescript
import { useWindowSize } from '@vueuse/core'

const { width } = useWindowSize()
const isMobile = computed(() => width.value <= 768)
```

**이유**: 반응형 로직, 유틸리티 함수들을 간단하게 처리 가능

---

### 2. **zod** ⭐⭐ (추천)
TypeScript-first 스키마 검증 라이브러리
```bash
npm install zod
npm install @vee-validate/zod  # Element Plus와 통합
```

**주요 기능:**
- 타입 안전한 스키마 검증
- Element Plus Form과 통합 가능
- 런타임 타입 체크

**사용 예시:**
```typescript
import { z } from 'zod'

const registerSchema = z.object({
  username: z.string().min(3).max(50),
  email: z.string().email(),
  password: z.string().min(6),
  confirmPassword: z.string()
}).refine(data => data.password === data.confirmPassword, {
  message: "비밀번호가 일치하지 않습니다",
  path: ["confirmPassword"]
})
```

**이유**: 현재 Element Plus Form Rules를 타입 안전하게 관리 가능

---

### 3. **nprogress** ⭐⭐ (추천)
페이지 로딩 인디케이터
```bash
npm install nprogress
npm install @types/nprogress --save-dev
```

**주요 기능:**
- 페이지 전환 시 상단 진행 바 표시
- API 요청 중 로딩 표시

**사용 예시:**
```typescript
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

// router에서
router.beforeEach(() => {
  NProgress.start()
})

router.afterEach(() => {
  NProgress.done()
})
```

**이유**: 사용자 경험 향상 (로딩 상태 명확히 표시)

---

### 4. **lodash-es** ⭐ (선택)
유틸리티 함수 라이브러리
```bash
npm install lodash-es
npm install @types/lodash-es --save-dev
```

**주요 기능:**
- 배열, 객체, 문자열 처리 함수
- 디바운스, 스로틀, 깊은 복사 등

**이유**: 자주 사용하는 유틸리티 함수 제공

---

### 5. **vue-i18n** ⭐ (선택)
다국어 지원
```bash
npm install vue-i18n@9
```

**이유**: 향후 다국어 지원이 필요할 경우

---

## 🚀 백엔드 (Spring Boot)

### 1. **SpringDoc OpenAPI (Swagger)** ⭐⭐⭐ (강력 추천)
API 문서 자동 생성
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

**주요 기능:**
- API 엔드포인트 자동 문서화
- Swagger UI 제공 (http://192.168.0.2:8089/swagger-ui.html)
- API 테스트 가능

**설정 예시:**
```java
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Always API")
                .version("1.0.0")
                .description("Always 프로젝트 REST API"));
    }
}
```

**이유**: API 문서 자동 생성, 프론트엔드 개발자와 협업 시 유용

---

### 2. **MapStruct** ⭐⭐ (추천)
DTO 매핑 라이브러리 (컴파일 타임 코드 생성)
```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>1.5.5.Final</version>
    <scope>provided</scope>
</dependency>
```

**주요 기능:**
- Entity ↔ DTO 자동 변환
- 컴파일 타임에 코드 생성 (런타임 오버헤드 없음)
- 타입 안전성 보장

**사용 예시:**
```java
@Mapper(componentModel = "spring")
public interface PostMapper {
    PostDTO toDTO(Post post);
    Post toEntity(PostDTO dto);
}
```

**이유**: Entity를 직접 반환하지 않고 DTO를 사용하는 것이 좋은 관행 (현재는 Entity를 직접 반환 중)

---

### 3. **QueryDSL** ⭐ (선택)
타입 안전한 동적 쿼리 작성
```xml
<dependency>
    <groupId>com.querydsl</groupId>
    <artifactId>querydsl-jpa</artifactId>
    <version>5.0.0</version>
</dependency>
<dependency>
    <groupId>com.querydsl</groupId>
    <artifactId>querydsl-apt</artifactId>
    <version>5.0.0</version>
    <scope>provided</scope>
</dependency>
```

**이유**: 복잡한 검색/필터링 쿼리 작성 시 유용

---

### 4. **Spring Boot Actuator** ⭐ (선택)
애플리케이션 모니터링 및 관리
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

**주요 기능:**
- 헬스 체크 (`/actuator/health`)
- 메트릭 수집 (`/actuator/metrics`)
- 애플리케이션 정보 (`/actuator/info`)

**이유**: 프로덕션 환경 모니터링에 유용

---

### 5. **SLF4J + Logback** (이미 포함됨)
로깅 프레임워크 - 추가 설정 파일 권장
- `logback-spring.xml` 파일 생성하여 로그 레벨, 파일 출력 등 설정

---

## 📋 우선순위별 설치 순서

### 즉시 추가 추천 (High Priority)
1. **@vueuse/core** - 반응형 로직 간소화
2. **SpringDoc OpenAPI** - API 문서화
3. **nprogress** - 사용자 경험 향상

### 단기간 내 추가 추천 (Medium Priority)
4. **zod** - 폼 검증 강화
5. **MapStruct** - DTO 패턴 적용

### 필요 시 추가 (Low Priority)
6. **lodash-es** - 유틸리티 함수
7. **QueryDSL** - 복잡한 쿼리
8. **Spring Boot Actuator** - 모니터링

---

## 설치 명령어 (프론트엔드)

```bash
# 즉시 추가 추천
npm install @vueuse/core
npm install nprogress
npm install @types/nprogress --save-dev

# 단기간 내 추가
npm install zod
npm install @vee-validate/zod
```

## 설치 명령어 (백엔드)

`pom.xml`에 다음 의존성 추가:

```xml
<!-- SpringDoc OpenAPI -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>

<!-- MapStruct -->
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>1.5.5.Final</version>
    <scope>provided</scope>
</dependency>
```

