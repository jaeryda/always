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
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { FormInstance, FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/auth'

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
</script>

<style scoped>
.login-main {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 60px);
  padding: 2rem;
}

.login-card {
  width: 100%;
  max-width: 450px;
}

.login-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  width: 100%;
  font-size: 14px;
  color: #606266;
}
</style>

