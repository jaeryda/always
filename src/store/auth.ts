import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi, User } from '@/api/auth'
import { ElMessage } from 'element-plus'
import router from '@/router'

export const useAuthStore = defineStore('auth', () => {
  // state
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)
  const loading = ref<boolean>(false)

  // getters
  const isAuthenticated = computed(() => token.value !== null && user.value !== null)
  const username = computed(() => user.value?.username || '')

  // actions
  async function login(username: string, password: string): Promise<boolean> {
    loading.value = true
    try {
      const response = await authApi.login({ username, password })
      const data = response.data

      if (data.success && data.user) {
        user.value = data.user
        // 쿠키에 토큰이 저장되므로 별도 저장 불필요
        // token은 restoreAuth에서 쿠키 기반으로 설정됨
        token.value = 'cookie-based' // 쿠키 사용 표시
        ElMessage.success(data.message || '로그인 성공')
        return true
      } else {
        ElMessage.error(data.message || '로그인 실패')
        return false
      }
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || '로그인 중 오류가 발생했습니다.'
      ElMessage.error(errorMessage)
      return false
    } finally {
      loading.value = false
    }
  }

  async function register(username: string, password: string, email: string): Promise<boolean> {
    loading.value = true
    try {
      const response = await authApi.register({ username, password, email })
      const data = response.data

      if (data.success) {
        ElMessage.success(data.message || '회원가입 성공')
        return true
      } else {
        ElMessage.error(data.message || '회원가입 실패')
        return false
      }
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || '회원가입 중 오류가 발생했습니다.'
      ElMessage.error(errorMessage)
      return false
    } finally {
      loading.value = false
    }
  }

  async function logout(): Promise<void> {
    try {
      // 백엔드에 로그아웃 요청 (쿠키 삭제)
      await authApi.logout()
    } catch (error) {
      console.error('로그아웃 요청 실패:', error)
    } finally {
      user.value = null
      token.value = null
      ElMessage.success('로그아웃되었습니다.')
      router.push('/login')
    }
  }

  // 앱 시작 시 인증 상태 복원 (쿠키 기반)
  async function restoreAuth(): Promise<void> {
    // 쿠키에서 토큰이 자동으로 전송되므로 /api/auth/me로 사용자 정보 확인
    try {
      const response = await authApi.getCurrentUser()
      const data = response.data
      if (data.success && data.user) {
        user.value = data.user
        token.value = 'cookie-based' // 쿠키 사용 표시
      } else {
        // 토큰이 유효하지 않음
        user.value = null
        token.value = null
      }
    } catch (error) {
      // 토큰이 유효하지 않거나 없음
      user.value = null
      token.value = null
    }
  }

  function setUser(newUser: User | null): void {
    user.value = newUser
  }

  return {
    user,
    token,
    loading,
    isAuthenticated,
    username,
    login,
    register,
    logout,
    setUser,
    restoreAuth
  }
})

