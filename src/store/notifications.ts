import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { notificationsApi, NotificationItem } from '@/api/notifications'
import { config } from '@/config'

export const useNotificationStore = defineStore('notifications', () => {
  const notifications = ref<NotificationItem[]>([])
  const eventSource = ref<EventSource | null>(null)

  const unreadCount = computed(() => notifications.value.filter((n) => !n.read).length)

  const load = async () => {
    const response = await notificationsApi.list()
    notifications.value = response.data.notifications || []
  }

  const connectStream = () => {
    if (eventSource.value) return
    const stream = new EventSource(`${config.apiBaseURL}/notifications/stream`, { withCredentials: true })
    stream.addEventListener('notification', (event: MessageEvent) => {
      try {
        const item = JSON.parse(event.data) as NotificationItem
        notifications.value.unshift(item)
      } catch {
        // ignore parse error
      }
    })
    stream.onerror = () => {
      stream.close()
      eventSource.value = null
      setTimeout(() => connectStream(), 3000)
    }
    eventSource.value = stream
  }

  const disconnectStream = () => {
    if (!eventSource.value) return
    eventSource.value.close()
    eventSource.value = null
  }

  const pushNotification = async (title: string, message: string) => {
    await notificationsApi.create(title, message)
    await load()
  }

  const markAsRead = async (id: number) => {
    await notificationsApi.markAsRead(id)
    await load()
  }

  const markAllAsRead = async () => {
    await notificationsApi.markAllAsRead()
    await load()
  }

  const removeNotification = async (id: number) => {
    await notificationsApi.delete(id)
    await load()
  }

  const clearAll = async () => {
    await notificationsApi.deleteAll()
    await load()
  }

  return {
    notifications,
    unreadCount,
    load,
    connectStream,
    disconnectStream,
    pushNotification,
    markAsRead,
    markAllAsRead,
    removeNotification,
    clearAll
  }
})
