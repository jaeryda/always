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
          
          <!-- 메시지 리스트 -->
          <div class="chat-messages" ref="messagesContainer">
            <div 
              v-for="(message, index) in messages" 
              :key="index"
              :class="['message', message.role]">
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
                  <span>답변을 생성하는 중...</span>
                </div>
              </div>
            </div>
            
            <div v-if="messages.length === 0 && !loading" class="empty-chat">
              <el-empty description="아래에 질문을 입력하고 전송하세요." />
            </div>
          </div>
          
          <!-- 입력 영역 -->
          <div class="chat-input-area">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="3"
              placeholder="질문을 입력하세요..."
              @keydown.ctrl.enter="sendMessage"
              @keydown.meta.enter="sendMessage"
              :disabled="loading"
              class="chat-input" />
            <div class="chat-actions">
              <el-button 
                type="primary" 
                @click="sendMessage"
                :loading="loading"
                :disabled="!inputMessage.trim() || loading">
                <el-icon><Promotion /></el-icon>
                <span>전송</span>
              </el-button>
              <el-button 
                @click="clearMessages"
                :disabled="messages.length === 0 || loading">
                <el-icon><Delete /></el-icon>
                <span>대화 지우기</span>
              </el-button>
            </div>
            <div class="chat-hint">
              <el-text size="small" type="info">Ctrl+Enter 또는 Cmd+Enter로 전송</el-text>
            </div>
          </div>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, User, Service, Loading, Promotion, Delete } from '@element-plus/icons-vue'
import { openaiApi } from '@/api/openai'
import dayjs from 'dayjs'

interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
  timestamp: Date
}

const messages = ref<ChatMessage[]>([])
const inputMessage = ref<string>('')
const loading = ref<boolean>(false)
const messagesContainer = ref<HTMLElement | null>(null)

const sendMessage = async () => {
  const message = inputMessage.value.trim()
  if (!message || loading.value) return

  // 사용자 메시지 추가
  const userMessage: ChatMessage = {
    role: 'user',
    content: message,
    timestamp: new Date()
  }
  messages.value.push(userMessage)
  inputMessage.value = ''

  // 스크롤을 맨 아래로
  await scrollToBottom()

  // AI 응답 요청
  loading.value = true
  try {
    const response = await openaiApi.generateCompletion({
      prompt: message,
      maxTokens: 1000,
      temperature: 0.7
    })

    if (response.data.success && response.data.result) {
      const aiMessage: ChatMessage = {
        role: 'assistant',
        content: response.data.result,
        timestamp: new Date()
      }
      messages.value.push(aiMessage)
    } else {
      ElMessage.error(response.data.message || '답변을 받는데 실패했습니다.')
    }
  } catch (error: any) {
    console.error('AI 응답 오류:', error)
    ElMessage.error(error.response?.data?.message || 'AI 응답 중 오류가 발생했습니다.')
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

const clearMessages = () => {
  messages.value = []
  ElMessage.success('대화 기록이 지워졌습니다.')
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const formatTime = (date: Date) => {
  return dayjs(date).format('HH:mm')
}

onMounted(() => {
  // 초기 환영 메시지
  // messages.value.push({
  //   role: 'assistant',
  //   content: '안녕하세요! 무엇이든 물어보세요.',
  //   timestamp: new Date()
  // })
})
</script>

