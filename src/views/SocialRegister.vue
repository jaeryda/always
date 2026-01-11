<template>
  <div class="page-container">
    <el-container>
      <el-main class="social-register-main">
        <el-card shadow="hover" class="social-register-card">
          <template #header>
            <div class="card-header">
              <el-icon class="card-header-icon"><UserFilled /></el-icon>
              <span>소셜 로그인 가입</span>
            </div>
          </template>

          <div class="social-register-info">
            <el-alert
              type="info"
              :closable="false"
              show-icon>
              <template #title>
                <div style="line-height: 1.6;">
                  <div style="font-weight: bold; margin-bottom: 8px;">{{ provider === 'kakao' ? '카카오' : provider === 'naver' ? '네이버' : provider }} 계정으로 가입하시겠습니까?</div>
                  <div style="font-size: 14px; color: #606266;">
                    <div>이메일: {{ email }}</div>
                    <div v-if="nickname">닉네임: {{ nickname }}</div>
                  </div>
                </div>
              </template>
            </el-alert>
          </div>

          <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            label-width="100px"
            style="margin-top: 24px;">
            <el-form-item label="사용자명" prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="사용자명을 입력하세요 (2자 이상)"
                clearable>
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
              <div style="font-size: 12px; color: #909399; margin-top: 4px;">
                기본값: {{ defaultUsername }}
              </div>
            </el-form-item>
          </el-form>

          <div style="margin-top: 24px;">
            <el-button
              type="primary"
              :loading="loading"
              @click="handleRegister"
              style="width: 100%">
              가입하기
            </el-button>
          </div>

          <div style="margin-top: 16px;">
            <el-button
              @click="handleCancel"
              style="width: 100%">
              취소
            </el-button>
          </div>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { FormInstance, FormRules } from 'element-plus'
import { User, UserFilled } from '@element-plus/icons-vue'
import { authApi } from '@/api/auth'
import { useAuthStore } from '@/store/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const registerFormRef = ref<FormInstance>()
const loading = ref(false)

// URL 파라미터에서 정보 가져오기
const email = ref('')
const nickname = ref('')
const kakaoId = ref('')
const naverId = ref('')
const provider = ref('kakao')

const defaultUsername = computed(() => {
  return nickname.value && nickname.value.trim() 
    ? nickname.value.trim() 
    : email.value.split('@')[0]
})

const registerForm = reactive({
  username: ''
})

const registerRules: FormRules = {
  username: [
    { required: true, message: '사용자명을 입력해주세요', trigger: 'blur' },
    { min: 2, max: 50, message: '사용자명은 2자 이상 50자 이하로 입력해주세요', trigger: 'blur' }
  ]
}

onMounted(() => {
  // URL 파라미터에서 정보 가져오기
  email.value = (route.query.email as string) || ''
  nickname.value = (route.query.nickname as string) || ''
  kakaoId.value = (route.query.kakaoId as string) || ''
  naverId.value = (route.query.naverId as string) || ''
  provider.value = (route.query.provider as string) || 'kakao'
  
  // 기본 사용자명 설정
  registerForm.username = defaultUsername.value
  
  // 필수 정보 확인
  if (!email.value || (!kakaoId.value && !naverId.value)) {
    ElMessage.error('필수 정보가 없습니다.')
    router.push('/login')
  }
})

const handleRegister = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        let response
        if (provider.value === 'naver') {
          response = await authApi.registerWithNaver({
            email: email.value,
            nickname: nickname.value,
            naverId: naverId.value,
            username: registerForm.username
          })
        } else {
          response = await authApi.registerWithKakao({
            email: email.value,
            nickname: nickname.value,
            kakaoId: kakaoId.value,
            username: registerForm.username
          })
        }
        
        const data = response.data as any
        if (data.success) {
          ElMessage.success('가입이 완료되었습니다.')
          // 로그인 상태 복원 (authStore를 통해)
          await authStore.restoreAuth()
          router.push('/')
        } else {
          ElMessage.error(data.message || '가입에 실패했습니다.')
        }
      } catch (error: any) {
        const errorMessage = error.response?.data?.message || '가입 중 오류가 발생했습니다.'
        ElMessage.error(errorMessage)
      } finally {
        loading.value = false
      }
    }
  })
}

const handleCancel = () => {
  router.push('/login')
}
</script>

<style scoped>
.social-register-main {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 60px);
  padding: 2rem;
}

.social-register-card {
  width: 100%;
  max-width: 500px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
}

.card-header-icon {
  font-size: 20px;
}

.social-register-info {
  margin-bottom: 16px;
}
</style>

