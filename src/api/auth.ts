import api from './index'
import { AxiosResponse } from 'axios'

export interface User {
  id: number
  username: string
  email: string
  createdAt?: string
}

export interface RegisterData {
  username: string
  password: string
  email: string
}

export interface LoginData {
  username: string
  password: string
}

export interface KakaoLoginData {
  accessToken: string
}

export interface AuthResponse {
  success: boolean
  message: string
  token?: string
  user?: User
  errors?: any[]
}

export const authApi = {
  /**
   * 회원가입
   */
  register: async (data: RegisterData): Promise<AxiosResponse<AuthResponse>> => {
    return api.post('/auth/register', data)
  },

  /**
   * 로그인
   */
  login: async (data: LoginData): Promise<AxiosResponse<AuthResponse>> => {
    return api.post('/auth/login', data)
  },

  /**
   * 현재 사용자 정보 조회 (토큰 검증)
   */
  getCurrentUser: async (): Promise<AxiosResponse<AuthResponse>> => {
    return api.get('/auth/me')
  },

  /**
   * 사용자 ID로 사용자 정보 조회
   */
  getUserById: async (userId: number): Promise<AxiosResponse<AuthResponse>> => {
    return api.get(`/auth/users/${userId}`)
  },

  /**
   * 로그아웃 (쿠키 삭제)
   */
  logout: async (): Promise<AxiosResponse<AuthResponse>> => {
    return api.post('/auth/logout')
  },

  /**
   * 카카오 로그인
   */
  loginWithKakao: async (data: KakaoLoginData): Promise<AxiosResponse<AuthResponse>> => {
    return api.post('/auth/kakao/login', data)
  }
}

