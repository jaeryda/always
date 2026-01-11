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
            <div class="social-login-container">
              <button
                id="kakao-login-btn"
                type="button"
                @click="loginWithKakao"
                class="kakao-login-button">
                <img 
                  src="@/assets/kakao_login_medium_wide.png" 
                  alt="카카오 로그인 버튼" />
              </button>
            </div>
            <div class="social-login-container" style="margin-top: 10px;">
              <button
                id="naver-login-btn"
                type="button"
                @click="loginWithNaver"
                class="naver-login-button">
                <img 
                  src="@/assets/NAVER_login_Dark_KR_green_wide_H56.png" 
                  alt="네이버 로그인 버튼" />
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
    { min: 2, max: 50, message: '사용자명은 2자 이상 50자 이하로 입력해주세요', trigger: 'blur' }
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

// 카카오 로그인 (REST API 방식)
const loginWithKakao = () => {
  if (typeof window === 'undefined') return
  
  console.log('=== [Vue] 카카오 로그인 시작 ===')
  console.log('[Vue] kakaoRestApiKey:', config.kakaoRestApiKey)
  console.log('[Vue] kakaoRedirectUri:', config.kakaoRedirectUri)
  
  // 카카오 인가 코드 요청 URL로 리다이렉트
  const kakaoAuthUrl = `https://kauth.kakao.com/oauth/authorize?client_id=${config.kakaoRestApiKey}&redirect_uri=${encodeURIComponent(config.kakaoRedirectUri)}&response_type=code`
  console.log('[Vue] 카카오 인가 URL:', kakaoAuthUrl)
  window.location.href = kakaoAuthUrl
}

const loginWithNaver = () => {
  console.log('[Vue] 네이버 로그인 시작')
  console.log('[Vue] naverClientId:', config.naverClientId)
  console.log('[Vue] naverRedirectUri:', config.naverRedirectUri)
  
  // 네이버 인가 코드 요청 URL로 리다이렉트
  // state는 CSRF 공격 방지를 위한 임의의 문자열 (실제로는 서버에서 생성하는 것이 좋음)
  const state = Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15)
  sessionStorage.setItem('naver_login_state', state)
  
  const naverAuthUrl = `https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=${config.naverClientId}&redirect_uri=${encodeURIComponent(config.naverRedirectUri)}&state=${state}`
  console.log('[Vue] 네이버 인가 URL:', naverAuthUrl)
  window.location.href = naverAuthUrl
}

// 카카오 로그인 콜백 처리 (백엔드에서 리다이렉트 후)
onMounted(() => {
  if (typeof window === 'undefined') return
  
  console.log('=== [Vue] Login 컴포넌트 마운트 ===')
  console.log('[Vue] 현재 URL:', window.location.href)
  console.log('[Vue] 현재 pathname:', window.location.pathname)
  console.log('[Vue] 현재 search:', window.location.search)
  
  // URL 파라미터에서 카카오 로그인 성공 여부 확인
  const urlParams = new URLSearchParams(window.location.search)
  const kakaoLogin = urlParams.get('kakaoLogin')
  console.log('[Vue] kakaoLogin 파라미터:', kakaoLogin)
  
  if (kakaoLogin === 'success') {
    console.log('[Vue] 카카오 로그인 성공 감지 - 사용자 정보 복원 시작')
    // 카카오 로그인 성공 시 사용자 정보 복원
    authStore.restoreAuth().then(() => {
      console.log('[Vue] 사용자 정보 복원 완료:', authStore.user)
      ElMessage.success('카카오 로그인 성공')
      router.push('/')
    }).catch((error) => {
      console.error('[Vue] 사용자 정보 복원 실패:', error)
    })
    
    // URL에서 파라미터 제거
    window.history.replaceState({}, document.title, window.location.pathname)
  }
})
</script>
