<template>
  <div class="page-container">
    <el-container>
      <el-main class="page-main">
        <el-card shadow="hover" class="posts-card">
          <template #header>
            <div class="card-header">
              <el-icon class="card-header-icon"><Document /></el-icon>
              <span>MySQL 포스트 목록</span>
              <el-tag type="info" size="small" style="margin-left: 8px;">{{ posts.length }}개</el-tag>
            </div>
          </template>
          <div class="api-demo">
            <el-space direction="vertical" :size="20" style="width: 100%">
              <div class="posts-header">
                <el-space :size="16" style="width: 100%; justify-content: space-between; flex-wrap: wrap;">
                  <el-input
                    v-model="searchKeyword"
                    placeholder="제목 또는 내용으로 검색..."
                    clearable
                    style="width: 300px;"
                    @keyup.enter="handleSearch"
                    @clear="handleSearch">
                    <template #prefix>
                      <el-icon><Search /></el-icon>
                    </template>
                    <template #append>
                      <el-button :icon="Search" @click="handleSearch">검색</el-button>
                    </template>
                  </el-input>
                  <el-space>
                    <el-button type="success" @click="openCreateDialog" :icon="Plus">
                      글쓰기
                    </el-button>
                    <el-button type="primary" :loading="loading" @click="fetchPosts" :icon="Refresh">
                      {{ loading ? '로딩 중...' : '새로고침' }}
                    </el-button>
                  </el-space>
                </el-space>
              </div>
              
              
              <!-- 데스크톱 테이블 뷰 -->
              <el-table
                v-if="posts.length > 0"
                :data="posts"
                stripe
                class="posts-table desktop-table"
                style="width: 100%"
                :max-height="650"
                table-layout="auto">
                <el-table-column prop="id" label="ID" width="70" align="center" fixed="left">
                  <template #default="{ row }">
                    <el-tag size="small" type="info">{{ row.id }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="title" label="제목" min-width="180" show-overflow-tooltip>
                  <template #default="{ row }">
                    <el-link 
                      type="primary" 
                      underline="never" 
                      @click="$router.push(`/posts/${row.id}`)"
                      class="table-title-link">
                      {{ row.title }}
                    </el-link>
                  </template>
                </el-table-column>
                <el-table-column prop="content" label="내용" min-width="250" show-overflow-tooltip>
                  <template #default="{ row }">
                    <span class="table-content">{{ row.content }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="이미지" width="110" align="center">
                  <template #default="{ row }">
                    <div class="image-cell">
                      <img 
                        v-if="row.imagePath" 
                        :src="getImageUrl(row.imagePath) || ''" 
                        class="post-thumbnail"
                        @click="openImageDialog(row)"
                        alt="이미지" />
                      <el-tag v-else size="small" type="info">없음</el-tag>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="이미지 업로드" width="140" align="center">
                  <template #default="{ row }">
                    <el-upload
                      :http-request="createImageUploadHandler(row.id)"
                      :show-file-list="false"
                      accept="image/*">
                      <el-button type="primary" size="small" plain>업로드</el-button>
                    </el-upload>
                  </template>
                </el-table-column>
                <el-table-column label="생성일" width="170" align="center">
                  <template #default="{ row }">
                    <span class="date-text">{{ formatDate(row.createdAt) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="작업" width="90" align="center" fixed="right">
                  <template #default="{ row }">
                    <el-button 
                      type="danger" 
                      size="small" 
                      plain
                      @click="handleDelete(row)"
                      :loading="deletingId === row.id">
                      삭제
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              
              <!-- 모바일 카드 뷰 -->
              <div v-if="posts.length > 0" class="mobile-posts-list">
                <el-card 
                  v-for="post in posts" 
                  :key="post.id" 
                  class="mobile-post-card"
                  shadow="hover">
                  <div class="mobile-post-header">
                    <el-tag size="small" type="info" class="mobile-post-id">ID: {{ post.id }}</el-tag>
                    <span class="mobile-post-date">{{ formatDate(post.createdAt) }}</span>
                  </div>
                  <div class="mobile-post-content">
                    <h3 class="mobile-post-title">{{ post.title }}</h3>
                    <p class="mobile-post-text">{{ post.content }}</p>
                  </div>
                  <div class="mobile-post-image" v-if="post.imagePath">
                    <img 
                      :src="getImageUrl(post.imagePath) || ''" 
                      class="mobile-thumbnail"
                      @click="openImageDialog(post)"
                      alt="이미지" />
                  </div>
                  <div class="mobile-post-actions">
                    <el-upload
                      :http-request="createImageUploadHandler(post.id)"
                      :show-file-list="false"
                      accept="image/*">
                      <el-button type="primary" size="small" plain>이미지 업로드</el-button>
                    </el-upload>
                    <el-button 
                      type="danger" 
                      size="small" 
                      plain
                      @click="handleDelete(post)"
                      :loading="deletingId === post.id">
                      삭제
                    </el-button>
                  </div>
                </el-card>
              </div>
              
              <!-- 이미지 미리보기 다이얼로그 -->
              <el-dialog 
                v-model="imageDialogVisible" 
                title="이미지 미리보기" 
                :width="isMobile ? '90%' : '600px'"
                align-center
                class="image-preview-dialog">
                <div style="text-align: center;">
                  <img :src="selectedImageUrl" style="max-width: 100%; max-height: 500px;" alt="이미지" />
                </div>
              </el-dialog>
              
              <!-- 글쓰기 다이얼로그 -->
              <el-dialog 
                v-model="createDialogVisible" 
                title="새 포스트 작성" 
                :width="isMobile ? '90%' : '600px'"
                @close="resetCreateForm"
                align-center>
                <el-form :model="newPost" label-width="80px">
                  <el-form-item label="제목" required>
                    <el-input 
                      v-model="newPost.title" 
                      placeholder="제목을 입력하세요"
                      maxlength="200"
                      show-word-limit />
                  </el-form-item>
                  <el-form-item label="내용" required>
                    <el-input 
                      v-model="newPost.content" 
                      type="textarea" 
                      :rows="6"
                      placeholder="내용을 입력하세요"
                      maxlength="5000"
                      show-word-limit />
                  </el-form-item>
                </el-form>
                <template #footer>
                  <el-space>
                    <el-button @click="createDialogVisible = false">취소</el-button>
                    <el-button type="primary" @click="handleCreate" :loading="creating">
                      작성하기
                    </el-button>
                  </el-space>
                </template>
              </el-dialog>
              
              <el-empty v-if="!loading && posts.length === 0 && !error" description="데이터가 없습니다." />
              
              <!-- 페이지네이션 (데스크톱) -->
              <el-pagination
                v-if="totalPages > 0 && !isMobile"
                v-model:current-page="currentPage"
                :page-size="pageSize"
                :total="totalCount"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleSizeChange"
                @current-change="handlePageChange"
                style="margin-top: 20px; justify-content: center;" />
              
              <!-- 페이지네이션 (모바일) -->
              <div v-if="totalPages > 0 && isMobile" class="mobile-pagination">
                <div class="mobile-pagination-info">
                  <span>{{ currentPage }} / {{ totalPages }} 페이지</span>
                  <span class="mobile-pagination-total">(총 {{ totalCount }}개)</span>
                </div>
                <div class="mobile-pagination-controls">
                  <el-button 
                    :disabled="currentPage === 1" 
                    @click="handlePageChange(currentPage - 1)"
                    :icon="ArrowLeft"
                    circle>
                  </el-button>
                  <span class="mobile-pagination-current">{{ currentPage }}</span>
                  <el-button 
                    :disabled="currentPage === totalPages" 
                    @click="handlePageChange(currentPage + 1)"
                    :icon="ArrowRight"
                    circle>
                  </el-button>
                </div>
              </div>
              
              <el-divider />
              
              <div style="text-align: center;">
                <el-button type="primary" @click="$router.push('/')">홈으로</el-button>
              </div>
            </el-space>
          </div>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Document, Refresh, Search, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { postsApi, Post } from '@/api/posts'
import { getImageUrl, getApiUrl } from '@/config'
import { formatDate } from '@/utils/date'
import { useWindowSize } from '@vueuse/core'

const posts = ref<Post[]>([])
const loading = ref<boolean>(false)
const error = ref<string>('')
const imageDialogVisible = ref<boolean>(false)
const selectedImageUrl = ref<string>('')
const createDialogVisible = ref<boolean>(false)
const creating = ref<boolean>(false)
const deletingId = ref<number | null>(null)
const newPost = ref<{
  title: string
  content: string
}>({
  title: '',
  content: ''
})

// 페이지네이션
const currentPage = ref<number>(1)
const pageSize = ref<number>(10)
const totalCount = ref<number>(0)
const totalPages = ref<number>(0)

// 검색
const searchKeyword = ref<string>('')

// 반응형 화면 크기 감지 (VueUse)
const { width } = useWindowSize()
const isMobile = computed(() => width.value <= 768)

const fetchPosts = async () => {
  loading.value = true
  error.value = ''
  
  try {
    // 페이지는 0부터 시작하므로 currentPage - 1
    const response = await postsApi.getAllPosts(
      currentPage.value - 1, 
      pageSize.value, 
      searchKeyword.value.trim() || undefined
    )
    if (response.data && 'posts' in response.data) {
      posts.value = response.data.posts
      totalCount.value = response.data.count || 0
      totalPages.value = response.data.totalPages || 0
    } else {
      posts.value = Array.isArray(response.data) ? response.data : []
      totalCount.value = 0
      totalPages.value = 0
    }
  } catch (err) {
    error.value = '데이터를 가져오는데 실패했습니다.'
    console.error(err)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1 // 검색 시 첫 페이지로 이동
  fetchPosts()
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchPosts()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1 // 페이지 크기가 변경되면 첫 페이지로
  fetchPosts()
}


const openImageDialog = (post: Post): void => {
  if (post.imagePath) {
    selectedImageUrl.value = getImageUrl(post.imagePath) || ''
    imageDialogVisible.value = true
  }
}

const createImageUploadHandler = (postId: number | undefined) => {
  return async (options: { file: File }) => {
    if (!postId) {
      ElMessage.error('포스트 ID를 찾을 수 없습니다.')
      return
    }

    const file = options.file
    if (!file) {
      ElMessage.error('파일을 선택해주세요.')
      return
    }

    try {
      const formData = new FormData()
      formData.append('file', file)

      // 쿠키가 자동으로 전송됨 (withCredentials: true)
      const response = await fetch(getApiUrl(`/posts/${postId}/image`), {
        method: 'POST',
        credentials: 'include', // 쿠키 자동 전송
        body: formData
      })

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}))
        throw new Error(errorData.message || '이미지 업로드에 실패했습니다.')
      }

      await response.json()
      ElMessage.success('이미지가 업로드되었습니다!')
      await fetchPosts()
    } catch (error: any) {
      console.error('이미지 업로드 실패:', error)
      ElMessage.error(error.message || '이미지 업로드에 실패했습니다.')
    }
  }
}

const openCreateDialog = () => {
  createDialogVisible.value = true
}

const resetCreateForm = () => {
  newPost.value = {
    title: '',
    content: ''
  }
}

const handleCreate = async () => {
  if (!newPost.value.title.trim()) {
    ElMessage.warning('제목을 입력해주세요.')
    return
  }
  if (!newPost.value.content.trim()) {
    ElMessage.warning('내용을 입력해주세요.')
    return
  }

  creating.value = true
  try {
    await postsApi.createPost({
      title: newPost.value.title.trim(),
      content: newPost.value.content.trim()
    })
    
    ElMessage.success('포스트가 작성되었습니다!')
    createDialogVisible.value = false
    resetCreateForm()
    await fetchPosts()
  } catch (err) {
    console.error('포스트 작성 실패:', err)
    ElMessage.error('포스트 작성에 실패했습니다.')
  } finally {
    creating.value = false
  }
}

const handleDelete = async (post: Post): Promise<void> => {
  try {
    await ElMessageBox.confirm(
      `"${post.title}" 포스트를 삭제하시겠습니까?`,
      '포스트 삭제',
      {
        confirmButtonText: '삭제',
        cancelButtonText: '취소',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )

    if (!post.id) {
      ElMessage.error('포스트 ID를 찾을 수 없습니다.')
      return
    }
    
    deletingId.value = post.id
    try {
      await postsApi.deletePost(post.id)
      ElMessage.success('포스트가 삭제되었습니다.')
      await fetchPosts()
    } catch (err) {
      console.error('포스트 삭제 실패:', err)
      ElMessage.error('포스트 삭제에 실패했습니다.')
    } finally {
      deletingId.value = null
    }
  } catch {
    ElMessage.info('삭제가 취소되었습니다.')
  }
}

onMounted(() => {
  fetchPosts()
})
</script>


