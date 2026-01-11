/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_KAKAO_JAVASCRIPT_KEY: string
  readonly VITE_KAKAO_REDIRECT_URI: string
  readonly VITE_NAVER_CLIENT_ID: string
  readonly VITE_NAVER_REDIRECT_URI: string
  readonly VITE_API_BASE_URL?: string
  readonly VITE_IMAGE_BASE_URL?: string
  readonly VITE_KAKAO_REST_API_KEY?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

