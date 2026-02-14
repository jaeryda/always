<template>
  <div class="page-container">
    <el-container>
      <el-main class="page-main">
        <el-card shadow="hover">
          <template #header><span>AI 이미지 생성</span></template>

          <el-form label-position="top">
            <el-form-item label="프롬프트">
              <el-input v-model="prompt" type="textarea" :rows="4" :disabled="loading" />
            </el-form-item>
            <el-form-item label="참고 이미지 (선택)">
              <el-upload :show-file-list="false" :before-upload="handleImageUpload" accept="image/*" drag>
                <img v-if="uploadedImageUrl" :src="uploadedImageUrl" style="max-width: 260px; max-height: 160px;" />
                <div v-else>이미지를 드래그/선택하세요</div>
              </el-upload>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="loading" @click="generateImage">생성</el-button>
              <el-button @click="clearResult">초기화</el-button>
            </el-form-item>
          </el-form>

          <el-divider />

          <div v-if="generatedImageUrl">
            <img :src="generatedImageUrl" style="max-width: 100%; border-radius: 8px;" />
            <div style="margin-top: 8px;">
              <el-button @click="downloadImage">다운로드</el-button>
            </div>
          </div>
          <el-empty v-else description="생성된 이미지가 없습니다." />
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { openaiApi } from '@/api/openai'
import { useAIHistoryStore } from '@/store/aiHistory'
import { useNotificationStore } from '@/store/notifications'

const prompt = ref<string>('')
const uploadedImageUrl = ref<string>('')
const uploadedImageBase64 = ref<string>('')
const generatedImageUrl = ref<string>('')
const loading = ref<boolean>(false)

const historyStore = useAIHistoryStore()
const notificationStore = useNotificationStore()

const handleImageUpload = (file: File): boolean => {
  const reader = new FileReader()
  reader.onload = (e) => {
    const result = e.target?.result as string
    uploadedImageUrl.value = result
    uploadedImageBase64.value = result.split(',')[1] || ''
  }
  reader.readAsDataURL(file)
  return false
}

const generateImage = async () => {
  if (!prompt.value.trim()) {
    ElMessage.warning('프롬프트를 입력해 주세요.')
    return
  }

  loading.value = true
  generatedImageUrl.value = ''

  try {
    const result = await openaiApi.generateImage({
      prompt: prompt.value,
      model: 'gemini-3-pro-image-preview',
      aspectRatio: '16:9',
      imageSize: '2K',
      referenceImage: uploadedImageBase64.value || undefined
    })

    if (result.success && result.imageUrl) {
      generatedImageUrl.value = result.imageUrl
      await historyStore.addItem({
        type: 'image',
        prompt: prompt.value,
        resultUrl: result.imageUrl,
        resultText: result.text
      })
      await notificationStore.pushNotification('AI 이미지 완료', '이미지 결과를 히스토리에 저장했습니다.')
      ElMessage.success('이미지가 생성되었습니다.')
    } else {
      ElMessage.error(result.message || '이미지 생성 실패')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '이미지 생성 중 오류')
  } finally {
    loading.value = false
  }
}

const downloadImage = () => {
  if (!generatedImageUrl.value) return
  const link = document.createElement('a')
  link.href = generatedImageUrl.value
  link.download = `generated-image-${Date.now()}.png`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const clearResult = () => {
  generatedImageUrl.value = ''
  uploadedImageBase64.value = ''
  uploadedImageUrl.value = ''
  prompt.value = ''
}
</script>
