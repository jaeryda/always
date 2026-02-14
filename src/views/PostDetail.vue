<template>
  <div class="page-container">
    <el-container>
      <el-main class="post-main">
        <div style="margin-bottom: 16px;">
          <el-button type="primary" @click="$router.push('/posts')" :icon="ArrowLeft">목록으로</el-button>
        </div>

        <el-card shadow="hover" v-loading="loading">
          <template #header>
            <div class="card-header">
              <el-icon class="card-header-icon"><Document /></el-icon>
              <span>포스트 상세 정보</span>
            </div>
          </template>

          <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />

          <div v-if="!loading && post" class="post-content">
            <div v-if="isEditing" class="edit-mode">
              <el-form :model="editForm" label-width="100px" label-position="top">
                <el-form-item label="제목" required>
                  <el-input v-model="editForm.title" maxlength="200" show-word-limit clearable />
                </el-form-item>
                <el-form-item label="내용" required>
                  <el-input v-model="editForm.content" type="textarea" :rows="10" maxlength="5000" show-word-limit />
                </el-form-item>
                <el-form-item>
                  <el-space>
                    <el-button type="primary" @click="handleUpdate" :loading="updating">저장</el-button>
                    <el-button @click="cancelEdit">취소</el-button>
                  </el-space>
                </el-form-item>
              </el-form>
            </div>

            <div v-else class="view-mode">
              <div class="post-info">
                <el-tag type="info" size="large">ID: {{ post.id }}</el-tag>
                <span class="post-date">{{ formatDate(post.createdAt) }}</span>
              </div>

              <h2>{{ post.title }}</h2>
              <p>{{ post.content }}</p>

              <div v-if="post.imagePath" class="post-image">
                <img :src="getImageUrl(post.imagePath) || undefined" class="detail-image" @click="openImageDialog" alt="image" />
              </div>

              <div class="post-actions">
                <el-space wrap>
                  <el-button type="primary" @click="startEdit" :icon="Edit">수정</el-button>
                  <el-upload :http-request="handleImageUpload" :show-file-list="false" accept="image/*">
                    <el-button type="success" :icon="Upload">이미지 업로드</el-button>
                  </el-upload>
                  <el-button type="danger" @click="handleDelete" :loading="deleting" :icon="Delete">삭제</el-button>
                </el-space>
              </div>

              <el-divider />

              <div class="social-actions">
                <el-space>
                  <el-button :type="socialStore.liked ? 'danger' : 'default'" plain @click="toggleLikeAction">
                    좋아요 {{ socialStore.likeCount }}
                  </el-button>
                  <el-button :type="socialStore.bookmarked ? 'warning' : 'default'" plain @click="toggleBookmarkAction">
                    북마크 {{ socialStore.bookmarkCount }}
                  </el-button>
                </el-space>
              </div>

              <el-divider />

              <div class="comments-section">
                <h3>댓글 {{ socialStore.comments.length }}</h3>
                <el-input v-model="commentInput" type="textarea" :rows="3" placeholder="댓글을 입력하세요" maxlength="500" show-word-limit />
                <div style="margin-top: 8px;">
                  <el-button type="primary" @click="submitComment">댓글 등록</el-button>
                </div>

                <el-empty v-if="socialStore.comments.length === 0" description="아직 댓글이 없습니다." />
                <el-card v-for="item in socialStore.comments" :key="item.id" shadow="never" class="comment-card">
                  <div class="comment-row">
                    <div>
                      <strong>{{ item.authorUsername }}</strong>
                      <div>{{ item.content }}</div>
                      <small>{{ formatDate(item.createdAt) }}</small>
                    </div>
                    <el-button
                      v-if="item.authorUsername === currentUsername"
                      size="small"
                      type="danger"
                      plain
                      @click="removeComment(item.id)">삭제</el-button>
                  </div>
                </el-card>
                <el-pagination
                  v-if="socialStore.commentsTotal > socialStore.commentsSize"
                  style="margin-top: 12px; justify-content: center;"
                  :current-page="socialStore.commentsPage + 1"
                  :page-size="socialStore.commentsSize"
                  :total="socialStore.commentsTotal"
                  layout="prev, pager, next"
                  @current-change="handleCommentPageChange" />
              </div>
            </div>
          </div>
        </el-card>
      </el-main>
    </el-container>

    <el-dialog v-model="imageDialogVisible" title="이미지 미리보기" width="90%" align-center>
      <div style="text-align: center;">
        <img v-if="post?.imagePath" :src="getImageUrl(post.imagePath) || undefined" style="max-width: 100%; max-height: 70vh;" alt="image" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Document, Edit, Upload, Delete } from '@element-plus/icons-vue'
import { postsApi, Post } from '@/api/posts'
import { getImageUrl, getApiUrl } from '@/config'
import { formatDate } from '@/utils/date'
import { usePostSocialStore } from '@/store/postSocial'
import { useAuthStore } from '@/store/auth'
import { useNotificationStore } from '@/store/notifications'

const route = useRoute()
const router = useRouter()
const socialStore = usePostSocialStore()
const authStore = useAuthStore()
const notificationStore = useNotificationStore()

const post = ref<Post | null>(null)
const loading = ref(false)
const updating = ref(false)
const deleting = ref(false)
const error = ref('')
const isEditing = ref(false)
const imageDialogVisible = ref(false)
const commentInput = ref('')

const editForm = ref({ title: '', content: '' })

const postId = computed(() => Number(route.params.id || 0))
const currentUsername = computed(() => authStore.username || '')

const fetchPost = async () => {
  if (!postId.value) {
    error.value = '포스트 ID가 유효하지 않습니다.'
    return
  }
  loading.value = true
  error.value = ''
  try {
    const response = await postsApi.getPostById(postId.value)
    post.value = (response.data as any)?.post || (response.data as any)
    if (post.value) {
      editForm.value = { title: post.value.title, content: post.value.content }
    }
    await socialStore.load(postId.value, 0, socialStore.commentsSize)
  } catch (err) {
    error.value = '포스트를 불러오는데 실패했습니다.'
    console.error(err)
  } finally {
    loading.value = false
  }
}

const startEdit = () => {
  if (!post.value) return
  editForm.value = { title: post.value.title, content: post.value.content }
  isEditing.value = true
}

const cancelEdit = () => {
  isEditing.value = false
  if (post.value) {
    editForm.value = { title: post.value.title, content: post.value.content }
  }
}

const handleUpdate = async () => {
  if (!post.value?.id || !editForm.value.title.trim() || !editForm.value.content.trim()) {
    ElMessage.warning('제목/내용을 입력해 주세요.')
    return
  }
  updating.value = true
  try {
    await postsApi.updatePost(post.value.id, {
      title: editForm.value.title.trim(),
      content: editForm.value.content.trim()
    })
    isEditing.value = false
    await fetchPost()
    ElMessage.success('수정되었습니다.')
  } finally {
    updating.value = false
  }
}

const handleDelete = async () => {
  if (!post.value?.id) return
  try {
    await ElMessageBox.confirm(`"${post.value.title}" 글을 삭제하시겠습니까?`, '삭제 확인', {
      confirmButtonText: '삭제',
      cancelButtonText: '취소',
      type: 'warning'
    })

    deleting.value = true
    await postsApi.deletePost(post.value.id)
    ElMessage.success('삭제되었습니다.')
    router.push('/posts')
  } catch {
    // canceled
  } finally {
    deleting.value = false
  }
}

const openImageDialog = () => {
  imageDialogVisible.value = true
}

const handleImageUpload = async (options: { file: File }) => {
  if (!post.value?.id) return
  const formData = new FormData()
  formData.append('file', options.file)
  const response = await fetch(getApiUrl(`/posts/${post.value.id}/image`), {
    method: 'POST',
    credentials: 'include',
    body: formData
  })

  if (!response.ok) {
    ElMessage.error('이미지 업로드 실패')
    return
  }

  ElMessage.success('이미지 업로드 완료')
  await fetchPost()
}

const toggleLikeAction = async () => {
  const next = await socialStore.toggleLike(postId.value)
  await notificationStore.pushNotification('게시글 반응', next ? '좋아요를 눌렀습니다.' : '좋아요를 취소했습니다.')
}

const toggleBookmarkAction = async () => {
  const next = await socialStore.toggleBookmark(postId.value)
  await notificationStore.pushNotification('북마크', next ? '북마크에 추가했습니다.' : '북마크를 해제했습니다.')
}

const submitComment = async () => {
  const content = commentInput.value.trim()
  if (!content) {
    ElMessage.warning('댓글을 입력하세요.')
    return
  }
  await socialStore.addComment(postId.value, content)
  commentInput.value = ''
  await notificationStore.pushNotification('댓글 등록', '댓글이 등록되었습니다.')
}

const removeComment = async (commentId: number) => {
  const ok = await socialStore.removeComment(postId.value, commentId)
  if (ok) {
    await notificationStore.pushNotification('댓글 삭제', '댓글이 삭제되었습니다.')
  }
}

const handleCommentPageChange = async (page: number) => {
  await socialStore.load(postId.value, page - 1, socialStore.commentsSize)
}

onMounted(fetchPost)
</script>

<style scoped>
.comment-card {
  margin-top: 12px;
}
.comment-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}
</style>
