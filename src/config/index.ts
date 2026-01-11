// 환경 변수 및 설정 관리
export const config = {
  apiBaseURL: import.meta.env.VITE_API_BASE_URL || 'http://192.168.75.80:8089/api',
  imageBaseURL: import.meta.env.VITE_IMAGE_BASE_URL || 'http://192.168.75.80:8089/images',
  kakaoRestApiKey: import.meta.env.VITE_KAKAO_REST_API_KEY || 'a46933f5e2f0ea147fd326f2cbceb3d8',
  kakaoRedirectUri: import.meta.env.VITE_KAKAO_REDIRECT_URI || 'http://192.168.75.80:8089/api/auth/kakao/callback',
  naverClientId: import.meta.env.VITE_NAVER_CLIENT_ID || 'vzvcNY435VBhY9kIUVwE',
  naverRedirectUri: import.meta.env.VITE_NAVER_REDIRECT_URI || 'http://192.168.75.80:8089/api/auth/naver/callback'
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

