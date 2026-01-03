<template>
  <div class="page-container">
    <el-container>
      <el-main class="page-main">
        <el-row :gutter="24">
          <!-- Pinia Store 예제 -->
          <el-col :span="24" :lg="12">
            <el-card shadow="hover" class="store-card">
              <template #header>
                <div class="card-header">
                  <el-icon class="card-header-icon"><Box /></el-icon>
                  <span>Pinia Store 예제</span>
                </div>
              </template>
              <div class="store-demo">
                <el-space direction="vertical" :size="16" style="width: 100%">
                  
                  <el-input
                    v-model="userName"
                    placeholder="이름을 입력하세요"
                    @change="updateName">
                    <template #prepend>이름</template>
                  </el-input>
                  
                  <div class="counter">
                    <el-statistic title="카운터" :value="userStore.count">
                      <template #suffix>
                        <span style="font-size: 14px">번</span>
                      </template>
                    </el-statistic>
                    <el-statistic title="2배 값" :value="userStore.doubleCount" style="margin-top: 10px">
                      <template #suffix>
                        <span style="font-size: 14px">번</span>
                      </template>
                    </el-statistic>
                  </div>
                  
                  <el-space wrap>
                    <el-button type="primary" @click="userStore.increment">증가</el-button>
                    <el-button type="warning" @click="userStore.decrement">감소</el-button>
                    <el-button type="danger" @click="userStore.reset">리셋</el-button>
                  </el-space>
                </el-space>
              </div>
            </el-card>
          </el-col>

          <!-- 빠른 링크 -->
          <el-col :span="24" :lg="12">
            <el-card shadow="hover" class="quick-links-card">
              <template #header>
                <div class="card-header">
                  <el-icon class="card-header-icon"><Connection /></el-icon>
                  <span>빠른 링크</span>
                </div>
              </template>
              <div class="quick-links">
                <el-space direction="vertical" :size="16" style="width: 100%">
                  <el-card shadow="hover" class="link-card" @click="$router.push('/posts')">
                    <div class="link-content">
                      <el-icon class="link-icon" style="color: #409eff;"><Document /></el-icon>
                      <div class="link-text">
                        <div class="link-title">포스트 관리</div>
                        <div class="link-description">MySQL 데이터베이스에서 포스트를 관리합니다</div>
                      </div>
                      <el-icon class="link-arrow"><ArrowRight /></el-icon>
                    </div>
                  </el-card>
                  
                  <el-card shadow="hover" class="link-card" @click="$router.push('/account-book')">
                    <div class="link-content">
                      <el-icon class="link-icon" style="color: #409eff;"><CreditCard /></el-icon>
                      <div class="link-text">
                        <div class="link-title">가계부</div>
                        <div class="link-description">수입과 지출을 관리하고 통계를 확인합니다</div>
                      </div>
                      <el-icon class="link-arrow"><ArrowRight /></el-icon>
                    </div>
                  </el-card>
                  
                  <el-card shadow="hover" class="link-card" @click="$router.push('/about')">
                    <div class="link-content">
                      <el-icon class="link-icon" style="color: #67c23a;"><InfoFilled /></el-icon>
                      <div class="link-text">
                        <div class="link-title">About</div>
                        <div class="link-description">프로젝트 정보를 확인합니다</div>
                      </div>
                      <el-icon class="link-arrow"><ArrowRight /></el-icon>
                    </div>
                  </el-card>
                </el-space>
              </div>
            </el-card>
          </el-col>
        </el-row>
        
        <el-row :gutter="24" style="margin-top: 24px;">
          <!-- 통계 카드 -->
          <el-col :span="24" :lg="8">
            <el-card shadow="hover" class="stats-card">
              <div class="stats-content">
                <el-icon class="stats-icon" style="color: #409eff;"><Document /></el-icon>
                <div class="stats-info">
                  <div class="stats-value">{{ postCount }}</div>
                  <div class="stats-label">총 포스트</div>
                </div>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="24" :lg="8">
            <el-card shadow="hover" class="stats-card">
              <div class="stats-content">
                <el-icon class="stats-icon" style="color: #67c23a;"><User /></el-icon>
                <div class="stats-info">
                  <div class="stats-value">{{ userStore.name }}</div>
                  <div class="stats-label">현재 사용자</div>
                </div>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="24" :lg="8">
            <el-card shadow="hover" class="stats-card">
              <div class="stats-content">
                <el-icon class="stats-icon" style="color: #e6a23c;"><Menu /></el-icon>
                <div class="stats-info">
                  <div class="stats-value">{{ menuStore.visibleMenus.length }}</div>
                  <div class="stats-label">메뉴 수</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Box, Document, InfoFilled, Connection, ArrowRight, User, Menu, CreditCard } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { useMenuStore } from '@/store/menu'
import { postsApi } from '@/api/posts'

const userStore = useUserStore()
const menuStore = useMenuStore()
const userName = ref(userStore.name)
const postCount = ref<number>(0)

const updateName = () => {
  if (userName.value.trim()) {
    userStore.setName(userName.value)
  }
}

const fetchPostCount = async () => {
  try {
    const response = await postsApi.getAllPosts(0, 1) // 첫 페이지, 1개만 가져와서 count 확인
    const data = response.data as any
    if (data && 'count' in data) {
      postCount.value = data.count
    } else if (data && 'posts' in data && Array.isArray(data.posts)) {
      postCount.value = data.posts.length
    } else if (Array.isArray(data)) {
      postCount.value = data.length
    }
  } catch (err) {
    console.error('포스트 개수 가져오기 실패:', err)
  }
}

onMounted(() => {
  menuStore.fetchMenus(true)
  fetchPostCount()
})
</script>

