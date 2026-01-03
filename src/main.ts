import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import '@/styles/index.css'
import router from './router'
import pinia from './store'
import { useAuthStore } from './store/auth'

// ResizeObserver 경고 메시지 무시 (Element Plus 등의 UI 라이브러리에서 발생하는 내부 경고)
if (typeof window !== 'undefined') {
  // window.onerror로 에러 차단
  const originalOnError = window.onerror
  window.onerror = (message, source, lineno, colno, error) => {
    if (
      typeof message === 'string' &&
      (message.includes('ResizeObserver loop') ||
        message.includes('ResizeObserver loop completed'))
    ) {
      return true // 에러 처리 완료 (기본 동작 방지)
    }
    if (originalOnError) {
      return originalOnError(message, source, lineno, colno, error)
    }
    return false
  }

  // ErrorEvent 리스너
  window.addEventListener('error', (e) => {
    if (
      e.message &&
      (e.message.includes('ResizeObserver loop') ||
        e.message.includes('ResizeObserver loop completed'))
    ) {
      e.stopImmediatePropagation()
      e.preventDefault()
    }
  }, true)

  // unhandledrejection 이벤트도 처리
  window.addEventListener('unhandledrejection', (e) => {
    if (
      e.reason &&
      typeof e.reason === 'string' &&
      (e.reason.includes('ResizeObserver loop') ||
        e.reason.includes('ResizeObserver loop completed'))
    ) {
      e.preventDefault()
    }
  })

  // console.error에서도 필터링
  const originalConsoleError = console.error
  console.error = (...args: any[]) => {
    const message = args[0]
    if (
      message &&
      typeof message === 'string' &&
      (message.includes('ResizeObserver loop') ||
        message.includes('ResizeObserver loop completed'))
    ) {
      return
    }
    originalConsoleError.apply(console, args)
  }
}

const app = createApp(App)
app.use(ElementPlus)
app.use(pinia)
app.use(router)

// 앱 시작 전 인증 상태 복원
const authStore = useAuthStore()
authStore.restoreAuth().then(() => {
  app.mount('#app')
})
