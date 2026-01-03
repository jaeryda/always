<template>
  <div class="page-container">
    <el-container>
      <el-main class="page-main">
        <el-card shadow="hover" v-if="user">
          <template #header>
            <div class="card-header">
              <el-icon class="card-header-icon"><User /></el-icon>
              <span>사용자 프로필</span>
            </div>
          </template>
          
          <div class="profile-content">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="사용자 ID">
                <el-tag type="info">{{ user.id }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="사용자명">
                {{ user.username }}
              </el-descriptions-item>
              <el-descriptions-item label="이메일">
                {{ user.email }}
              </el-descriptions-item>
              <el-descriptions-item label="가입일">
                {{ formatDate(user.createdAt) }}
              </el-descriptions-item>
            </el-descriptions>
            
            <div v-if="isOwnProfile" class="profile-actions" style="margin-top: 20px;">
              <el-button type="primary" @click="handleEdit">프로필 수정</el-button>
            </div>
          </div>
        </el-card>
        
        <el-card shadow="hover" style="margin-top: 20px;">
          <template #header>
            <div class="card-header">
              <el-icon class="card-header-icon"><Document /></el-icon>
              <span>작성한 포스트</span>
            </div>
          </template>
          
          <div v-if="loading" class="loading-container">
            <el-skeleton :rows="5" animated />
          </div>
          
          <div v-else-if="userPosts.length === 0" class="empty-container">
            <el-empty description="작성한 포스트가 없습니다." />
          </div>
          
          <div v-else class="posts-list">
            <el-table :data="userPosts" stripe style="width: 100%">
              <el-table-column prop="id" label="ID" width="70" align="center">
                <template #default="{ row }">
                  <el-tag size="small" type="info">{{ row.id }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="title" label="제목" min-width="200">
                <template #default="{ row }">
                  <el-link 
                    type="primary" 
                    underline="never" 
                    @click="$router.push(`/posts/${row.id}`)">
                    {{ row.title }}
                  </el-link>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="작성일" width="170" align="center">
                <template #default="{ row }">
                  {{ formatDate(row.createdAt) }}
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Document } from '@element-plus/icons-vue'
import { authApi, User as UserType } from '@/api/auth'
import { postsApi, Post } from '@/api/posts'
import { formatDate } from '@/utils/date'
import { useAuthStore } from '@/store/auth'

const route = useRoute()
const authStore = useAuthStore()

const user = ref<UserType | null>(null)
const userPosts = ref<Post[]>([])
const loading = ref<boolean>(false)

const userId = computed(() => {
  const id = route.params.id
  return id ? Number(id) : authStore.user?.id
})

const isOwnProfile = computed(() => {
  return authStore.user && userId.value === authStore.user.id
})

const fetchUser = async () => {
  if (!userId.value) {
    ElMessage.error('사용자 ID를 찾을 수 없습니다.')
    return
  }
  
  loading.value = true
  try {
    const response = await authApi.getUserById(userId.value)
    const data = response.data as any
    if (data.success && data.user) {
      user.value = data.user
      await fetchUserPosts()
    } else {
      ElMessage.error(data.message || '사용자 정보를 불러올 수 없습니다.')
    }
  } catch (error: any) {
    console.error('사용자 정보 조회 실패:', error)
    ElMessage.error(error.response?.data?.message || '사용자 정보를 불러올 수 없습니다.')
  } finally {
    loading.value = false
  }
}

const fetchUserPosts = async () => {
  if (!userId.value) return
  
  try {
    const response = await postsApi.getPostsByAuthor(userId.value, 0, 100)
    const data = response.data as any
    if (data.posts) {
      userPosts.value = data.posts
    } else if (Array.isArray(data)) {
      userPosts.value = data
    }
  } catch (error) {
    console.error('사용자 포스트 조회 실패:', error)
  }
}

const handleEdit = () => {
  ElMessage.info('프로필 수정 기능은 준비 중입니다.')
}

onMounted(() => {
  fetchUser()
})
</script>

