<template>
  <div class="page-container">
    <el-container>
      <el-main class="login-main">
        <el-card shadow="hover" class="login-card">
          <template #header>
            <div class="card-header">
              <el-icon class="card-header-icon"><User /></el-icon>
              <span>로그인</span>
            </div>
          </template>

          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            label-width="80px"
            @submit.prevent="handleLogin">
            <el-form-item label="사용자명" prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="사용자명을 입력하세요"
                clearable
                @keyup.enter="handleLogin">
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="비밀번호" prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="비밀번호를 입력하세요"
                show-password
                clearable
                @keyup.enter="handleLogin">
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                :loading="authStore.loading"
                @click="handleLogin"
                style="width: 100%">
                로그인
              </el-button>
            </el-form-item>

            <el-form-item>
              <div class="login-footer">
                <span>계정이 없으신가요?</span>
                <el-link type="primary" @click="$router.push('/register')">
                  회원가입
                </el-link>
              </div>
            </el-form-item>
          </el-form>

          <el-divider>또는</el-divider>

          <el-form-item>
            <div class="kakao-login-container">
              <button
                id="kakao-login-btn"
                type="button"
                @click="loginWithKakao"
                class="kakao-login-button">
                <img 
                  src="https://k.kakaocdn.net/14/dn/btroDszwNrM/I6efHub1SN5KCJqLm1Ovx1/o.jpg" 
                  alt="카카오 로그인 버튼" 
                  width="222" />
              </button>
            </div>
          </el-form-item>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { FormInstance, FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/auth'
import { config } from '@/config'
import { authApi } from '@/api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

const loginFormRef = ref<FormInstance>()

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules: FormRules = {
  username: [
    { required: true, message: '사용자명을 입력해주세요', trigger: 'blur' },
    { min: 3, max: 50, message: '사용자명은 3자 이상 50자 이하로 입력해주세요', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '비밀번호를 입력해주세요', trigger: 'blur' },
    { min: 6, message: '비밀번호는 6자 이상이어야 합니다', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      const success = await authStore.login(loginForm.username, loginForm.password)
      if (success) {
        router.push('/')
      }
    }
  })
}

// 카카오 로그인
const loginWithKakao = () => {
  if (typeof window === 'undefined') return
  
  const Kakao = (window as any).Kakao
  if (!Kakao || !Kakao.Auth) {
    ElMessage.error('카카오 SDK를 로드할 수 없습니다.')
    return
  }

  if (!config.kakaoJavaScriptKey) {
    ElMessage.error('카카오 JavaScript 키가 설정되지 않았습니다.')
    return
  }

  // 카카오 SDK 초기화 (아직 초기화되지 않은 경우)
  if (!Kakao.isInitialized()) {
    Kakao.init(config.kakaoJavaScriptKey)
  }

  // 카카오 로그인 시작 (리다이렉트 방식)
  Kakao.Auth.authorize({
    redirectUri: config.kakaoRedirectUri
  })
}

// 카카오 SDK 초기화 및 콜백 처리
onMounted(() => {
  if (typeof window === 'undefined') return
  
  const Kakao = (window as any).Kakao
  // 카카오 SDK 초기화
  if (Kakao && config.kakaoJavaScriptKey) {
    if (!Kakao.isInitialized()) {
      Kakao.init(config.kakaoJavaScriptKey)
    }
  }

  // URL 파라미터에서 코드 확인 (카카오 인증 후 리다이렉트)
  const urlParams = new URLSearchParams(window.location.search)
  const code = urlParams.get('code')
  
  if (code && Kakao) {
    // 카카오 인증 코드가 있는 경우 처리
    handleKakaoCallback(code, Kakao)
  }
})

// 카카오 콜백 처리 함수
const handleKakaoCallback = async (code: string, Kakao: any) => {
  try {
    // 카카오 SDK가 이미 토큰을 쿠키에 저장했을 수 있으므로 확인
    if (Kakao && Kakao.Auth) {
      const token = Kakao.Auth.getAccessToken()
      if (token) {
        // 카카오 액세스 토큰이 있으면 백엔드로 전송
        await handleKakaoLogin(token)
        return
      }
    }

    // 토큰이 없으면 코드를 백엔드로 전송 (백엔드에서 처리)
    ElMessage.warning('카카오 로그인 처리 중...')
  } catch (error) {
    console.error('카카오 로그인 오류:', error)
    ElMessage.error('카카오 로그인 중 오류가 발생했습니다.')
  }
}

// 카카오 로그인 처리 (액세스 토큰을 백엔드로 전송)
const handleKakaoLogin = async (accessToken: string) => {
  try {
    const response = await authApi.loginWithKakao({ accessToken })
    const data = response.data

    if (data.success && data.user) {
      authStore.setUser(data.user)
      authStore.token = 'cookie-based'
      ElMessage.success('카카오 로그인 성공')
      router.push('/')
    } else {
      ElMessage.error(data.message || '카카오 로그인 실패')
    }
  } catch (error: any) {
    console.error('카카오 로그인 오류:', error)
    const errorMessage = error.response?.data?.message || '카카오 로그인 중 오류가 발생했습니다.'
    ElMessage.error(errorMessage)
  }
}
</script>
