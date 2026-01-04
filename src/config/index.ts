// 환경 변수 및 설정 관리
export const config = {
  apiBaseURL: import.meta.env.VITE_API_BASE_URL || 'http://192.168.75.85:8089/api',
  imageBaseURL: import.meta.env.VITE_IMAGE_BASE_URL || 'http://192.168.75.85:8089/images',
  kakaoJavaScriptKey: import.meta.env.VITE_KAKAO_JAVASCRIPT_KEY || '',
  kakaoRedirectUri: import.meta.env.VITE_KAKAO_REDIRECT_URI || 'http://localhost:8088/auth/kakao/callback'
}

// API URL 생성 헬퍼 함수
export const getApiUrl = (path: string): string => {
  const baseURL = config.apiBaseURL.replace(/\/$/, '') // 끝의 슬래시 제거
  const cleanPath = path.startsWith('/') ? path : `/${path}`
  return `${baseURL}${cleanPath}`
}

// 이미지 URL 생성 헬퍼 함수
export const getImageUrl = (imagePath: string | null | undefined): string | null => {
  if (!imagePath) return null
  return `${config.imageBaseURL}/${imagePath}`
}

