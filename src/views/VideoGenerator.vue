<template>
  <div class="page-container">
    <el-container>
      <el-main class="page-main">
        <el-card shadow="hover">
          <template #header><span>AI 동영상 생성</span></template>

          <el-form label-position="top">
            <el-form-item label="프롬프트">
              <el-input v-model="prompt" type="textarea" :rows="4" :disabled="loading || polling" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="loading || polling" @click="generateVideo">생성</el-button>
              <el-button @click="clearResult">초기화</el-button>
            </el-form-item>
          </el-form>

          <el-progress v-if="polling" :percentage="pollingProgress" />

          <el-divider />

          <video v-if="generatedVideoUrl" :src="generatedVideoUrl" controls style="width: 100%; max-height: 420px;" />
          <el-empty v-else description="생성된 동영상이 없습니다." />
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
const generatedVideoUrl = ref<string>('')
const loading = ref<boolean>(false)
const polling = ref<boolean>(false)
const pollingProgress = ref<number>(0)
let pollingInterval: ReturnType<typeof setInterval> | null = null

const historyStore = useAIHistoryStore()
const notificationStore = useNotificationStore()

const generateVideo = async () => {
  if (!prompt.value.trim()) {
    ElMessage.warning('프롬프트를 입력해 주세요.')
    return
  }

  loading.value = true
  generatedVideoUrl.value = ''

  try {
    const result = await openaiApi.generateVideo({
      prompt: prompt.value,
      model: 'veo-3.1-fast-generate-preview',
      aspectRatio: '16:9',
      resolution: '720p'
    })

    if (result.success && result.operation) {
      polling.value = true
      pollingProgress.value = 10
      startPolling(result.operation)
      ElMessage.success('동영상 생성을 시작했습니다.')
    } else {
      ElMessage.error(result.message || '동영상 생성 실패')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '동영상 생성 오류')
  } finally {
    loading.value = false
  }
}

const startPolling = (operation: any) => {
  let currentOperation = operation
  let attempts = 0

  pollingInterval = setInterval(async () => {
    attempts += 1
    pollingProgress.value = Math.min(10 + attempts * 6, 95)

    const result = await openaiApi.checkVideoOperation(currentOperation)
    if (result.operation) {
      currentOperation = result.operation
    }

    if (result.success && result.videoUrl) {
      stopPolling()
      generatedVideoUrl.value = result.videoUrl
      pollingProgress.value = 100
      await historyStore.addItem({
        type: 'video',
        prompt: prompt.value,
        resultUrl: result.videoUrl
      })
      await notificationStore.pushNotification('AI 동영상 완료', '동영상 결과를 히스토리에 저장했습니다.')
      ElMessage.success('동영상 생성이 완료되었습니다.')
    } else if (attempts >= 40) {
      stopPolling()
      ElMessage.warning('생성 시간이 오래 걸려 중지했습니다. 다시 시도해 주세요.')
    }
  }, 10000)
}

const stopPolling = () => {
  if (pollingInterval) {
    clearInterval(pollingInterval)
    pollingInterval = null
  }
  polling.value = false
}

const clearResult = () => {
  stopPolling()
  generatedVideoUrl.value = ''
  prompt.value = ''
  pollingProgress.value = 0
}
</script>
