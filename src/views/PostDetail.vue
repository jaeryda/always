<template>
  <div class="page-container">
    <el-container>
      <el-main class="post-main">
        <div style="margin-bottom: 16px;">
          <el-button 
            type="primary" 
            @click="$router.push('/posts')" 
            :icon="ArrowLeft"
            class="back-button">
            목록으로
          </el-button>
        </div>
        <el-card shadow="hover" v-loading="loading">
          <template #header>
            <div class="card-header">
              <el-icon class="card-header-icon"><Document /></el-icon>
              <span>포스트 상세 정보</span>
            </div>
          </template>
          
          <div v-if="error" class="error-message">
            <el-alert :title="error" type="error" :closable="false" show-icon />
          </div>
          
          <div v-if="!loading && post" class="post-content">
            <!-- 편집 모드 -->
            <div v-if="isEditing" class="edit-mode">
              <el-form :model="editForm" label-width="100px" label-position="top">
                <el-form-item label="제목" required>
                  <el-input 
                    v-model="editForm.title" 
                    placeholder="제목을 입력하세요"
                    maxlength="200"
                    show-word-limit
                    clearable />
                </el-form-item>
                
                <el-form-item label="내용" required>
                  <el-input 
                    v-model="editForm.content" 
                    type="textarea" 
                    :rows="10"
                    placeholder="내용을 입력하세요"
                    maxlength="5000"
                    show-word-limit />
                </el-form-item>
                
                <el-form-item>
                  <el-space>
                    <el-button type="primary" @click="handleUpdate" :loading="updating">
                      저장
                    </el-button>
                    <el-button @click="cancelEdit">취소</el-button>
                  </el-space>
                </el-form-item>
              </el-form>
            </div>
            
            <!-- 보기 모드 -->
            <div v-else class="view-mode">
              <div class="post-info">
                <el-tag type="info" size="large">ID: {{ post.id }}</el-tag>
                <span class="post-date">{{ formatDate(post.createdAt) }}</span>
              </div>
              
              <div class="post-title">
                <h2>{{ post.title }}</h2>
              </div>
              
              <div class="post-body">
                <p>{{ post.content }}</p>
              </div>
              
              <div v-if="post.imagePath" class="post-image">
                <img 
                  :src="getImageUrl(post.imagePath) || undefined" 
                  class="detail-image"
                  @click="openImageDialog"
                  alt="이미지" />
              </div>
              
              <div class="post-actions">
                <el-space wrap>
                  <el-button type="primary" @click="startEdit" :icon="Edit">
                    수정
                  </el-button>
                  <el-upload
                    :http-request="handleImageUpload"
                    :show-file-list="false"
                    accept="image/*">
                    <el-button type="success" :icon="Upload">이미지 업로드</el-button>
                  </el-upload>
                  <el-button 
                    type="danger" 
                    @click="handleDelete"
                    :loading="deleting"
                    :icon="Delete">
                    삭제
                  </el-button>
                </el-space>
              </div>
            </div>
          </div>
        </el-card>
      </el-main>
    </el-container>
    
    <!-- 이미지 미리보기 다이얼로그 -->
    <el-dialog 
      v-model="imageDialogVisible" 
      title="이미지 미리보기" 
      width="90%"
      align-center>
      <div style="text-align: center;">
        <img 
          v-if="post?.imagePath" 
          :src="getImageUrl(post.imagePath) || undefined" 
          style="max-width: 100%; max-height: 70vh;" 
          alt="이미지" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Document, Edit, Upload, Delete } from '@element-plus/icons-vue'
import { postsApi, Post } from '@/api/posts'
import { getImageUrl, getApiUrl } from '@/config'
import { formatDate } from '@/utils/date'

const route = useRoute()
const router = useRouter()
const post = ref<Post | null>(null)
const loading = ref<boolean>(false)
const updating = ref<boolean>(false)
const deleting = ref<boolean>(false)
const error = ref<string>('')
const isEditing = ref<boolean>(false)
const imageDialogVisible = ref<boolean>(false)

const editForm = ref({
  title: '',
  content: ''
})

const fetchPost = async () => {
  const postId = Number(route.params.id)
  if (!postId) {
    error.value = '포스트 ID가 유효하지 않습니다.'
    return
  }

  loading.value = true
  error.value = ''
  
  try {
    const response = await postsApi.getPostById(postId)
    if (response.data && 'post' in response.data) {
      post.value = response.data.post as Post
    } else {
      post.value = response.data as Post
    }
    
    if (post.value) {
      editForm.value = {
        title: post.value.title,
        content: post.value.content
      }
    }
  } catch (err) {
    error.value = '포스트를 불러오는데 실패했습니다.'
    console.error(err)
    ElMessage.error('포스트를 불러오는데 실패했습니다.')
  } finally {
    loading.value = false
  }
}


const startEdit = () => {
  if (post.value) {
    editForm.value = {
      title: post.value.title,
      content: post.value.content
    }
    isEditing.value = true
  }
}

const cancelEdit = () => {
  isEditing.value = false
  if (post.value) {
    editForm.value = {
      title: post.value.title,
      content: post.value.content
    }
  }
}

const handleUpdate = async () => {
  if (!post.value?.id) {
    ElMessage.error('포스트 ID를 찾을 수 없습니다.')
    return
  }
  
  if (!editForm.value.title.trim()) {
    ElMessage.warning('제목을 입력해주세요.')
    return
  }
  
  if (!editForm.value.content.trim()) {
    ElMessage.warning('내용을 입력해주세요.')
    return
  }

  updating.value = true
  try {
    await postsApi.updatePost(post.value.id, {
      title: editForm.value.title.trim(),
      content: editForm.value.content.trim()
    })
    
    ElMessage.success('포스트가 수정되었습니다!')
    isEditing.value = false
    await fetchPost()
  } catch (err) {
    console.error('포스트 수정 실패:', err)
    ElMessage.error('포스트 수정에 실패했습니다.')
  } finally {
    updating.value = false
  }
}

const handleDelete = async () => {
  if (!post.value?.id) {
    ElMessage.error('포스트 ID를 찾을 수 없습니다.')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `"${post.value.title}" 포스트를 삭제하시겠습니까?`,
      '포스트 삭제',
      {
        confirmButtonText: '삭제',
        cancelButtonText: '취소',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )

    deleting.value = true
    try {
      await postsApi.deletePost(post.value.id)
      ElMessage.success('포스트가 삭제되었습니다.')
      router.push('/posts')
    } catch (err) {
      console.error('포스트 삭제 실패:', err)
      ElMessage.error('포스트 삭제에 실패했습니다.')
    } finally {
      deleting.value = false
    }
  } catch {
    ElMessage.info('삭제가 취소되었습니다.')
  }
}

const openImageDialog = () => {
  imageDialogVisible.value = true
}

const handleImageUpload = async (options: { file: File }): Promise<void> => {
  if (!post.value?.id) {
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
    const response = await fetch(getApiUrl(`/posts/${post.value.id}/image`), {
      method: 'POST',
      credentials: 'include', // 쿠키 자동 전송
      body: formData
    })

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      throw new Error(errorData.message || '이미지 업로드에 실패했습니다.')
    }

    ElMessage.success('이미지가 업로드되었습니다!')
    await fetchPost()
  } catch (error: any) {
    console.error('이미지 업로드 실패:', error)
    ElMessage.error(error.message || '이미지 업로드에 실패했습니다.')
  }
}

onMounted(() => {
  fetchPost()
})
</script>


