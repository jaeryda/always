# 프로젝트 코드 검수 보고서

## 📋 프로젝트 개요

- **프로젝트명**: always-web (Vue 3 + TypeScript) / always-svc (Spring Boot)
- **스택**: Vue 3, TypeScript, Element Plus, Pinia, Vue Router, Axios, Spring Boot, MySQL

---

## ✅ 잘 구현된 부분

1. **TypeScript 적용**: 프로젝트 전반에 TypeScript가 잘 적용되어 있음
2. **구조**: API, Store, Router, Views로 명확하게 분리
3. **반응형 디자인**: 모바일/데스크톱 반응형 UI 구현
4. **에러 핸들링**: try-catch를 통한 기본적인 에러 처리
5. **코드 스타일**: 일관된 코딩 스타일 유지

---

## 🔧 개선 필요 사항

### 1. 환경 변수 관리
**현재 상태**: `.env` 파일이 없고 하드코딩된 값 사용
```typescript
// src/api/index.ts
baseURL: process.env.VUE_APP_API_BASE_URL || 'http://192.168.75.207:8081/api'
```

**개선 방안**:
- `.env`, `.env.development`, `.env.production` 파일 추가
- 환경별 설정 분리

### 2. 유틸리티 함수 중복
**현재 상태**: `formatDate` 함수가 여러 컴포넌트에 중복 정의됨
```typescript
// Posts.vue, PostDetail.vue 등에서 중복
const formatDate = (dateString: string | undefined): string => {
  // ...
}
```

**개선 방안**:
- `src/utils/date.ts` 파일 생성하여 공통 유틸리티로 추출

### 3. API 응답 타입 정의
**현재 상태**: API 응답 타입이 불완전하거나 일관성 부족
```typescript
// posts.ts - 일부 타입이 부정확
getPostById(id: number) {
  return api.get<{ post: Post; timestamp: string }>(`/posts/${id}`)
}
```

**개선 방안**:
- API 응답 타입을 더 정확하게 정의
- 공통 응답 타입 인터페이스 생성

### 4. 에러 처리 개선
**현재 상태**: 각 컴포넌트에서 개별적으로 에러 처리
```typescript
// 여러 컴포넌트에서 반복되는 패턴
catch (err) {
  console.error(err)
  ElMessage.error('에러 메시지')
}
```

**개선 방안**:
- 전역 에러 핸들러 구현
- API 인터셉터에서 공통 에러 처리

### 5. 폼 검증
**현재 상태**: 수동 검증 (trim() 체크만)
```typescript
if (!newPost.value.title.trim()) {
  ElMessage.warning('제목을 입력해주세요.')
  return
}
```

**개선 방안**:
- 폼 검증 라이브러리 도입 (zod, yup 등)
- Element Plus Form Validation 활용

### 6. 로딩 상태 관리
**현재 상태**: 각 컴포넌트에서 개별적으로 로딩 상태 관리
```typescript
const loading = ref<boolean>(false)
const creating = ref<boolean>(false)
const deletingId = ref<number | null>(null)
```

**개선 방안**:
- 전역 로딩 상태 관리 (Pinia store 또는 composable)

### 7. 상수 관리
**현재 상태**: 매직 넘버/문자열이 코드에 하드코딩
```typescript
const isMobile = ref<boolean>(window.innerWidth <= 768) // 768은 매직 넘버
```

**개선 방안**:
- `src/constants/` 디렉토리 생성하여 상수 관리

### 8. 불필요한 파일
- `src/components/HelloWorld.vue` - 사용하지 않는 컴포넌트

---

## 📦 추천 라이브러리

### 1. **dayjs** ⭐ (필수)
날짜 포맷팅 및 조작 라이브러리
```bash
npm install dayjs
```
**이유**: 현재 `formatDate` 함수를 여러 곳에서 중복 정의하고 있음. dayjs로 통일하면 코드가 간결해지고 일관성 유지 가능.

### 2. **@vueuse/core** ⭐ (강력 추천)
Vue Composition API 유틸리티 라이브러리
```bash
npm install @vueuse/core
```
**주요 기능**:
- `useWindowSize()` - 반응형 크기 감지 (현재 `isMobile` 로직 대체)
- `useLocalStorage()` - 로컬 스토리지 관리
- `useDebounceFn()` - 디바운스 함수
- `useFetch()` - HTTP 요청 (axios 대체 가능)

**이유**: 현재 수동으로 구현한 반응형 로직을 간단하게 처리 가능.

### 3. **pinia-plugin-persistedstate** (선택)
Pinia 상태 영속화 플러그인
```bash
npm install pinia-plugin-persistedstate
```
**이유**: 사용자 설정 등을 브라우저에 저장하고 싶을 때 유용.

### 4. **nprogress** (선택)
로딩 인디케이터 (페이지 상단 진행 바)
```bash
npm install nprogress
npm install --save-dev @types/nprogress
```
**이유**: 페이지 전환 시 시각적 피드백 제공.

### 5. **zod** 또는 **yup** (선택)
폼 검증 라이브러리
```bash
npm install zod
# 또는
npm install yup
```
**이유**: 복잡한 폼 검증 로직을 더 체계적으로 관리 가능.

### 6. **lodash-es** (선택)
유틸리티 함수 라이브러리
```bash
npm install lodash-es
npm install --save-dev @types/lodash-es
```
**이유**: 배열/객체 조작, 깊은 복사 등 유틸리티 함수 제공.

---

## 🔨 즉시 적용 가능한 개선사항

### 1. 환경 변수 파일 생성
`.env.development` 파일 생성:
```env
VUE_APP_API_BASE_URL=http://192.168.75.207:8081/api
VUE_APP_IMAGE_BASE_URL=http://192.168.75.207:8081/images
```

`.env.production` 파일 생성:
```env
VUE_APP_API_BASE_URL=https://api.example.com/api
VUE_APP_IMAGE_BASE_URL=https://api.example.com/images
```

### 2. 유틸리티 함수 추출
`src/utils/date.ts` 생성:
```typescript
export const formatDate = (dateString: string | undefined): string => {
  // 공통 로직
}
```

`src/utils/constants.ts` 생성:
```typescript
export const BREAKPOINTS = {
  MOBILE: 768
} as const
```

### 3. API 타입 개선
`src/types/api.ts` 생성:
```typescript
export interface ApiResponse<T> {
  data: T
  message?: string
  timestamp: string
}
```

---

## 📊 코드 품질 점수

| 항목 | 점수 | 평가 |
|------|------|------|
| 구조/아키텍처 | 8/10 | ✅ 명확한 구조 |
| 타입 안정성 | 7/10 | ⚠️ 일부 any 사용 |
| 코드 재사용성 | 6/10 | ⚠️ 중복 코드 존재 |
| 에러 처리 | 7/10 | ⚠️ 개선 여지 있음 |
| 테스트 | 0/10 | ❌ 테스트 코드 없음 |
| 문서화 | 7/10 | ✅ README는 잘 작성됨 |

**종합 점수: 6.8/10** (양호)

---

## 🚀 다음 단계 추천

1. **우선순위 높음**:
   - [ ] dayjs 설치 및 적용
   - [ ] 환경 변수 파일 생성
   - [ ] 공통 유틸리티 함수 추출
   - [ ] 사용하지 않는 파일 제거

2. **우선순위 중간**:
   - [ ] @vueuse/core 설치 (반응형 로직 개선)
   - [ ] 전역 에러 핸들러 구현
   - [ ] 상수 관리 개선

3. **우선순위 낮음**:
   - [ ] 테스트 코드 작성
   - [ ] 코드 스플리팅 구현
   - [ ] 폼 검증 라이브러리 도입

---

## 📝 참고사항

- 현재 프로젝트는 전반적으로 잘 구성되어 있음
- 주요 개선 사항은 코드 중복 제거와 유틸리티 라이브러리 활용
- 프로덕션 배포 전에는 환경 변수 관리와 에러 핸들링 개선 권장

