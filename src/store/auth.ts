import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi, User } from '@/api/auth'
import { ElMessage } from 'element-plus'
import router from '@/router'

const ROLE_STORAGE_KEY = 'always_user_role'
const AUTH_STORAGE_KEY = 'always_auth_state'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)
  const loading = ref<boolean>(false)

  const isAuthenticated = computed(() => token.value !== null && user.value !== null)
  const username = computed(() => user.value?.username || '')
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  const saveSession = (nextUser: User | null) => {
    if (!nextUser) {
      localStorage.removeItem(ROLE_STORAGE_KEY)
      localStorage.removeItem(AUTH_STORAGE_KEY)
      return
    }
    localStorage.setItem(ROLE_STORAGE_KEY, nextUser.role === 'ADMIN' ? 'ADMIN' : 'USER')
    localStorage.setItem(AUTH_STORAGE_KEY, '1')
  }

  async function login(username: string, password: string): Promise<boolean> {
    loading.value = true
    try {
      const response = await authApi.login({ username, password })
      const data = response.data

      if (data.success && data.user) {
        user.value = data.user
        token.value = 'cookie-based'
        saveSession(data.user)
        ElMessage.success('로그인되었습니다.')
        return true
      }

      ElMessage.error('아이디 또는 비밀번호를 확인해주세요.')
      return false
    } catch (error: any) {
      ElMessage.error('로그인 중 오류가 발생했습니다.')
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
        ElMessage.success('회원가입이 완료되었습니다.')
        return true
      }

      ElMessage.error('회원가입에 실패했습니다. 입력값을 확인해주세요.')
      return false
    } catch (error: any) {
      ElMessage.error('회원가입 중 오류가 발생했습니다.')
      return false
    } finally {
      loading.value = false
    }
  }

  async function logout(): Promise<void> {
    try {
      const response = await authApi.logout()
      const data = response.data as any

      if (data.kakaoLogoutUrl && data.isKakaoUser) {
        user.value = null
        token.value = null
        saveSession(null)
        window.location.href = data.kakaoLogoutUrl
        return
      }
    } catch (error) {
      console.error('로그아웃 요청 실패:', error)
    } finally {
      user.value = null
      token.value = null
      saveSession(null)
      ElMessage.success('로그아웃되었습니다')
      router.push('/login')
    }
  }

  async function restoreAuth(forceCheck = false): Promise<void> {
    const shouldTry = forceCheck || localStorage.getItem(AUTH_STORAGE_KEY) === '1'
    if (!shouldTry) {
      user.value = null
      token.value = null
      return
    }

    try {
      const response = await authApi.getCurrentUser()
      const data = response.data
      if (data.success && data.user) {
        user.value = data.user
        token.value = 'cookie-based'
        saveSession(data.user)
      } else {
        user.value = null
        token.value = null
        saveSession(null)
      }
    } catch {
      user.value = null
      token.value = null
      saveSession(null)
    }
  }

  function setUser(newUser: User | null): void {
    user.value = newUser
    saveSession(newUser)
  }

  return {
    user,
    token,
    loading,
    isAuthenticated,
    username,
    isAdmin,
    login,
    register,
    logout,
    setUser,
    restoreAuth
  }
})
