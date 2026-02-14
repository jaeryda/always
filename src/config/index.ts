export const config = {
  apiBaseURL: import.meta.env.VITE_API_BASE_URL || 'http://192.168.0.2:8089/api',
  imageBaseURL: import.meta.env.VITE_IMAGE_BASE_URL || 'http://192.168.0.2:8089/images',
  kakaoRestApiKey: import.meta.env.VITE_KAKAO_REST_API_KEY || 'a46933f5e2f0ea147fd326f2cbceb3d8',
  kakaoRedirectUri: import.meta.env.VITE_KAKAO_REDIRECT_URI || 'http://192.168.0.2:8089/api/auth/kakao/callback',
  naverClientId: import.meta.env.VITE_NAVER_CLIENT_ID || 'vzvcNY435VBhY9kIUVwE',
  naverRedirectUri: import.meta.env.VITE_NAVER_REDIRECT_URI || 'http://192.168.0.2:8089/api/auth/naver/callback'
}

export const getApiUrl = (path: string): string => {
  const baseURL = config.apiBaseURL.replace(/\/$/, '')
  const cleanPath = path.startsWith('/') ? path : `/${path}`
  return `${baseURL}${cleanPath}`
}

export const getImageUrl = (imagePath: string | null | undefined): string | null => {
  if (!imagePath) return null
  return `${config.imageBaseURL}/${imagePath}`
}
