// 환경 변수 및 설정 관리
export const config = {
  apiBaseURL: process.env.VUE_APP_API_BASE_URL || 'http://localhost:8089/api',
  imageBaseURL: process.env.VUE_APP_IMAGE_BASE_URL || 'http://localhost:8089/images'
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

