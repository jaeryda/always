/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_KAKAO_JAVASCRIPT_KEY: string
  readonly VITE_KAKAO_REDIRECT_URI: string
  readonly VITE_NAVER_CLIENT_ID: string
  readonly VITE_NAVER_REDIRECT_URI: string
  readonly VITE_API_BASE_URL?: string
  readonly VITE_IMAGE_BASE_URL?: string
  readonly VITE_KAKAO_REST_API_KEY?: string
  readonly VITE_GEMINI_API_KEY?: string
  readonly VUE_APP_GEMINI_API_KEY?: string // Vue CLI 지원
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

// Vue CLI 환경 변수 타입 정의
declare namespace NodeJS {
  interface ProcessEnv {
    readonly VUE_APP_GEMINI_API_KEY?: string
    readonly VITE_GEMINI_API_KEY?: string
  }
}

