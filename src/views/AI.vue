<template>
  <div class="page-container">
    <el-container>
      <el-main class="page-main">
        <el-card shadow="hover" class="ai-chat-card">
          <template #header>
            <div class="card-header">
              <el-icon class="card-header-icon"><ChatDotRound /></el-icon>
              <span>AI와 대화하기</span>
            </div>
          </template>

          <div class="chat-messages" ref="messagesContainer">
            <div v-for="(message, index) in messages" :key="index" :class="['message', message.role]">
              <div class="message-content">
                <div class="message-role">
                  <el-icon v-if="message.role === 'user'"><User /></el-icon>
                  <el-icon v-else><Service /></el-icon>
                  <span>{{ message.role === 'user' ? '나' : 'AI' }}</span>
                </div>
                <div class="message-text">{{ message.content }}</div>
                <div class="message-time">{{ formatTime(message.timestamp) }}</div>
              </div>
            </div>

            <div v-if="loading" class="message assistant">
              <div class="message-content">
                <div class="message-role">
                  <el-icon><Service /></el-icon>
                  <span>AI</span>
                </div>
                <div class="message-text">
                  <el-icon class="is-loading"><Loading /></el-icon>
                  <span>응답 생성 중...</span>
                </div>
              </div>
            </div>

            <div v-if="messages.length === 0 && !loading" class="empty-chat">
              <el-empty description="질문을 입력하고 전송해보세요." />
            </div>
          </div>

          <div class="chat-input-area">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="3"
              placeholder="질문을 입력하세요"
              @keydown.ctrl.enter="sendMessage"
              @keydown.meta.enter="sendMessage"
              :disabled="loading"
              class="chat-input" />
            <div class="chat-actions">
              <el-button type="primary" @click="sendMessage" :loading="loading" :disabled="!inputMessage.trim() || loading">
                <el-icon><Promotion /></el-icon>
                <span>전송</span>
              </el-button>
              <el-button @click="clearMessages" :disabled="messages.length === 0 || loading">
                <el-icon><Delete /></el-icon>
                <span>대화 지우기</span>
              </el-button>
            </div>
          </div>

          <el-divider />
          <div style="text-align: center;">
            <el-button type="primary" @click="$router.push('/')">홈으로</el-button>
          </div>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, User, Service, Loading, Promotion, Delete } from '@element-plus/icons-vue'
import { openaiApi } from '@/api/openai'
import dayjs from 'dayjs'
import { useAIHistoryStore } from '@/store/aiHistory'
import { useNotificationStore } from '@/store/notifications'

interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
  timestamp: Date
}

const messages = ref<ChatMessage[]>([])
const inputMessage = ref<string>('')
const loading = ref<boolean>(false)
const messagesContainer = ref<HTMLElement | null>(null)

const historyStore = useAIHistoryStore()
const notificationStore = useNotificationStore()

const sendMessage = async () => {
  const message = inputMessage.value.trim()
  if (!message || loading.value) return

  messages.value.push({ role: 'user', content: message, timestamp: new Date() })
  inputMessage.value = ''
  await scrollToBottom()

  loading.value = true
  try {
    const recentMessages = messages.value.slice(-10).map((msg) => ({ role: msg.role, content: msg.content }))

    const response = await openaiApi.generateChat({
      prompt: message,
      model: 'gemini-3-flash-preview',
      messages: recentMessages,
      temperature: 0.7,
      maxTokens: 2000
    })

    if (response.success && response.result) {
      messages.value.push({ role: 'assistant', content: response.result, timestamp: new Date() })
      await historyStore.addItem({
        type: 'chat',
        prompt: message,
        resultText: response.result
      })
      await notificationStore.pushNotification('AI 채팅 완료', 'AI 응답이 히스토리에 저장되었습니다.')
    } else {
      ElMessage.error(response.message || '응답을 받지 못했습니다.')
    }
  } catch (error: any) {
    ElMessage.error(error.message || 'AI 응답 중 오류가 발생했습니다.')
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

const clearMessages = () => {
  messages.value = []
  ElMessage.success('대화 기록을 지웠습니다.')
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const formatTime = (date: Date) => dayjs(date).format('HH:mm')
</script>
