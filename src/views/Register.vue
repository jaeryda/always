<template>
  <div class="page-container">
    <el-container>
      <el-main class="register-main">
        <el-card shadow="hover" class="register-card">
          <template #header>
            <div class="card-header">
              <el-icon class="card-header-icon"><UserFilled /></el-icon>
              <span>회원가입</span>
            </div>
          </template>

          <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            label-width="100px"
            @submit.prevent="handleRegister">
            <el-form-item label="사용자명" prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="사용자명을 입력하세요 (3자 이상)"
                clearable>
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="이메일" prop="email">
              <el-input
                v-model="registerForm.email"
                type="email"
                placeholder="이메일을 입력하세요"
                clearable>
                <template #prefix>
                  <el-icon><Message /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="비밀번호" prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="비밀번호를 입력하세요 (6자 이상)"
                show-password
                clearable>
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="비밀번호 확인" prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="비밀번호를 다시 입력하세요"
                show-password
                clearable>
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                :loading="authStore.loading"
                @click="handleRegister"
                style="width: 100%">
                회원가입
              </el-button>
            </el-form-item>

            <el-form-item>
              <div class="register-footer">
                <span>이미 계정이 있으신가요?</span>
                <el-link type="primary" @click="$router.push('/login')">
                  로그인
                </el-link>
              </div>
            </el-form-item>
          </el-form>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { FormInstance, FormRules } from 'element-plus'
import { User, UserFilled, Message, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const authStore = useAuthStore()

const registerFormRef = ref<FormInstance>()

const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('비밀번호를 다시 입력해주세요'))
  } else if (value !== registerForm.password) {
    callback(new Error('비밀번호가 일치하지 않습니다'))
  } else {
    callback()
  }
}

const registerRules: FormRules = {
  username: [
    { required: true, message: '사용자명을 입력해주세요', trigger: 'blur' },
    { min: 3, max: 50, message: '사용자명은 3자 이상 50자 이하로 입력해주세요', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '이메일을 입력해주세요', trigger: 'blur' },
    { type: 'email', message: '올바른 이메일 형식이 아닙니다', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '비밀번호를 입력해주세요', trigger: 'blur' },
    { min: 6, message: '비밀번호는 6자 이상이어야 합니다', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '비밀번호를 다시 입력해주세요', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      const success = await authStore.register(
        registerForm.username,
        registerForm.password,
        registerForm.email
      )
      if (success) {
        router.push('/login')
      }
    }
  })
}
</script>

<style scoped>
.register-main {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 60px);
  padding: 2rem;
}

.register-card {
  width: 100%;
  max-width: 500px;
}

.register-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  width: 100%;
  font-size: 14px;
  color: #606266;
}
</style>

