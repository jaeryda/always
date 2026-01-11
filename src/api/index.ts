import axios, { AxiosInstance, AxiosResponse, AxiosError } from 'axios'

import { config } from '@/config'

// Axios 인스턴스 생성
const api: AxiosInstance = axios.create({
  baseURL: config.apiBaseURL,
  timeout: 10000,
  withCredentials: true, // 쿠키 자동 전송
  headers: {
    'Content-Type': 'application/json'
  }
})

// 요청 인터셉터
api.interceptors.request.use(
  (config: any) => {
    // API 호출 로깅
    const method = config.method?.toUpperCase() || 'GET'
    const url = config.url || ''
    const fullUrl = (config.baseURL || '') + url
    console.log(`🚀 [API Request] ${method} ${fullUrl}`)
    if (config.params) {
      console.log('  Params:', config.params)
    }
    if (config.data) {
      console.log('  Data:', config.data)
    }
    
    // 쿠키를 사용하므로 Authorization 헤더는 선택적으로만 추가 (하위 호환성)
    // 쿠키가 자동으로 전송되므로 별도 설정 불필요
    return config
  },
  (error: AxiosError) => {
    console.error('❌ [API Request Error]', error)
    return Promise.reject(error)
  }
)

// 응답 인터셉터
api.interceptors.response.use(
  (response: AxiosResponse) => {
    // API 응답 로깅
    const method = response.config.method?.toUpperCase() || 'GET'
    const url = response.config.url || ''
    const fullUrl = (response.config.baseURL || '') + url
    const status = response.status
    const statusText = response.statusText
    
    console.log(`✅ [API Response] ${method} ${fullUrl} - ${status} ${statusText}`, response.data)
    
    // 응답 데이터를 그대로 반환
    return response
  },
  (error: AxiosError) => {
    // 에러 로깅
    if (error.response) {
      // 서버가 응답했지만 에러 상태 코드
      const method = error.config?.method?.toUpperCase() || 'GET'
      const url = error.config?.url || ''
      const fullUrl = (error.config?.baseURL || '') + url
      const status = error.response.status
      
      switch (status) {
        case 401:
          // 인증 에러 처리 (쿠키는 백엔드에서 처리)
          // /api/auth/me는 restoreAuth에서 호출되므로 조용히 처리 (정상)
          // 로그인 페이지나 소셜 가입 페이지에서는 401 오류를 조용히 처리 (정상적인 상황)
          if (typeof window !== 'undefined') {
            const isAuthMeEndpoint = url.includes('/auth/me')
            const isPublicPage = window.location.pathname.includes('/login') || 
                                 window.location.pathname.includes('/social-register') ||
                                 window.location.pathname.includes('/register')
            
            if (isAuthMeEndpoint || isPublicPage) {
              // /api/auth/me 또는 공개 페이지에서는 401 오류를 조용히 처리 (정상)
              break
            }
            
            console.error(`❌ [API Error] ${method} ${fullUrl} - ${status}`, error.response.data)
            console.error('인증 오류가 발생했습니다.')
            window.location.href = '/login'
          }
          break
        case 404:
          console.error('요청한 리소스를 찾을 수 없습니다.')
          break
        case 500:
          console.error('서버 오류가 발생했습니다.')
          break
        default:
          console.error('에러가 발생했습니다:', error.message)
      }
    } else if (error.request) {
      // 요청은 보냈지만 응답을 받지 못함
      console.error('❌ [API Network Error] 네트워크 오류가 발생했습니다.', error.request)
    } else {
      // 요청 설정 중 에러
      console.error('❌ [API Config Error] 요청 설정 오류:', error.message)
    }
    return Promise.reject(error)
  }
)

export default api

